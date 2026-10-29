package com.farm2market.server.db;

import com.farm2market.core.enums.*;
import com.farm2market.core.model.Order;
import com.farm2market.core.model.OrderItem;
import com.farm2market.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcOrderRepository {

    private final DatabaseConnection db;

    public JdbcOrderRepository() {
        this.db = DatabaseConnection.getInstance();
    }

    public Order save(Order order) {
        String sql = """
            INSERT INTO orders (order_id, customer_id, farmer_id, subtotal, delivery_fee, total,
                fulfilment_method, delivery_address_snapshot, contact_name, contact_phone,
                payment_method, payment_status, order_status, customer_note, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, order.getOrderId());
            stmt.setString(2, order.getCustomerId());
            stmt.setString(3, order.getFarmerId());
            stmt.setDouble(4, order.getSubtotal());
            stmt.setDouble(5, order.getDeliveryFee());
            stmt.setDouble(6, order.getTotal());
            stmt.setString(7, order.getFulfilmentMethod() != null ? order.getFulfilmentMethod().name() : "DELIVERY");
            stmt.setString(8, order.getDeliveryAddressSnapshot());
            stmt.setString(9, order.getContactName());
            stmt.setString(10, order.getContactPhone());
            stmt.setString(11, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD");
            stmt.setString(12, order.getPaymentStatus().name());
            stmt.setString(13, order.getOrderStatus().name());
            stmt.setString(14, order.getCustomerNote());
            stmt.setLong(15, order.getCreatedAt());
            stmt.setLong(16, order.getUpdatedAt());
            stmt.executeUpdate();

            // Save order items
            for (OrderItem item : order.getItems()) {
                saveOrderItem(order.getOrderId(), item);
            }

            // Add initial status history
            addStatusHistory(order.getOrderId(), OrderStatus.PENDING, "Order placed");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    private void saveOrderItem(String orderId, OrderItem item) {
        String sql = """
            INSERT INTO order_items (order_item_id, order_id, product_id, product_name_snapshot,
                unit_price_snapshot, unit_snapshot, quantity, line_total)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getOrderItemId() != null ? item.getOrderItemId() : IdGenerator.generateUUID());
            stmt.setString(2, orderId);
            stmt.setString(3, item.getProductId());
            stmt.setString(4, item.getProductNameSnapshot());
            stmt.setDouble(5, item.getUnitPriceSnapshot());
            stmt.setString(6, item.getUnitSnapshot() != null ? item.getUnitSnapshot().name() : "KG");
            stmt.setDouble(7, item.getQuantity());
            stmt.setDouble(8, item.getLineTotal());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addStatusHistory(String orderId, OrderStatus status, String note) {
        String sql = """
            INSERT INTO order_status_history (history_id, order_id, new_status, note, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, IdGenerator.generateUUID());
            stmt.setString(2, orderId);
            stmt.setString(3, status.name());
            stmt.setString(4, note);
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<Order> findById(String orderId) {
        String sql = """
            SELECT o.*, c.name as customer_name, f.name as farmer_name
            FROM orders o
            LEFT JOIN users c ON o.customer_id = c.user_id
            LEFT JOIN users f ON o.farmer_id = f.user_id
            WHERE o.order_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findOrderItems(orderId));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, c.name as customer_name, f.name as farmer_name
            FROM orders o
            LEFT JOIN users c ON o.customer_id = c.user_id
            LEFT JOIN users f ON o.farmer_id = f.user_id
            ORDER BY o.created_at DESC
            LIMIT 1000
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findOrderItems(order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> findByCustomerId(String customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, c.name as customer_name, f.name as farmer_name
            FROM orders o
            LEFT JOIN users c ON o.customer_id = c.user_id
            LEFT JOIN users f ON o.farmer_id = f.user_id
            WHERE o.customer_id = ?
            ORDER BY o.created_at DESC
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findOrderItems(order.getOrderId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> findByFarmerId(String farmerId) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, c.name as customer_name, f.name as farmer_name
            FROM orders o
            LEFT JOIN users c ON o.customer_id = c.user_id
            LEFT JOIN users f ON o.farmer_id = f.user_id
            WHERE o.farmer_id = ?
            ORDER BY o.created_at DESC
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findOrderItems(order.getOrderId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private List<OrderItem> findOrderItems(String orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getString("order_item_id"));
                    item.setOrderId(rs.getString("order_id"));
                    item.setProductId(rs.getString("product_id"));
                    item.setProductNameSnapshot(rs.getString("product_name_snapshot"));
                    item.setUnitPriceSnapshot(rs.getDouble("unit_price_snapshot"));
                    item.setQuantity(rs.getDouble("quantity"));
                    item.setLineTotal(rs.getDouble("line_total"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void updateStatus(String orderId, OrderStatus status, String note) {
        String sql = "UPDATE orders SET order_status = ?, updated_at = ? WHERE order_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, orderId);
            stmt.executeUpdate();

            addStatusHistory(orderId, status, note);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getString("order_id"));
        order.setCustomerId(rs.getString("customer_id"));
        order.setFarmerId(rs.getString("farmer_id"));
        order.setSubtotal(rs.getDouble("subtotal"));
        order.setDeliveryFee(rs.getDouble("delivery_fee"));
        order.setTotal(rs.getDouble("total"));

        try {
            order.setFulfilmentMethod(FulfilmentMethod.valueOf(rs.getString("fulfilment_method")));
        } catch (Exception e) {
            order.setFulfilmentMethod(FulfilmentMethod.DELIVERY);
        }

        order.setDeliveryAddressSnapshot(rs.getString("delivery_address_snapshot"));
        order.setContactName(rs.getString("contact_name"));
        order.setContactPhone(rs.getString("contact_phone"));

        try {
            order.setPaymentMethod(PaymentMethodType.valueOf(rs.getString("payment_method")));
        } catch (Exception e) {
            order.setPaymentMethod(PaymentMethodType.CASH_ON_DELIVERY);
        }

        try {
            order.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        } catch (Exception e) {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        try {
            order.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
        } catch (Exception e) {
            order.setOrderStatus(OrderStatus.PENDING);
        }

        order.setCustomerNote(rs.getString("customer_note"));
        order.setCreatedAt(rs.getLong("created_at"));
        order.setUpdatedAt(rs.getLong("updated_at"));

        // Transient fields
        order.setCustomerName(rs.getString("customer_name"));
        order.setFarmerName(rs.getString("farmer_name"));

        return order;
    }
}
