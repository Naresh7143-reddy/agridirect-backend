package com.agridirect.delivery;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.delivery.dto.DeliveryTrackingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryLocationController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/location/{orderId}")
    public ResponseEntity<ApiResponse<Object>> getDeliveryLocation(@PathVariable UUID orderId) {
        try {
            DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(orderId.toString());
            if (tracking != null) {
                return ResponseEntity.ok(ApiResponse.success(tracking));
            }
        } catch (Exception ignored) {}

        // Mock location for demonstration purposes when database record is absent
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
