package com.agridirect.order.dto;

import java.util.UUID;

public class OrderItemRequest {

    private UUID productId;
    private Double quantity;

    public OrderItemRequest() {}

    public UUID getProductId()    { return productId; }
    public Double getQuantity()   { return quantity; }
    public void setProductId(UUID v)   { this.productId = v; }
    public void setQuantity(Double v)  { this.quantity = v; }
}
