package com.agridirect.buyer;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/buyer")
public class BuyerController {

    @Autowired private BuyerService buyerService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<BuyerProfile>> getProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(buyerService.getProfile(UUID.fromString(userId))));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<BuyerProfile>> updateProfile(@RequestBody Map<String, Object> updates) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(buyerService.updateProfile(UUID.fromString(userId), updates)));
    }
}
