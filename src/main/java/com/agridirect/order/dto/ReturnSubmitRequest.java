package com.agridirect.order.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public class ReturnSubmitRequest {
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;

    private String reason;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
