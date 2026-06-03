package com.agridirect.product;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    private String unit = "kg";

    @Column(name = "stock_quantity")
    private Double stockQuantity;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @Column(name = "is_available")
    private boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product() {}

    private Product(Builder b) {
        this.farmerId = b.farmerId;
        this.categoryId = b.categoryId;
        this.name = b.name;
        this.description = b.description;
        this.price = b.price;
        this.unit = b.unit != null ? b.unit : "kg";
        this.stockQuantity = b.stockQuantity;
        this.imageUrls = b.imageUrls;
        this.isAvailable = b.isAvailable;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID farmerId, categoryId;
        private String name, description, unit;
        private Double price, stockQuantity;
        private List<String> imageUrls;
        private boolean isAvailable = true;

        public Builder farmerId(UUID v)        { this.farmerId = v; return this; }
        public Builder categoryId(UUID v)      { this.categoryId = v; return this; }
        public Builder name(String v)          { this.name = v; return this; }
        public Builder description(String v)   { this.description = v; return this; }
        public Builder price(Double v)         { this.price = v; return this; }
        public Builder unit(String v)          { this.unit = v; return this; }
        public Builder stockQuantity(Double v) { this.stockQuantity = v; return this; }
        public Builder imageUrls(List<String> v){ this.imageUrls = v; return this; }
        public Builder isAvailable(boolean v)  { this.isAvailable = v; return this; }
        public Product build()                 { return new Product(this); }
    }

    public UUID getId()              { return id; }
    public UUID getFarmerId()        { return farmerId; }
    public UUID getCategoryId()      { return categoryId; }
    public String getName()          { return name; }
    public String getDescription()   { return description; }
    public Double getPrice()         { return price; }
    public String getUnit()          { return unit; }
    public Double getStockQuantity() { return stockQuantity; }
    public List<String> getImageUrls(){ return imageUrls; }
    public boolean isAvailable()     { return isAvailable; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }

    public void setId(UUID v)              { this.id = v; }
    public void setFarmerId(UUID v)        { this.farmerId = v; }
    public void setCategoryId(UUID v)      { this.categoryId = v; }
    public void setName(String v)          { this.name = v; }
    public void setDescription(String v)   { this.description = v; }
    public void setPrice(Double v)         { this.price = v; }
    public void setUnit(String v)          { this.unit = v; }
    public void setStockQuantity(Double v) { this.stockQuantity = v; }
    public void setImageUrls(List<String> v){ this.imageUrls = v; }
    public void setAvailable(boolean v)    { this.isAvailable = v; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v){ this.updatedAt = v; }
}
