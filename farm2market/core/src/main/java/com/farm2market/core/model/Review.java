package com.farm2market.core.model;

public class Review implements Entity<String> {
    private String reviewId;
    private String orderId;
    private String customerId;
    private String farmerId;
    private int rating;
    private String comment;
    private long createdAt;
    private long updatedAt;

    // Transient fields for display
    private transient String customerName;
    private transient String customerImageUrl;

    public Review() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Review(String reviewId, String orderId, String customerId, String farmerId, int rating) {
        this();
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.farmerId = farmerId;
        setRating(rating);
    }

    @Override
    public String getId() {
        return reviewId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;
        this.rating = rating;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerImageUrl() {
        return customerImageUrl;
    }

    public void setCustomerImageUrl(String customerImageUrl) {
        this.customerImageUrl = customerImageUrl;
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
}
