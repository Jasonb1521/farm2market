package com.farm2market.server.db;

import com.farm2market.core.enums.ReportStatus;
import com.farm2market.core.model.Report;
import com.farm2market.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcReportRepository {

    private final DatabaseConnection db;

    public JdbcReportRepository() {
        this.db = DatabaseConnection.getInstance();
    }

    public Optional<Report> findById(String reportId) {
        String sql = """
            SELECT r.*, u.name as reporter_name
            FROM reports r
            JOIN users u ON r.reporter_id = u.user_id
            WHERE r.report_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reportId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Report> findAll() {
        String sql = """
            SELECT r.*, u.name as reporter_name
            FROM reports r
            JOIN users u ON r.reporter_id = u.user_id
            ORDER BY r.created_at DESC
            """;

        List<Report> reports = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reports.add(mapReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public List<Report> findByStatus(ReportStatus status) {
        String sql = """
            SELECT r.*, u.name as reporter_name
            FROM reports r
            JOIN users u ON r.reporter_id = u.user_id
            WHERE r.status = ?
            ORDER BY r.created_at DESC
            """;

        List<Report> reports = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(mapReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public List<Report> findByReporterId(String reporterId) {
        String sql = """
            SELECT r.*, u.name as reporter_name
            FROM reports r
            JOIN users u ON r.reporter_id = u.user_id
            WHERE r.reporter_id = ?
            ORDER BY r.created_at DESC
            """;

        List<Report> reports = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reporterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(mapReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public List<Report> findByTargetUser(String userId) {
        String sql = """
            SELECT r.*, u.name as reporter_name
            FROM reports r
            JOIN users u ON r.reporter_id = u.user_id
            WHERE (r.target_type = 'USER' AND r.target_id = ?)
               OR (r.target_type = 'LISTING' AND r.target_id IN
                   (SELECT product_id FROM products WHERE farmer_id = ?))
            ORDER BY r.created_at DESC
            """;

        List<Report> reports = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(mapReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public int countByStatus(ReportStatus status) {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Report save(Report report) {
        String sql = """
            INSERT INTO reports (report_id, reporter_id, target_type, target_id, reason, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (report.getReportId() == null) {
                report.setReportId(IdGenerator.generateUUID());
            }
            if (report.getCreatedAt() == 0) {
                report.setCreatedAt(System.currentTimeMillis());
            }
            if (report.getStatus() == null) {
                report.setStatus(ReportStatus.OPEN);
            }

            stmt.setString(1, report.getReportId());
            stmt.setString(2, report.getReporterId());
            stmt.setString(3, report.getTargetTypeString());
            stmt.setString(4, report.getTargetId());
            stmt.setString(5, report.getReason());
            stmt.setString(6, report.getStatus().name());
            stmt.setLong(7, report.getCreatedAt());

            stmt.executeUpdate();
            return report;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save report", e);
        }
    }

    public void resolve(String reportId, String adminId, ReportStatus status, String adminNote) {
        String sql = """
            UPDATE reports
            SET status = ?, admin_id = ?, admin_note = ?, resolved_at = ?
            WHERE report_id = ?
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, adminId);
            stmt.setString(3, adminNote);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setString(5, reportId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Report mapReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getString("report_id"));
        report.setReporterId(rs.getString("reporter_id"));
        report.setTargetType(rs.getString("target_type"));
        report.setTargetId(rs.getString("target_id"));
        report.setReason(rs.getString("reason"));
        report.setStatus(ReportStatus.valueOf(rs.getString("status")));
        report.setAdminId(rs.getString("admin_id"));
        report.setAdminNote(rs.getString("admin_note"));
        report.setCreatedAt(rs.getLong("created_at"));

        long resolvedAt = rs.getLong("resolved_at");
        if (!rs.wasNull()) {
            report.setResolvedAt(resolvedAt);
        }

        try {
            report.setReporterName(rs.getString("reporter_name"));
        } catch (SQLException ignored) {}

        return report;
    }
}
