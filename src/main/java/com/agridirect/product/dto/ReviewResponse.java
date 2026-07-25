package com.agridirect.product.dto;

import com.agridirect.product.Review;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewResponse {
    private UUID id;
    private UUID productId;
    private UUID buyerId;
    private UUID farmerId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponse(Review review) {
        this.id = review.getId();
        this.productId = review.getProductId();
        this.buyerId = review.getBuyerId();
        this.farmerId = review.getFarmerId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getBuyerId() { return buyerId; }
    public UUID getFarmerId() { return farmerId; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
