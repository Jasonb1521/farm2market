package com.farm2market.core.dto;

public class CartItemRequest {
    private String cartItemId;
    private String productId;
    private double quantity;

    public CartItemRequest() {}

    public CartItemRequest(String productId, double quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}
