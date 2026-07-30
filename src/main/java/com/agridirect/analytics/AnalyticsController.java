package com.agridirect.analytics;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.farmer.FarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private FarmerService farmerService;

    @GetMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFarmerAnalytics() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID farmerId = UUID.fromString(userIdStr);

        Map<String, Object> analytics = farmerService.getDashboard(farmerId);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}

