package com.farm2market.core.service;

import com.farm2market.core.model.Notification;
import com.farm2market.core.model.Order;

public interface NotificationService {

    void send(Notification notification);

    void sendOrderPlaced(Order order);

    void sendOrderAccepted(Order order);

    void sendOrderRejected(Order order, String reason);

    void sendOrderStatusUpdate(Order order);

    void sendOrderCompleted(Order order);

    void sendOrderCancelled(Order order);

    void sendNewReview(String farmerId, String customerName, int rating);

    void broadcast(String title, String message);
}
