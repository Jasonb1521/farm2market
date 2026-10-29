package com.farm2market.core.service;

import com.farm2market.core.enums.PaymentMethodType;
import com.farm2market.core.enums.PaymentStatus;
import com.farm2market.core.model.Order;

public class PayOnPickup implements PaymentMethod {

    @Override
    public PaymentMethodType getType() {
        return PaymentMethodType.PAY_ON_PICKUP;
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
        return PaymentMethodType.PAY_ON_PICKUP.getDisplayName();
    }

    @Override
    public String getDescription() {
        return PaymentMethodType.PAY_ON_PICKUP.getDescription();
    }
}
