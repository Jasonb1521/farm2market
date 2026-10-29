package com.farm2market.core.model;

import com.farm2market.core.enums.Unit;

public class OrderItem implements Entity<String> {
    private String orderItemId;
    private String orderId;
    private String productId;
    private String productNameSnapshot;
    private double unitPriceSnapshot;
    private Unit unitSnapshot;
    private double quantity;
    private double lineTotal;

    public OrderItem() {
    }

    public OrderItem(String orderItemId, String orderId, String productId,
                     String productName, double unitPrice, Unit unit, double quantity) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productNameSnapshot = productName;
        this.unitPriceSnapshot = unitPrice;
        this.unitSnapshot = unit;
        this.quantity = quantity;
        this.lineTotal = unitPrice * quantity;
    }

    public static OrderItem fromCartItem(String orderItemId, String orderId, CartItem cartItem, Product product) {
        return new OrderItem(
            orderItemId,
            orderId,
            cartItem.getProductId(),
            product.getName(),
            cartItem.getPriceSnapshot(),
            product.getUnit(),
            cartItem.getQuantity()
        );
    }

    @Override
    public String getId() {
        return orderItemId;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }

    public double getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(double unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public Unit getUnitSnapshot() {
        return unitSnapshot;
    }

    public void setUnitSnapshot(Unit unitSnapshot) {
        this.unitSnapshot = unitSnapshot;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.lineTotal = this.unitPriceSnapshot * quantity;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getFormattedPrice() {
        return String.format("₹%.2f/%s", unitPriceSnapshot, unitSnapshot.getSymbol());
    }

    public String getFormattedQuantity() {
        return unitSnapshot.format(quantity);
    }

    public String getFormattedLineTotal() {
        return String.format("₹%.2f", lineTotal);
    }
}
