package com.farm2market.core.exception;

import com.farm2market.core.enums.OrderStatus;

public class InvalidStatusTransitionException extends Farm2MarketException {
    private final OrderStatus currentStatus;
    private final OrderStatus requestedStatus;

    public InvalidStatusTransitionException(OrderStatus currentStatus, OrderStatus requestedStatus) {
        super(String.format("Cannot transition order from %s to %s",
                currentStatus.getDisplayName(), requestedStatus.getDisplayName()),
                "INVALID_STATUS_TRANSITION");
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public InvalidStatusTransitionException(String orderId, OrderStatus currentStatus, OrderStatus requestedStatus) {
        super(String.format("Order %s cannot transition from %s to %s",
                orderId, currentStatus.getDisplayName(), requestedStatus.getDisplayName()),
                "INVALID_STATUS_TRANSITION");
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getRequestedStatus() {
        return requestedStatus;
    }
}
