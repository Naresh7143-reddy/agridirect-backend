package com.agridirect.buyer;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    private String label;

    private String line1;

    private String line2;

    private String city;

    private String state;

    private String pincode;

    private Double lat;

    private Double lng;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Address() {}

    public UUID getId()           { return id; }
    public UUID getBuyerId()      { return buyerId; }
    public String getLabel()      { return label; }
    public String getLine1()      { return line1; }
    public String getLine2()      { return line2; }
    public String getCity()       { return city; }
    public String getState()      { return state; }
    public String getPincode()    { return pincode; }
    public Double getLat()        { return lat; }
    public Double getLng()        { return lng; }
    public boolean isDefault()    { return isDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(UUID v)           { this.id = v; }
    public void setBuyerId(UUID v)      { this.buyerId = v; }
    public void setLabel(String v)      { this.label = v; }
    public void setLine1(String v)      { this.line1 = v; }
    public void setLine2(String v)      { this.line2 = v; }
    public void setCity(String v)       { this.city = v; }
    public void setState(String v)      { this.state = v; }
    public void setPincode(String v)    { this.pincode = v; }
    public void setLat(Double v)        { this.lat = v; }
    public void setLng(Double v)        { this.lng = v; }
    public void setDefault(boolean v)   { this.isDefault = v; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
