package com.agridirect.delivery;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryProofController {

    @Autowired
    private DeliveryProofService deliveryProofService;

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/verify-otp/{orderId}")
    @PreAuthorize("hasRole('DELIVERY') or hasRole('DELIVERY_AGENT')")
    public ResponseEntity<ApiResponse<Object>> verifyOtp(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> request) {
        
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID deliveryAgentId = UUID.fromString(userIdStr);
        String otp = request != null ? request.get("otp") : null;

        try {
            boolean verified = deliveryService.verifyOtp(deliveryAgentId, orderId, otp);
            if (verified) {
                return ResponseEntity.ok(ApiResponse.success("OTP verified and order delivered successfully", true));
            }
        } catch (Exception e) {
            // Fallback to DeliveryProofService
        }

        try {
            DeliveryProof proof = deliveryProofService.verifyOtp(orderId, deliveryAgentId, otp);
            return ResponseEntity.ok(ApiResponse.success(proof));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
