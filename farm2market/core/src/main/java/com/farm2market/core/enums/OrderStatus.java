package com.farm2market.core.enums;

import java.util.Set;

public enum OrderStatus {
    PENDING("Pending", "#FFC107"),
    ACCEPTED("Accepted", "#2196F3"),
    PREPARING("Preparing", "#9C27B0"),
    READY_FOR_PICKUP("Ready for Pickup", "#00BCD4"),
    OUT_FOR_DELIVERY("Out for Delivery", "#FF9800"),
    COMPLETED("Completed", "#4CAF50"),
    REJECTED("Rejected", "#F44336"),
    CANCELLED("Cancelled", "#9E9E9E");

    private final String displayName;
    private final String colorHex;

    OrderStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public Set<OrderStatus> getAllowedNextStatuses() {
        return switch (this) {
            case PENDING -> Set.of(ACCEPTED, REJECTED, CANCELLED);
            case ACCEPTED -> Set.of(PREPARING, CANCELLED);
            case PREPARING -> Set.of(READY_FOR_PICKUP, OUT_FOR_DELIVERY);
            case READY_FOR_PICKUP -> Set.of(COMPLETED);
            case OUT_FOR_DELIVERY -> Set.of(COMPLETED);
            case COMPLETED, REJECTED, CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(OrderStatus next) {
        return getAllowedNextStatuses().contains(next);
    }

    public boolean isFinal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }

    public boolean isCancellable() {
        return this == PENDING || this == ACCEPTED;
    }

    public boolean isActive() {
        return !isFinal();
    }
}
