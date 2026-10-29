package com.farm2market.core.model;

import com.farm2market.core.enums.Role;
import com.farm2market.core.enums.UserStatus;

public abstract class User implements Entity<String> {
    private String userId;
    private String email;
    private String mobile;
    private String name;
    private Role role;
    private String profileImageUrl;
    private UserStatus status;
    private long createdAt;
    private long updatedAt;

    protected User() {
        this.status = UserStatus.ACTIVE;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    protected User(String userId, String email, String name, Role role) {
        this();
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    @Override
    public String getId() {
        return userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = System.currentTimeMillis();
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean canLogin() {
        return status.canLogin();
    }

    public abstract boolean canAccessFeature(String feature);
}
