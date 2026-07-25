package com.agridirect.order;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.order.dto.SubscriptionRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Subscription>> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID buyerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.createSubscription(buyerId, request)));
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<Subscription>>> getBuyerSubscriptions() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID buyerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getBuyerSubscriptions(buyerId)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Subscription>> cancelSubscription(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID buyerId = UUID.fromString(userIdStr);
        try {
            return ResponseEntity.ok(ApiResponse.success(subscriptionService.cancelSubscription(id, buyerId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
