package com.agridirect.order;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.order.dto.OrderDetailResponse;
import com.agridirect.order.dto.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class OrderController {

    @Autowired private OrderService orderService;

    @PostMapping("/api/buyer/orders")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Order>> placeOrder(@RequestBody OrderRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.placeOrder(UUID.fromString(userId), req)));
    }

    @GetMapping("/api/buyer/orders")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<Order>>> getBuyerOrders() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(orderService.getBuyerOrders(UUID.fromString(userId))));
    }

    @GetMapping("/api/buyer/orders/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderById(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderService.getOrderById(id);
        if (!UUID.fromString(userId).equals(order.getBuyerId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not your order"));
        }
        return ResponseEntity.ok(ApiResponse.success(orderService.buildOrderDetail(id)));
    }

    @GetMapping("/api/farmer/orders/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getFarmerOrderById(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(orderService.buildOrderDetail(id)));
    }

    @GetMapping("/api/admin/orders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getAdminOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.buildOrderDetail(id)));
    }

    @PostMapping("/api/buyer/orders/{id}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", orderService.cancelOrder(UUID.fromString(userId), id)));
    }

    @GetMapping("/api/farmer/orders")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<List<Order>>> getFarmerOrders() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(orderService.getFarmerOrders(UUID.fromString(userId))));
    }

    @PutMapping("/api/farmer/orders/{id}/accept")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Order>> acceptOrder(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(orderService.acceptOrder(UUID.fromString(userId), id)));
    }

    @PutMapping("/api/farmer/orders/{id}/packed")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Order>> markPacked(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(orderService.markPacked(UUID.fromString(userId), id)));
    }

    @PostMapping("/api/admin/orders/{id}/assign-delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Order>> assignDelivery(@PathVariable UUID id,
                                                              @RequestBody Map<String, String> body) {
        UUID agentId = UUID.fromString(body.get("agentId"));
        return ResponseEntity.ok(ApiResponse.success(orderService.assignDeliveryAgent(id, agentId)));
    }
}
