package com.farm2market.core.model;

import com.farm2market.core.enums.FulfilmentMethod;
import com.farm2market.core.enums.OrderStatus;
import com.farm2market.core.enums.PaymentMethodType;
import com.farm2market.core.enums.PaymentStatus;
import java.util.ArrayList;
import java.util.List;

public class Order implements Entity<String> {
    private String orderId;
    private String customerId;
    private String farmerId;
    private List<OrderItem> items;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private FulfilmentMethod fulfilmentMethod;
    private String deliveryAddressSnapshot;
    private String contactName;
    private String contactPhone;
    private PaymentMethodType paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private String rejectionReason;
    private String cancellationReason;
    private String customerNote;
    private long createdAt;
    private long updatedAt;
    private Long acceptedAt;
    private Long completedAt;

    // Transient fields for display
    private transient String customerName;
    private transient String farmerName;
    private transient String farmName;
    private transient List<OrderStatusHistory> statusHistory;

    public Order() {
        this.items = new ArrayList<>();
        this.orderStatus = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
        this.deliveryFee = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Order(String orderId, String customerId, String farmerId) {
        this();
        this.orderId = orderId;
        this.customerId = customerId;
        this.farmerId = farmerId;
    }

    @Override
    public String getId() {
        return orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        recalculateTotal();
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
        recalculateTotal();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public FulfilmentMethod getFulfilmentMethod() {
        return fulfilmentMethod;
    }

    public void setFulfilmentMethod(FulfilmentMethod fulfilmentMethod) {
        this.fulfilmentMethod = fulfilmentMethod;
    }

    public String getDeliveryAddressSnapshot() {
        return deliveryAddressSnapshot;
    }

    public void setDeliveryAddressSnapshot(String deliveryAddressSnapshot) {
        this.deliveryAddressSnapshot = deliveryAddressSnapshot;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public PaymentMethodType getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethodType paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.updatedAt = System.currentTimeMillis();
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
        this.updatedAt = System.currentTimeMillis();
        if (orderStatus == OrderStatus.ACCEPTED && acceptedAt == null) {
            this.acceptedAt = System.currentTimeMillis();
        }
        if (orderStatus == OrderStatus.COMPLETED && completedAt == null) {
            this.completedAt = System.currentTimeMillis();
        }
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Long acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<OrderStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    private void recalculateTotal() {
        this.subtotal = items.stream().mapToDouble(OrderItem::getLineTotal).sum();
        this.total = this.subtotal + this.deliveryFee;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return orderStatus.canTransitionTo(newStatus);
    }

    public boolean isCancellable() {
        return orderStatus.isCancellable();
    }

    public boolean isCompleted() {
        return orderStatus == OrderStatus.COMPLETED;
    }

    public boolean isFinal() {
        return orderStatus.isFinal();
    }

    public boolean isDelivery() {
        return fulfilmentMethod == FulfilmentMethod.DELIVERY;
    }

    public int getItemCount() {
        return items.size();
    }

    public String getFormattedTotal() {
        return String.format("₹%.2f", total);
    }

    public static class OrderStatusHistory {
        private String historyId;
        private String orderId;
        private OrderStatus oldStatus;
        private OrderStatus newStatus;
        private String changedBy;
        private String note;
        private long createdAt;

        public OrderStatusHistory() {}

        public OrderStatusHistory(String historyId, String orderId, OrderStatus oldStatus,
                                  OrderStatus newStatus, String changedBy, String note) {
            this.historyId = historyId;
            this.orderId = orderId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.changedBy = changedBy;
            this.note = note;
            this.createdAt = System.currentTimeMillis();
        }

        public String getHistoryId() { return historyId; }
        public void setHistoryId(String historyId) { this.historyId = historyId; }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public OrderStatus getOldStatus() { return oldStatus; }
        public void setOldStatus(OrderStatus oldStatus) { this.oldStatus = oldStatus; }
        public OrderStatus getNewStatus() { return newStatus; }
        public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }
        public String getChangedBy() { return changedBy; }
        public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    }
}
