package com.agridirect.auth.dto;

import java.time.LocalDateTime;

/**
 * Auth response — matches the AgriDirect mobile app's expected shape:
 *   { user: {...}, tokens: { accessToken, refreshToken, expiresIn } }
 */
public class AuthResponse {

    private UserDto user;
    private TokensDto tokens;

    public AuthResponse() {}

    public AuthResponse(UserDto user, TokensDto tokens) {
        this.user = user;
        this.tokens = tokens;
    }

    public UserDto getUser()      { return user; }
    public TokensDto getTokens()  { return tokens; }
    public void setUser(UserDto v)     { this.user = v; }
    public void setTokens(TokensDto v) { this.tokens = v; }

    // ─── Nested: user ─────────────────────────────────────────────────────────

    public static class UserDto {
        private String id;
        private String name;
        private String phone;
        private String email;
        private String role;
        private boolean isVerified;
        private boolean isBlocked;
        private String fcmToken;
        private String avatarUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public UserDto() {}

        public UserDto(String id, String name, String phone, String email, String role,
                       boolean isVerified, boolean isBlocked, String fcmToken,
                       String avatarUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.role = role;
            this.isVerified = isVerified;
            this.isBlocked = isBlocked;
            this.fcmToken = fcmToken;
            this.avatarUrl = avatarUrl;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId()             { return id; }
        public String getName()           { return name; }
        public String getPhone()          { return phone; }
        public String getEmail()          { return email; }
        public String getRole()           { return role; }
        public boolean isVerified()       { return isVerified; }
        public boolean isBlocked()        { return isBlocked; }
        public String getFcmToken()       { return fcmToken; }
        public String getAvatarUrl()      { return avatarUrl; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    // ─── Nested: tokens ───────────────────────────────────────────────────────

    public static class TokensDto {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;

        public TokensDto() {}

        public TokensDto(String accessToken, String refreshToken, long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken()  { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public long getExpiresIn()      { return expiresIn; }
    }
}
