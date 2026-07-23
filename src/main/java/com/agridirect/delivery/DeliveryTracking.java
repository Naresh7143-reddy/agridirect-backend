package com.agridirect.delivery;

import jakarta.persistence.*;

/**
 * Entity for tracking delivery status and location updates
 */
@Entity
@Table(name = "delivery_tracking")
public class DeliveryTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String deliveryPartnerId;
    
    @Column(name = "status")
    private String status; // ASSIGNED, PICKED_UP, IN_TRANSIT, NEAR_DELIVERY, DELIVERED, CANCELLED
    
    @Column(name = "current_latitude")
    private Double currentLatitude;
    
    @Column(name = "current_longitude")
    private Double currentLongitude;
    
    @Column(name = "current_address", length = 500)
    private String currentAddress;
    
    @Column(name = "distance_remaining_km")
    private Double distanceRemainingKm;
    
    @Column(name = "estimated_arrival_time")
    private Long estimatedArrivalTime; // Unix timestamp in ms
    
    @Column(name = "last_update_time")
    private Long lastUpdateTime;
    
    @Column(name = "assigned_at")
    private Long assignedAt;
    
    @Column(name = "picked_up_at")
    private Long pickedUpAt;
    
    @Column(name = "delivered_at")
    private Long deliveredAt;
    
    @Column(name = "total_delay_seconds")
    private Integer totalDelaySeconds = 0;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "updated_at")
    private Long updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        lastUpdateTime = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
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
