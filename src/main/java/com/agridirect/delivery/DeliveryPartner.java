package com.agridirect.delivery;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String phone;
    
    @Column(name = "vehicle_type")
    private String vehicleType; // BIKE, CYCLE, SCOOTER, AUTO
    
    @Column(name = "vehicle_registration")
    private String vehicleRegistration;
    
    @Column(name = "current_latitude")
    private Double currentLatitude;
    
    @Column(name = "current_longitude")
    private Double currentLongitude;
    
    @Column(name = "is_available")
    private Boolean isAvailable = false;
    
    @Column(name = "current_orders_count")
    private Integer currentOrdersCount = 0;
    
    @Column(name = "max_concurrent_orders")
    private Integer maxConcurrentOrders = 3;
    
    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;
    
    @Column(name = "avg_rating")
    private Double avgRating = 4.5;
    
    @Column(name = "verification_status")
    private String verificationStatus; // PENDING, VERIFIED, REJECTED
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_location_update")
    private Long lastLocationUpdate;
    
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "updated_at")
    private Long updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
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

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }

    public Integer getCurrentOrdersCount() {
        return currentOrdersCount;
    }

    public void setCurrentOrdersCount(Integer currentOrdersCount) {
        this.currentOrdersCount = currentOrdersCount;
    }

    public Integer getMaxConcurrentOrders() {
        return maxConcurrentOrders;
    }

    public void setMaxConcurrentOrders(Integer maxConcurrentOrders) {
        this.maxConcurrentOrders = maxConcurrentOrders;
    }

    public Integer getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Long getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(Long lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
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

    public boolean canAcceptMoreOrders() {
        return currentOrdersCount < maxConcurrentOrders;
    }
}
