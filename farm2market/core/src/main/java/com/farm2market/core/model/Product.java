package com.farm2market.core.model;

import com.farm2market.core.enums.Category;
import com.farm2market.core.enums.ProductStatus;
import com.farm2market.core.enums.Unit;
import java.util.ArrayList;
import java.util.List;

public class Product implements Entity<String> {
    private String productId;
    private String farmerId;
    private Category category;
    private String name;
    private String description;
    private double price;
    private Unit unit;
    private double stockQuantity;
    private double minOrderQuantity;
    private ProductStatus status;
    private List<String> imageUrls;
    private boolean seasonal;
    private boolean organic;
    private boolean locallyGrown;
    private Long harvestDate;
    private Long bestBefore;
    private long createdAt;
    private long updatedAt;

    // Transient fields for display
    private transient String farmerName;
    private transient String farmName;
    private transient double farmerRating;

    public Product() {
        this.status = ProductStatus.ACTIVE;
        this.imageUrls = new ArrayList<>();
        this.minOrderQuantity = 1.0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Product(String productId, String farmerId, String name, Category category,
                   double price, Unit unit, double stockQuantity) {
        this();
        this.productId = productId;
        this.farmerId = farmerId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String getId() {
        return productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.updatedAt = System.currentTimeMillis();
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(double stockQuantity) {
        this.stockQuantity = stockQuantity;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(double minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void addImageUrl(String url) {
        this.imageUrls.add(url);
    }

    public String getPrimaryImageUrl() {
        return imageUrls.isEmpty() ? null : imageUrls.get(0);
    }

    public boolean isSeasonal() {
        return seasonal;
    }

    public void setSeasonal(boolean seasonal) {
        this.seasonal = seasonal;
    }

    public boolean isOrganic() {
        return organic;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public boolean isLocallyGrown() {
        return locallyGrown;
    }

    public void setLocallyGrown(boolean locallyGrown) {
        this.locallyGrown = locallyGrown;
    }

    public Long getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(Long harvestDate) {
        this.harvestDate = harvestDate;
    }

    public Long getBestBefore() {
        return bestBefore;
    }

    public void setBestBefore(Long bestBefore) {
        this.bestBefore = bestBefore;
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

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public double getFarmerRating() {
        return farmerRating;
    }

    public void setFarmerRating(double farmerRating) {
        this.farmerRating = farmerRating;
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stockQuantity > 0;
    }

    public boolean canOrder(double quantity) {
        return isAvailable() && quantity >= minOrderQuantity && quantity <= stockQuantity;
    }

    public String getFormattedPrice() {
        return unit.formatPrice(price);
    }

    public String getFormattedStock() {
        return unit.format(stockQuantity);
    }

    public double calculateTotal(double quantity) {
        return price * quantity;
    }
}
