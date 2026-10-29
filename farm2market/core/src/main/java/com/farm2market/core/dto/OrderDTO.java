package com.farm2market.core.dto;

import com.farm2market.core.enums.FulfilmentMethod;
import com.farm2market.core.enums.OrderStatus;
import com.farm2market.core.enums.PaymentMethodType;
import com.farm2market.core.enums.PaymentStatus;
import com.farm2market.core.model.Order;
import com.farm2market.core.model.OrderItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDTO {
    private String orderId;
    private String customerId;
    private String customerName;
    private String farmerId;
    private String farmerName;
    private String farmName;
    private List<OrderItemDTO> items;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private FulfilmentMethod fulfilmentMethod;
    private String deliveryAddress;
    private String contactName;
    private String contactPhone;
    private PaymentMethodType paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private String rejectionReason;
    private String cancellationReason;
    private String customerNote;
    private long createdAt;
    private Long acceptedAt;
    private Long completedAt;

    public OrderDTO() {
        this.items = new ArrayList<>();
    }

    public static OrderDTO fromOrder(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerName(order.getCustomerName());
        dto.setFarmerId(order.getFarmerId());
        dto.setFarmerName(order.getFarmerName());
        dto.setFarmName(order.getFarmName());
        dto.setItems(order.getItems().stream()
                .map(OrderItemDTO::fromOrderItem)
                .collect(Collectors.toList()));
        dto.setSubtotal(order.getSubtotal());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTotal(order.getTotal());
        dto.setFulfilmentMethod(order.getFulfilmentMethod());
        dto.setDeliveryAddress(order.getDeliveryAddressSnapshot());
        dto.setContactName(order.getContactName());
        dto.setContactPhone(order.getContactPhone());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setRejectionReason(order.getRejectionReason());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCustomerNote(order.getCustomerNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setAcceptedAt(order.getAcceptedAt());
        dto.setCompletedAt(order.getCompletedAt());
        return dto;
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public FulfilmentMethod getFulfilmentMethod() { return fulfilmentMethod; }
    public void setFulfilmentMethod(FulfilmentMethod fulfilmentMethod) { this.fulfilmentMethod = fulfilmentMethod; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public PaymentMethodType getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethodType paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public String getCustomerNote() { return customerNote; }
    public void setCustomerNote(String customerNote) { this.customerNote = customerNote; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Long getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Long acceptedAt) { this.acceptedAt = acceptedAt; }
    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }

    public String getFormattedTotal() {
        return String.format("₹%.2f", total);
    }

    public int getItemCount() {
        return items.size();
    }
}
