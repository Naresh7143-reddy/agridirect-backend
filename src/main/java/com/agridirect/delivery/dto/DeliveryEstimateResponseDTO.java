package com.agridirect.delivery.dto;

public class DeliveryEstimateResponseDTO {
    
    private Double distanceKm;
    private Integer estimatedTimeMinutes;
    private Double baseCost;
    private Double distanceCost;
    private Double timeCost;
    private Double totalDeliveryCost;
    private Double platformFee;
    private Double grandTotal; // total including platform fee
    private String estimatedDeliveryTime; // e.g., "45 mins"
    private String estimatedDeliveryRange; // e.g., "45-50 mins"

    // Constructors
    public DeliveryEstimateResponseDTO() {
    }

    public DeliveryEstimateResponseDTO(Double distanceKm, Integer estimatedTimeMinutes, 
                                      Double baseCost, Double distanceCost, Double timeCost) {
        this.distanceKm = distanceKm;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.baseCost = baseCost;
        this.distanceCost = distanceCost;
        this.timeCost = timeCost;
        this.totalDeliveryCost = baseCost + distanceCost + timeCost;
    }

    // Getters and Setters
    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public Double getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(Double baseCost) {
        this.baseCost = baseCost;
    }

    public Double getDistanceCost() {
        return distanceCost;
    }

    public void setDistanceCost(Double distanceCost) {
        this.distanceCost = distanceCost;
    }

    public Double getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Double timeCost) {
        this.timeCost = timeCost;
    }

    public Double getTotalDeliveryCost() {
        return totalDeliveryCost;
    }

    public void setTotalDeliveryCost(Double totalDeliveryCost) {
        this.totalDeliveryCost = totalDeliveryCost;
    }

    public Double getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(Double platformFee) {
        this.platformFee = platformFee;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getEstimatedDeliveryRange() {
        return estimatedDeliveryRange;
    }

    public void setEstimatedDeliveryRange(String estimatedDeliveryRange) {
        this.estimatedDeliveryRange = estimatedDeliveryRange;
    }
}
