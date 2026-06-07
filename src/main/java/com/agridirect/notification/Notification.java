package com.agridirect.notification;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String role;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sent_count")
    private Integer sentCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public UUID getId()         { return id; }
    public String getTitle()    { return title; }
    public String getBody()     { return body; }
    public String getRole()     { return role; }
    public String getImageUrl() { return imageUrl; }
    public Integer getSentCount(){ return sentCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(UUID v)          { this.id = v; }
    public void setTitle(String v)     { this.title = v; }
    public void setBody(String v)      { this.body = v; }
    public void setRole(String v)      { this.role = v; }
    public void setImageUrl(String v)  { this.imageUrl = v; }
    public void setSentCount(Integer v){ this.sentCount = v; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
