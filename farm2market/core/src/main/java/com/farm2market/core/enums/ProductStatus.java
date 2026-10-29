package com.farm2market.core.enums;

public enum ProductStatus {
    ACTIVE("Active", "Product is available for purchase"),
    INACTIVE("Inactive", "Product is temporarily unavailable"),
    HIDDEN("Hidden", "Product is hidden by admin"),
    DELETED("Deleted", "Product has been deleted");

    private final String displayName;
    private final String description;

    ProductStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisible() {
        return this == ACTIVE;
    }

    public boolean canBeOrdered() {
        return this == ACTIVE;
    }
}
