package com.farm2market.core.dto;

import com.farm2market.core.enums.Unit;
import com.farm2market.core.model.OrderItem;

public class OrderItemDTO {
    private String orderItemId;
    private String productId;
    private String productName;
    private double unitPrice;
    private Unit unit;
    private double quantity;
    private double lineTotal;

    public OrderItemDTO() {}

    public static OrderItemDTO fromOrderItem(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductNameSnapshot());
        dto.setUnitPrice(item.getUnitPriceSnapshot());
        dto.setUnit(item.getUnitSnapshot());
        dto.setQuantity(item.getQuantity());
        dto.setLineTotal(item.getLineTotal());
        return dto;
    }

    public String getOrderItemId() { return orderItemId; }
    public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }

    public String getFormattedPrice() {
        return unit != null ? unit.formatPrice(unitPrice) : String.format("₹%.2f", unitPrice);
    }
}
