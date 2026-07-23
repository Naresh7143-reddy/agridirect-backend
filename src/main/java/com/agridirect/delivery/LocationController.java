package com.agridirect.delivery;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.delivery.dto.GeoLocationDTO;
import com.agridirect.delivery.dto.LocationDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for managing user locations
 */
@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    
    @Autowired
    private DeliveryService deliveryService;
    
    /**
     * Create a new location for the current user
     * 
     * @param locationType FARMER, BUYER, or DELIVERY_PARTNER
     * @param locationDTO location details
     * @return Created location
     */
    @PostMapping("/{locationType}")
    @PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<LocationDTO>> createLocation(
            @PathVariable String locationType,
            @Valid @RequestBody GeoLocationDTO locationDTO,
            Authentication authentication) {
        
        String userId = authentication.getName();
        logger.info("Creating {} location for user {}", locationType, userId);
        
        try {
            // Validate location type
            if (!isValidLocationType(locationType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, "Invalid location type: " + locationType, null));
            }
            
            LocationDTO created = deliveryService.createLocation(userId, locationType, locationDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "Location created successfully", created));
        } catch (Exception e) {
            logger.error("Error creating location", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error creating location: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get primary location for the current user
     * 
     * @return Primary location
     */
    @GetMapping("/primary")
    @PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<LocationDTO>> getPrimaryLocation(
            Authentication authentication) {
        
        String userId = authentication.getName();
        logger.info("Getting primary location for user {}", userId);
        
        try {
            LocationDTO location = deliveryService.getPrimaryLocation(userId);
            
            if (location == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "No primary location found", null));
            }
            
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Primary location retrieved", location));
        } catch (Exception e) {
            logger.error("Error retrieving primary location", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error retrieving location: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get all locations for the current user
     * 
     * @return List of all user locations
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('FARMER', 'BUYER', 'DELIVERY_PARTNER')")
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getUserLocations(
            Authentication authentication) {
        
        String userId = authentication.getName();
        logger.info("Getting all locations for user {}", userId);
        
        try {
            List<LocationDTO> locations = deliveryService.getUserLocations(userId);
            
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "User locations retrieved", locations));
        } catch (Exception e) {
            logger.error("Error retrieving user locations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error retrieving locations: " + e.getMessage(), null));
        }
    }
    
    // Helper method to validate location type
    private boolean isValidLocationType(String locationType) {
        return "FARMER".equals(locationType) || 
               "BUYER".equals(locationType) || 
               "DELIVERY_PARTNER".equals(locationType);
    }
}
