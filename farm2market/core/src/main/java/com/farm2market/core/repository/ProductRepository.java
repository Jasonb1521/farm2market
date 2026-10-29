package com.farm2market.core.repository;

import com.farm2market.core.enums.Category;
import com.farm2market.core.enums.ProductStatus;
import com.farm2market.core.model.Product;
import java.util.List;

public interface ProductRepository extends Repository<Product, String> {

    List<Product> findByFarmerId(String farmerId);

    List<Product> findByFarmerId(String farmerId, int limit, int offset);

    List<Product> findByCategory(Category category);

    List<Product> findByCategory(Category category, int limit, int offset);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findActive(int limit, int offset);

    List<Product> findAvailable(int limit, int offset);

    List<Product> search(String query, int limit, int offset);

    List<Product> searchWithFilters(String query, Category category,
                                     Double minPrice, Double maxPrice,
                                     Boolean inStock, int limit, int offset);

    List<Product> findFeatured(int limit);

    List<Product> findRecent(int limit);

    List<Product> findByIds(List<String> productIds);

    void updateStock(String productId, double newQuantity);

    void decrementStock(String productId, double quantity);

    void incrementStock(String productId, double quantity);

    void updateStatus(String productId, ProductStatus status);

    long countByFarmerId(String farmerId);

    long countActive();
}
