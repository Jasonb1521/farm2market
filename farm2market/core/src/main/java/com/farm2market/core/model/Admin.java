package com.farm2market.core.model;

import com.farm2market.core.enums.Role;

public class Admin extends User {
    private String department;
    private boolean superAdmin;

    public Admin() {
        super();
        setRole(Role.ADMIN);
        this.superAdmin = false;
    }

    public Admin(String userId, String email, String name) {
        super(userId, email, name, Role.ADMIN);
        this.superAdmin = false;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    @Override
    public boolean canAccessFeature(String feature) {
        return switch (feature) {
            case "browse", "profile" -> true;
            case "admin", "moderation", "reports", "users", "listings", "summary" -> true;
            case "cart", "checkout", "sell" -> false;
            default -> superAdmin;
        };
    }

    public boolean canModerateUser(User user) {
        if (user instanceof Admin otherAdmin) {
            return superAdmin && !otherAdmin.isSuperAdmin();
        }
        return true;
    }
}
