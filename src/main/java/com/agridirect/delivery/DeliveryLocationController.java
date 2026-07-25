package com.agridirect.delivery;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryLocationController {

    @GetMapping("/location/{orderId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeliveryLocation(@PathVariable UUID orderId) {
        // Mock location for demonstration purposes.
        // In a real application, the delivery agent's app would continuously POST location updates
        // and this endpoint would fetch the latest from the database or cache.
        
        // Simulating a location somewhere in India (e.g. Hyderabad coordinates)
        // We'll add some randomness so it appears to move if polled.
        double baseLat = 17.385044;
        double baseLng = 78.486671;
        
        double randomOffsetLat = (Math.random() - 0.5) * 0.01;
        double randomOffsetLng = (Math.random() - 0.5) * 0.01;

        Map<String, Object> location = new HashMap<>();
        location.put("orderId", orderId);
        location.put("latitude", baseLat + randomOffsetLat);
        location.put("longitude", baseLng + randomOffsetLng);
        location.put("timestamp", System.currentTimeMillis());
        location.put("status", "ON_THE_WAY");
        
        return ResponseEntity.ok(ApiResponse.success(location));
    }
}
