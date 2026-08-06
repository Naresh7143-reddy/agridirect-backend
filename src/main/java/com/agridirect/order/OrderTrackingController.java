package com.agridirect.order;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.delivery.DeliveryPartner;
import com.agridirect.delivery.DeliveryPartnerRepository;
import com.agridirect.delivery.DeliveryProfile;
import com.agridirect.delivery.DeliveryRepository;
import com.agridirect.user.User;
import com.agridirect.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
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
    @Autowired private DeliveryPartnerRepository deliveryPartnerRepository;
    @Autowired private UserRepository userRepository;

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
    public static void broadcastLocation(UUID orderId, UUID deliveryAgentId, Double lat, Double lng, String status, OrderRepository orderRepository, DeliveryPartnerRepository deliveryPartnerRepository, UserRepository userRepository) {
        SseEmitter emitter = EMITTERS.get(orderId.toString());
        if (emitter == null) return;
        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("lat", lat != null ? lat : 0);
            locationData.put("lng", lng != null ? lng : 0);
            locationData.put("status", status != null ? status : "");
            
            // Include agent profile info
            if (deliveryAgentId != null) {
                DeliveryPartner partner = deliveryPartnerRepository.findByUserId(deliveryAgentId.toString()).orElse(null);
                if (partner != null) {
                    locationData.put("agentName", partner.getName());
                    locationData.put("agentPhone", partner.getPhone());
                    locationData.put("vehicleType", partner.getVehicleType() != null ? partner.getVehicleType() : "BIKE");
                    locationData.put("vehicleRegistration", partner.getVehicleRegistration() != null ? partner.getVehicleRegistration() : "");
                    locationData.put("rating", partner.getAvgRating() != null ? partner.getAvgRating() : 4.5);
                }
            }
            
            emitter.send(SseEmitter.event()
                    .name("location")
                    .data(locationData));
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

    /**
     * Get delivery agent's current location AND profile for an order (polling fallback).
     * Returns: lat, lng, status, name, phone, vehicleType, vehicleRegistration, rating, totalDeliveries
     * — everything the buyer needs to display a Swiggy/Zomato-style tracking card.
     */
    @GetMapping("/api/buyer/orders/{id}/agent-location")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAgentLocation(@PathVariable UUID id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null || order.getDeliveryAgentId() == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("available", false)));
        }

        // Get location from DeliveryProfile
        DeliveryProfile dp = deliveryRepository.findByUserId(order.getDeliveryAgentId()).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("available", true);
        result.put("lat", dp != null && dp.getCurrentLat() != null ? dp.getCurrentLat() : 0.0);
        result.put("lng", dp != null && dp.getCurrentLng() != null ? dp.getCurrentLng() : 0.0);
        result.put("status", order.getStatus());

        // Enrich with partner profile (name, phone, vehicle, rating)
        DeliveryPartner partner = deliveryPartnerRepository.findByUserId(order.getDeliveryAgentId().toString()).orElse(null);
        if (partner != null) {
            result.put("agentName", partner.getName());
            result.put("agentPhone", partner.getPhone());
            result.put("vehicleType", partner.getVehicleType() != null ? partner.getVehicleType() : "BIKE");
            result.put("vehicleRegistration", partner.getVehicleRegistration() != null ? partner.getVehicleRegistration() : "");
            result.put("rating", partner.getAvgRating() != null ? partner.getAvgRating() : 4.5);
            result.put("totalDeliveries", partner.getTotalDeliveries() != null ? partner.getTotalDeliveries() : 0);
        } else {
            // Fallback to User table for name/phone
            userRepository.findById(order.getDeliveryAgentId()).ifPresent(user -> {
                result.put("agentName", user.getName() != null ? user.getName() : "Delivery Partner");
                result.put("agentPhone", user.getPhone());
            });
            result.put("vehicleType", "BIKE");
            result.put("vehicleRegistration", "");
            result.put("rating", 4.5);
            result.put("totalDeliveries", 0);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
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
                    // Send enriched agent info on first connect
                    DeliveryPartner partner = deliveryPartnerRepository.findByUserId(order.getDeliveryAgentId().toString()).orElse(null);
                    Map<String, Object> agentInfo = new HashMap<>();
                    agentInfo.put("lat", dp.getCurrentLat() != null ? dp.getCurrentLat() : 0.0);
                    agentInfo.put("lng", dp.getCurrentLng() != null ? dp.getCurrentLng() : 0.0);
                    agentInfo.put("status", order.getStatus());
                    
                    if (partner != null) {
                        agentInfo.put("agentName", partner.getName());
                        agentInfo.put("agentPhone", partner.getPhone());
                        agentInfo.put("vehicleType", partner.getVehicleType() != null ? partner.getVehicleType() : "BIKE");
                        agentInfo.put("vehicleRegistration", partner.getVehicleRegistration() != null ? partner.getVehicleRegistration() : "");
                        agentInfo.put("rating", partner.getAvgRating() != null ? partner.getAvgRating() : 4.5);
                        agentInfo.put("totalDeliveries", partner.getTotalDeliveries() != null ? partner.getTotalDeliveries() : 0);
                    } else {
                        userRepository.findById(order.getDeliveryAgentId()).ifPresent(user -> {
                            agentInfo.put("agentName", user.getName() != null ? user.getName() : "Delivery Partner");
                            agentInfo.put("agentPhone", user.getPhone());
                        });
                        agentInfo.put("vehicleType", "BIKE");
                        agentInfo.put("vehicleRegistration", "");
                        agentInfo.put("rating", 4.5);
                        agentInfo.put("totalDeliveries", 0);
                    }
                    
                    emitter.send(SseEmitter.event()
                            .name("agent-info")
                            .data(agentInfo));
                    
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
