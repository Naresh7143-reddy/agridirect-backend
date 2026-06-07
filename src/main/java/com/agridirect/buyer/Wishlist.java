package com.agridirect.buyer;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wishlists")
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    public Wishlist() {}

    public UUID getId()         { return id; }
    public UUID getBuyerId()    { return buyerId; }
    public UUID getProductId()  { return productId; }
    public LocalDateTime getAddedAt() { return addedAt; }

    public void setId(UUID v)        { this.id = v; }
    public void setBuyerId(UUID v)   { this.buyerId = v; }
    public void setProductId(UUID v) { this.productId = v; }
    public void setAddedAt(LocalDateTime v) { this.addedAt = v; }
}
