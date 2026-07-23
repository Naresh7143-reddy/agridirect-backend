package com.agridirect.delivery;

import com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Maps Service Tests")
class MapsServiceTest {
    
    private MapsService mapsService;
    
    @BeforeEach
    void setUp() {
        mapsService = new MapsService();
    }
    
    @Test
    @DisplayName("Should calculate Haversine distance correctly")
    void testHaversineDistance() {
        // Arrange - Two points in Bangalore
        Double lat1 = 12.9716;
        Double lon1 = 77.5946;
        Double lat2 = 12.9352;
        Double lon2 = 77.6245;
        
        // Act
        Double distance = mapsService.getHaversineDistance(lat1, lon1, lat2, lon2);
        
        // Assert
        assertTrue(distance > 0);
        assertTrue(distance < 100); // Should be less than 100 km
        assertEquals(5.4, distance, 0.5); // Approximately 5.4 km
    }
    
    @Test
    @DisplayName("Should return zero distance for same coordinates")
    void testZeroDistance() {
        // Arrange
        Double lat = 12.9716;
        Double lon = 77.5946;
        
        // Act
        Double distance = mapsService.getHaversineDistance(lat, lon, lat, lon);
        
        // Assert
        assertEquals(0.0, distance, 0.01);
    }
    
    @Test
    @DisplayName("Should estimate delivery time based on distance")
    void testEstimateDeliveryTime() {
        // Arrange
        Double distanceKm = 5.0;
        
        // Act
        Integer timeMinutes = mapsService.estimateDeliveryTimeMinutes(distanceKm);
        
        // Assert
        assertTrue(timeMinutes > 0);
        // At 20 km/h + 5 min buffer: (5/20)*60 + 5 = 20 minutes
        assertEquals(20, timeMinutes);
    }
    
    @Test
    @DisplayName("Should add buffer time to delivery estimate")
    void testDeliveryTimeBuffer() {
        // Arrange
        Double distanceKm = 0.5; // Very short distance
        
        // Act
        Integer timeMinutes = mapsService.estimateDeliveryTimeMinutes(distanceKm);
        
        // Assert
        // Should include 5 minute buffer even for short distances
        assertTrue(timeMinutes >= 5);
    }
    
    @Test
    @DisplayName("Should handle large distances")
    void testLargeDistance() {
        // Arrange
        Double lat1 = 0.0;
        Double lon1 = 0.0;
        Double lat2 = 10.0;
        Double lon2 = 10.0;
        
        // Act
        Double distance = mapsService.getHaversineDistance(lat1, lon1, lat2, lon2);
        
        // Assert
        assertTrue(distance > 0);
        assertTrue(distance > 1000); // Should be > 1000 km
    }
}
