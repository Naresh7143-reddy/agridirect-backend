package com.agridirect.product;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_product_id", columnList = "product_id"),
    @Index(name = "idx_reviews_farmer_id",  columnList = "farmer_id")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "farmer_id")
    private UUID farmerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Review() {}

    private Review(Builder b) {
        this.productId = b.productId;
        this.buyerId = b.buyerId;
        this.farmerId = b.farmerId;
        this.rating = b.rating;
        this.comment = b.comment;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID productId, buyerId, farmerId;
        private Integer rating;
        private String comment;

        public Builder productId(UUID v) { this.productId = v; return this; }
        public Builder buyerId(UUID v)   { this.buyerId = v; return this; }
        public Builder farmerId(UUID v)  { this.farmerId = v; return this; }
        public Builder rating(Integer v) { this.rating = v; return this; }
        public Builder comment(String v) { this.comment = v; return this; }
        public Review build()            { return new Review(this); }
    }

    public UUID getId()                 { return id; }
    public UUID getProductId()          { return productId; }
    public UUID getBuyerId()            { return buyerId; }
    public UUID getFarmerId()           { return farmerId; }
    public Integer getRating()          { return rating; }
    public String getComment()          { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(UUID v)                 { this.id = v; }
    public void setProductId(UUID v)          { this.productId = v; }
    public void setBuyerId(UUID v)            { this.buyerId = v; }
    public void setFarmerId(UUID v)           { this.farmerId = v; }
    public void setRating(Integer v)          { this.rating = v; }
    public void setComment(String v)          { this.comment = v; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
