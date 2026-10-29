package com.farm2market.core.model;

public class Notification implements Entity<String> {
    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private String relatedOrderId;
    private NotificationType type;
    private boolean read;
    private Long readAt;
    private long createdAt;

    public enum NotificationType {
        ORDER_PLACED,
        ORDER_ACCEPTED,
        ORDER_REJECTED,
        ORDER_PREPARING,
        ORDER_READY,
        ORDER_DELIVERING,
        ORDER_COMPLETED,
        ORDER_CANCELLED,
        NEW_REVIEW,
        REPORT_RESOLVED,
        ACCOUNT_UPDATE,
        GENERAL
    }

    public Notification() {
        this.read = false;
        this.createdAt = System.currentTimeMillis();
    }

    public Notification(String notificationId, String userId, String title, String message) {
        this();
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = NotificationType.GENERAL;
    }

    public static Notification orderNotification(String notificationId, String userId,
                                                  String orderId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setUserId(userId);
        notification.setRelatedOrderId(orderId);
        notification.setType(type);
        notification.setTitle(getTitleForType(type));
        notification.setMessage(message);
        return notification;
    }

    private static String getTitleForType(NotificationType type) {
        return switch (type) {
            case ORDER_PLACED -> "Order Placed";
            case ORDER_ACCEPTED -> "Order Accepted";
            case ORDER_REJECTED -> "Order Rejected";
            case ORDER_PREPARING -> "Order Being Prepared";
            case ORDER_READY -> "Order Ready";
            case ORDER_DELIVERING -> "Order Out for Delivery";
            case ORDER_COMPLETED -> "Order Completed";
            case ORDER_CANCELLED -> "Order Cancelled";
            case NEW_REVIEW -> "New Review";
            case REPORT_RESOLVED -> "Report Resolved";
            case ACCOUNT_UPDATE -> "Account Update";
            case GENERAL -> "Notification";
        };
    }

    @Override
    public String getId() {
        return notificationId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(String relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
        if (read && readAt == null) {
            this.readAt = System.currentTimeMillis();
        }
    }

    public Long getReadAt() {
        return readAt;
    }

    public void setReadAt(Long readAt) {
        this.readAt = readAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void markAsRead() {
        this.read = true;
        this.readAt = System.currentTimeMillis();
    }

    public boolean isOrderRelated() {
        return relatedOrderId != null && !relatedOrderId.isEmpty();
    }
}
