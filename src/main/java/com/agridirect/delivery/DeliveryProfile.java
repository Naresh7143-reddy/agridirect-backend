package com.agridirect.delivery;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Delivery partner profile entity
 */
@Entity
@Table(name = "delivery_profiles")
public class DeliveryProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(length = 50)
    private String vehicleType;
    
    @Column(name = "license_no", length = 100)
    private String licenseNo;

    @Column(name = "vehicle_registration", length = 100)
    private String vehicleRegistration;
    
    @Column(name = "is_available")
    private Boolean isAvailable;
    
    @Column(name = "current_latitude")
    private Double currentLatitude;
    
    @Column(name = "current_longitude")
    private Double currentLongitude;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public DeliveryProfile() {
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DeliveryProfile profile = new DeliveryProfile();

        public Builder userId(UUID userId) {
            profile.userId = userId;
            return this;
        }

        public Builder vehicleType(String vehicleType) {
            profile.vehicleType = vehicleType;
            return this;
        }

        public Builder licenseNo(String licenseNo) {
            profile.licenseNo = licenseNo;
            return this;
        }

        public Builder vehicleRegistration(String vehicleRegistration) {
            profile.vehicleRegistration = vehicleRegistration;
            return this;
        }

        public Builder isAvailable(Boolean isAvailable) {
            profile.isAvailable = isAvailable;
            return this;
        }

        public DeliveryProfile build() {
            return profile;
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
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

    public Double getCurrentLat() {
        return currentLatitude;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLatitude = currentLat;
    }

    public Double getCurrentLng() {
        return currentLongitude;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLongitude = currentLng;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
