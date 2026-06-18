package com.agridirect.order;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.delivery.DeliveryProfile;
import com.agridirect.delivery.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Server-Sent Events endpoint for real-time order tracking.
 * Buyers subscribe to /api/buyer/orders/{id}/stream and receive
 * status + location updates whenever the delivery partner's location
 * is updated or the order status changes.
 */
@RestController
public class OrderTrackingController {

    // orderId → active SSE emitter (one per order)
    private static final ConcurrentHashMap<String, SseEmitter> EMITTERS = new ConcurrentHashMap<>();

    @Autowired private OrderRepository orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;

    /** Buyer subscribes to live order updates. */
    @GetMapping(value = "/api/buyer/orders/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('BUYER')")
    public SseEmitter streamOrderUpdates(@PathVariable UUID id) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5 min timeout
        String key = id.toString();
        EMITTERS.put(key, emitter);
        emitter.onCompletion(() -> EMITTERS.remove(key));
        emitter.onTimeout(() -> EMITTERS.remove(key));
        emitter.onError(e -> EMITTERS.remove(key));

        // Send current state immediately
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                sendCurrentState(emitter, id);
            } catch (IOException ignored) {}
        });
        return emitter;
    }

    /** Called by DeliveryController when location is updated. */
    public static void broadcastLocation(UUID orderId, Double lat, Double lng, String status) {
        SseEmitter emitter = EMITTERS.get(orderId.toString());
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name("location")
                    .data(Map.of("lat", lat != null ? lat : 0,
                                 "lng", lng != null ? lng : 0,
                                 "status", status != null ? status : "")));
        } catch (IOException e) {
            EMITTERS.remove(orderId.toString());
        }
    }

    /** Called when order status changes. */
    public static void broadcastStatus(UUID orderId, String newStatus) {
        SseEmitter emitter = EMITTERS.get(orderId.toString());
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name("status")
                    .data(Map.of("status", newStatus)));
            if ("DELIVERED".equals(newStatus) || "CANCELLED".equals(newStatus)) {
                emitter.complete();
                EMITTERS.remove(orderId.toString());
            }
        } catch (IOException e) {
            EMITTERS.remove(orderId.toString());
        }
    }

    /** Get delivery agent's current location for an order (polling fallback). */
    @GetMapping("/api/buyer/orders/{id}/agent-location")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAgentLocation(@PathVariable UUID id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null || order.getDeliveryAgentId() == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("available", false)));
        }
        DeliveryProfile dp = deliveryRepository.findByUserId(order.getDeliveryAgentId()).orElse(null);
        if (dp == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("available", false)));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "available", true,
                "lat", dp.getCurrentLat() != null ? dp.getCurrentLat() : 0.0,
                "lng", dp.getCurrentLng() != null ? dp.getCurrentLng() : 0.0,
                "status", order.getStatus()
        )));
    }

    private void sendCurrentState(SseEmitter emitter, UUID orderId) throws IOException {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;
        emitter.send(SseEmitter.event()
                .name("status")
                .data(Map.of("status", order.getStatus())));
        if (order.getDeliveryAgentId() != null) {
            deliveryRepository.findByUserId(order.getDeliveryAgentId()).ifPresent(dp -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("location")
                            .data(Map.of(
                                    "lat", dp.getCurrentLat() != null ? dp.getCurrentLat() : 0.0,
                                    "lng", dp.getCurrentLng() != null ? dp.getCurrentLng() : 0.0,
                                    "status", order.getStatus())));
                } catch (IOException ignored) {}
            });
        }
    }
}
