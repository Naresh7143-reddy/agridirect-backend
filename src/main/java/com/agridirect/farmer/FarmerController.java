package com.agridirect.farmer;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.storage.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    @Autowired private FarmerService farmerService;
    @Autowired private CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<FarmerProfile>> getProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(farmerService.getProfile(UUID.fromString(userId))));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<FarmerProfile>> updateProfile(@RequestBody Map<String, Object> updates) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(farmerService.updateProfile(UUID.fromString(userId), updates)));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(farmerService.getDashboard(UUID.fromString(userId))));
    }

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEarnings() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(farmerService.getEarnings(UUID.fromString(userId))));
    }

    @PostMapping("/profile/photo")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(@RequestParam MultipartFile file) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String url = cloudinaryService.uploadProfilePhoto(file, userId);
        return ResponseEntity.ok(ApiResponse.success("Profile photo updated", url));
    }
}
