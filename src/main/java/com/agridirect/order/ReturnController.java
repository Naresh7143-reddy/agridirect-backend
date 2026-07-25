package com.agridirect.order;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.order.dto.ReturnSubmitRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<ReturnRequest>> requestReturn(@Valid @RequestBody ReturnSubmitRequest request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID buyerId = UUID.fromString(userIdStr);
        try {
            ReturnRequest returnRequest = returnService.submitReturnRequest(buyerId, request);
            return ResponseEntity.ok(ApiResponse.success(returnRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<ReturnRequest>>> getBuyerReturns() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID buyerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(ApiResponse.success(returnService.getBuyerReturns(buyerId)));
    }

    @GetMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<List<ReturnRequest>>> getFarmerReturns() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID farmerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(ApiResponse.success(returnService.getFarmerReturns(farmerId)));
    }
}
