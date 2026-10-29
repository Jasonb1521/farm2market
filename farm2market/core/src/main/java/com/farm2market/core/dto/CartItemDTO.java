package com.farm2market.core.dto;

import com.farm2market.core.model.CartItem;
import com.farm2market.core.model.Product;

public class CartItemDTO {
    private String cartItemId;
    private String productId;
    private String productName;
    private String productImageUrl;
    private double unitPrice;
    private String unitSymbol;
    private double quantity;
    private double lineTotal;
    private double availableStock;
    private String farmerId;
    private String farmerName;

    public CartItemDTO() {}

    public static CartItemDTO fromCartItem(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(item.getCartItemId());
        dto.setProductId(item.getProductId());
        dto.setUnitPrice(item.getPriceSnapshot());
        dto.setQuantity(item.getQuantity());
        dto.setLineTotal(item.getLineTotal());

        Product product = item.getProduct();
        if (product != null) {
            dto.setProductName(product.getName());
            dto.setProductImageUrl(product.getPrimaryImageUrl());
            dto.setUnitSymbol(product.getUnit().getSymbol());
            dto.setAvailableStock(product.getStockQuantity());
            dto.setFarmerId(product.getFarmerId());
            dto.setFarmerName(product.getFarmerName());
        }
        return dto;
    }

    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public String getUnitSymbol() { return unitSymbol; }
    public void setUnitSymbol(String unitSymbol) { this.unitSymbol = unitSymbol; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
    public double getAvailableStock() { return availableStock; }
    public void setAvailableStock(double availableStock) { this.availableStock = availableStock; }
    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getFormattedPrice() {
        return String.format("₹%.2f/%s", unitPrice, unitSymbol != null ? unitSymbol : "unit");
    }

    public boolean isStockSufficient() {
        return quantity <= availableStock;
    }
}
