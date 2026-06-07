package com.agridirect.delivery;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_profiles")
public class DeliveryProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "license_no")
    private String licenseNo;

    @Column(name = "is_available")
    private boolean isAvailable = true;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DeliveryProfile() {}

    private DeliveryProfile(Builder b) {
        this.userId = b.userId;
        this.vehicleType = b.vehicleType;
        this.licenseNo = b.licenseNo;
        this.isAvailable = b.isAvailable;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID userId;
        private String vehicleType, licenseNo;
        private boolean isAvailable = true;
        public Builder userId(UUID v)          { this.userId = v; return this; }
        public Builder vehicleType(String v)   { this.vehicleType = v; return this; }
        public Builder licenseNo(String v)     { this.licenseNo = v; return this; }
        public Builder isAvailable(boolean v)  { this.isAvailable = v; return this; }
        public DeliveryProfile build()         { return new DeliveryProfile(this); }
    }

    // Getters
    public UUID getId()             { return id; }
    public UUID getUserId()         { return userId; }
    public String getVehicleType()  { return vehicleType; }
    public String getLicenseNo()    { return licenseNo; }
    public boolean isAvailable()    { return isAvailable; }
    public String getPhotoUrl()     { return photoUrl; }
    public Double getCurrentLat()   { return currentLat; }
    public Double getCurrentLng()   { return currentLng; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(UUID id)              { this.id = id; }
    public void setUserId(UUID userId)      { this.userId = userId; }
    public void setVehicleType(String v)    { this.vehicleType = v; }
    public void setLicenseNo(String v)      { this.licenseNo = v; }
    public void setAvailable(boolean v)     { this.isAvailable = v; }
    public void setPhotoUrl(String v)       { this.photoUrl = v; }
    public void setCurrentLat(Double v)     { this.currentLat = v; }
    public void setCurrentLng(Double v)     { this.currentLng = v; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }
}
