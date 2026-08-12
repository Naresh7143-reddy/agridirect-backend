package com.agridirect.delivery;

import com.agridirect.delivery.dto.DeliveryEstimateRequestDTO;
import com.agridirect.delivery.dto.DeliveryEstimateResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.mock.mockito.MockBean;

@DisplayName("Delivery Estimation Controller Tests")
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class DeliveryEstimationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private DeliveryService deliveryService;
    
    private DeliveryEstimateRequestDTO validRequest;
    private DeliveryEstimateResponseDTO validResponse;
    
    @BeforeEach
    void setUp() {
        // Create a valid request
        validRequest = new DeliveryEstimateRequestDTO();
        validRequest.setSourceLatitude(12.9716);
        validRequest.setSourceLongitude(77.5946);
        validRequest.setDestLatitude(12.9352);
        validRequest.setDestLongitude(77.6245);
        validRequest.setSourceAddress("Pickup Location");
        validRequest.setDestAddress("Delivery Location");
        validRequest.setOrderAmount(500.0);
        
        // Create a valid response
        validResponse = new DeliveryEstimateResponseDTO();
        validResponse.setDistanceKm(5.0);
        validResponse.setEstimatedTimeMinutes(20);
        validResponse.setBaseCost(50.0);
        validResponse.setDistanceCost(40.0);
        validResponse.setTimeCost(20.0);
        validResponse.setTotalDeliveryCost(110.0);
        validResponse.setPlatformFee(5.5);
        validResponse.setGrandTotal(115.5);
        validResponse.setEstimatedDeliveryTime("20 mins");
        validResponse.setEstimatedDeliveryRange("15-25 mins");
        validResponse.setStatus("SUCCESS");
    }
    
    @Test
    @DisplayName("Should estimate delivery successfully")
    void testEstimateDeliverySuccess() throws Exception {
        // Arrange
        when(deliveryService.estimateDelivery(any(DeliveryEstimateRequestDTO.class)))
                .thenReturn(validResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/delivery/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.distanceKm").value(5.0))
                .andExpect(jsonPath("$.data.totalDeliveryCost").value(110.0));
    }
    
    @Test
    @DisplayName("Should reject invalid request with missing fields")
    void testEstimateDeliveryInvalidRequest() throws Exception {
        // Arrange - Invalid request (missing destination)
        DeliveryEstimateRequestDTO invalidRequest = new DeliveryEstimateRequestDTO();
        invalidRequest.setSourceLatitude(12.9716);
        invalidRequest.setSourceLongitude(77.5946);
        
        // Act & Assert
        mockMvc.perform(post("/api/delivery/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should check delivery availability")
    void testCheckAvailability() throws Exception {
        // Arrange
        DeliveryMatchingService.DeliveryAvailabilityStatus status = 
                new DeliveryMatchingService.DeliveryAvailabilityStatus();
        status.setIsAvailable(true);
        status.setAvailablePartnersCount(5);
        status.setAvgRating(4.5);
        status.setEstimatedWaitMinutes(10);
        status.setAvailabilityStatus("HIGH");
        
        when(deliveryService.checkDeliveryAvailability(12.9716, 77.5946))
                .thenReturn(status);
        
        // Act & Assert
        mockMvc.perform(get("/api/delivery/availability")
                .param("latitude", "12.9716")
                .param("longitude", "77.5946"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isAvailable").value(true))
                .andExpect(jsonPath("$.data.availablePartnersCount").value(5));
    }
    
    @Test
    @DisplayName("Should track delivery successfully")
    void testTrackDelivery() throws Exception {
        // Arrange
        com.agridirect.delivery.dto.DeliveryTrackingDTO trackingDTO = 
                new com.agridirect.delivery.dto.DeliveryTrackingDTO();
        trackingDTO.setId("tracking1");
        trackingDTO.setOrderId("order1");
        trackingDTO.setStatus("IN_TRANSIT");
        trackingDTO.setCurrentLatitude(12.9716);
        trackingDTO.setCurrentLongitude(77.5946);
        trackingDTO.setDistanceRemainingKm(2.5);
        
        when(deliveryService.getDeliveryTracking("order1"))
                .thenReturn(trackingDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/delivery/track/order1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.data.distanceRemainingKm").value(2.5));
    }
    
    @Test
    @DisplayName("Should return 404 for non-existent order")
    void testTrackDeliveryNotFound() throws Exception {
        // Arrange
        when(deliveryService.getDeliveryTracking("invalid"))
                .thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(get("/api/delivery/track/invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
