package com.agridirect.delivery;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.agridirect.order.Order;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired private DeliveryService deliveryService;

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
    public ResponseEntity<ApiResponse<List<Order>>> getOrders() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getAssignedOrders(UUID.fromString(userId))));
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String status = body.get("status");
        return ResponseEntity.ok(ApiResponse.success(deliveryService.updateOrderStatus(UUID.fromString(userId), orderId, status)));
    }
}
