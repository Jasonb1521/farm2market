package com.farm2market.server.db;

import com.farm2market.core.model.Cart;
import com.farm2market.core.model.CartItem;
import com.farm2market.core.model.Product;
import com.farm2market.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCartRepository {

    private final DatabaseConnection db;

    public JdbcCartRepository() {
        this.db = DatabaseConnection.getInstance();
    }

    public Optional<Cart> findByCustomerId(String customerId) {
        String sql = "SELECT * FROM carts WHERE customer_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cart cart = new Cart();
                    cart.setCartId(rs.getString("cart_id"));
                    cart.setCustomerId(rs.getString("customer_id"));
                    cart.setCreatedAt(rs.getLong("created_at"));
                    cart.setUpdatedAt(rs.getLong("updated_at"));
                    cart.setItems(findCartItems(cart.getCartId()));
                    return Optional.of(cart);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Cart getOrCreateCart(String customerId) {
        Optional<Cart> existing = findByCustomerId(customerId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Cart cart = new Cart();
        cart.setCartId(IdGenerator.generateCartId());
        cart.setCustomerId(customerId);
        cart.setCreatedAt(System.currentTimeMillis());
        cart.setUpdatedAt(System.currentTimeMillis());
        cart.setItems(new ArrayList<>());

        String sql = "INSERT INTO carts (cart_id, customer_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cart.getCartId());
            stmt.setString(2, cart.getCustomerId());
            stmt.setLong(3, cart.getCreatedAt());
            stmt.setLong(4, cart.getUpdatedAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cart;
    }

    private List<CartItem> findCartItems(String cartId) {
        List<CartItem> items = new ArrayList<>();
        String sql = """
            SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.price_snapshot, ci.added_at,
                   p.name, p.description, p.price, p.unit, p.category, p.stock_quantity, p.farmer_id,
                   u.name as farmer_name,
                   (SELECT image_url FROM product_images WHERE product_id = p.product_id AND is_primary = 1 LIMIT 1) as image_url
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.product_id
            LEFT JOIN users u ON p.farmer_id = u.user_id
            WHERE ci.cart_id = ?
            ORDER BY ci.added_at DESC
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getString("cart_item_id"));
                    item.setCartId(rs.getString("cart_id"));
                    item.setProductId(rs.getString("product_id"));
                    item.setQuantity(rs.getDouble("quantity"));
                    item.setPriceSnapshot(rs.getDouble("price_snapshot"));
                    item.setAddedAt(rs.getLong("added_at"));

                    // Populate transient Product
                    Product product = new Product();
                    product.setProductId(rs.getString("product_id"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getDouble("price"));
                    try {
                        product.setUnit(com.farm2market.core.enums.Unit.valueOf(rs.getString("unit")));
                    } catch (Exception e) {
                        product.setUnit(com.farm2market.core.enums.Unit.KG);
                    }
                    try {
                        product.setCategory(com.farm2market.core.enums.Category.valueOf(rs.getString("category")));
                    } catch (Exception e) {
                        product.setCategory(com.farm2market.core.enums.Category.VEGETABLES);
                    }
                    product.setStockQuantity(rs.getDouble("stock_quantity"));
                    product.setFarmerId(rs.getString("farmer_id"));
                    product.setFarmerName(rs.getString("farmer_name"));
                    String imageUrl = rs.getString("image_url");
                    if (imageUrl != null) {
                        product.setImageUrls(java.util.List.of(imageUrl));
                    }
                    item.setProduct(product);

                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public CartItem addItem(String cartId, String productId, double quantity, double price) {
        // Check if item already exists
        Optional<CartItem> existing = findCartItem(cartId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            updateItemQuantity(item.getCartItemId(), item.getQuantity());
            return item;
        }

        CartItem item = new CartItem();
        item.setCartItemId(IdGenerator.generateUUID());
        item.setCartId(cartId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPriceSnapshot(price);
        item.setAddedAt(System.currentTimeMillis());

        String sql = "INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, price_snapshot, added_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getCartItemId());
            stmt.setString(2, item.getCartId());
            stmt.setString(3, item.getProductId());
            stmt.setDouble(4, item.getQuantity());
            stmt.setDouble(5, item.getPriceSnapshot());
            stmt.setLong(6, item.getAddedAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateCartTimestamp(cartId);
        return item;
    }

    private Optional<CartItem> findCartItem(String cartId, String productId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cartId);
            stmt.setString(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getString("cart_item_id"));
                    item.setCartId(rs.getString("cart_id"));
                    item.setProductId(rs.getString("product_id"));
                    item.setQuantity(rs.getDouble("quantity"));
                    item.setPriceSnapshot(rs.getDouble("price_snapshot"));
                    item.setAddedAt(rs.getLong("added_at"));
                    return Optional.of(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void updateItemQuantity(String cartItemId, double quantity) {
        String sql = "UPDATE cart_items SET quantity = ?, updated_at = ? WHERE cart_item_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantity);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, cartItemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeItem(String cartItemId) {
        String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cartItemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearCart(String cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cartId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCartTimestamp(cartId);
    }

    private void updateCartTimestamp(String cartId) {
        String sql = "UPDATE carts SET updated_at = ? WHERE cart_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, cartId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
