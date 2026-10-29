package com.farm2market.core.model;

public class Address implements Entity<String> {
    private String addressId;
    private String userId;
    private String label;
    private String fullAddress;
    private String landmark;
    private String city;
    private String pincode;
    private String contactName;
    private String contactPhone;
    private boolean isDefault;
    private long createdAt;

    public Address() {
        this.isDefault = false;
        this.createdAt = System.currentTimeMillis();
    }

    public Address(String addressId, String userId, String fullAddress) {
        this();
        this.addressId = addressId;
        this.userId = userId;
        this.fullAddress = fullAddress;
    }

    @Override
    public String getId() {
        return addressId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullAddress);
        if (landmark != null && !landmark.isEmpty()) {
            sb.append(", Near ").append(landmark);
        }
        if (city != null && !city.isEmpty()) {
            sb.append(", ").append(city);
        }
        if (pincode != null && !pincode.isEmpty()) {
            sb.append(" - ").append(pincode);
        }
        return sb.toString();
    }

    public String toSnapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFormattedAddress());
        if (contactName != null && !contactName.isEmpty()) {
            sb.append("\nContact: ").append(contactName);
        }
        if (contactPhone != null && !contactPhone.isEmpty()) {
            sb.append(", ").append(contactPhone);
        }
        return sb.toString();
    }
}
