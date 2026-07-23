package com.agridirect.delivery;

import com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO;
import com.agridirect.delivery.dto.DeliveryEstimateResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Delivery Cost Calculator Tests")
class DeliveryCostCalculatorTest {
    
    private DeliveryCostCalculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new DeliveryCostCalculator();
    }
    
    @Test
    @DisplayName("Should calculate cost for short distance delivery")
    void testCalculateCostForShortDistance() {
        // Arrange
        DeliveryDistanceMatrixDTO distanceMatrix = new DeliveryDistanceMatrixDTO();
        distanceMatrix.setDistanceMeters(2000.0); // 2 km
        distanceMatrix.setDurationSeconds(600L); // 10 minutes
        distanceMatrix.setStatus("OK");
        
        // Act
        DeliveryEstimateResponseDTO estimate = calculator.calculateDeliveryCost(distanceMatrix);
        
        // Assert
        assertEquals("SUCCESS", estimate.getStatus());
        assertEquals(2.0, estimate.getDistanceKm());
        assertEquals(10, estimate.getEstimatedTimeMinutes());
        assertTrue(estimate.getTotalDeliveryCost() > 0);
        assertTrue(estimate.getGrandTotal() > estimate.getTotalDeliveryCost());
    }
    
    @Test
    @DisplayName("Should calculate cost for medium distance delivery")
    void testCalculateCostForMediumDistance() {
        // Arrange
        DeliveryDistanceMatrixDTO distanceMatrix = new DeliveryDistanceMatrixDTO();
        distanceMatrix.setDistanceMeters(5000.0); // 5 km
        distanceMatrix.setDurationSeconds(1200L); // 20 minutes
        distanceMatrix.setStatus("OK");
        
        // Act
        DeliveryEstimateResponseDTO estimate = calculator.calculateDeliveryCost(distanceMatrix);
        
        // Assert
        assertEquals("SUCCESS", estimate.getStatus());
        assertEquals(5.0, estimate.getDistanceKm());
        assertEquals(20, estimate.getEstimatedTimeMinutes());
        assertTrue(estimate.getDistanceCost() > 0);
        assertTrue(estimate.getTimeCost() > 0);
    }
    
    @Test
    @DisplayName("Should reject delivery outside maximum radius")
    void testRejectDeliveryOutsideMaxRadius() {
        // Arrange
        DeliveryDistanceMatrixDTO distanceMatrix = new DeliveryDistanceMatrixDTO();
        distanceMatrix.setDistanceMeters(30000.0); // 30 km (exceeds max)
        distanceMatrix.setDurationSeconds(5400L);
        distanceMatrix.setStatus("OK");
        
        // Act
        DeliveryEstimateResponseDTO estimate = calculator.calculateDeliveryCost(distanceMatrix);
        
        // Assert
        assertEquals("OUT_OF_DELIVERY_RANGE", estimate.getStatus());
    }
    
    @Test
    @DisplayName("Should handle API failure gracefully")
    void testHandleApiFailure() {
        // Arrange
        DeliveryDistanceMatrixDTO distanceMatrix = new DeliveryDistanceMatrixDTO();
        distanceMatrix.setStatus("ERROR");
        
        // Act
        DeliveryEstimateResponseDTO estimate = calculator.calculateDeliveryCost(distanceMatrix);
        
        // Assert
        assertEquals("DELIVERY_NOT_AVAILABLE", estimate.getStatus());
    }
    
    @Test
    @DisplayName("Should include platform fee in grand total")
    void testPlatformFeeCalculation() {
        // Arrange
        DeliveryDistanceMatrixDTO distanceMatrix = new DeliveryDistanceMatrixDTO();
        distanceMatrix.setDistanceMeters(3000.0);
        distanceMatrix.setDurationSeconds(900L);
        distanceMatrix.setStatus("OK");
        
        // Act
        DeliveryEstimateResponseDTO estimate = calculator.calculateDeliveryCost(distanceMatrix);
        
        // Assert
        Double platformFee = estimate.getTotalDeliveryCost() * 0.05;
        assertEquals(Math.round(platformFee * 100.0) / 100.0, estimate.getPlatformFee(), 0.01);
        assertEquals(estimate.getTotalDeliveryCost() + estimate.getPlatformFee(), 
                    estimate.getGrandTotal(), 0.01);
    }
}
