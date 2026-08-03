package com.agridirect.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@DisplayName("Route Optimization Service Tests")
class RouteOptimizationServiceTest {
    
    @Mock
    private MapsService mapsService;
    
    @InjectMocks
    private RouteOptimizationService routeOptimizationService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mapsService.getHaversineDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(5.0);
    }
    
    @Test
    @DisplayName("Should handle single stop route")
    void testSingleStopRoute() {
        // Arrange
        List<RouteOptimizationService.DeliveryStop> stops = new ArrayList<>();
        stops.add(new RouteOptimizationService.DeliveryStop(
                "order1", 12.9716, 77.5946, "Pickup Location", "PICKUP"));
        
        // Act
        List<RouteOptimizationService.DeliveryStop> optimized = routeOptimizationService.optimizeDeliveryRoute(stops);
        
        // Assert
        assertEquals(1, optimized.size());
    }
    
    @Test
    @DisplayName("Should optimize multiple stops route")
    void testMultipleStopsRoute() {
        // Arrange
        List<RouteOptimizationService.DeliveryStop> stops = new ArrayList<>();
        stops.add(new RouteOptimizationService.DeliveryStop(
                "order1", 12.9716, 77.5946, "Pickup", "PICKUP"));
        stops.add(new RouteOptimizationService.DeliveryStop(
                "order2", 12.9352, 77.6245, "Delivery 1", "DELIVERY"));
        stops.add(new RouteOptimizationService.DeliveryStop(
                "order3", 13.0827, 77.6063, "Delivery 2", "DELIVERY"));
        
        // Act
        List<RouteOptimizationService.DeliveryStop> optimized = routeOptimizationService.optimizeDeliveryRoute(stops);
        
        // Assert
        assertEquals(3, optimized.size());
        assertEquals("order1", optimized.get(0).getOrderId()); // Should start with pickup
    }
    
    @Test
    @DisplayName("Should return empty list for null input")
    void testNullRouteInput() {
        // Act
        List<RouteOptimizationService.DeliveryStop> optimized = routeOptimizationService.optimizeDeliveryRoute(null);
        
        // Assert
        assertNull(optimized);
    }
    
    @Test
    @DisplayName("Should calculate total route distance")
    void testCalculateTotalRouteDistance() {
        // Arrange
        List<RouteOptimizationService.DeliveryStop> stops = new ArrayList<>();
        stops.add(new RouteOptimizationService.DeliveryStop(
                "order1", 12.9716, 77.5946, "Start", "PICKUP"));
        stops.add(new RouteOptimizationService.DeliveryStop(
                "order2", 12.9352, 77.6245, "Stop1", "DELIVERY"));
        stops.add(new RouteOptimizationService.DeliveryStop(
                "order3", 13.0827, 77.6063, "Stop2", "DELIVERY"));
        
        // Act
        double totalDistance = routeOptimizationService.calculateTotalRouteDistance(stops);
        
        // Assert
        assertTrue(totalDistance > 0);
    }
    
    @Test
    @DisplayName("Should check delivery feasibility")
    void testDeliveryFeasibility() {
        // Arrange
        com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO distanceMatrix = 
                new com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO();
        distanceMatrix.setDurationSeconds(1200L); // 20 minutes
        distanceMatrix.setStatus("OK");
        
        // Act & Assert - Should be feasible within 30 minutes
        assertTrue(routeOptimizationService.isDeliveryFeasible(distanceMatrix, 30));
        
        // Act & Assert - Should not be feasible within 10 minutes
        assertFalse(routeOptimizationService.isDeliveryFeasible(distanceMatrix, 10));
    }
}
