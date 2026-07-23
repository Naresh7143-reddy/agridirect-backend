package com.agridirect.delivery;

import java.util.UUID;

/**
 * Delivery partner profile DTO for API responses
 */
public class DeliveryPartnerProfile {
    
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String vehicleType;
    private String vehicleRegistration;
    private Boolean available;
    private Double rating;
    private Integer totalDeliveries;
    private Integer currentOrderCount;
    private Integer maxConcurrentOrders;
    private Double currentLat;
    private Double currentLng;

    // Constructors
    public DeliveryPartnerProfile() {
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DeliveryPartnerProfile profile = new DeliveryPartnerProfile();

        public Builder id(String id) {
            profile.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            profile.userId = userId.toString();
            return this;
        }

        public Builder userId(String userId) {
            profile.userId = userId;
            return this;
        }

        public Builder name(String name) {
            profile.name = name;
            return this;
        }

        public Builder phone(String phone) {
            profile.phone = phone;
            return this;
        }

        public Builder vehicleType(String vehicleType) {
            profile.vehicleType = vehicleType;
            return this;
        }

        public Builder vehicleRegistration(String vehicleRegistration) {
            profile.vehicleRegistration = vehicleRegistration;
            return this;
        }

        public Builder available(Boolean available) {
            profile.available = available;
            return this;
        }

        public Builder rating(Double rating) {
            profile.rating = rating;
            return this;
        }

        public Builder totalDeliveries(Integer totalDeliveries) {
            profile.totalDeliveries = totalDeliveries;
            return this;
        }

        public Builder currentOrderCount(Integer currentOrderCount) {
            profile.currentOrderCount = currentOrderCount;
            return this;
        }

        public Builder maxConcurrentOrders(Integer maxConcurrentOrders) {
            profile.maxConcurrentOrders = maxConcurrentOrders;
            return this;
        }

        public Builder currentLat(Double currentLat) {
            profile.currentLat = currentLat;
            return this;
        }

        public Builder currentLng(Double currentLng) {
            profile.currentLng = currentLng;
            return this;
        }

        public DeliveryPartnerProfile build() {
            return profile;
        }
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public Integer getCurrentOrderCount() {
        return currentOrderCount;
    }

    public void setCurrentOrderCount(Integer currentOrderCount) {
        this.currentOrderCount = currentOrderCount;
    }

    public Integer getMaxConcurrentOrders() {
        return maxConcurrentOrders;
    }

    public void setMaxConcurrentOrders(Integer maxConcurrentOrders) {
        this.maxConcurrentOrders = maxConcurrentOrders;
    }

    public Double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLat = currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLng = currentLng;
    }
}
