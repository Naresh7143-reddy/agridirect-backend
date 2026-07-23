package com.agridirect.delivery;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.delivery.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for delivery estimation, cost calculation, and tracking
 * Endpoints similar to Swiggy/Zomato for delivery cost and time estimation
 */
@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryEstimationController {
    
    private static final Logger logger = LoggerFactory.getLogger(DeliveryEstimationController.class);
    
    @Autowired
    private DeliveryService deliveryService;
    
    /**
     * Estimate delivery cost and time for an order
     * Public endpoint - used by buyers/farmers before placing order
     * 
     * @param request containing source and destination coordinates
     * @return DeliveryEstimateResponseDTO with cost breakdown and time estimate
     */
    @PostMapping("/estimate")
    public ResponseEntity<ApiResponse<DeliveryEstimateResponseDTO>> estimateDelivery(
            @Valid @RequestBody DeliveryEstimateRequestDTO request) {
        
        logger.info("Delivery estimate request: source ({},{}), destination ({},{})",
                   request.getSourceLatitude(), request.getSourceLongitude(),
                   request.getDestLatitude(), request.getDestLongitude());
        
        try {
            DeliveryEstimateResponseDTO estimate = deliveryService.estimateDelivery(request);
            
            if ("SUCCESS".equals(estimate.getStatus())) {
                return ResponseEntity.ok(
                        new ApiResponse<>(200, "Delivery estimate calculated successfully", estimate));
            } else if ("OUT_OF_DELIVERY_RANGE".equals(estimate.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, "Delivery location is outside service area", estimate));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ApiResponse<>(503, "Unable to calculate delivery estimate", estimate));
            }
        } catch (Exception e) {
            logger.error("Error estimating delivery", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error calculating delivery estimate: " + e.getMessage(), null));
        }
    }
    
    /**
     * Check delivery availability at a location
     * Public endpoint - used to check if delivery is available
     * 
     * @param latitude destination latitude
     * @param longitude destination longitude
     * @return Availability status with partner count and estimated wait time
     */
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<DeliveryMatchingService.DeliveryAvailabilityStatus>> checkAvailability(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        logger.info("Checking delivery availability at ({},{})", latitude, longitude);
        
        try {
            DeliveryMatchingService.DeliveryAvailabilityStatus status = 
                    deliveryService.checkDeliveryAvailability(latitude, longitude);
            
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Delivery availability checked", status));
        } catch (Exception e) {
            logger.error("Error checking delivery availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error checking availability: " + e.getMessage(), null));
        }
    }
    
    /**
     * Match order with best delivery partner
     * Internal endpoint - used by backend order processing
     * 
     * @param orderId order ID
     * @param pickupLat pickup latitude
     * @param pickupLng pickup longitude
     * @param deliveryLat delivery latitude
     * @param deliveryLng delivery longitude
     * @return Matched delivery partner details
     */
    @PostMapping("/match-partner")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryPartnerDTO>> matchPartner(
            @RequestParam String orderId,
            @RequestParam Double pickupLat,
            @RequestParam Double pickupLng,
            @RequestParam Double deliveryLat,
            @RequestParam Double deliveryLng) {
        
        logger.info("Matching order {} with delivery partner", orderId);
        
        try {
            DeliveryPartnerDTO partner = deliveryService.matchOrderWithPartner(
                    orderId, pickupLat, pickupLng, deliveryLat, deliveryLng);
            
            if (partner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "No available delivery partners found", null));
            }
            
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Delivery partner matched successfully", partner));
        } catch (Exception e) {
            logger.error("Error matching delivery partner", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error matching partner: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get delivery tracking for an order
     * Public endpoint - used by buyers to track their order
     * 
     * @param orderId order ID
     * @return Current delivery tracking status and location
     */
    @GetMapping("/track/{orderId}")
    public ResponseEntity<ApiResponse<DeliveryTrackingDTO>> trackDelivery(
            @PathVariable String orderId) {
        
        logger.info("Tracking delivery for order {}", orderId);
        
        try {
            DeliveryTrackingDTO tracking = deliveryService.getDeliveryTracking(orderId);
            
            if (tracking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Delivery tracking not found", null));
            }
            
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Delivery tracking retrieved", tracking));
        } catch (Exception e) {
            logger.error("Error retrieving delivery tracking", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error retrieving tracking: " + e.getMessage(), null));
        }
    }
    
    /**
     * Update delivery tracking
     * Internal endpoint - used by delivery partners to update status
     * 
     * @param updateDTO containing updated tracking information
     * @return Updated tracking details
     */
    @PutMapping("/track")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<DeliveryTrackingDTO>> updateTracking(
            @Valid @RequestBody DeliveryTrackingUpdateDTO updateDTO) {
        
        logger.info("Updating tracking for order {}, status: {}", 
                   updateDTO.getOrderId(), updateDTO.getStatus());
        
        try {
            DeliveryTrackingDTO tracking = deliveryService.updateDeliveryTracking(updateDTO);
            
            if (tracking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Delivery tracking not found", null));
            }
            
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Delivery tracking updated successfully", tracking));
        } catch (Exception e) {
            logger.error("Error updating delivery tracking", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error updating tracking: " + e.getMessage(), null));
        }
    }
    
    /**
     * Update delivery partner location
     * Internal endpoint - used by delivery partners to update their location
     * 
     * @param partnerId partner ID
     * @param latitude current latitude
     * @param longitude current longitude
     * @return Success response
     */
    @PutMapping("/partner/{partnerId}/location")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<String>> updatePartnerLocation(
            @PathVariable String partnerId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        logger.info("Updating location for partner {}: ({},{})", partnerId, latitude, longitude);
        
        try {
            deliveryService.updatePartnerLocation(partnerId, latitude, longitude);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Partner location updated successfully", "OK"));
        } catch (Exception e) {
            logger.error("Error updating partner location", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error updating location: " + e.getMessage(), null));
        }
    }
    
    /**
     * Update delivery partner availability
     * Internal endpoint - used by delivery partners to toggle availability
     * 
     * @param partnerId partner ID
     * @param isAvailable availability status
     * @param latitude current latitude (optional)
     * @param longitude current longitude (optional)
     * @return Success response
     */
    @PutMapping("/partner/{partnerId}/availability")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<String>> updatePartnerAvailability(
            @PathVariable String partnerId,
            @RequestParam Boolean isAvailable,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        logger.info("Updating availability for partner {}: {}", partnerId, isAvailable);
        
        try {
            deliveryService.updatePartnerAvailability(partnerId, isAvailable, latitude, longitude);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Partner availability updated successfully", "OK"));
        } catch (Exception e) {
            logger.error("Error updating partner availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error updating availability: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get available partners count
     * Admin endpoint - for monitoring
     * 
     * @return Count of available delivery partners
     */
    @GetMapping("/partners/available/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> getAvailablePartnersCount() {
        try {
            Integer count = deliveryService.getAvailablePartnersCount();
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Available partners count retrieved", count));
        } catch (Exception e) {
            logger.error("Error retrieving available partners count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error retrieving count: " + e.getMessage(), null));
        }
    }
}
