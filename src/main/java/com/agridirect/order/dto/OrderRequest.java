package com.agridirect.order.dto;

import java.util.List;

public class OrderRequest {

    private List<OrderItemRequest> items;
    private String deliveryAddress;
    private String notes;

    public OrderRequest() {}

    public List<OrderItemRequest> getItems()    { return items; }
    public String getDeliveryAddress()          { return deliveryAddress; }
    public String getNotes()                    { return notes; }
    public void setItems(List<OrderItemRequest> v) { this.items = v; }
    public void setDeliveryAddress(String v)    { this.deliveryAddress = v; }
    public void setNotes(String v)              { this.notes = v; }
}
