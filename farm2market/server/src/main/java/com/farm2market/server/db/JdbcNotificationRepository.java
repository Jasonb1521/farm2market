package com.farm2market.server.db;

import com.farm2market.core.model.Notification;
import com.farm2market.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcNotificationRepository {

    private final DatabaseConnection db;

    public JdbcNotificationRepository() {
        this.db = DatabaseConnection.getInstance();
    }

    public Optional<Notification> findById(String notificationId) {
        String sql = "SELECT * FROM notifications WHERE notification_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notificationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Notification> findByUserId(String userId) {
        String sql = """
            SELECT * FROM notifications
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT 50
            """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public List<Notification> findUnreadByUserId(String userId) {
        String sql = """
            SELECT * FROM notifications
            WHERE user_id = ? AND is_read = 0
            ORDER BY created_at DESC
            """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public Notification save(Notification notification) {
        String sql = """
            INSERT INTO notifications (notification_id, user_id, title, message, related_order_id, is_read, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (notification.getNotificationId() == null) {
                notification.setNotificationId(IdGenerator.generateUUID());
            }
            if (notification.getCreatedAt() == 0) {
                notification.setCreatedAt(System.currentTimeMillis());
            }

            stmt.setString(1, notification.getNotificationId());
            stmt.setString(2, notification.getUserId());
            stmt.setString(3, notification.getTitle());
            stmt.setString(4, notification.getMessage());
            stmt.setString(5, notification.getRelatedOrderId());
            stmt.setBoolean(6, notification.isRead());
            stmt.setLong(7, notification.getCreatedAt());

            stmt.executeUpdate();
            return notification;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save notification", e);
        }
    }

    public void markAsRead(String notificationId) {
        String sql = "UPDATE notifications SET is_read = 1, read_at = ? WHERE notification_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAllAsRead(String userId) {
        String sql = "UPDATE notifications SET is_read = 1, read_at = ? WHERE user_id = ? AND is_read = 0";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUnreadCount(String userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteOldNotifications(int daysOld) {
        String sql = "DELETE FROM notifications WHERE created_at < ? AND is_read = 1";

        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60 * 60 * 1000);

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cutoffTime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createOrderNotification(String userId, String orderId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedOrderId(orderId);
        notification.setRead(false);
        save(notification);
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rs.getString("notification_id"));
        notification.setUserId(rs.getString("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setRelatedOrderId(rs.getString("related_order_id"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getLong("created_at"));

        long readAt = rs.getLong("read_at");
        if (!rs.wasNull()) {
            notification.setReadAt(readAt);
        }

        return notification;
    }
}
