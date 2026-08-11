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

    @Column(name = "delivery_lat")
    private Double deliveryLat;

    @Column(name = "delivery_lng")
    private Double deliveryLng;

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

    @Column(name = "delivery_otp", length = 6)
    private String deliveryOtp;

    @Column(name = "return_status")
    private String returnStatus;

    @Column(name = "required_vehicle_type")
    private String requiredVehicleType;

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
        this.deliveryLat = b.deliveryLat;
        this.deliveryLng = b.deliveryLng;
        this.deliveryAgentId = b.deliveryAgentId;
        this.paymentStatus = b.paymentStatus != null ? b.paymentStatus : "PENDING";
        this.razorpayOrderId = b.razorpayOrderId;
        this.razorpayPaymentId = b.razorpayPaymentId;
        this.notes = b.notes;
        this.deliveryOtp = b.deliveryOtp;
        this.returnStatus = b.returnStatus;
        this.requiredVehicleType = b.requiredVehicleType;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID buyerId, deliveryAgentId;
        private String status, deliveryAddress, paymentStatus, razorpayOrderId, razorpayPaymentId, notes, deliveryOtp, returnStatus, requiredVehicleType;
        private Double totalAmount, deliveryLat, deliveryLng;

        public Builder buyerId(UUID v)              { this.buyerId = v; return this; }
        public Builder status(String v)             { this.status = v; return this; }
        public Builder totalAmount(Double v)        { this.totalAmount = v; return this; }
        public Builder deliveryAddress(String v)    { this.deliveryAddress = v; return this; }
        public Builder deliveryLat(Double v)        { this.deliveryLat = v; return this; }
        public Builder deliveryLng(Double v)        { this.deliveryLng = v; return this; }
        public Builder deliveryAgentId(UUID v)      { this.deliveryAgentId = v; return this; }
        public Builder paymentStatus(String v)      { this.paymentStatus = v; return this; }
        public Builder razorpayOrderId(String v)    { this.razorpayOrderId = v; return this; }
        public Builder razorpayPaymentId(String v)  { this.razorpayPaymentId = v; return this; }
        public Builder notes(String v)              { this.notes = v; return this; }
        public Builder deliveryOtp(String v)        { this.deliveryOtp = v; return this; }
        public Builder returnStatus(String v)       { this.returnStatus = v; return this; }
        public Builder requiredVehicleType(String v) { this.requiredVehicleType = v; return this; }
        public Order build()                        { return new Order(this); }
    }

    public UUID getId()                  { return id; }
    public UUID getBuyerId()             { return buyerId; }
    public String getStatus()            { return status; }
    public Double getTotalAmount()       { return totalAmount; }
    public String getDeliveryAddress()   { return deliveryAddress; }
    public Double getDeliveryLat()       { return deliveryLat; }
    public Double getDeliveryLng()       { return deliveryLng; }
    public UUID getDeliveryAgentId()     { return deliveryAgentId; }
    public String getPaymentStatus()     { return paymentStatus; }
    public String getRazorpayOrderId()   { return razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public String getNotes()             { return notes; }
    public String getDeliveryOtp()       { return deliveryOtp; }
    public String getReturnStatus()      { return returnStatus; }
    public String getRequiredVehicleType() { return requiredVehicleType; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    public void setId(UUID v)                  { this.id = v; }
    public void setBuyerId(UUID v)             { this.buyerId = v; }
    public void setStatus(String v)            { this.status = v; }
    public void setTotalAmount(Double v)       { this.totalAmount = v; }
    public void setDeliveryAddress(String v)   { this.deliveryAddress = v; }
    public void setDeliveryLat(Double v)       { this.deliveryLat = v; }
    public void setDeliveryLng(Double v)       { this.deliveryLng = v; }
    public void setDeliveryAgentId(UUID v)     { this.deliveryAgentId = v; }
    public void setPaymentStatus(String v)     { this.paymentStatus = v; }
    public void setRazorpayOrderId(String v)   { this.razorpayOrderId = v; }
    public void setRazorpayPaymentId(String v) { this.razorpayPaymentId = v; }
    public void setNotes(String v)             { this.notes = v; }
    public void setDeliveryOtp(String v)       { this.deliveryOtp = v; }
    public void setReturnStatus(String v)      { this.returnStatus = v; }
    public void setRequiredVehicleType(String v) { this.requiredVehicleType = v; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v)  { this.updatedAt = v; }
}
