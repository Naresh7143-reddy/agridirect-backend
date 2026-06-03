package com.agridirect.order;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "farmer_id")
    private UUID farmerId;

    @Column(name = "product_name")
    private String productName;

    private Double quantity;

    private String unit;

    @Column(name = "price_at_order")
    private Double priceAtOrder;

    public OrderItem() {}

    private OrderItem(Builder b) {
        this.orderId = b.orderId;
        this.productId = b.productId;
        this.farmerId = b.farmerId;
        this.productName = b.productName;
        this.quantity = b.quantity;
        this.unit = b.unit;
        this.priceAtOrder = b.priceAtOrder;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID orderId, productId, farmerId;
        private String productName, unit;
        private Double quantity, priceAtOrder;

        public Builder orderId(UUID v)       { this.orderId = v; return this; }
        public Builder productId(UUID v)     { this.productId = v; return this; }
        public Builder farmerId(UUID v)      { this.farmerId = v; return this; }
        public Builder productName(String v) { this.productName = v; return this; }
        public Builder quantity(Double v)    { this.quantity = v; return this; }
        public Builder unit(String v)        { this.unit = v; return this; }
        public Builder priceAtOrder(Double v){ this.priceAtOrder = v; return this; }
        public OrderItem build()             { return new OrderItem(this); }
    }

    public UUID getId()            { return id; }
    public UUID getOrderId()       { return orderId; }
    public UUID getProductId()     { return productId; }
    public UUID getFarmerId()      { return farmerId; }
    public String getProductName() { return productName; }
    public Double getQuantity()    { return quantity; }
    public String getUnit()        { return unit; }
    public Double getPriceAtOrder(){ return priceAtOrder; }

    public void setId(UUID v)             { this.id = v; }
    public void setOrderId(UUID v)        { this.orderId = v; }
    public void setProductId(UUID v)      { this.productId = v; }
    public void setFarmerId(UUID v)       { this.farmerId = v; }
    public void setProductName(String v)  { this.productName = v; }
    public void setQuantity(Double v)     { this.quantity = v; }
    public void setUnit(String v)         { this.unit = v; }
    public void setPriceAtOrder(Double v) { this.priceAtOrder = v; }
}
