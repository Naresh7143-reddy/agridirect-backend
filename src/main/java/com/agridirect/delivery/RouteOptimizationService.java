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
 * Service for optimizing delivery routes and finding best delivery partners
 * Uses a combination of proximity, rating, and availability metrics
 */
@Service
public class RouteOptimizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteOptimizationService.class);
    
    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;
    
    @Autowired
    private MapsService mapsService;
    
    @Value("${delivery.delivery-partner-search-radius-km:5}")
    private Double searchRadiusKm;
    
    /**
     * Find best delivery partner for an order
     * Scoring algorithm:
     * - Proximity (50% weight): Closer is better
     * - Rating (30% weight): Higher rating is better
     * - Availability (20% weight): Current order load vs capacity
     */
    public DeliveryPartner findBestDeliveryPartner(
            Double pickupLat, Double pickupLng,
            Double deliveryLat, Double deliveryLng) {
        
        logger.info("Finding best delivery partner for route: ({},{}) -> ({},{})", 
                   pickupLat, pickupLng, deliveryLat, deliveryLng);
        
        // Find nearby available partners
        List<DeliveryPartner> nearbyPartners = deliveryPartnerRepository
                .findNearbyAvailablePartners(pickupLat, pickupLng, searchRadiusKm);
        
        if (nearbyPartners.isEmpty()) {
            logger.warn("No available delivery partners found within {} km", searchRadiusKm);
            return null;
        }
        
        // Score each partner
        Map<DeliveryPartner, Double> partnerScores = new HashMap<>();
        for (DeliveryPartner partner : nearbyPartners) {
            double score = calculatePartnerScore(partner, pickupLat, pickupLng);
            partnerScores.put(partner, score);
        }
        
        // Return partner with highest score
        return partnerScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * Calculate score for a delivery partner
     * Score = (50 * proximity_score) + (30 * rating_score) + (20 * availability_score)
     */
    private double calculatePartnerScore(DeliveryPartner partner, Double pickupLat, Double pickupLng) {
        // Proximity score (0-50): inversely proportional to distance
        double distance = mapsService.getHaversineDistance(
                partner.getCurrentLatitude(), partner.getCurrentLongitude(),
                pickupLat, pickupLng);
        double proximityScore = Math.max(0, 50 * (1 - distance / searchRadiusKm));
        
        // Rating score (0-30): normalized from 0-5 to 0-30
        double ratingScore = (partner.getAvgRating() / 5.0) * 30;
        
        // Availability score (0-20): based on current load
        double availabilityRatio = (double) partner.getCurrentOrdersCount() / partner.getMaxConcurrentOrders();
        double availabilityScore = Math.max(0, 20 * (1 - availabilityRatio));
        
        double totalScore = proximityScore + ratingScore + availabilityScore;
        logger.debug("Partner {} score: proximity={}, rating={}, availability={}, total={}", 
                    partner.getId(), proximityScore, ratingScore, availabilityScore, totalScore);
        
        return totalScore;
    }
    
    /**
     * Find multiple delivery partners sorted by score (for fallback options)
     */
    public List<DeliveryPartner> findTopDeliveryPartners(
            Double pickupLat, Double pickupLng, int topN) {
        
        List<DeliveryPartner> nearbyPartners = deliveryPartnerRepository
                .findNearbyAvailablePartners(pickupLat, pickupLng, searchRadiusKm);
        
        if (nearbyPartners.isEmpty()) {
            return new ArrayList<>();
        }
        
        return nearbyPartners.stream()
                .sorted((p1, p2) -> {
                    double score1 = calculatePartnerScore(p1, pickupLat, pickupLng);
                    double score2 = calculatePartnerScore(p2, pickupLat, pickupLng);
                    return Double.compare(score2, score1);
                })
                .limit(topN)
                .collect(Collectors.toList());
    }
    
    /**
     * Optimize delivery route for multiple stops (traveling salesman problem approximation)
     * Uses nearest neighbor heuristic for simplicity
     */
    public List<DeliveryStop> optimizeDeliveryRoute(List<DeliveryStop> stops) {
        if (stops == null || stops.size() <= 1) {
            return stops;
        }
        
        List<DeliveryStop> optimized = new ArrayList<>();
        List<DeliveryStop> remaining = new ArrayList<>(stops);
        
        // Start with first stop
        DeliveryStop current = remaining.remove(0);
        optimized.add(current);
        
        // Nearest neighbor algorithm
        while (!remaining.isEmpty()) {
            DeliveryStop nearest = remaining.stream()
                    .min(Comparator.comparingDouble(stop -> 
                        calculateDistance(current.getLatitude(), current.getLongitude(),
                                        stop.getLatitude(), stop.getLongitude())))
                    .orElse(null);
            
            if (nearest != null) {
                optimized.add(nearest);
                remaining.remove(nearest);
                current = nearest;
            }
        }
        
        logger.info("Optimized delivery route with {} stops", optimized.size());
        return optimized;
    }
    
    /**
     * Calculate Haversine distance between two coordinates (in km)
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        return mapsService.getHaversineDistance(lat1, lon1, lat2, lon2);
    }
    
    /**
     * Check if delivery is feasible within given time constraint
     */
    public boolean isDeliveryFeasible(DeliveryDistanceMatrixDTO distanceMatrix, Integer maxTimeMinutes) {
        if (!distanceMatrix.isSuccessful()) {
            return false;
        }
        return distanceMatrix.getDurationMinutes() <= maxTimeMinutes;
    }
    
    /**
     * Calculate total distance for a route
     */
    public double calculateTotalRouteDistance(List<DeliveryStop> stops) {
        double totalDistance = 0;
        for (int i = 0; i < stops.size() - 1; i++) {
            DeliveryStop current = stops.get(i);
            DeliveryStop next = stops.get(i + 1);
            totalDistance += calculateDistance(current.getLatitude(), current.getLongitude(),
                                             next.getLatitude(), next.getLongitude());
        }
        return totalDistance;
    }
    
    // Inner class for delivery stops
    public static class DeliveryStop {
        private String orderId;
        private Double latitude;
        private Double longitude;
        private String address;
        private String type; // PICKUP, DELIVERY
        
        public DeliveryStop(String orderId, Double latitude, Double longitude, String address, String type) {
            this.orderId = orderId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.type = type;
        }

        public String getOrderId() { return orderId; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
        public String getAddress() { return address; }
        public String getType() { return type; }
    }
}
