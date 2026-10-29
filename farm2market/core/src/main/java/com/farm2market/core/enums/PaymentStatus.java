package com.farm2market.core.enums;

public enum PaymentStatus {
    PENDING("Pending", "Payment not yet received"),
    PAID("Paid", "Payment received"),
    REFUNDED("Refunded", "Payment has been refunded"),
    FAILED("Failed", "Payment failed");

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isComplete() {
        return this == PAID;
    }
}
