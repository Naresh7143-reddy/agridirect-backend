package com.agridirect.product.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProductResponse {

    private UUID id;
    private UUID farmerId;
    private UUID categoryId;
    private String name;
    private String description;
    private Double price;
    private String unit;
    private Double stockQuantity;
    private List<String> imageUrls;
    private boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String farmerName;
    private String farmerLocation;
    private String categoryName;
    private Double averageRating = 0.0;

    public ProductResponse() {}

    private ProductResponse(Builder b) {
        this.id = b.id;
        this.farmerId = b.farmerId;
        this.categoryId = b.categoryId;
        this.name = b.name;
        this.description = b.description;
        this.price = b.price;
        this.unit = b.unit;
        this.stockQuantity = b.stockQuantity;
        this.imageUrls = b.imageUrls;
        this.isAvailable = b.isAvailable;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
        this.farmerName = b.farmerName;
        this.farmerLocation = b.farmerLocation;
        this.categoryName = b.categoryName;
        this.averageRating = b.averageRating;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id, farmerId, categoryId;
        private String name, description, unit, farmerName, farmerLocation, categoryName;
        private Double price, stockQuantity, averageRating = 0.0;
        private List<String> imageUrls;
        private boolean isAvailable;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(UUID v)                { this.id = v; return this; }
        public Builder farmerId(UUID v)          { this.farmerId = v; return this; }
        public Builder categoryId(UUID v)        { this.categoryId = v; return this; }
        public Builder name(String v)            { this.name = v; return this; }
        public Builder description(String v)     { this.description = v; return this; }
        public Builder price(Double v)           { this.price = v; return this; }
        public Builder unit(String v)            { this.unit = v; return this; }
        public Builder stockQuantity(Double v)   { this.stockQuantity = v; return this; }
        public Builder imageUrls(List<String> v) { this.imageUrls = v; return this; }
        public Builder isAvailable(boolean v)    { this.isAvailable = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        public Builder farmerName(String v)      { this.farmerName = v; return this; }
        public Builder farmerLocation(String v)  { this.farmerLocation = v; return this; }
        public Builder categoryName(String v)    { this.categoryName = v; return this; }
        public Builder averageRating(Double v)   { this.averageRating = v; return this; }
        public ProductResponse build()           { return new ProductResponse(this); }
    }

    public UUID getId()               { return id; }
    public UUID getFarmerId()         { return farmerId; }
    public UUID getCategoryId()       { return categoryId; }
    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public Double getPrice()          { return price; }
    public String getUnit()           { return unit; }
    public Double getStockQuantity()  { return stockQuantity; }
    public List<String> getImageUrls(){ return imageUrls; }
    public boolean isAvailable()      { return isAvailable; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public String getFarmerName()     { return farmerName; }
    public String getFarmerLocation() { return farmerLocation; }
    public String getCategoryName()   { return categoryName; }
    public Double getAverageRating()  { return averageRating; }
}
