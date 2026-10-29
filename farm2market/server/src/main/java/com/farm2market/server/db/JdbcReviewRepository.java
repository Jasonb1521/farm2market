package com.farm2market.server.db;

import com.farm2market.core.model.Review;
import com.farm2market.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcReviewRepository {

    private final DatabaseConnection db;

    public JdbcReviewRepository() {
        this.db = DatabaseConnection.getInstance();
    }

    public Optional<Review> findById(String reviewId) {
        String sql = """
            SELECT r.*, u.name as customer_name
            FROM reviews r
            JOIN users u ON r.customer_id = u.user_id
            WHERE r.review_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reviewId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Review> findByOrderId(String orderId) {
        String sql = """
            SELECT r.*, u.name as customer_name
            FROM reviews r
            JOIN users u ON r.customer_id = u.user_id
            WHERE r.order_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Review> findByFarmerId(String farmerId) {
        String sql = """
            SELECT r.*, u.name as customer_name
            FROM reviews r
            JOIN users u ON r.customer_id = u.user_id
            WHERE r.farmer_id = ?
            ORDER BY r.created_at DESC
            """;

        List<Review> reviews = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(mapReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public List<Review> findByCustomerId(String customerId) {
        String sql = """
            SELECT r.*, u.name as customer_name
            FROM reviews r
            JOIN users u ON r.customer_id = u.user_id
            WHERE r.customer_id = ?
            ORDER BY r.created_at DESC
            """;

        List<Review> reviews = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(mapReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public Review save(Review review) {
        String sql = """
            INSERT INTO reviews (review_id, order_id, customer_id, farmer_id, rating, comment, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (review.getReviewId() == null) {
                review.setReviewId(IdGenerator.generateUUID());
            }
            if (review.getCreatedAt() == 0) {
                review.setCreatedAt(System.currentTimeMillis());
            }

            stmt.setString(1, review.getReviewId());
            stmt.setString(2, review.getOrderId());
            stmt.setString(3, review.getCustomerId());
            stmt.setString(4, review.getFarmerId());
            stmt.setInt(5, review.getRating());
            stmt.setString(6, review.getComment());
            stmt.setLong(7, review.getCreatedAt());

            stmt.executeUpdate();

            updateFarmerRating(conn, review.getFarmerId());

            return review;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save review", e);
        }
    }

    private void updateFarmerRating(Connection conn, String farmerId) throws SQLException {
        String sql = """
            UPDATE farmer_profiles
            SET rating_sum = (SELECT COALESCE(SUM(rating), 0) FROM reviews WHERE farmer_id = ?),
                rating_count = (SELECT COUNT(*) FROM reviews WHERE farmer_id = ?)
            WHERE farmer_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmerId);
            stmt.setString(2, farmerId);
            stmt.setString(3, farmerId);
            stmt.executeUpdate();
        }
    }

    public double getAverageRating(String farmerId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE farmer_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getReviewCount(String farmerId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE farmer_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Review mapReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getString("review_id"));
        review.setOrderId(rs.getString("order_id"));
        review.setCustomerId(rs.getString("customer_id"));
        review.setFarmerId(rs.getString("farmer_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getLong("created_at"));

        try {
            review.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException ignored) {}

        return review;
    }
}
