package com.farm2market.core.repository;

import com.farm2market.core.model.Cart;
import com.farm2market.core.model.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends Repository<Cart, String> {

    Optional<Cart> findByCustomerId(String customerId);

    Cart getOrCreateCart(String customerId);

    List<CartItem> findItemsByCartId(String cartId);

    Optional<CartItem> findCartItem(String cartId, String productId);

    CartItem saveItem(CartItem item);

    CartItem updateItem(CartItem item);

    void deleteItem(String cartItemId);

    void deleteItemByProductId(String cartId, String productId);

    void clearCart(String cartId);

    int countItems(String cartId);

    double calculateSubtotal(String cartId);

    void saveCartAction(String cartId, Cart.CartAction action);

    List<Cart.CartAction> getRecentActions(String cartId, int limit);
}
