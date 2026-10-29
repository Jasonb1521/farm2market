package com.farm2market.core.service;

import com.farm2market.core.enums.PaymentMethodType;
import com.farm2market.core.enums.PaymentStatus;
import com.farm2market.core.model.Order;

public class CashOnDelivery implements PaymentMethod {

    @Override
    public PaymentMethodType getType() {
        return PaymentMethodType.CASH_ON_DELIVERY;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean processPayment(Order order) {
        order.setPaymentStatus(PaymentStatus.PENDING);
        return true;
    }

    @Override
    public boolean refundPayment(Order order) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return PaymentMethodType.CASH_ON_DELIVERY.getDisplayName();
    }

    @Override
    public String getDescription() {
        return PaymentMethodType.CASH_ON_DELIVERY.getDescription();
    }
}
