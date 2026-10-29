package com.farm2market.core.service;

import com.farm2market.core.enums.PaymentMethodType;
import com.farm2market.core.model.Order;

public interface PaymentMethod {

    PaymentMethodType getType();

    boolean isAvailable();

    boolean processPayment(Order order);

    boolean refundPayment(Order order);

    String getDisplayName();

    String getDescription();
}
