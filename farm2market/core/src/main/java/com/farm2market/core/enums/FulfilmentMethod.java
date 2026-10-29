package com.farm2market.core.enums;

public enum FulfilmentMethod {
    PICKUP("Pickup", "Collect your order from the farmer's location"),
    DELIVERY("Delivery", "Get your order delivered to your address");

    private final String displayName;
    private final String description;

    FulfilmentMethod(String displayName, String description) {
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
