package com.agridirect.delivery;

import com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO;
import com.agridirect.delivery.dto.DeliveryEstimateResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to calculate delivery costs and fees (similar to Swiggy/Zomato)
 * 
 * Pricing Model:
 * - Base Cost: Fixed charge per delivery
 * - Distance Cost: Charged per km
 * - Time Cost: Charged per minute (peak hours multiplier)
 * - Platform Fee: 5% of total delivery cost
 */
@Service
public class DeliveryCostCalculator {
    
    @Value("${delivery.base-cost:50}")
    private Double baseCost;
    
    @Value("${delivery.per-km-cost:8}")
    private Double perKmCost;
    
    @Value("${delivery.per-minute-cost:1}")
    private Double perMinuteCost;
    
    @Value("${delivery.min-delivery-radius-km:0.5}")
    private Double minDeliveryRadius;
    
    @Value("${delivery.max-delivery-radius-km:25}")
    private Double maxDeliveryRadius;
    
    /**
     * Calculate delivery cost based on distance and time
     */
    public DeliveryEstimateResponseDTO calculateDeliveryCost(
            DeliveryDistanceMatrixDTO distanceMatrix, Double weight) {
        
        DeliveryEstimateResponseDTO response = new DeliveryEstimateResponseDTO();
        
        if (!distanceMatrix.isSuccessful()) {
            response.setStatus("DELIVERY_NOT_AVAILABLE");
            return response;
        }
        
        Double distanceKm = distanceMatrix.getDistanceKm();
        Integer timeMinutes = distanceMatrix.getDurationMinutes();
        
        // Check if delivery is within service radius
        if (distanceKm < minDeliveryRadius || distanceKm > maxDeliveryRadius) {
            response.setStatus("OUT_OF_DELIVERY_RANGE");
            return response;
        }
        
        response.setDistanceKm(distanceKm);
        response.setEstimatedTimeMinutes(timeMinutes);
        
        // Calculate individual costs
        Double baseCostCharged = baseCost;
        Double distanceCost = Math.max(0, (distanceKm - minDeliveryRadius) * perKmCost);
        Double timeCost = timeMinutes * perMinuteCost;
        
        // Weight charges: ₹2 per kg (default to 5kg if null)
        double actualWeight = weight != null ? weight : 5.0;
        Double weightCharges = actualWeight * 2.0;
        
        // Surge pricing multiplier
        Double surgeMultiplier = calculateSurgeMultiplier();
        Double subtotal = baseCostCharged + distanceCost + timeCost + weightCharges;
        Double surgeCharges = subtotal * (surgeMultiplier - 1.0);
        
        // Weather surcharge (flat ₹30)
        Double weatherSurcharge = 30.0;
        
        Double totalDeliveryCost = subtotal + surgeCharges + weatherSurcharge;
        
        response.setBaseCost(baseCostCharged);
        response.setDistanceCost(distanceCost);
        response.setTimeCost(timeCost);
        response.setWeightCharges(Math.round(weightCharges * 100.0) / 100.0);
        response.setSurgeCharges(Math.round(surgeCharges * 100.0) / 100.0);
        response.setWeatherSurcharge(weatherSurcharge);
        response.setTotalDeliveryCost(Math.round(totalDeliveryCost * 100.0) / 100.0);
        
        // Platform fee/commission (5%)
        Double platformCommission = Math.round(totalDeliveryCost * 0.05 * 100.0) / 100.0;
        response.setPlatformCommission(platformCommission);
        response.setPlatformFee(platformCommission);
        
        // Delivery Partner Earnings (95% of total delivery cost, minimum ₹30 for shorter locations)
        Double partnerEarnings = Math.max(30.0, totalDeliveryCost - platformCommission);
        response.setDeliveryPartnerEarnings(Math.round(partnerEarnings * 100.0) / 100.0);
        
        // Farmer Payout (e.g. simulated from orderAmount if available, or just mock it)
        response.setFarmerPayout(0.0); // Will be updated in Service
        
        // Total Buyer Payment (Simulated in Service)
        response.setTotalBuyerPayment(Math.round((totalDeliveryCost + platformCommission) * 100.0) / 100.0);
        
        // Grand total
        Double grandTotal = response.getTotalDeliveryCost() + platformCommission;
        response.setGrandTotal(Math.round(grandTotal * 100.0) / 100.0);
        
        // Format estimated delivery time
        int minTime = Math.max(20, timeMinutes - 5);
        int maxTime = Math.min(timeMinutes + 5, timeMinutes + 10);
        
        response.setEstimatedDeliveryTime(timeMinutes + " mins");
        response.setEstimatedDeliveryRange(minTime + "-" + maxTime + " mins");
        response.setStatus("SUCCESS");
        
        return response;
    }

    // Overload for backward compatibility if needed
    public DeliveryEstimateResponseDTO calculateDeliveryCost(
            DeliveryDistanceMatrixDTO distanceMatrix) {
        return calculateDeliveryCost(distanceMatrix, 5.0);
    }
    
    /**
     * Calculate surge multiplier based on current time (peak hours)
     * Peak hours: 9-11 AM (1.2x), 12-2 PM (1.3x), 7-9 PM (1.25x)
     */
    private Double calculateSurgeMultiplier() {
        int currentHour = java.time.LocalTime.now().getHour();
        
        if (currentHour >= 9 && currentHour < 11) {
            return 1.2;  // Breakfast peak
        } else if (currentHour >= 12 && currentHour < 14) {
            return 1.3;  // Lunch peak
        } else if (currentHour >= 19 && currentHour < 21) {
            return 1.25; // Dinner peak
        }
        
        return 1.0;  // Normal hours
    }
    
    /**
     * Recalculate cost with updated parameters (for testing/updates)
     */
    public void updateCostParameters(Double newBaseCost, Double newPerKmCost, Double newPerMinuteCost) {
        if (newBaseCost != null && newBaseCost > 0) {
            this.baseCost = newBaseCost;
        }
        if (newPerKmCost != null && newPerKmCost > 0) {
            this.perKmCost = newPerKmCost;
        }
        if (newPerMinuteCost != null && newPerMinuteCost > 0) {
            this.perMinuteCost = newPerMinuteCost;
        }
    }
    
    // Getters for testing
    public Double getBaseCost() {
        return baseCost;
    }
    
    public Double getPerKmCost() {
        return perKmCost;
    }
    
    public Double getPerMinuteCost() {
        return perMinuteCost;
    }
    
    public Double getMinDeliveryRadius() {
        return minDeliveryRadius;
    }
    
    public Double getMaxDeliveryRadius() {
        return maxDeliveryRadius;
    }
}
