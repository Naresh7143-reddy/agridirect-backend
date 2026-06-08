package com.agridirect.product.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;
import java.util.UUID;

public class ProductRequest {

    private String name;
    private String description;
    private String unit;
    private Double price;
    // Mobile app sends "stock" (see CreateProductRequest in src/types/product.ts);
    // accept both names so the field isn't silently dropped as null.
    @JsonAlias({"stock"})
    private Double stockQuantity;
    private UUID categoryId;
    @JsonAlias({"images"})
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
