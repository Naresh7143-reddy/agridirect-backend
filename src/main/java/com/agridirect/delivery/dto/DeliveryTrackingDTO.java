package com.agridirect.delivery.dto;

public class DeliveryTrackingDTO {
    
    private String id;
    private String orderId;
    private String deliveryPartnerId;
    private String status;
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentAddress;
    private Double distanceRemainingKm;
    private Long estimatedArrivalTime;
    private Long lastUpdateTime;
    private Long assignedAt;
    private Long pickedUpAt;
    private Long deliveredAt;
    private Integer totalDelaySeconds;
    private String notes;
    private Long createdAt;
    private Long updatedAt;

    // Constructors
    public DeliveryTrackingDTO() {
    }

    public DeliveryTrackingDTO(String orderId, String deliveryPartnerId, String status,
                             Double currentLatitude, Double currentLongitude) {
        this.orderId = orderId;
        this.deliveryPartnerId = deliveryPartnerId;
        this.status = status;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(String deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
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

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Long getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Long assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Long getPickedUpAt() {
        return pickedUpAt;
    }

    public void setPickedUpAt(Long pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }

    public Long getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Long deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Integer getTotalDelaySeconds() {
        return totalDelaySeconds;
    }

    public void setTotalDelaySeconds(Integer totalDelaySeconds) {
        this.totalDelaySeconds = totalDelaySeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
