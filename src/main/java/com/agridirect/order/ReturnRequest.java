package com.agridirect.order;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "return_requests")
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "farmer_id")
    private UUID farmerId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, REFUNDED

    @Column(name = "refund_amount")
    private Double refundAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ReturnRequest() {}

    private ReturnRequest(Builder b) {
        this.orderId = b.orderId;
        this.buyerId = b.buyerId;
        this.farmerId = b.farmerId;
        this.reason = b.reason;
        this.status = b.status != null ? b.status : "PENDING";
        this.refundAmount = b.refundAmount;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID orderId, buyerId, farmerId;
        private String reason, status;
        private Double refundAmount;

        public Builder orderId(UUID v)      { this.orderId = v; return this; }
        public Builder buyerId(UUID v)      { this.buyerId = v; return this; }
        public Builder farmerId(UUID v)     { this.farmerId = v; return this; }
        public Builder reason(String v)     { this.reason = v; return this; }
        public Builder status(String v)     { this.status = v; return this; }
        public Builder refundAmount(Double v){ this.refundAmount = v; return this; }
        public ReturnRequest build()        { return new ReturnRequest(this); }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getBuyerId() { return buyerId; }
    public UUID getFarmerId() { return farmerId; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public Double getRefundAmount() { return refundAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public void setBuyerId(UUID buyerId) { this.buyerId = buyerId; }
    public void setFarmerId(UUID farmerId) { this.farmerId = farmerId; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(String status) { this.status = status; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }
}
