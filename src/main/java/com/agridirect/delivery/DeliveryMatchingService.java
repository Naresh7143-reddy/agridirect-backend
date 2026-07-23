package com.agridirect.delivery;

import com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for matching orders with available delivery partners
 * Uses intelligent matching based on:
 * - Proximity to pickup location
 * - Current availability (order capacity)
 * - Performance rating
 * - Verification status
 * - Vehicle type suitability
 */
@Service
public class DeliveryMatchingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeliveryMatchingService.class);
    
    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;
    
    @Autowired
    private RouteOptimizationService routeOptimizationService;
    
    @Autowired
    private MapsService mapsService;
    
    @Autowired
    private DeliveryTrackingRepository deliveryTrackingRepository;
    
    @Value("${delivery.delivery-partner-search-radius-km:5}")
    private Double searchRadiusKm;
    
    @Value("${delivery.base-cost:50}")
    private Double baseCost;
    
    /**
     * Find and match best delivery partner for an order
     * Returns the matched partner or null if no suitable partner found
     */
    public DeliveryPartner matchOrderWithPartner(
            String orderId,
            Double pickupLatitude, Double pickupLongitude,
            Double deliveryLatitude, Double deliveryLongitude) {
        
        logger.info("Attempting to match order {} with delivery partner", orderId);
        
        // Step 1: Find nearby available partners
        List<DeliveryPartner> candidatePartners = findCandidatePartners(pickupLatitude, pickupLongitude);
        
        if (candidatePartners.isEmpty()) {
            logger.warn("No candidate partners found for order {}", orderId);
            return null;
        }
        
        // Step 2: Score and rank candidates
        Map<DeliveryPartner, Double> partnerScores = scorePartners(
                candidatePartners, 
                pickupLatitude, pickupLongitude,
                deliveryLatitude, deliveryLongitude);
        
        if (partnerScores.isEmpty()) {
            logger.warn("No partners passed filtering for order {}", orderId);
            return null;
        }
        
        // Step 3: Select best partner
        DeliveryPartner bestPartner = partnerScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        if (bestPartner != null) {
            logger.info("Matched order {} with delivery partner {}", orderId, bestPartner.getId());
            incrementPartnerOrderCount(bestPartner);
        }
        
        return bestPartner;
    }
    
    /**
     * Find candidate delivery partners within search radius
     * Only includes verified, active, and available partners
     */
    private List<DeliveryPartner> findCandidatePartners(Double latitude, Double longitude) {
        return deliveryPartnerRepository.findNearbyAvailablePartners(latitude, longitude, searchRadiusKm);
    }
    
    /**
     * Score partners based on multiple criteria
     * Scoring breakdown:
     * - Distance score (40%): Closer partners score higher
     * - Rating score (35%): Higher rated partners score higher
     * - Availability score (25%): Partners with fewer orders score higher
     */
    private Map<DeliveryPartner, Double> scorePartners(
            List<DeliveryPartner> partners,
            Double pickupLat, Double pickupLng,
            Double deliveryLat, Double deliveryLng) {
        
        Map<DeliveryPartner, Double> scores = new HashMap<>();
        
        for (DeliveryPartner partner : partners) {
            // Skip if partner cannot accept more orders
            if (!partner.canAcceptMoreOrders()) {
                logger.debug("Partner {} is at max capacity", partner.getId());
                continue;
            }
            
            // Calculate distance score (40%)
            double distanceToPickup = mapsService.getHaversineDistance(
                    partner.getCurrentLatitude(), partner.getCurrentLongitude(),
                    pickupLat, pickupLng);
            
            double distanceScore = Math.max(0, 40 * (1 - distanceToPickup / searchRadiusKm));
            
            // Calculate rating score (35%)
            double ratingScore = (partner.getAvgRating() / 5.0) * 35;
            
            // Calculate availability score (25%)
            double availabilityRatio = (double) partner.getCurrentOrdersCount() / partner.getMaxConcurrentOrders();
            double availabilityScore = Math.max(0, 25 * (1 - availabilityRatio));
            
            // Bonus for recently active (5% bonus if updated in last 5 minutes)
            double bonusScore = 0;
            long currentTime = System.currentTimeMillis();
            if (partner.getLastLocationUpdate() != null && 
                (currentTime - partner.getLastLocationUpdate()) < 5 * 60 * 1000) {
                bonusScore = 5;
            }
            
            double totalScore = distanceScore + ratingScore + availabilityScore + bonusScore;
            scores.put(partner, totalScore);
            
            logger.debug("Partner {} scored: distance={}, rating={}, availability={}, bonus={}, total={}", 
                        partner.getId(), distanceScore, ratingScore, availabilityScore, bonusScore, totalScore);
        }
        
        return scores;
    }
    
    /**
     * Check if a partner is available for delivery at given location and time
     */
    public boolean isPartnerAvailable(DeliveryPartner partner, 
                                      Double pickupLat, Double pickupLng,
                                      Long estimatedPickupTime) {
        
        // Check basic availability
        if (!partner.getIsAvailable() || !partner.getIsActive() || 
            !"VERIFIED".equals(partner.getVerificationStatus())) {
            return false;
        }
        
        // Check order capacity
        if (!partner.canAcceptMoreOrders()) {
            return false;
        }
        
        // Check distance
        double distance = mapsService.getHaversineDistance(
                partner.getCurrentLatitude(), partner.getCurrentLongitude(),
                pickupLat, pickupLng);
        
        if (distance > searchRadiusKm) {
            return false;
        }
        
        // Check last location update (not older than 5 minutes)
        if (partner.getLastLocationUpdate() != null) {
            long ageMinutes = (System.currentTimeMillis() - partner.getLastLocationUpdate()) / (60 * 1000);
            if (ageMinutes > 5) {
                logger.warn("Partner {} location data is stale ({} minutes old)", partner.getId(), ageMinutes);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get delivery availability status at a location
     * Returns number of available partners and estimated wait time
     */
    public DeliveryAvailabilityStatus checkAvailability(Double latitude, Double longitude) {
        List<DeliveryPartner> availablePartners = findCandidatePartners(latitude, longitude);
        
        DeliveryAvailabilityStatus status = new DeliveryAvailabilityStatus();
        status.setAvailablePartnersCount(availablePartners.size());
        status.setIsAvailable(availablePartners.size() > 0);
        
        if (!availablePartners.isEmpty()) {
            // Calculate average rating of available partners
            double avgRating = availablePartners.stream()
                    .mapToDouble(DeliveryPartner::getAvgRating)
                    .average()
                    .orElse(4.0);
            
            // Estimate wait time based on partner load
            double avgOrderLoad = availablePartners.stream()
                    .mapToDouble(p -> (double) p.getCurrentOrdersCount())
                    .average()
                    .orElse(0.0);
            
            // Rough estimation: 5-10 minutes base + 2 minutes per existing order
            int estimatedWaitMinutes = (int) (5 + (avgOrderLoad * 2));
            
            status.setAvgRating(Math.round(avgRating * 10.0) / 10.0);
            status.setEstimatedWaitMinutes(estimatedWaitMinutes);
            status.setAvailabilityStatus("HIGH");
        } else {
            status.setAvailabilityStatus("LOW");
            status.setEstimatedWaitMinutes(30); // Default wait time when unavailable
        }
        
        return status;
    }
    
    /**
     * Increment partner's current order count
     */
    private void incrementPartnerOrderCount(DeliveryPartner partner) {
        partner.setCurrentOrdersCount(partner.getCurrentOrdersCount() + 1);
        partner.setLastLocationUpdate(System.currentTimeMillis());
        deliveryPartnerRepository.save(partner);
    }
    
    /**
     * Decrement partner's current order count (after delivery completed)
     */
    public void decrementPartnerOrderCount(String partnerId) {
        Optional<DeliveryPartner> partner = deliveryPartnerRepository.findById(partnerId);
        if (partner.isPresent()) {
            DeliveryPartner p = partner.get();
            p.setCurrentOrdersCount(Math.max(0, p.getCurrentOrdersCount() - 1));
            deliveryPartnerRepository.save(p);
            logger.info("Decremented order count for partner {}", partnerId);
        }
    }
    
    /**
     * Update partner's availability status
     */
    public void updatePartnerAvailability(String partnerId, Boolean isAvailable, 
                                        Double latitude, Double longitude) {
        Optional<DeliveryPartner> partner = deliveryPartnerRepository.findById(partnerId);
        if (partner.isPresent()) {
            DeliveryPartner p = partner.get();
            p.setIsAvailable(isAvailable);
            if (latitude != null && longitude != null) {
                p.setCurrentLatitude(latitude);
                p.setCurrentLongitude(longitude);
            }
            p.setLastLocationUpdate(System.currentTimeMillis());
            deliveryPartnerRepository.save(p);
            logger.info("Updated availability for partner {}: available={}", partnerId, isAvailable);
        }
    }
    
    /**
     * Update partner location
     */
    public void updatePartnerLocation(String partnerId, Double latitude, Double longitude) {
        Optional<DeliveryPartner> partner = deliveryPartnerRepository.findById(partnerId);
        if (partner.isPresent()) {
            DeliveryPartner p = partner.get();
            p.setCurrentLatitude(latitude);
            p.setCurrentLongitude(longitude);
            p.setLastLocationUpdate(System.currentTimeMillis());
            deliveryPartnerRepository.save(p);
        }
    }
    
    /**
     * Check if there are any available delivery partners in the system
     */
    public Integer getAvailablePartnersCount() {
        return deliveryPartnerRepository.countAvailablePartners();
    }
    
    // Inner class for availability status
    public static class DeliveryAvailabilityStatus {
        private Boolean isAvailable;
        private Integer availablePartnersCount;
        private Double avgRating;
        private Integer estimatedWaitMinutes;
        private String availabilityStatus; // HIGH, MEDIUM, LOW
        
        public DeliveryAvailabilityStatus() {
            this.isAvailable = false;
            this.availablePartnersCount = 0;
            this.avgRating = 0.0;
            this.estimatedWaitMinutes = 30;
            this.availabilityStatus = "LOW";
        }

        // Getters and Setters
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean available) { isAvailable = available; }
        
        public Integer getAvailablePartnersCount() { return availablePartnersCount; }
        public void setAvailablePartnersCount(Integer count) { availablePartnersCount = count; }
        
        public Double getAvgRating() { return avgRating; }
        public void setAvgRating(Double rating) { avgRating = rating; }
        
        public Integer getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
        public void setEstimatedWaitMinutes(Integer minutes) { estimatedWaitMinutes = minutes; }
        
        public String getAvailabilityStatus() { return availabilityStatus; }
        public void setAvailabilityStatus(String status) { availabilityStatus = status; }
    }
}
