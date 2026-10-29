package com.farm2market.core.enums;

public enum ReportTargetType {
    LISTING("Listing", "Report a product listing"),
    REVIEW("Review", "Report a review"),
    USER("User", "Report a user");

    private final String displayName;
    private final String description;

    ReportTargetType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
