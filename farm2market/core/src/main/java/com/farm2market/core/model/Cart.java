package com.farm2market.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class Cart implements Entity<String> {
    private String cartId;
    private String customerId;
    private List<CartItem> items;
    private long createdAt;
    private long updatedAt;

    // For undo feature
    private transient Stack<CartAction> actionHistory;

    public Cart() {
        this.items = new ArrayList<>();
        this.actionHistory = new Stack<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Cart(String cartId, String customerId) {
        this();
        this.cartId = cartId;
        this.customerId = customerId;
    }

    @Override
    public String getId() {
        return cartId;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
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

    public Stack<CartAction> getActionHistory() {
        if (actionHistory == null) {
            actionHistory = new Stack<>();
        }
        return actionHistory;
    }

    public void addItem(CartItem item) {
        Optional<CartItem> existing = findItem(item.getProductId());
        if (existing.isPresent()) {
            CartItem existingItem = existing.get();
            double oldQuantity = existingItem.getQuantity();
            existingItem.setQuantity(oldQuantity + item.getQuantity());
            recordAction(CartAction.Type.UPDATE_QUANTITY, item.getProductId(), oldQuantity, existingItem.getQuantity());
        } else {
            items.add(item);
            recordAction(CartAction.Type.ADD, item.getProductId(), 0, item.getQuantity());
        }
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateItemQuantity(String productId, double newQuantity) {
        findItem(productId).ifPresent(item -> {
            double oldQuantity = item.getQuantity();
            item.setQuantity(newQuantity);
            recordAction(CartAction.Type.UPDATE_QUANTITY, productId, oldQuantity, newQuantity);
            this.updatedAt = System.currentTimeMillis();
        });
    }

    public void removeItem(String productId) {
        findItem(productId).ifPresent(item -> {
            recordAction(CartAction.Type.REMOVE, productId, item.getQuantity(), 0);
            items.remove(item);
            this.updatedAt = System.currentTimeMillis();
        });
    }

    public Optional<CartItem> findItem(String productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    public void clear() {
        items.clear();
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.size();
    }

    public double getTotalQuantity() {
        return items.stream().mapToDouble(CartItem::getQuantity).sum();
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    private void recordAction(CartAction.Type type, String productId, double oldQuantity, double newQuantity) {
        getActionHistory().push(new CartAction(type, productId, oldQuantity, newQuantity));
        if (actionHistory.size() > 20) {
            actionHistory.remove(0);
        }
    }

    public boolean canUndo() {
        return !getActionHistory().isEmpty();
    }

    public CartAction popLastAction() {
        return canUndo() ? actionHistory.pop() : null;
    }

    public static class CartAction {
        public enum Type { ADD, REMOVE, UPDATE_QUANTITY }

        private final Type type;
        private final String productId;
        private final double oldQuantity;
        private final double newQuantity;
        private final long timestamp;

        public CartAction(Type type, String productId, double oldQuantity, double newQuantity) {
            this.type = type;
            this.productId = productId;
            this.oldQuantity = oldQuantity;
            this.newQuantity = newQuantity;
            this.timestamp = System.currentTimeMillis();
        }

        public Type getType() { return type; }
        public String getProductId() { return productId; }
        public double getOldQuantity() { return oldQuantity; }
        public double getNewQuantity() { return newQuantity; }
        public long getTimestamp() { return timestamp; }
    }
}
