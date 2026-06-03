package com.agridirect.order;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_agent_id")
    private UUID deliveryAgentId;

    @Column(name = "payment_status")
    private String paymentStatus = "PENDING";

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Order() {}

    private Order(Builder b) {
        this.buyerId = b.buyerId;
        this.status = b.status != null ? b.status : "PENDING";
        this.totalAmount = b.totalAmount;
        this.deliveryAddress = b.deliveryAddress;
        this.deliveryAgentId = b.deliveryAgentId;
        this.paymentStatus = b.paymentStatus != null ? b.paymentStatus : "PENDING";
        this.razorpayOrderId = b.razorpayOrderId;
        this.razorpayPaymentId = b.razorpayPaymentId;
        this.notes = b.notes;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID buyerId, deliveryAgentId;
        private String status, deliveryAddress, paymentStatus, razorpayOrderId, razorpayPaymentId, notes;
        private Double totalAmount;

        public Builder buyerId(UUID v)              { this.buyerId = v; return this; }
        public Builder status(String v)             { this.status = v; return this; }
        public Builder totalAmount(Double v)        { this.totalAmount = v; return this; }
        public Builder deliveryAddress(String v)    { this.deliveryAddress = v; return this; }
        public Builder deliveryAgentId(UUID v)      { this.deliveryAgentId = v; return this; }
        public Builder paymentStatus(String v)      { this.paymentStatus = v; return this; }
        public Builder razorpayOrderId(String v)    { this.razorpayOrderId = v; return this; }
        public Builder razorpayPaymentId(String v)  { this.razorpayPaymentId = v; return this; }
        public Builder notes(String v)              { this.notes = v; return this; }
        public Order build()                        { return new Order(this); }
    }

    public UUID getId()                  { return id; }
    public UUID getBuyerId()             { return buyerId; }
    public String getStatus()            { return status; }
    public Double getTotalAmount()       { return totalAmount; }
    public String getDeliveryAddress()   { return deliveryAddress; }
    public UUID getDeliveryAgentId()     { return deliveryAgentId; }
    public String getPaymentStatus()     { return paymentStatus; }
    public String getRazorpayOrderId()   { return razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public String getNotes()             { return notes; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    public void setId(UUID v)                  { this.id = v; }
    public void setBuyerId(UUID v)             { this.buyerId = v; }
    public void setStatus(String v)            { this.status = v; }
    public void setTotalAmount(Double v)       { this.totalAmount = v; }
    public void setDeliveryAddress(String v)   { this.deliveryAddress = v; }
    public void setDeliveryAgentId(UUID v)     { this.deliveryAgentId = v; }
    public void setPaymentStatus(String v)     { this.paymentStatus = v; }
    public void setRazorpayOrderId(String v)   { this.razorpayOrderId = v; }
    public void setRazorpayPaymentId(String v) { this.razorpayPaymentId = v; }
    public void setNotes(String v)             { this.notes = v; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v)  { this.updatedAt = v; }
}
