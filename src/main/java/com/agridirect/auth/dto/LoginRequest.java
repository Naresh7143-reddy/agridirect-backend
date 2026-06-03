package com.agridirect.auth.dto;

public class LoginRequest {

    private String idToken;
    private String fcmToken;

    public LoginRequest() {}

    public String getIdToken()   { return idToken; }
    public String getFcmToken()  { return fcmToken; }

    public void setIdToken(String v)  { this.idToken = v; }
    public void setFcmToken(String v) { this.fcmToken = v; }
}
