package com.farm2market.core.enums;

public enum UserStatus {
    ACTIVE("Active", "User account is active"),
    SUSPENDED("Suspended", "User account is suspended"),
    DELETED("Deleted", "User account has been deleted");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
