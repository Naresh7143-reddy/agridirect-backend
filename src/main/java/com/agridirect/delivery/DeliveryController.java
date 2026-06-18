package com.agridirect.delivery;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.order.OrderService;
import com.agridirect.order.dto.DeliveryOrderResponse;
import com.agridirect.storage.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.agridirect.order.Order;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired private DeliveryService deliveryService;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private OrderService orderService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryProfile>> getProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getProfile(UUID.fromString(userId))));
    }

    @PutMapping("/availability")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryProfile>> updateAvailability(@RequestBody Map<String, Boolean> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean available = Boolean.TRUE.equals(body.get("available"));
        return ResponseEntity.ok(ApiResponse.success(deliveryService.updateAvailability(UUID.fromString(userId), available)));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<List<DeliveryOrderResponse>>> getOrders() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(orderService.getAssignedOrdersAsDto(UUID.fromString(userId))));
    }

    /** Available pool: packed orders that nobody has claimed yet. */
    @GetMapping("/orders/available")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<List<DeliveryOrderResponse>>> getAvailableOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAvailableOrdersAsDto()));
    }

    /** Self-claim an available order. */
    @PostMapping("/orders/{id}/claim")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> claimOrder(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Order claimed = deliveryService.claimOrder(UUID.fromString(userId), id);
        return ResponseEntity.ok(ApiResponse.success("Order claimed", orderService.buildDeliveryOrderResponse(claimed)));
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String status = body.get("status");
        Order updated = deliveryService.updateOrderStatus(UUID.fromString(userId), orderId, status);
        return ResponseEntity.ok(ApiResponse.success(orderService.buildDeliveryOrderResponse(updated)));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> getOrderById(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = deliveryService.getOrderById(UUID.fromString(userId), id);
        return ResponseEntity.ok(ApiResponse.success(orderService.buildDeliveryOrderResponse(order)));
    }

    @PostMapping("/orders/{id}/confirm")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> confirmOrder(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Order confirmed = deliveryService.confirmOrder(UUID.fromString(userId), id);
        return ResponseEntity.ok(ApiResponse.success("Delivery confirmed", orderService.buildDeliveryOrderResponse(confirmed)));
    }

    @PutMapping("/location")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<Void>> updateLocation(@RequestBody Map<String, Object> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Double lat = body.get("lat") != null ? ((Number) body.get("lat")).doubleValue() : null;
        Double lng = body.get("lng") != null ? ((Number) body.get("lng")).doubleValue() : null;
        deliveryService.updateLocationAndBroadcast(UUID.fromString(userId), lat, lng);
        return ResponseEntity.ok(ApiResponse.success("Location updated", null));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<DeliveryProfile>> updateProfile(@RequestBody Map<String, Object> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(deliveryService.updateProfile(UUID.fromString(userId), body)));
    }

    @PostMapping("/profile/photo")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(@RequestParam MultipartFile file) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String url = cloudinaryService.uploadProfilePhoto(file, userId);
        deliveryService.updatePhoto(UUID.fromString(userId), url);
        return ResponseEntity.ok(ApiResponse.success("Profile photo updated", url));
    }

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEarnings() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getEarnings(UUID.fromString(userId))));
    }
}
