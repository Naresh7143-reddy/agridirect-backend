package com.agridirect.notification;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_notifications", indexes = {
    @Index(name = "idx_user_notif_user", columnList = "user_id"),
    @Index(name = "idx_user_notif_read", columnList = "is_read")
})
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String type;        // ORDER_UPDATE, PAYMENT, DELIVERY, GENERAL

    @Column(name = "reference_id")
    private String referenceId; // orderId, paymentId, etc.

    @Column(name = "is_read")
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public UserNotification() {}

    public UserNotification(UUID userId, String title, String body, String type, String referenceId) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.type = type;
        this.referenceId = referenceId;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getType() { return type; }
    public String getReferenceId() { return referenceId; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setType(String type) { this.type = type; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public void setRead(boolean read) { this.read = read; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
