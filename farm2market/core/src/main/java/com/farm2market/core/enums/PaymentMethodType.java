package com.farm2market.core.enums;

public enum PaymentMethodType {
    CASH_ON_DELIVERY("Cash on Delivery", "Pay with cash when your order is delivered"),
    PAY_ON_PICKUP("Pay on Pickup", "Pay with cash when you pick up your order"),
    DEMO("Demo Payment", "Demo payment for testing purposes"),
    ONLINE("Online Payment", "Pay online using UPI or card");

    private final String displayName;
    private final String description;

    PaymentMethodType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresOnlineProcessing() {
        return this == ONLINE;
    }

    public boolean isAvailableForDelivery() {
        return this != PAY_ON_PICKUP;
    }

    public boolean isAvailableForPickup() {
        return this != CASH_ON_DELIVERY;
    }
}
