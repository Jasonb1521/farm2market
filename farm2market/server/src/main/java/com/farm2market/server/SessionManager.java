package com.farm2market.server;

import com.farm2market.core.model.User;
import com.farm2market.core.util.AppConstants;
import com.farm2market.core.util.IdGenerator;
import com.farm2market.server.db.DatabaseConnection;
import com.farm2market.server.db.JdbcUserRepository;

import java.sql.*;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static SessionManager instance;
    private final DatabaseConnection db;
    private final JdbcUserRepository userRepository;
    private final ConcurrentHashMap<String, CachedSession> sessionCache;

    private SessionManager() {
        this.db = DatabaseConnection.getInstance();
        this.userRepository = new JdbcUserRepository();
        this.sessionCache = new ConcurrentHashMap<>();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String createSession(String userId) {
        String sessionId = IdGenerator.generateUUID();
        String token = IdGenerator.generateSessionToken();
        long now = System.currentTimeMillis();
        long expiresAt = now + AppConstants.SESSION_DURATION_MS;

        String sql = """
            INSERT INTO sessions (session_id, user_id, token, created_at, expires_at, is_active)
            VALUES (?, ?, ?, ?, ?, 1)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, userId);
            stmt.setString(3, token);
            stmt.setLong(4, now);
            stmt.setLong(5, expiresAt);
            stmt.executeUpdate();

            // Cache the session
            userRepository.findById(userId).ifPresent(user ->
                sessionCache.put(token, new CachedSession(userId, user, expiresAt))
            );

            return token;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Optional<User> validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }

        // Check cache first
        CachedSession cached = sessionCache.get(token);
        if (cached != null) {
            if (System.currentTimeMillis() < cached.expiresAt) {
                return Optional.of(cached.user);
            } else {
                sessionCache.remove(token);
            }
        }

        // Check database
        String sql = """
            SELECT user_id, expires_at FROM sessions
            WHERE token = ? AND is_active = 1 AND expires_at > ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setLong(2, System.currentTimeMillis());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    long expiresAt = rs.getLong("expires_at");

                    Optional<User> userOpt = userRepository.findById(userId);
                    userOpt.ifPresent(user ->
                        sessionCache.put(token, new CachedSession(userId, user, expiresAt))
                    );
                    return userOpt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void invalidateSession(String token) {
        sessionCache.remove(token);

        String sql = "UPDATE sessions SET is_active = 0 WHERE token = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void invalidateAllSessions(String userId) {
        sessionCache.values().removeIf(session -> session.userId.equals(userId));

        String sql = "UPDATE sessions SET is_active = 0 WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessionCache.values().removeIf(session -> session.expiresAt < now);

        String sql = "DELETE FROM sessions WHERE expires_at < ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, now);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("[Session] Cleaned up " + deleted + " expired sessions");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getExpiresAt(String token) {
        CachedSession cached = sessionCache.get(token);
        if (cached != null) {
            return cached.expiresAt;
        }
        return System.currentTimeMillis() + AppConstants.SESSION_DURATION_MS;
    }

    private static class CachedSession {
        final String userId;
        final User user;
        final long expiresAt;

        CachedSession(String userId, User user, long expiresAt) {
            this.userId = userId;
            this.user = user;
            this.expiresAt = expiresAt;
        }
    }
}
