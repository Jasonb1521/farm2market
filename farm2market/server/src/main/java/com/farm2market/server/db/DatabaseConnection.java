package com.farm2market.server.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final HikariDataSource dataSource;
    private final String dbType;

    private DatabaseConnection() {
        Properties props = loadProperties();
        this.dbType = props.getProperty("db.type", "sqlite");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url", "jdbc:sqlite:farm2market.db"));

        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        if (username != null && !username.isEmpty()) {
            config.setUsername(username);
            config.setPassword(password);
        }

        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.min.idle", "2")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.max.wait", "10000")));
        config.setAutoCommit(true);

        if ("sqlite".equalsIgnoreCase(dbType)) {
            config.addDataSourceProperty("foreign_keys", "true");
        }

        this.dataSource = new HikariDataSource(config);
        System.out.println("[DB] Connection pool initialized for " + dbType);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("[DB] Could not load server.properties: " + e.getMessage());
        }
        return props;
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public String getDbType() {
        return dbType;
    }

    public void initializeSchema() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                System.err.println("[DB] schema.sql not found");
                return;
            }

            String schema = new String(is.readAllBytes());
            System.out.println("[DB] Schema file loaded, " + schema.length() + " bytes");
            executeBatchWithDebug(schema);
            System.out.println("[DB] Schema initialized successfully");
        } catch (IOException | SQLException e) {
            System.err.println("[DB] Failed to initialize schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeBatchWithDebug(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String[] statements = sql.split(";");
            System.out.println("[DB] Found " + statements.length + " statements to execute");
            int executed = 0;
            int errors = 0;
            for (int i = 0; i < statements.length; i++) {
                String trimmed = statements[i].trim();
                // Strip leading comment lines
                while (trimmed.startsWith("--")) {
                    int newlineIdx = trimmed.indexOf('\n');
                    if (newlineIdx == -1) {
                        trimmed = "";
                        break;
                    }
                    trimmed = trimmed.substring(newlineIdx + 1).trim();
                }
                if (!trimmed.isEmpty()) {
                    String preview = trimmed.split("\\n")[0];
                    if (preview.length() > 60) preview = preview.substring(0, 60);
                    try {
                        stmt.execute(trimmed);
                        executed++;
                        if (trimmed.toUpperCase().startsWith("CREATE TABLE")) {
                            System.out.println("[DB] Created: " + preview);
                        }
                    } catch (SQLException e) {
                        errors++;
                        if (!e.getMessage().contains("already exists")) {
                            System.err.println("[DB] Error on stmt " + i + ": " + preview);
                            System.err.println("[DB] " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("[DB] Executed " + executed + " statements, " + errors + " errors");
        }
    }

    public void loadSeedData() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("seed_data.sql")) {
            if (is == null) {
                System.err.println("[DB] seed_data.sql not found");
                return;
            }

            String seedData = new String(is.readAllBytes());
            executeBatch(seedData);
            System.out.println("[DB] Seed data loaded successfully");
        } catch (IOException | SQLException e) {
            System.err.println("[DB] Failed to load seed data: " + e.getMessage());
        }
    }

    private void executeBatch(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String[] statements = sql.split(";");
            int executed = 0;
            int errors = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                // Strip leading comment lines
                while (trimmed.startsWith("--")) {
                    int newlineIdx = trimmed.indexOf('\n');
                    if (newlineIdx == -1) {
                        trimmed = "";
                        break;
                    }
                    trimmed = trimmed.substring(newlineIdx + 1).trim();
                }
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                        executed++;
                    } catch (SQLException e) {
                        errors++;
                        if (!e.getMessage().contains("already exists") &&
                            !e.getMessage().contains("UNIQUE constraint")) {
                            System.err.println("[DB] Error executing: " + trimmed.substring(0, Math.min(60, trimmed.length())));
                            System.err.println("[DB] " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("[DB] Executed " + executed + " statements, " + errors + " errors");
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DB] Connection pool closed");
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
