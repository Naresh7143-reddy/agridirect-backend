package com.agridirect.farmer;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "farmer_profiles")
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "farm_name")
    private String farmName;

    private String location;

    @Column(name = "land_acres")
    private Double landAcres;

    private boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public FarmerProfile() {}

    private FarmerProfile(Builder b) {
        this.userId = b.userId;
        this.farmName = b.farmName;
        this.location = b.location;
        this.landAcres = b.landAcres;
        this.verified = b.verified;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID userId;
        private String farmName, location;
        private Double landAcres;
        private boolean verified = false;
        public Builder userId(UUID v)       { this.userId = v; return this; }
        public Builder farmName(String v)   { this.farmName = v; return this; }
        public Builder location(String v)   { this.location = v; return this; }
        public Builder landAcres(Double v)  { this.landAcres = v; return this; }
        public Builder verified(boolean v)  { this.verified = v; return this; }
        public FarmerProfile build()        { return new FarmerProfile(this); }
    }

    // Getters
    public UUID getId()             { return id; }
    public UUID getUserId()         { return userId; }
    public String getFarmName()     { return farmName; }
    public String getLocation()     { return location; }
    public Double getLandAcres()    { return landAcres; }
    public boolean isVerified()     { return verified; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(UUID id)              { this.id = id; }
    public void setUserId(UUID userId)      { this.userId = userId; }
    public void setFarmName(String v)       { this.farmName = v; }
    public void setLocation(String v)       { this.location = v; }
    public void setLandAcres(Double v)      { this.landAcres = v; }
    public void setVerified(boolean v)      { this.verified = v; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }
}
