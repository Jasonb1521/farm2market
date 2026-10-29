package com.farm2market.core.enums;

public enum Role {
    GUEST("Guest"),
    CUSTOMER("Customer"),
    FARMER("Farmer"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canBrowse() {
        return true;
    }

    public boolean canPurchase() {
        return this == CUSTOMER;
    }

    public boolean canSell() {
        return this == FARMER;
    }

    public boolean canModerate() {
        return this == ADMIN;
    }
}
