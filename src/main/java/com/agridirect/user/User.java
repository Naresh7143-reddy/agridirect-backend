package com.agridirect.user;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String phone;

    private String name;

    private String email;

    private String role;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    private User(Builder b) {
        this.phone = b.phone;
        this.name = b.name;
        this.email = b.email;
        this.role = b.role;
        this.fcmToken = b.fcmToken;
        this.isActive = b.isActive;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String phone, name, email, role, fcmToken;
        private boolean isActive = true;
        public Builder phone(String v)    { this.phone = v; return this; }
        public Builder name(String v)     { this.name = v; return this; }
        public Builder email(String v)    { this.email = v; return this; }
        public Builder role(String v)     { this.role = v; return this; }
        public Builder fcmToken(String v) { this.fcmToken = v; return this; }
        public Builder isActive(boolean v){ this.isActive = v; return this; }
        public User build() { return new User(this); }
    }

    // Getters
    public UUID getId()           { return id; }
    public String getPhone()      { return phone; }
    public String getName()       { return name; }
    public String getEmail()      { return email; }
    public String getRole()       { return role; }
    public String getFcmToken()   { return fcmToken; }
    public boolean isActive()     { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(UUID id)              { this.id = id; }
    public void setPhone(String phone)      { this.phone = phone; }
    public void setName(String name)        { this.name = name; }
    public void setEmail(String email)      { this.email = email; }
    public void setRole(String role)        { this.role = role; }
    public void setFcmToken(String fcmToken){ this.fcmToken = fcmToken; }
    public void setActive(boolean active)   { this.isActive = active; }
    public void setCreatedAt(LocalDateTime c){ this.createdAt = c; }
}
