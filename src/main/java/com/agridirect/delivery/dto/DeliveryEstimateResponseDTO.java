package com.agridirect.delivery.dto;

public class DeliveryEstimateResponseDTO {
    
    private String status; // e.g., SUCCESS, OUT_OF_DELIVERY_RANGE, DELIVERY_NOT_AVAILABLE
    private Double distanceKm;
    private Integer estimatedTimeMinutes;
    private Double baseCost;
    private Double distanceCost;
    private Double timeCost;
    private Double weightCharges;
    private Double surgeCharges;
    private Double weatherSurcharge;
    private Double platformCommission;
    private Double deliveryPartnerEarnings;
    private Double farmerPayout;
    private Double totalBuyerPayment;
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
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public Double getWeightCharges() { return weightCharges; }
    public void setWeightCharges(Double weightCharges) { this.weightCharges = weightCharges; }

    public Double getSurgeCharges() { return surgeCharges; }
    public void setSurgeCharges(Double surgeCharges) { this.surgeCharges = surgeCharges; }

    public Double getWeatherSurcharge() { return weatherSurcharge; }
    public void setWeatherSurcharge(Double weatherSurcharge) { this.weatherSurcharge = weatherSurcharge; }

    public Double getPlatformCommission() { return platformCommission; }
    public void setPlatformCommission(Double platformCommission) { this.platformCommission = platformCommission; }

    public Double getDeliveryPartnerEarnings() { return deliveryPartnerEarnings; }
    public void setDeliveryPartnerEarnings(Double deliveryPartnerEarnings) { this.deliveryPartnerEarnings = deliveryPartnerEarnings; }

    public Double getFarmerPayout() { return farmerPayout; }
    public void setFarmerPayout(Double farmerPayout) { this.farmerPayout = farmerPayout; }

    public Double getTotalBuyerPayment() { return totalBuyerPayment; }
    public void setTotalBuyerPayment(Double totalBuyerPayment) { this.totalBuyerPayment = totalBuyerPayment; }

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
