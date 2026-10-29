package com.farm2market.core.repository;

import com.farm2market.core.model.Notification;
import java.util.List;

public interface NotificationRepository extends Repository<Notification, String> {

    List<Notification> findByUserId(String userId);

    List<Notification> findByUserId(String userId, int limit, int offset);

    List<Notification> findUnreadByUserId(String userId);

    List<Notification> findByUserIdAndOrderId(String userId, String orderId);

    void markAsRead(String notificationId);

    void markAllAsRead(String userId);

    long countUnreadByUserId(String userId);

    void deleteOlderThan(long timestamp);
}
