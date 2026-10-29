package com.farm2market.server.db;

import com.farm2market.core.enums.Category;
import com.farm2market.core.enums.ProductStatus;
import com.farm2market.core.enums.Unit;
import com.farm2market.core.model.Product;
import com.farm2market.core.repository.ProductRepository;
import com.farm2market.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepository {

    private final DatabaseConnection db;

    public JdbcProductRepository() {
        this.db = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<Product> findById(String id) {
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.product_id = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        return findAll(100, 0);
    }

    @Override
    public List<Product> findAll(int limit, int offset) {
        List<Product> products = new ArrayList<>();
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            ORDER BY p.created_at DESC LIMIT ? OFFSET ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findByFarmerId(String farmerId) {
        return findByFarmerId(farmerId, 100, 0);
    }

    @Override
    public List<Product> findByFarmerId(String farmerId, int limit, int offset) {
        List<Product> products = new ArrayList<>();
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.farmer_id = ? AND p.status != 'DELETED'
            ORDER BY p.created_at DESC LIMIT ? OFFSET ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmerId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return findByCategory(category, 100, 0);
    }

    @Override
    public List<Product> findByCategory(Category category, int limit, int offset) {
        List<Product> products = new ArrayList<>();
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.category = ? AND p.status = 'ACTIVE'
            ORDER BY p.created_at DESC LIMIT ? OFFSET ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.name());
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        List<Product> products = new ArrayList<>();
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.status = ?
            ORDER BY p.created_at DESC
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findActive(int limit, int offset) {
        List<Product> products = new ArrayList<>();
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.status = 'ACTIVE'
            ORDER BY p.created_at DESC LIMIT ? OFFSET ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findAvailable(int limit, int offset) {
        List<Product> products = new ArrayList<>();
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.status = 'ACTIVE' AND p.stock_quantity > 0
            ORDER BY p.created_at DESC LIMIT ? OFFSET ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> search(String query, int limit, int offset) {
        List<Product> products = new ArrayList<>();
        String sql = """
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.status = 'ACTIVE' AND (p.name LIKE ? OR p.description LIKE ?)
            ORDER BY p.created_at DESC LIMIT ? OFFSET ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> searchWithFilters(String query, Category category,
                                            Double minPrice, Double maxPrice,
                                            Boolean inStock, int limit, int offset) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.status = 'ACTIVE'
            """);

        List<Object> params = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        }
        if (category != null) {
            sql.append(" AND p.category = ?");
            params.add(category.name());
        }
        if (minPrice != null) {
            sql.append(" AND p.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.price <= ?");
            params.add(maxPrice);
        }
        if (inStock != null && inStock) {
            sql.append(" AND p.stock_quantity > 0");
        }

        sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findFeatured(int limit) {
        return findAvailable(limit, 0);
    }

    @Override
    public List<Product> findRecent(int limit) {
        return findActive(limit, 0);
    }

    @Override
    public List<Product> findByIds(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> products = new ArrayList<>();
        String placeholders = String.join(",", productIds.stream().map(id -> "?").toList());
        String sql = String.format("""
            SELECT p.*, u.name as farmer_name, fp.farm_name, fp.rating_sum, fp.rating_count
            FROM products p
            JOIN users u ON p.farmer_id = u.user_id
            LEFT JOIN farmer_profiles fp ON p.farmer_id = fp.farmer_id
            WHERE p.product_id IN (%s)
            """, placeholders);

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < productIds.size(); i++) {
                stmt.setString(i + 1, productIds.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public Product save(Product product) {
        if (product.getProductId() == null) {
            product.setProductId(IdGenerator.generateProductId());
        }
        product.setCreatedAt(System.currentTimeMillis());
        product.setUpdatedAt(System.currentTimeMillis());

        String sql = """
            INSERT INTO products (product_id, farmer_id, category, name, description,
                                  price, unit, stock_quantity, min_order_quantity, status,
                                  is_seasonal, is_organic, is_locally_grown,
                                  harvest_date, best_before, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getProductId());
            stmt.setString(2, product.getFarmerId());
            stmt.setString(3, product.getCategory().name());
            stmt.setString(4, product.getName());
            stmt.setString(5, product.getDescription());
            stmt.setDouble(6, product.getPrice());
            stmt.setString(7, product.getUnit().name());
            stmt.setDouble(8, product.getStockQuantity());
            stmt.setDouble(9, product.getMinOrderQuantity());
            stmt.setString(10, product.getStatus().name());
            stmt.setInt(11, product.isSeasonal() ? 1 : 0);
            stmt.setInt(12, product.isOrganic() ? 1 : 0);
            stmt.setInt(13, product.isLocallyGrown() ? 1 : 0);
            stmt.setObject(14, product.getHarvestDate());
            stmt.setObject(15, product.getBestBefore());
            stmt.setLong(16, product.getCreatedAt());
            stmt.setLong(17, product.getUpdatedAt());
            stmt.executeUpdate();

            saveProductImages(product);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    private void saveProductImages(Product product) throws SQLException {
        if (product.getImageUrls() == null || product.getImageUrls().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO product_images (image_id, product_id, image_url, is_primary, display_order, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int order = 0;
            for (String imageUrl : product.getImageUrls()) {
                stmt.setString(1, IdGenerator.generateUUID());
                stmt.setString(2, product.getProductId());
                stmt.setString(3, imageUrl);
                stmt.setInt(4, order == 0 ? 1 : 0);
                stmt.setInt(5, order);
                stmt.setLong(6, System.currentTimeMillis());
                stmt.addBatch();
                order++;
            }
            stmt.executeBatch();
        }
    }

    @Override
    public Product update(Product product) {
        product.setUpdatedAt(System.currentTimeMillis());

        String sql = """
            UPDATE products SET category = ?, name = ?, description = ?,
                                price = ?, unit = ?, stock_quantity = ?, status = ?,
                                is_seasonal = ?, is_organic = ?, is_locally_grown = ?,
                                harvest_date = ?, best_before = ?, updated_at = ?
            WHERE product_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getCategory().name());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getPrice());
            stmt.setString(5, product.getUnit().name());
            stmt.setDouble(6, product.getStockQuantity());
            stmt.setString(7, product.getStatus().name());
            stmt.setInt(8, product.isSeasonal() ? 1 : 0);
            stmt.setInt(9, product.isOrganic() ? 1 : 0);
            stmt.setInt(10, product.isLocallyGrown() ? 1 : 0);
            stmt.setObject(11, product.getHarvestDate());
            stmt.setObject(12, product.getBestBefore());
            stmt.setLong(13, product.getUpdatedAt());
            stmt.setString(14, product.getProductId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    @Override
    public void delete(String id) {
        updateStatus(id, ProductStatus.DELETED);
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM products WHERE product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM products WHERE status != 'DELETED'";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void updateStock(String productId, double newQuantity) {
        String sql = "UPDATE products SET stock_quantity = ?, updated_at = ? WHERE product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newQuantity);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decrementStock(String productId, double quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ?, updated_at = ? WHERE product_id = ? AND stock_quantity >= ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantity);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, productId);
            stmt.setDouble(4, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void incrementStock(String productId, double quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ?, updated_at = ? WHERE product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantity);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateStatus(String productId, ProductStatus status) {
        String sql = "UPDATE products SET status = ?, updated_at = ? WHERE product_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long countByFarmerId(String farmerId) {
        String sql = "SELECT COUNT(*) FROM products WHERE farmer_id = ? AND status != 'DELETED'";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setFarmerId(rs.getString("farmer_id"));
        product.setCategory(Category.valueOf(rs.getString("category")));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setUnit(Unit.valueOf(rs.getString("unit")));
        product.setStockQuantity(rs.getDouble("stock_quantity"));
        product.setMinOrderQuantity(rs.getDouble("min_order_quantity"));
        product.setStatus(ProductStatus.valueOf(rs.getString("status")));
        product.setSeasonal(rs.getInt("is_seasonal") == 1);
        product.setOrganic(rs.getInt("is_organic") == 1);
        product.setLocallyGrown(rs.getInt("is_locally_grown") == 1);
        product.setHarvestDate(rs.getObject("harvest_date") != null ? rs.getLong("harvest_date") : null);
        product.setBestBefore(rs.getObject("best_before") != null ? rs.getLong("best_before") : null);
        product.setCreatedAt(rs.getLong("created_at"));
        product.setUpdatedAt(rs.getLong("updated_at"));

        product.setFarmerName(rs.getString("farmer_name"));
        product.setFarmName(rs.getString("farm_name"));
        double ratingSum = rs.getDouble("rating_sum");
        int ratingCount = rs.getInt("rating_count");
        product.setFarmerRating(ratingCount > 0 ? ratingSum / ratingCount : 0);

        loadProductImages(product);

        return product;
    }

    private void loadProductImages(Product product) {
        String sql = "SELECT image_url FROM product_images WHERE product_id = ? ORDER BY display_order";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getProductId());
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> images = new ArrayList<>();
                while (rs.next()) {
                    images.add(rs.getString("image_url"));
                }
                product.setImageUrls(images);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
