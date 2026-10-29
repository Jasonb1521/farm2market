package com.farm2market.core.model;

import com.farm2market.core.enums.Role;
import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private List<String> addressIds;
    private String defaultAddressId;
    private List<String> recentSearches;
    private List<String> wishlistProductIds;

    public Customer() {
        super();
        setRole(Role.CUSTOMER);
        this.addressIds = new ArrayList<>();
        this.recentSearches = new ArrayList<>();
        this.wishlistProductIds = new ArrayList<>();
    }

    public Customer(String userId, String email, String name) {
        super(userId, email, name, Role.CUSTOMER);
        this.addressIds = new ArrayList<>();
        this.recentSearches = new ArrayList<>();
        this.wishlistProductIds = new ArrayList<>();
    }

    public List<String> getAddressIds() {
        return addressIds;
    }

    public void setAddressIds(List<String> addressIds) {
        this.addressIds = addressIds;
    }

    public void addAddress(String addressId) {
        if (!addressIds.contains(addressId)) {
            addressIds.add(addressId);
        }
    }

    public String getDefaultAddressId() {
        return defaultAddressId;
    }

    public void setDefaultAddressId(String defaultAddressId) {
        this.defaultAddressId = defaultAddressId;
    }

    public List<String> getRecentSearches() {
        return recentSearches;
    }

    public void setRecentSearches(List<String> recentSearches) {
        this.recentSearches = recentSearches;
    }

    public void addRecentSearch(String searchTerm) {
        recentSearches.remove(searchTerm);
        recentSearches.add(0, searchTerm);
        if (recentSearches.size() > 10) {
            recentSearches = new ArrayList<>(recentSearches.subList(0, 10));
        }
    }

    public List<String> getWishlistProductIds() {
        return wishlistProductIds;
    }

    public void setWishlistProductIds(List<String> wishlistProductIds) {
        this.wishlistProductIds = wishlistProductIds;
    }

    public void addToWishlist(String productId) {
        if (!wishlistProductIds.contains(productId)) {
            wishlistProductIds.add(productId);
        }
    }

    public void removeFromWishlist(String productId) {
        wishlistProductIds.remove(productId);
    }

    public boolean isInWishlist(String productId) {
        return wishlistProductIds.contains(productId);
    }

    @Override
    public boolean canAccessFeature(String feature) {
        return switch (feature) {
            case "browse", "search", "cart", "checkout", "orders", "reviews", "profile" -> true;
            case "sell", "listings", "farmer_dashboard" -> false;
            case "admin", "moderation" -> false;
            default -> false;
        };
    }
}
