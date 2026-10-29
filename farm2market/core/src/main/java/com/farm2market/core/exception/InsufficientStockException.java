package com.farm2market.core.exception;

public class InsufficientStockException extends Farm2MarketException {
    private final String productId;
    private final double requestedQuantity;
    private final double availableQuantity;

    public InsufficientStockException(String productId, double requestedQuantity, double availableQuantity) {
        super(String.format("Insufficient stock for product %s. Requested: %.2f, Available: %.2f",
                productId, requestedQuantity, availableQuantity), "INSUFFICIENT_STOCK");
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(String productName, String productId,
                                       double requestedQuantity, double availableQuantity) {
        super(String.format("Insufficient stock for '%s'. Requested: %.2f, Available: %.2f",
                productName, requestedQuantity, availableQuantity), "INSUFFICIENT_STOCK");
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public double getRequestedQuantity() {
        return requestedQuantity;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }
}
