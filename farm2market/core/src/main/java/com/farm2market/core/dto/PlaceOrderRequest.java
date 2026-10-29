package com.farm2market.core.dto;

import com.farm2market.core.enums.FulfilmentMethod;
import com.farm2market.core.enums.PaymentMethodType;

public class PlaceOrderRequest {
    private FulfilmentMethod fulfilmentMethod;
    private String addressId;
    private String deliveryAddress;
    private String contactName;
    private String contactPhone;
    private PaymentMethodType paymentMethod;
    private String customerNote;

    public PlaceOrderRequest() {}

    public FulfilmentMethod getFulfilmentMethod() { return fulfilmentMethod; }
    public void setFulfilmentMethod(FulfilmentMethod fulfilmentMethod) { this.fulfilmentMethod = fulfilmentMethod; }
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public PaymentMethodType getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethodType paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCustomerNote() { return customerNote; }
    public void setCustomerNote(String customerNote) { this.customerNote = customerNote; }

    public boolean isDelivery() {
        return fulfilmentMethod == FulfilmentMethod.DELIVERY;
    }
}
