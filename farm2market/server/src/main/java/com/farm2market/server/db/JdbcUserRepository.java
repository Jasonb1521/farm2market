package com.farm2market.server.db;

import com.farm2market.core.enums.FulfilmentMethod;
import com.farm2market.core.enums.Role;
import com.farm2market.core.enums.UserStatus;
import com.farm2market.core.model.*;
import com.farm2market.core.repository.UserRepository;
import com.farm2market.core.util.IdGenerator;

import java.sql.*;
import java.util.*;

public class JdbcUserRepository implements UserRepository {

    private final DatabaseConnection db;

    public JdbcUserRepository() {
        this.db = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByMobile(String mobile) {
        String sql = "SELECT * FROM users WHERE mobile = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mobile);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmailOrMobile(String emailOrMobile) {
        String sql = "SELECT * FROM users WHERE email = ? OR mobile = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailOrMobile.toLowerCase());
            stmt.setString(2, emailOrMobile);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return findAll(100, 0);
    }

    @Override
    public List<User> findAll(int limit, int offset) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public List<User> findByRole(Role role) {
        return findByRole(role, 100, 0);
    }

    @Override
    public List<User> findByRole(Role role, int limit, int offset) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User save(User user) {
        if (user.getUserId() == null) {
            user.setUserId(IdGenerator.generateUserId());
        }
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        String sql = """
            INSERT INTO users (user_id, email, mobile, password_hash, name, role,
                               profile_image_url, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getEmail().toLowerCase());
            stmt.setString(3, user.getMobile());
            stmt.setString(4, ""); // Password hash set separately
            stmt.setString(5, user.getName());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getProfileImageUrl());
            stmt.setString(8, user.getStatus().name());
            stmt.setLong(9, user.getCreatedAt());
            stmt.setLong(10, user.getUpdatedAt());
            stmt.executeUpdate();

            if (user instanceof Farmer farmer) {
                saveFarmerProfile(farmer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User saveWithPassword(User user, String passwordHash) {
        if (user.getUserId() == null) {
            user.setUserId(IdGenerator.generateUserId());
        }
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        String sql = """
            INSERT INTO users (user_id, email, mobile, password_hash, name, role,
                               profile_image_url, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getEmail().toLowerCase());
            stmt.setString(3, user.getMobile());
            stmt.setString(4, passwordHash);
            stmt.setString(5, user.getName());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getProfileImageUrl());
            stmt.setString(8, user.getStatus().name());
            stmt.setLong(9, user.getCreatedAt());
            stmt.setLong(10, user.getUpdatedAt());
            stmt.executeUpdate();

            if (user instanceof Farmer farmer) {
                saveFarmerProfile(farmer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    private void saveFarmerProfile(Farmer farmer) throws SQLException {
        String sql = """
            INSERT INTO farmer_profiles (farmer_id, farm_name, location, description,
                                         fulfilment_options, rating_sum, rating_count,
                                         verified, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmer.getUserId());
            stmt.setString(2, farmer.getFarmName());
            stmt.setString(3, farmer.getLocation());
            stmt.setString(4, farmer.getDescription());
            stmt.setString(5, formatFulfilmentOptions(farmer.getFulfilmentOptions()));
            stmt.setDouble(6, farmer.getRatingSum());
            stmt.setInt(7, farmer.getRatingCount());
            stmt.setInt(8, farmer.isVerified() ? 1 : 0);
            stmt.setLong(9, System.currentTimeMillis());
            stmt.setLong(10, System.currentTimeMillis());
            stmt.executeUpdate();
        }
    }

    @Override
    public User update(User user) {
        user.setUpdatedAt(System.currentTimeMillis());

        String sql = """
            UPDATE users SET email = ?, mobile = ?, name = ?,
                             profile_image_url = ?, status = ?, updated_at = ?
            WHERE user_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail().toLowerCase());
            stmt.setString(2, user.getMobile());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getProfileImageUrl());
            stmt.setString(5, user.getStatus().name());
            stmt.setLong(6, user.getUpdatedAt());
            stmt.setString(7, user.getUserId());
            stmt.executeUpdate();

            if (user instanceof Farmer farmer) {
                updateFarmerProfile(farmer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    private void updateFarmerProfile(Farmer farmer) throws SQLException {
        String sql = """
            UPDATE farmer_profiles SET farm_name = ?, location = ?, description = ?,
                                       farming_practices = ?, service_area = ?,
                                       fulfilment_options = ?, updated_at = ?
            WHERE farmer_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmer.getFarmName());
            stmt.setString(2, farmer.getLocation());
            stmt.setString(3, farmer.getDescription());
            stmt.setString(4, farmer.getFarmingPractices());
            stmt.setString(5, farmer.getServiceArea());
            stmt.setString(6, formatFulfilmentOptions(farmer.getFulfilmentOptions()));
            stmt.setLong(7, System.currentTimeMillis());
            stmt.setString(8, farmer.getUserId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(String id) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UserStatus.DELETED.name());
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
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
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existsByMobile(String mobile) {
        String sql = "SELECT 1 FROM users WHERE mobile = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mobile);
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
        String sql = "SELECT COUNT(*) FROM users WHERE status != 'DELETED'";
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
    public long countByRole(Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ? AND status != 'DELETED'";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
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
    public List<User> search(String query, int limit) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE (name LIKE ? OR email LIKE ?) AND status != 'DELETED' LIMIT ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setInt(3, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void updatePassword(String userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProfileImage(String userId, String imageUrl) {
        String sql = "UPDATE users SET profile_image_url = ?, updated_at = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, imageUrl);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPasswordHash(String userId) {
        String sql = "SELECT password_hash FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPasswordHashByEmail(String email) {
        String sql = "SELECT password_hash FROM users WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        User user;

        switch (role) {
            case FARMER -> {
                Farmer farmer = new Farmer();
                user = farmer;
                loadFarmerProfile(farmer);
            }
            case ADMIN -> user = new Admin();
            default -> user = new Customer();
        }

        user.setUserId(rs.getString("user_id"));
        user.setEmail(rs.getString("email"));
        user.setMobile(rs.getString("mobile"));
        user.setName(rs.getString("name"));
        user.setRole(role);
        user.setProfileImageUrl(rs.getString("profile_image_url"));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));
        user.setCreatedAt(rs.getLong("created_at"));
        user.setUpdatedAt(rs.getLong("updated_at"));

        return user;
    }

    private void loadFarmerProfile(Farmer farmer) {
        String sql = "SELECT * FROM farmer_profiles WHERE farmer_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, farmer.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    farmer.setFarmName(rs.getString("farm_name"));
                    farmer.setLocation(rs.getString("location"));
                    farmer.setDescription(rs.getString("description"));
                    farmer.setFarmingPractices(rs.getString("farming_practices"));
                    farmer.setServiceArea(rs.getString("service_area"));
                    farmer.setFulfilmentOptions(parseFulfilmentOptions(rs.getString("fulfilment_options")));
                    farmer.setRatingSum(rs.getDouble("rating_sum"));
                    farmer.setRatingCount(rs.getInt("rating_count"));
                    farmer.setVerified(rs.getInt("verified") == 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String formatFulfilmentOptions(Set<FulfilmentMethod> options) {
        if (options == null || options.isEmpty()) {
            return "PICKUP";
        }
        return String.join(",", options.stream().map(Enum::name).toList());
    }

    private Set<FulfilmentMethod> parseFulfilmentOptions(String options) {
        Set<FulfilmentMethod> set = new HashSet<>();
        if (options != null && !options.isEmpty()) {
            for (String opt : options.split(",")) {
                try {
                    set.add(FulfilmentMethod.valueOf(opt.trim()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (set.isEmpty()) {
            set.add(FulfilmentMethod.PICKUP);
        }
        return set;
    }
}
