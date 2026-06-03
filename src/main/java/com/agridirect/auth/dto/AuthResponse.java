package com.agridirect.auth.dto;

public class AuthResponse {

    private String token;
    private String role;
    private String name;
    private String userId;
    private String phone;

    public AuthResponse() {}

    public AuthResponse(String token, String role, String name, String userId, String phone) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.userId = userId;
        this.phone = phone;
    }

    public String getToken()   { return token; }
    public String getRole()    { return role; }
    public String getName()    { return name; }
    public String getUserId()  { return userId; }
    public String getPhone()   { return phone; }

    public void setToken(String v)  { this.token = v; }
    public void setRole(String v)   { this.role = v; }
    public void setName(String v)   { this.name = v; }
    public void setUserId(String v) { this.userId = v; }
    public void setPhone(String v)  { this.phone = v; }
}
