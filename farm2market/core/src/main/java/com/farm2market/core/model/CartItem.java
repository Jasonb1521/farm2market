package com.farm2market.core.model;

public class CartItem implements Entity<String> {
    private String cartItemId;
    private String cartId;
    private String productId;
    private double quantity;
    private double priceSnapshot;
    private long addedAt;

    // Transient fields for display
    private transient Product product;

    public CartItem() {
        this.addedAt = System.currentTimeMillis();
    }

    public CartItem(String cartItemId, String cartId, String productId, double quantity, double priceSnapshot) {
        this();
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
    }

    @Override
    public String getId() {
        return cartItemId;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPriceSnapshot() {
        return priceSnapshot;
    }

    public void setPriceSnapshot(double priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getLineTotal() {
        return priceSnapshot * quantity;
    }

    public void incrementQuantity(double amount) {
        this.quantity += amount;
    }

    public void decrementQuantity(double amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }
}
