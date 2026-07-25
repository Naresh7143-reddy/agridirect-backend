package com.agridirect.delivery;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_proofs")
public class DeliveryProof {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "delivery_agent_id", nullable = false)
    private UUID deliveryAgentId;

    @Column(name = "proof_type", nullable = false) // "OTP" or "PHOTO"
    private String proofType;

    @Column(name = "otp_provided")
    private String otpProvided;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Column(nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DeliveryProof() {}

    private DeliveryProof(Builder b) {
        this.orderId = b.orderId;
        this.deliveryAgentId = b.deliveryAgentId;
        this.proofType = b.proofType;
        this.otpProvided = b.otpProvided;
        this.photoUrl = b.photoUrl;
        this.verified = b.verified;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID orderId, deliveryAgentId;
        private String proofType, otpProvided, photoUrl;
        private boolean verified = false;

        public Builder orderId(UUID v)           { this.orderId = v; return this; }
        public Builder deliveryAgentId(UUID v)   { this.deliveryAgentId = v; return this; }
        public Builder proofType(String v)       { this.proofType = v; return this; }
        public Builder otpProvided(String v)     { this.otpProvided = v; return this; }
        public Builder photoUrl(String v)        { this.photoUrl = v; return this; }
        public Builder verified(boolean v)       { this.verified = v; return this; }
        public DeliveryProof build()             { return new DeliveryProof(this); }
    }

    public UUID getId()                   { return id; }
    public UUID getOrderId()              { return orderId; }
    public UUID getDeliveryAgentId()      { return deliveryAgentId; }
    public String getProofType()          { return proofType; }
    public String getOtpProvided()        { return otpProvided; }
    public String getPhotoUrl()           { return photoUrl; }
    public boolean isVerified()           { return verified; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public void setId(UUID v)                   { this.id = v; }
    public void setOrderId(UUID v)              { this.orderId = v; }
    public void setDeliveryAgentId(UUID v)      { this.deliveryAgentId = v; }
    public void setProofType(String v)          { this.proofType = v; }
    public void setOtpProvided(String v)        { this.otpProvided = v; }
    public void setPhotoUrl(String v)           { this.photoUrl = v; }
    public void setVerified(boolean v)          { this.verified = v; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }
}
