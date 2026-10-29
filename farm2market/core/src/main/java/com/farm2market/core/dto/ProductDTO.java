package com.farm2market.core.dto;

import com.farm2market.core.enums.Category;
import com.farm2market.core.enums.ProductStatus;
import com.farm2market.core.enums.Unit;
import com.farm2market.core.model.Product;
import java.util.List;

public class ProductDTO {
    private String productId;
    private String farmerId;
    private String farmerName;
    private String farmName;
    private double farmerRating;
    private Category category;
    private String name;
    private String description;
    private double price;
    private Unit unit;
    private double stockQuantity;
    private ProductStatus status;
    private List<String> imageUrls;
    private boolean seasonal;
    private boolean organic;
    private boolean locallyGrown;
    private Long harvestDate;
    private Long bestBefore;
    private long createdAt;

    public ProductDTO() {}

    public static ProductDTO fromProduct(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setFarmerId(product.getFarmerId());
        dto.setFarmerName(product.getFarmerName());
        dto.setFarmName(product.getFarmName());
        dto.setFarmerRating(product.getFarmerRating());
        dto.setCategory(product.getCategory());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setUnit(product.getUnit());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setStatus(product.getStatus());
        dto.setImageUrls(product.getImageUrls());
        dto.setSeasonal(product.isSeasonal());
        dto.setOrganic(product.isOrganic());
        dto.setLocallyGrown(product.isLocallyGrown());
        dto.setHarvestDate(product.getHarvestDate());
        dto.setBestBefore(product.getBestBefore());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }

    public Product toProduct() {
        Product product = new Product();
        product.setProductId(productId);
        product.setFarmerId(farmerId);
        product.setCategory(category);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setUnit(unit);
        product.setStockQuantity(stockQuantity);
        product.setStatus(status != null ? status : ProductStatus.ACTIVE);
        product.setImageUrls(imageUrls);
        product.setSeasonal(seasonal);
        product.setOrganic(organic);
        product.setLocallyGrown(locallyGrown);
        product.setHarvestDate(harvestDate);
        product.setBestBefore(bestBefore);
        return product;
    }

    // Getters and setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public double getFarmerRating() { return farmerRating; }
    public void setFarmerRating(double farmerRating) { this.farmerRating = farmerRating; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public double getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(double stockQuantity) { this.stockQuantity = stockQuantity; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public boolean isSeasonal() { return seasonal; }
    public void setSeasonal(boolean seasonal) { this.seasonal = seasonal; }
    public boolean isOrganic() { return organic; }
    public void setOrganic(boolean organic) { this.organic = organic; }
    public boolean isLocallyGrown() { return locallyGrown; }
    public void setLocallyGrown(boolean locallyGrown) { this.locallyGrown = locallyGrown; }
    public Long getHarvestDate() { return harvestDate; }
    public void setHarvestDate(Long harvestDate) { this.harvestDate = harvestDate; }
    public Long getBestBefore() { return bestBefore; }
    public void setBestBefore(Long bestBefore) { this.bestBefore = bestBefore; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getFormattedPrice() {
        return unit != null ? unit.formatPrice(price) : String.format("₹%.2f", price);
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stockQuantity > 0;
    }
}
