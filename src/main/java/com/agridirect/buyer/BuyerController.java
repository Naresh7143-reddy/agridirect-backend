package com.agridirect.buyer;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.order.Order;
import com.agridirect.storage.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/buyer")
public class BuyerController {

    @Autowired private BuyerService buyerService;
    @Autowired private CloudinaryService cloudinaryService;

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

    @PostMapping("/profile/photo")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(@RequestParam MultipartFile file) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String url = cloudinaryService.uploadProfilePhoto(file, userId);
        return ResponseEntity.ok(ApiResponse.success("Profile photo updated", url));
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    @GetMapping("/addresses")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<Address>>> getAddresses() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(buyerService.getAddresses(UUID.fromString(userId))));
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Address>> addAddress(@RequestBody Map<String, Object> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success("Address added", buyerService.addAddress(UUID.fromString(userId), body)));
    }

    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Address>> updateAddress(@PathVariable UUID addressId, @RequestBody Map<String, Object> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success("Address updated", buyerService.updateAddress(UUID.fromString(userId), addressId, body)));
    }

    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable UUID addressId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        buyerService.deleteAddress(UUID.fromString(userId), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    @PatchMapping("/addresses/{addressId}/default")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(@PathVariable UUID addressId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        buyerService.setDefaultAddress(UUID.fromString(userId), addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address set", null));
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @GetMapping("/orders/{id}/track")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> trackOrder(@PathVariable UUID id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(buyerService.trackOrder(UUID.fromString(userId), id)));
    }

    @PostMapping("/orders/{id}/rate")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Void>> rateOrder(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        buyerService.rateOrder(UUID.fromString(userId), id, body);
        return ResponseEntity.ok(ApiResponse.success("Thanks for your feedback", null));
    }

    // ── Wishlist ──────────────────────────────────────────────────────────────

    @GetMapping("/wishlist")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWishlist() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(buyerService.getWishlist(UUID.fromString(userId))));
    }

    @PostMapping("/wishlist")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(@RequestBody Map<String, String> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        buyerService.addToWishlist(UUID.fromString(userId), UUID.fromString(body.get("productId")));
        return ResponseEntity.ok(ApiResponse.success("Added to wishlist", null));
    }

    @DeleteMapping("/wishlist/{productId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable UUID productId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        buyerService.removeFromWishlist(UUID.fromString(userId), productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }

    @GetMapping("/wishlist/{productId}/check")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlist(@PathVariable UUID productId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean inWishlist = buyerService.isInWishlist(UUID.fromString(userId), productId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inWishlist", inWishlist)));
    }
}
