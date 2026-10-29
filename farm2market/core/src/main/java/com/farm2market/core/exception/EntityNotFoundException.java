package com.farm2market.core.exception;

public class EntityNotFoundException extends Farm2MarketException {
    private final String entityType;
    private final String entityId;

    public EntityNotFoundException(String entityType, String entityId) {
        super(String.format("%s with ID %s not found", entityType, entityId), "NOT_FOUND");
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String message) {
        super(message, "NOT_FOUND");
        this.entityType = null;
        this.entityId = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public static EntityNotFoundException user(String userId) {
        return new EntityNotFoundException("User", userId);
    }

    public static EntityNotFoundException product(String productId) {
        return new EntityNotFoundException("Product", productId);
    }

    public static EntityNotFoundException order(String orderId) {
        return new EntityNotFoundException("Order", orderId);
    }

    public static EntityNotFoundException cart(String cartId) {
        return new EntityNotFoundException("Cart", cartId);
    }

    public static EntityNotFoundException review(String reviewId) {
        return new EntityNotFoundException("Review", reviewId);
    }
}
