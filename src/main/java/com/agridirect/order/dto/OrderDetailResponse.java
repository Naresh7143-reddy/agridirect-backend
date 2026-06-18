package com.agridirect.order.dto;

import com.agridirect.order.OrderItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Rich order detail response that includes contact info for all parties.
 * Returned to buyers (farmer + agent info) and farmers (buyer info).
 */
public class OrderDetailResponse {

    // ── Core order fields ─────────────────────────────────────────────────────
    private UUID id;
    private String orderNumber;
    private String status;
    private String paymentStatus;
    private Double totalAmount;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Items ─────────────────────────────────────────────────────────────────
    private List<OrderItem> items;

    // ── Buyer contact ─────────────────────────────────────────────────────────
    private UUID buyerId;
    private String buyerName;
    private String buyerPhone;

    // ── Farmer contact (primary farmer for single-farmer orders) ──────────────
    private UUID farmerId;
    private String farmerName;
    private String farmerPhone;
    private String farmName;
    private String farmLocation;

    // ── Delivery agent contact ────────────────────────────────────────────────
    private UUID deliveryAgentId;
    private String agentName;
    private String agentPhone;
    private Double agentLat;
    private Double agentLng;
    private String agentVehicleType;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public UUID getBuyerId() { return buyerId; }
    public void setBuyerId(UUID buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public UUID getFarmerId() { return farmerId; }
    public void setFarmerId(UUID farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getFarmerPhone() { return farmerPhone; }
    public void setFarmerPhone(String farmerPhone) { this.farmerPhone = farmerPhone; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public String getFarmLocation() { return farmLocation; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }

    public UUID getDeliveryAgentId() { return deliveryAgentId; }
    public void setDeliveryAgentId(UUID deliveryAgentId) { this.deliveryAgentId = deliveryAgentId; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getAgentPhone() { return agentPhone; }
    public void setAgentPhone(String agentPhone) { this.agentPhone = agentPhone; }

    public Double getAgentLat() { return agentLat; }
    public void setAgentLat(Double agentLat) { this.agentLat = agentLat; }

    public Double getAgentLng() { return agentLng; }
    public void setAgentLng(Double agentLng) { this.agentLng = agentLng; }

    public String getAgentVehicleType() { return agentVehicleType; }
    public void setAgentVehicleType(String agentVehicleType) { this.agentVehicleType = agentVehicleType; }
}
