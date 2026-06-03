package com.agridirect.buyer;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "buyer_profiles")
public class BuyerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "buyer_type")
    private String buyerType;

    private String address;

    @Column(name = "gst_number")
    private String gstNumber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public BuyerProfile() {}

    private BuyerProfile(Builder b) {
        this.userId = b.userId;
        this.buyerType = b.buyerType;
        this.address = b.address;
        this.gstNumber = b.gstNumber;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID userId;
        private String buyerType, address, gstNumber;
        public Builder userId(UUID v)       { this.userId = v; return this; }
        public Builder buyerType(String v)  { this.buyerType = v; return this; }
        public Builder address(String v)    { this.address = v; return this; }
        public Builder gstNumber(String v)  { this.gstNumber = v; return this; }
        public BuyerProfile build()         { return new BuyerProfile(this); }
    }

    // Getters
    public UUID getId()           { return id; }
    public UUID getUserId()       { return userId; }
    public String getBuyerType()  { return buyerType; }
    public String getAddress()    { return address; }
    public String getGstNumber()  { return gstNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(UUID id)              { this.id = id; }
    public void setUserId(UUID userId)      { this.userId = userId; }
    public void setBuyerType(String v)      { this.buyerType = v; }
    public void setAddress(String v)        { this.address = v; }
    public void setGstNumber(String v)      { this.gstNumber = v; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }
}
