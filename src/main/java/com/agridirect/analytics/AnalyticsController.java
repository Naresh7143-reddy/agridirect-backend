package com.agridirect.analytics;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFarmerAnalytics() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID farmerId = UUID.fromString(userIdStr);
        
        // This is a simplified analytics aggregation. In a production system, this would be a custom JPQL/SQL query.
        // For demonstration, we'll fetch all orders for the farmer and aggregate them in memory.
        
        // Note: For this example, we assume `orderRepository` has a method to get orders by farmer id. 
        // We'll mock the response structure.
        
        // Dummy data for analytics
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalRevenue", 145000.0);
        analytics.put("totalOrders", 142);
        analytics.put("activeSubscriptions", 24);
        analytics.put("pendingReturns", 3);
        
        // Monthly trend mock
        List<Map<String, Object>> salesTrend = List.of(
            Map.of("month", "Jan", "sales", 12000),
            Map.of("month", "Feb", "sales", 19000),
            Map.of("month", "Mar", "sales", 15000),
            Map.of("month", "Apr", "sales", 22000),
            Map.of("month", "May", "sales", 28000),
            Map.of("month", "Jun", "sales", 25000)
        );
        analytics.put("salesTrend", salesTrend);

        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
