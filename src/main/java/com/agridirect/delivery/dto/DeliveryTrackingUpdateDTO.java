package com.agridirect.delivery.dto;

import jakarta.validation.constraints.*;

public class DeliveryTrackingUpdateDTO {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "Status is required")
    private String status; // PICKED_UP, IN_TRANSIT, NEAR_DELIVERY, DELIVERED, CANCELLED
    
    @NotNull(message = "Current latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double currentLatitude;
    
    @NotNull(message = "Current longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double currentLongitude;
    
    private String currentAddress;
    
    private Double distanceRemainingKm;
    
    private Long estimatedArrivalTime;
    
    private String notes;

    // Constructors
    public DeliveryTrackingUpdateDTO() {
    }

    public DeliveryTrackingUpdateDTO(String orderId, String status, Double latitude, Double longitude) {
        this.orderId = orderId;
        this.status = status;
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public Double getDistanceRemainingKm() {
        return distanceRemainingKm;
    }

    public void setDistanceRemainingKm(Double distanceRemainingKm) {
        this.distanceRemainingKm = distanceRemainingKm;
    }

    public Long getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }

    public void setEstimatedArrivalTime(Long estimatedArrivalTime) {
        this.estimatedArrivalTime = estimatedArrivalTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
