package com.agridirect.support;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // ID of the buyer or farmer

    @Column(name = "is_from_user", nullable = false)
    private boolean isFromUser = true;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ChatMessage() {}

    public ChatMessage(UUID userId, boolean isFromUser, String content) {
        this.userId = userId;
        this.isFromUser = isFromUser;
        this.content = content;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public boolean isFromUser() { return isFromUser; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
