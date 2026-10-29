package com.farm2market.core.dto;

import com.farm2market.core.model.Cart;
import com.farm2market.core.model.CartItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CartDTO {
    private String cartId;
    private String customerId;
    private List<CartItemDTO> items;
    private double subtotal;
    private int itemCount;

    public CartDTO() {
        this.items = new ArrayList<>();
    }

    public static CartDTO fromCart(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        dto.setCustomerId(cart.getCustomerId());
        dto.setItems(cart.getItems().stream()
                .map(CartItemDTO::fromCartItem)
                .collect(Collectors.toList()));
        dto.setSubtotal(cart.getSubtotal());
        dto.setItemCount(cart.getItemCount());
        return dto;
    }

    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public List<CartItemDTO> getItems() { return items; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public String getFormattedSubtotal() {
        return String.format("₹%.2f", subtotal);
    }
}
