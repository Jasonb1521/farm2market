package com.farm2market.core.dto;

import com.farm2market.core.enums.Role;

public class LoginResponse {
    private String userId;
    private String token;
    private String name;
    private String email;
    private Role role;
    private String profileImageUrl;
    private long expiresAt;

    public LoginResponse() {}

    public LoginResponse(String userId, String token, String name, String email, Role role) {
        this.userId = userId;
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
}
