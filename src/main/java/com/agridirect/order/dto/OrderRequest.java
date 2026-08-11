package com.agridirect.order.dto;

import java.util.List;

public class OrderRequest {

    private List<OrderItemRequest> items;
    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;
    private String notes;
    private String requiredVehicleType;

    public OrderRequest() {}

    public List<OrderItemRequest> getItems()    { return items; }
    public String getDeliveryAddress()          { return deliveryAddress; }
    public Double getDeliveryLat()              { return deliveryLat; }
    public Double getDeliveryLng()              { return deliveryLng; }
    public String getNotes()                    { return notes; }
    public String getRequiredVehicleType()      { return requiredVehicleType; }
    public void setItems(List<OrderItemRequest> v) { this.items = v; }
    public void setDeliveryAddress(String v)    { this.deliveryAddress = v; }
    public void setDeliveryLat(Double v)        { this.deliveryLat = v; }
    public void setDeliveryLng(Double v)        { this.deliveryLng = v; }
    public void setNotes(String v)              { this.notes = v; }
    public void setRequiredVehicleType(String v) { this.requiredVehicleType = v; }
}
