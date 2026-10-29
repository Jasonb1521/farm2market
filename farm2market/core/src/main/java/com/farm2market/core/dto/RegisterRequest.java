package com.farm2market.core.dto;

import com.farm2market.core.enums.Role;

public class RegisterRequest {
    private String name;
    private String email;
    private String mobile;
    private String password;
    private Role role;

    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String mobile, String password, Role role) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.role = role;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
