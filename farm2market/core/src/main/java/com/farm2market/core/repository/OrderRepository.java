package com.farm2market.core.repository;

import com.farm2market.core.enums.OrderStatus;
import com.farm2market.core.model.Order;
import java.util.List;

public interface OrderRepository extends Repository<Order, String> {

    List<Order> findByCustomerId(String customerId);

    List<Order> findByCustomerId(String customerId, int limit, int offset);

    List<Order> findByFarmerId(String farmerId);

    List<Order> findByFarmerId(String farmerId, int limit, int offset);

    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);

    List<Order> findByFarmerIdAndStatus(String farmerId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findActiveByCustomerId(String customerId);

    List<Order> findActiveByFarmerId(String farmerId);

    List<Order> findCompletedByFarmerId(String farmerId, long fromDate, long toDate);

    void updateStatus(String orderId, OrderStatus newStatus, String changedBy, String note);

    void addStatusHistory(Order.OrderStatusHistory history);

    List<Order.OrderStatusHistory> getStatusHistory(String orderId);

    long countByCustomerId(String customerId);

    long countByFarmerId(String farmerId);

    long countByStatus(OrderStatus status);

    long countToday();

    double sumTotalByFarmerId(String farmerId, long fromDate, long toDate);
}
