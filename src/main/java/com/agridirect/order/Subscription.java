package com.agridirect.order;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "farmer_id")
    private UUID farmerId;

    @Column(name = "frequency", nullable = false)
    private String frequency; // WEEKLY, BI_WEEKLY, MONTHLY

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, PAUSED, CANCELLED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Subscription() {}

    private Subscription(Builder b) {
        this.buyerId = b.buyerId;
        this.productId = b.productId;
        this.farmerId = b.farmerId;
        this.frequency = b.frequency;
        this.quantity = b.quantity;
        this.deliveryAddress = b.deliveryAddress;
        this.status = b.status != null ? b.status : "ACTIVE";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID buyerId, productId, farmerId;
        private String frequency, deliveryAddress, status;
        private Double quantity;

        public Builder buyerId(UUID v)           { this.buyerId = v; return this; }
        public Builder productId(UUID v)         { this.productId = v; return this; }
        public Builder farmerId(UUID v)          { this.farmerId = v; return this; }
        public Builder frequency(String v)       { this.frequency = v; return this; }
        public Builder quantity(Double v)        { this.quantity = v; return this; }
        public Builder deliveryAddress(String v) { this.deliveryAddress = v; return this; }
        public Builder status(String v)          { this.status = v; return this; }
        public Subscription build()              { return new Subscription(this); }
    }

    public UUID getId() { return id; }
    public UUID getBuyerId() { return buyerId; }
    public UUID getProductId() { return productId; }
    public UUID getFarmerId() { return farmerId; }
    public String getFrequency() { return frequency; }
    public Double getQuantity() { return quantity; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(String status) { this.status = status; }
}
