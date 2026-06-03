package com.agridirect.product.dto;

import java.util.List;
import java.util.UUID;

public class ProductRequest {

    private String name;
    private String description;
    private String unit;
    private Double price;
    private Double stockQuantity;
    private UUID categoryId;
    private List<String> imageUrls;

    public ProductRequest() {}

    public String getName()          { return name; }
    public String getDescription()   { return description; }
    public String getUnit()          { return unit; }
    public Double getPrice()         { return price; }
    public Double getStockQuantity() { return stockQuantity; }
    public UUID getCategoryId()      { return categoryId; }
    public List<String> getImageUrls(){ return imageUrls; }

    public void setName(String v)          { this.name = v; }
    public void setDescription(String v)   { this.description = v; }
    public void setUnit(String v)          { this.unit = v; }
    public void setPrice(Double v)         { this.price = v; }
    public void setStockQuantity(Double v) { this.stockQuantity = v; }
    public void setCategoryId(UUID v)      { this.categoryId = v; }
    public void setImageUrls(List<String> v){ this.imageUrls = v; }
}
