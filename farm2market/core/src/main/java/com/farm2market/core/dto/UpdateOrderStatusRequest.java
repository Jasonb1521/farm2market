package com.farm2market.core.dto;

import com.farm2market.core.enums.OrderStatus;

public class UpdateOrderStatusRequest {
    private String orderId;
    private OrderStatus newStatus;
    private String reason;

    public UpdateOrderStatusRequest() {}

    public UpdateOrderStatusRequest(String orderId, OrderStatus newStatus) {
        this.orderId = orderId;
        this.newStatus = newStatus;
    }

    public UpdateOrderStatusRequest(String orderId, OrderStatus newStatus, String reason) {
        this.orderId = orderId;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
