package com.agridirect.order.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SubscriptionRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Farmer ID is required")
    private UUID farmerId;

    @NotNull(message = "Frequency is required")
    private String frequency;

    private Double quantity;
    private String deliveryAddress;

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getFarmerId() { return farmerId; }
    public void setFarmerId(UUID farmerId) { this.farmerId = farmerId; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
