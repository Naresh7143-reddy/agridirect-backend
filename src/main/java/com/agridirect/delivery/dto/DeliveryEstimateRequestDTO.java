package com.agridirect.delivery.dto;

import jakarta.validation.constraints.*;

public class DeliveryEstimateRequestDTO {
    
    @NotNull(message = "Source latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double sourceLatitude;
    
    @NotNull(message = "Source longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double sourceLongitude;
    
    @NotNull(message = "Destination latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double destLatitude;
    
    @NotNull(message = "Destination longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double destLongitude;
    
    @NotBlank(message = "Source address is required")
    private String sourceAddress;
    
    @NotBlank(message = "Destination address is required")
    private String destAddress;
    
    @Min(value = 1, message = "Order amount must be at least 1")
    private Double orderAmount; // for priority calculations

    // Constructors
    public DeliveryEstimateRequestDTO() {
    }

    public DeliveryEstimateRequestDTO(Double sourceLatitude, Double sourceLongitude, 
                                     Double destLatitude, Double destLongitude,
                                     String sourceAddress, String destAddress, Double orderAmount) {
        this.sourceLatitude = sourceLatitude;
        this.sourceLongitude = sourceLongitude;
        this.destLatitude = destLatitude;
        this.destLongitude = destLongitude;
        this.sourceAddress = sourceAddress;
        this.destAddress = destAddress;
        this.orderAmount = orderAmount;
    }

    // Getters and Setters
    public Double getSourceLatitude() {
        return sourceLatitude;
    }

    public void setSourceLatitude(Double sourceLatitude) {
        this.sourceLatitude = sourceLatitude;
    }

    public Double getSourceLongitude() {
        return sourceLongitude;
    }

    public void setSourceLongitude(Double sourceLongitude) {
        this.sourceLongitude = sourceLongitude;
    }

    public Double getDestLatitude() {
        return destLatitude;
    }

    public void setDestLatitude(Double destLatitude) {
        this.destLatitude = destLatitude;
    }

    public Double getDestLongitude() {
        return destLongitude;
    }

    public void setDestLongitude(Double destLongitude) {
        this.destLongitude = destLongitude;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public Double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }
}
