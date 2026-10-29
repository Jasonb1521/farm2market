package com.farm2market.core.model;

import com.farm2market.core.enums.FulfilmentMethod;
import com.farm2market.core.enums.Role;
import java.util.HashSet;
import java.util.Set;

public class Farmer extends User {
    private String farmName;
    private String location;
    private String description;
    private String farmingPractices;
    private String harvestFrequency;
    private String serviceArea;
    private Set<FulfilmentMethod> fulfilmentOptions;
    private double ratingSum;
    private int ratingCount;
    private boolean verified;

    public Farmer() {
        super();
        setRole(Role.FARMER);
        this.fulfilmentOptions = new HashSet<>();
        this.fulfilmentOptions.add(FulfilmentMethod.PICKUP);
        this.ratingSum = 0;
        this.ratingCount = 0;
        this.verified = false;
    }

    public Farmer(String userId, String email, String name) {
        super(userId, email, name, Role.FARMER);
        this.fulfilmentOptions = new HashSet<>();
        this.fulfilmentOptions.add(FulfilmentMethod.PICKUP);
        this.ratingSum = 0;
        this.ratingCount = 0;
        this.verified = false;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFarmingPractices() {
        return farmingPractices;
    }

    public void setFarmingPractices(String farmingPractices) {
        this.farmingPractices = farmingPractices;
    }

    public String getHarvestFrequency() {
        return harvestFrequency;
    }

    public void setHarvestFrequency(String harvestFrequency) {
        this.harvestFrequency = harvestFrequency;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Set<FulfilmentMethod> getFulfilmentOptions() {
        return fulfilmentOptions;
    }

    public void setFulfilmentOptions(Set<FulfilmentMethod> fulfilmentOptions) {
        this.fulfilmentOptions = fulfilmentOptions;
    }

    public void addFulfilmentOption(FulfilmentMethod method) {
        this.fulfilmentOptions.add(method);
    }

    public boolean supportsFulfilment(FulfilmentMethod method) {
        return fulfilmentOptions.contains(method);
    }

    public double getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(double ratingSum) {
        this.ratingSum = ratingSum;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public double getAverageRating() {
        return ratingCount > 0 ? ratingSum / ratingCount : 0.0;
    }

    public void addRating(int rating) {
        this.ratingSum += rating;
        this.ratingCount++;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getDisplayName() {
        return farmName != null && !farmName.isEmpty() ? farmName : getName();
    }

    @Override
    public boolean canAccessFeature(String feature) {
        return switch (feature) {
            case "browse", "profile" -> true;
            case "sell", "listings", "farmer_dashboard", "orders", "sales" -> true;
            case "cart", "checkout" -> false;
            case "admin", "moderation" -> false;
            default -> false;
        };
    }
}
