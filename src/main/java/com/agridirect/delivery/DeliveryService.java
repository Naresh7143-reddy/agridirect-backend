package com.agridirect.delivery;

import com.agridirect.delivery.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main delivery service orchestrating all delivery-related operations
 */
@Service
public class DeliveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);
    
    @Autowired
    private MapsService mapsService;
    
    @Autowired
    private DeliveryCostCalculator deliveryCostCalculator;
    
    @Autowired
    private DeliveryMatchingService deliveryMatchingService;
    
    @Autowired
    private RouteOptimizationService routeOptimizationService;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;
    
    @Autowired
    private DeliveryTrackingRepository deliveryTrackingRepository;
    
    /**
     * Estimate delivery cost and time for an order
     * This is the main API endpoint for cost/time calculation
     */
    public DeliveryEstimateResponseDTO estimateDelivery(DeliveryEstimateRequestDTO request) {
        logger.info("Estimating delivery from ({},{}) to ({},{})", 
                   request.getSourceLatitude(), request.getSourceLongitude(),
                   request.getDestLatitude(), request.getDestLongitude());
        
        // Get distance and duration from Maps API
        DeliveryDistanceMatrixDTO distanceMatrix = mapsService.getDistanceAndDuration(
                request.getSourceLatitude(), request.getSourceLongitude(),
                request.getDestLatitude(), request.getDestLongitude());
        
        // If Maps API fails, use fallback Haversine formula
        if (!distanceMatrix.isSuccessful()) {
            logger.warn("Maps API failed, using Haversine fallback");
            Double distanceKm = mapsService.getHaversineDistance(
                    request.getSourceLatitude(), request.getSourceLongitude(),
                    request.getDestLatitude(), request.getDestLongitude());
            Integer timeMinutes = mapsService.estimateDeliveryTimeMinutes(distanceKm);
            
            distanceMatrix.setDistanceMeters(distanceKm * 1000);
            distanceMatrix.setDurationSeconds((long) timeMinutes * 60);
            distanceMatrix.setStatus("OK");
        }
        
        // Calculate cost based on distance and time
        DeliveryEstimateResponseDTO estimate = deliveryCostCalculator.calculateDeliveryCost(distanceMatrix);
        
        logger.info("Delivery estimate calculated: {} km, {} mins, Rs. {}", 
                   estimate.getDistanceKm(), estimate.getEstimatedTimeMinutes(), estimate.getTotalDeliveryCost());
        
        return estimate;
    }
    
    /**
     * Check delivery availability at a location
     */
    public DeliveryMatchingService.DeliveryAvailabilityStatus checkDeliveryAvailability(Double latitude, Double longitude) {
        return deliveryMatchingService.checkAvailability(latitude, longitude);
    }
    
    /**
     * Match order with best delivery partner
     */
    public DeliveryPartnerDTO matchOrderWithPartner(
            String orderId,
            Double pickupLat, Double pickupLng,
            Double deliveryLat, Double deliveryLng) {
        
        DeliveryPartner partner = deliveryMatchingService.matchOrderWithPartner(
                orderId, pickupLat, pickupLng, deliveryLat, deliveryLng);
        
        if (partner == null) {
            logger.warn("Could not find delivery partner for order {}", orderId);
            return null;
        }
        
        return convertToDTO(partner);
    }
    
    /**
     * Create location for a user
     */
    public LocationDTO createLocation(String userId, String locationType, GeoLocationDTO locationDTO) {
        Location location = new Location();
        location.setUserId(userId);
        location.setLocationType(locationType);
        location.setLatitude(locationDTO.getLatitude());
        location.setLongitude(locationDTO.getLongitude());
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setPincode(locationDTO.getPincode());
        location.setPrimary(locationDTO.getIsPrimary());
        location.setActive(true);
        
        // If marking as primary, unmark others
        if (locationDTO.getIsPrimary()) {
            List<Location> existingLocations = locationRepository.findByUserId(userId);
            for (Location loc : existingLocations) {
                loc.setPrimary(false);
                locationRepository.save(loc);
            }
        }
        
        location = locationRepository.save(location);
        logger.info("Created location {} for user {}", location.getId(), userId);
        
        return convertToLocationDTO(location);
    }
    
    /**
     * Get user's primary location
     */
    public LocationDTO getPrimaryLocation(String userId) {
        Optional<Location> location = locationRepository.findByUserIdAndIsPrimaryTrue(userId);
        return location.map(this::convertToLocationDTO).orElse(null);
    }
    
    /**
     * Get all locations for a user
     */
    public List<LocationDTO> getUserLocations(String userId) {
        return locationRepository.findByUserId(userId).stream()
                .map(this::convertToLocationDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Update delivery partner location
     */
    public void updatePartnerLocation(String partnerId, Double latitude, Double longitude) {
        deliveryMatchingService.updatePartnerLocation(partnerId, latitude, longitude);
        logger.info("Updated location for partner {}", partnerId);
    }
    
    /**
     * Update delivery partner availability
     */
    public void updatePartnerAvailability(String partnerId, Boolean isAvailable, 
                                         Double latitude, Double longitude) {
        deliveryMatchingService.updatePartnerAvailability(partnerId, isAvailable, latitude, longitude);
        logger.info("Updated availability for partner {}: {}", partnerId, isAvailable);
    }
    
    /**
     * Update delivery tracking
     */
    public DeliveryTrackingDTO updateDeliveryTracking(DeliveryTrackingUpdateDTO updateDTO) {
        Optional<DeliveryTracking> existing = deliveryTrackingRepository.findByOrderId(updateDTO.getOrderId());
        
        if (!existing.isPresent()) {
            logger.warn("Delivery tracking not found for order {}", updateDTO.getOrderId());
            return null;
        }
        
        DeliveryTracking tracking = existing.get();
        tracking.setStatus(updateDTO.getStatus());
        tracking.setCurrentLatitude(updateDTO.getCurrentLatitude());
        tracking.setCurrentLongitude(updateDTO.getCurrentLongitude());
        tracking.setCurrentAddress(updateDTO.getCurrentAddress());
        tracking.setDistanceRemainingKm(updateDTO.getDistanceRemainingKm());
        tracking.setEstimatedArrivalTime(updateDTO.getEstimatedArrivalTime());
        tracking.setNotes(updateDTO.getNotes());
        
        // Update status-specific timestamps
        if ("PICKED_UP".equals(updateDTO.getStatus()) && tracking.getPickedUpAt() == null) {
            tracking.setPickedUpAt(System.currentTimeMillis());
        } else if ("DELIVERED".equals(updateDTO.getStatus()) && tracking.getDeliveredAt() == null) {
            tracking.setDeliveredAt(System.currentTimeMillis());
            
            // Calculate delay if estimated arrival was provided
            if (tracking.getEstimatedArrivalTime() != null) {
                long delayMs = System.currentTimeMillis() - tracking.getEstimatedArrivalTime();
                if (delayMs > 0) {
                    tracking.setTotalDelaySeconds((int) (delayMs / 1000));
                }
            }
            
            // Decrement partner's order count
            deliveryMatchingService.decrementPartnerOrderCount(tracking.getDeliveryPartnerId());
        }
        
        tracking = deliveryTrackingRepository.save(tracking);
        logger.info("Updated tracking for order {}: status={}", updateDTO.getOrderId(), updateDTO.getStatus());
        
        return convertToTrackingDTO(tracking);
    }
    
    /**
     * Get delivery tracking for an order
     */
    public DeliveryTrackingDTO getDeliveryTracking(String orderId) {
        Optional<DeliveryTracking> tracking = deliveryTrackingRepository.findByOrderId(orderId);
        return tracking.map(this::convertToTrackingDTO).orElse(null);
    }
    
    /**
     * Get active deliveries for a partner
     */
    public List<DeliveryTrackingDTO> getActiveDeliveriesForPartner(String partnerId) {
        return deliveryTrackingRepository.findActiveDeliveriesForPartner(partnerId).stream()
                .map(this::convertToTrackingDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get delivery partner details
     */
    public DeliveryPartnerDTO getDeliveryPartner(String partnerId) {
        Optional<DeliveryPartner> partner = deliveryPartnerRepository.findById(partnerId);
        return partner.map(this::convertToDTO).orElse(null);
    }
    
    /**
     * Get available delivery partners count
     */
    public Integer getAvailablePartnersCount() {
        return deliveryMatchingService.getAvailablePartnersCount();
    }
    
    // Conversion helper methods
    private LocationDTO convertToLocationDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setUserId(location.getUserId());
        dto.setLocationType(location.getLocationType());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setAddress(location.getAddress());
        dto.setCity(location.getCity());
        dto.setState(location.getState());
        dto.setPincode(location.getPincode());
        dto.setIsPrimary(location.getPrimary());
        dto.setIsActive(location.getActive());
        dto.setCreatedAt(location.getCreatedAt());
        dto.setUpdatedAt(location.getUpdatedAt());
        return dto;
    }
    
    private DeliveryTrackingDTO convertToTrackingDTO(DeliveryTracking tracking) {
        DeliveryTrackingDTO dto = new DeliveryTrackingDTO();
        dto.setId(tracking.getId());
        dto.setOrderId(tracking.getOrderId());
        dto.setDeliveryPartnerId(tracking.getDeliveryPartnerId());
        dto.setStatus(tracking.getStatus());
        dto.setCurrentLatitude(tracking.getCurrentLatitude());
        dto.setCurrentLongitude(tracking.getCurrentLongitude());
        dto.setCurrentAddress(tracking.getCurrentAddress());
        dto.setDistanceRemainingKm(tracking.getDistanceRemainingKm());
        dto.setEstimatedArrivalTime(tracking.getEstimatedArrivalTime());
        dto.setLastUpdateTime(tracking.getLastUpdateTime());
        dto.setAssignedAt(tracking.getAssignedAt());
        dto.setPickedUpAt(tracking.getPickedUpAt());
        dto.setDeliveredAt(tracking.getDeliveredAt());
        dto.setTotalDelaySeconds(tracking.getTotalDelaySeconds());
        dto.setNotes(tracking.getNotes());
        dto.setCreatedAt(tracking.getCreatedAt());
        dto.setUpdatedAt(tracking.getUpdatedAt());
        return dto;
    }
    
    private DeliveryPartnerDTO convertToDTO(DeliveryPartner partner) {
        DeliveryPartnerDTO dto = new DeliveryPartnerDTO();
        dto.setId(partner.getId());
        dto.setUserId(partner.getUserId());
        dto.setName(partner.getName());
        dto.setPhone(partner.getPhone());
        dto.setVehicleType(partner.getVehicleType());
        dto.setVehicleRegistration(partner.getVehicleRegistration());
        dto.setCurrentLatitude(partner.getCurrentLatitude());
        dto.setCurrentLongitude(partner.getCurrentLongitude());
        dto.setIsAvailable(partner.getIsAvailable());
        dto.setCurrentOrdersCount(partner.getCurrentOrdersCount());
        dto.setMaxConcurrentOrders(partner.getMaxConcurrentOrders());
        dto.setTotalDeliveries(partner.getTotalDeliveries());
        dto.setAvgRating(partner.getAvgRating());
        dto.setVerificationStatus(partner.getVerificationStatus());
        dto.setIsActive(partner.getIsActive());
        dto.setLastLocationUpdate(partner.getLastLocationUpdate());
        dto.setCreatedAt(partner.getCreatedAt());
        dto.setUpdatedAt(partner.getUpdatedAt());
        return dto;
    }
}
