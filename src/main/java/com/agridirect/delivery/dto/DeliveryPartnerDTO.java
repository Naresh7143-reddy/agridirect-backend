package com.agridirect.delivery.dto;

public class DeliveryPartnerDTO {
    
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String vehicleType;
    private String vehicleRegistration;
    private Double currentLatitude;
    private Double currentLongitude;
    private Boolean isAvailable;
    private Integer currentOrdersCount;
    private Integer maxConcurrentOrders;
    private Integer totalDeliveries;
    private Double avgRating;
    private String verificationStatus;
    private Boolean isActive;
    private Long lastLocationUpdate;
    private Long createdAt;
    private Long updatedAt;

    // Constructors
    public DeliveryPartnerDTO() {
    }

    public DeliveryPartnerDTO(String id, String name, String phone, String vehicleType,
                            Double latitude, Double longitude, Boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.vehicleType = vehicleType;
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }

    public Integer getCurrentOrdersCount() {
        return currentOrdersCount;
    }

    public void setCurrentOrdersCount(Integer currentOrdersCount) {
        this.currentOrdersCount = currentOrdersCount;
    }

    public Integer getMaxConcurrentOrders() {
        return maxConcurrentOrders;
    }

    public void setMaxConcurrentOrders(Integer maxConcurrentOrders) {
        this.maxConcurrentOrders = maxConcurrentOrders;
    }

    public Integer getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Long getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(Long lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
