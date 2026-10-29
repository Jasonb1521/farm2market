package com.farm2market.server;

import com.farm2market.core.model.Notification;
import com.farm2market.core.model.Order;
import com.farm2market.core.protocol.MessageType;
import com.farm2market.core.protocol.ProtocolSerializer;
import com.farm2market.core.protocol.Response;
import com.farm2market.core.util.IdGenerator;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotificationDispatcher extends Thread {

    private static NotificationDispatcher instance;
    private final BlockingQueue<Notification> notificationQueue;
    private final ConcurrentHashMap<String, PrintWriter> connectedClients;
    private final AtomicBoolean running;

    private NotificationDispatcher() {
        super("NotificationDispatcher");
        this.notificationQueue = new LinkedBlockingQueue<>();
        this.connectedClients = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(false);
        setDaemon(true);
    }

    public static synchronized NotificationDispatcher getInstance() {
        if (instance == null) {
            instance = new NotificationDispatcher();
        }
        return instance;
    }

    @Override
    public void run() {
        running.set(true);
        System.out.println("[NotificationDispatcher] Started");

        while (running.get()) {
            try {
                Notification notification = notificationQueue.take();
                deliverNotification(notification);
            } catch (InterruptedException e) {
                if (running.get()) {
                    System.err.println("[NotificationDispatcher] Interrupted: " + e.getMessage());
                }
            }
        }

        System.out.println("[NotificationDispatcher] Stopped");
    }

    private void deliverNotification(Notification notification) {
        String userId = notification.getUserId();
        PrintWriter writer = connectedClients.get(userId);

        if (writer != null) {
            try {
                Response response = Response.success(
                        MessageType.NOTIFICATION_PUSH,
                        "push-" + System.currentTimeMillis(),
                        notification
                );
                writer.print(ProtocolSerializer.serialize(response));
                writer.flush();
                System.out.printf("[NotificationDispatcher] Sent notification to user %s: %s%n",
                        userId, notification.getTitle());
            } catch (Exception e) {
                System.err.println("[NotificationDispatcher] Failed to send to " + userId + ": " + e.getMessage());
                connectedClients.remove(userId);
            }
        } else {
            System.out.printf("[NotificationDispatcher] User %s not connected, notification stored%n", userId);
        }
    }

    public void registerClient(String userId, PrintWriter writer) {
        connectedClients.put(userId, writer);
        System.out.println("[NotificationDispatcher] Client registered: " + userId);
    }

    public void unregisterClient(String userId) {
        connectedClients.remove(userId);
        System.out.println("[NotificationDispatcher] Client unregistered: " + userId);
    }

    public void queueNotification(Notification notification) {
        notificationQueue.offer(notification);
    }

    public void notifyOrderPlaced(Order order) {
        Notification farmerNotification = Notification.orderNotification(
                IdGenerator.generateNotificationId(),
                order.getFarmerId(),
                order.getOrderId(),
                Notification.NotificationType.ORDER_PLACED,
                String.format("New order #%s from %s for ₹%.2f",
                        order.getOrderId(), order.getCustomerName(), order.getTotal())
        );
        queueNotification(farmerNotification);
    }

    public void notifyOrderAccepted(Order order) {
        Notification customerNotification = Notification.orderNotification(
                IdGenerator.generateNotificationId(),
                order.getCustomerId(),
                order.getOrderId(),
                Notification.NotificationType.ORDER_ACCEPTED,
                String.format("Your order #%s has been accepted by %s",
                        order.getOrderId(), order.getFarmerName())
        );
        queueNotification(customerNotification);
    }

    public void notifyOrderRejected(Order order, String reason) {
        Notification customerNotification = Notification.orderNotification(
                IdGenerator.generateNotificationId(),
                order.getCustomerId(),
                order.getOrderId(),
                Notification.NotificationType.ORDER_REJECTED,
                String.format("Your order #%s was rejected. Reason: %s",
                        order.getOrderId(), reason != null ? reason : "Not specified")
        );
        queueNotification(customerNotification);
    }

    public void notifyOrderStatusUpdate(Order order) {
        Notification.NotificationType type = switch (order.getOrderStatus()) {
            case PREPARING -> Notification.NotificationType.ORDER_PREPARING;
            case READY_FOR_PICKUP -> Notification.NotificationType.ORDER_READY;
            case OUT_FOR_DELIVERY -> Notification.NotificationType.ORDER_DELIVERING;
            case COMPLETED -> Notification.NotificationType.ORDER_COMPLETED;
            default -> Notification.NotificationType.GENERAL;
        };

        Notification customerNotification = Notification.orderNotification(
                IdGenerator.generateNotificationId(),
                order.getCustomerId(),
                order.getOrderId(),
                type,
                String.format("Order #%s is now %s",
                        order.getOrderId(), order.getOrderStatus().getDisplayName())
        );
        queueNotification(customerNotification);
    }

    public void notifyOrderCancelled(Order order, boolean byCustomer) {
        String targetUserId = byCustomer ? order.getFarmerId() : order.getCustomerId();
        String message = byCustomer
                ? String.format("Order #%s was cancelled by the customer", order.getOrderId())
                : String.format("Your order #%s was cancelled", order.getOrderId());

        Notification notification = Notification.orderNotification(
                IdGenerator.generateNotificationId(),
                targetUserId,
                order.getOrderId(),
                Notification.NotificationType.ORDER_CANCELLED,
                message
        );
        queueNotification(notification);
    }

    public void notifyNewReview(String farmerId, String customerName, int rating) {
        Notification notification = new Notification(
                IdGenerator.generateNotificationId(),
                farmerId,
                "New Review",
                String.format("%s left a %d-star review", customerName, rating)
        );
        notification.setType(Notification.NotificationType.NEW_REVIEW);
        queueNotification(notification);
    }

    public void shutdown() {
        running.set(false);
        interrupt();
    }

    public int getQueueSize() {
        return notificationQueue.size();
    }

    public int getConnectedClientCount() {
        return connectedClients.size();
    }
}
