package com.agridirect.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order response tailored for delivery partners.
 * Uses lowercase status values matching the mobile DeliveryStatus type.
 */
public class DeliveryOrderResponse {

    private UUID id;
    private String orderId;
    private String orderNumber;
    private String status;          // lowercase: assigned, picked_up, in_transit, delivered
    private Double totalAmount;
    private Double deliveryFee;
    private Double distance;

    // Buyer
    private String buyerName;
    private String buyerPhone;
    private String dropAddress;
    private Double dropLat;
    private Double dropLng;

    // Farmer
    private String farmerName;
    private String farmerPhone;
    private String pickupAddress;
    private Double pickupLat;
    private Double pickupLng;

    // Items summary
    private int itemCount;
    private List<ItemSummary> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime assignedAt;

    public static class ItemSummary {
        private String productName;
        private Double quantity;
        private String unit;
        private Double price;

        public String getProductName() { return productName; }
        public void setProductName(String v) { this.productName = v; }
        public Double getQuantity() { return quantity; }
        public void setQuantity(Double v) { this.quantity = v; }
        public String getUnit() { return unit; }
        public void setUnit(String v) { this.unit = v; }
        public Double getPrice() { return price; }
        public void setPrice(Double v) { this.price = v; }
    }

    /** Map backend UPPERCASE status to mobile lowercase status. */
    public static String mapStatus(String backendStatus) {
        if (backendStatus == null) return "assigned";
        return switch (backendStatus.toUpperCase()) {
            case "ASSIGNED"    -> "assigned";
            case "PICKED_UP"   -> "picked_up";
            case "IN_TRANSIT",
                 "ON_THE_WAY"  -> "in_transit";
            case "DELIVERED"   -> "delivered";
            case "FAILED"      -> "failed";
            default            -> "assigned";
        };
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String v) { this.orderId = v; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String v) { this.orderNumber = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double v) { this.totalAmount = v; }
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double v) { this.deliveryFee = v; }
    public Double getDistance() { return distance; }
    public void setDistance(Double v) { this.distance = v; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String v) { this.buyerName = v; }
    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String v) { this.buyerPhone = v; }
    public String getDropAddress() { return dropAddress; }
    public void setDropAddress(String v) { this.dropAddress = v; }
    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double v) { this.dropLat = v; }
    public Double getDropLng() { return dropLng; }
    public void setDropLng(Double v) { this.dropLng = v; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String v) { this.farmerName = v; }
    public String getFarmerPhone() { return farmerPhone; }
    public void setFarmerPhone(String v) { this.farmerPhone = v; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String v) { this.pickupAddress = v; }
    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double v) { this.pickupLat = v; }
    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double v) { this.pickupLng = v; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int v) { this.itemCount = v; }
    public List<ItemSummary> getItems() { return items; }
    public void setItems(List<ItemSummary> v) { this.items = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime v) { this.assignedAt = v; }
}
