package com.agridirect.payment;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    private Double amount;

    private String currency = "INR";

    private String status = "CREATED";

    @Column(name = "refund_id")
    private String refundId;

    @Column(name = "refund_status")
    private String refundStatus;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Payment() {}

    private Payment(Builder b) {
        this.orderId = b.orderId;
        this.razorpayOrderId = b.razorpayOrderId;
        this.razorpayPaymentId = b.razorpayPaymentId;
        this.amount = b.amount;
        this.currency = b.currency != null ? b.currency : "INR";
        this.status = b.status != null ? b.status : "CREATED";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID orderId;
        private String razorpayOrderId, razorpayPaymentId, currency, status;
        private Double amount;

        public Builder orderId(UUID v)              { this.orderId = v; return this; }
        public Builder razorpayOrderId(String v)    { this.razorpayOrderId = v; return this; }
        public Builder razorpayPaymentId(String v)  { this.razorpayPaymentId = v; return this; }
        public Builder amount(Double v)             { this.amount = v; return this; }
        public Builder currency(String v)           { this.currency = v; return this; }
        public Builder status(String v)             { this.status = v; return this; }
        public Payment build()                      { return new Payment(this); }
    }

    public UUID getId()                  { return id; }
    public UUID getOrderId()             { return orderId; }
    public String getRazorpayOrderId()   { return razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public Double getAmount()            { return amount; }
    public String getCurrency()          { return currency; }
    public String getStatus()            { return status; }
    public String getRefundId()          { return refundId; }
    public String getRefundStatus()      { return refundStatus; }
    public Double getRefundAmount()      { return refundAmount; }
    public String getRefundReason()      { return refundReason; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setId(UUID v)                  { this.id = v; }
    public void setOrderId(UUID v)             { this.orderId = v; }
    public void setRazorpayOrderId(String v)   { this.razorpayOrderId = v; }
    public void setRazorpayPaymentId(String v) { this.razorpayPaymentId = v; }
    public void setAmount(Double v)            { this.amount = v; }
    public void setCurrency(String v)          { this.currency = v; }
    public void setStatus(String v)            { this.status = v; }
    public void setRefundId(String v)          { this.refundId = v; }
    public void setRefundStatus(String v)      { this.refundStatus = v; }
    public void setRefundAmount(Double v)      { this.refundAmount = v; }
    public void setRefundReason(String v)      { this.refundReason = v; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
}
