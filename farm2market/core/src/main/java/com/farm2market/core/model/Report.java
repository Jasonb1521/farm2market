package com.farm2market.core.model;

import com.farm2market.core.enums.ReportStatus;
import com.farm2market.core.enums.ReportTargetType;

public class Report implements Entity<String> {
    private String reportId;
    private String reporterId;
    private ReportTargetType targetType;
    private String targetId;
    private String reason;
    private ReportStatus status;
    private String adminId;
    private String adminNote;
    private long createdAt;
    private Long resolvedAt;

    // Transient fields for display
    private transient String reporterName;
    private transient String adminName;
    private transient String targetDescription;

    public Report() {
        this.status = ReportStatus.OPEN;
        this.createdAt = System.currentTimeMillis();
    }

    public Report(String reportId, String reporterId, ReportTargetType targetType,
                  String targetId, String reason) {
        this();
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }

    @Override
    public String getId() {
        return reportId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(ReportTargetType targetType) {
        this.targetType = targetType;
    }

    public void setTargetType(String targetType) {
        try {
            this.targetType = ReportTargetType.valueOf(targetType);
        } catch (Exception e) {
            this.targetType = ReportTargetType.USER;
        }
    }

    public String getTargetTypeString() {
        return targetType != null ? targetType.name() : "";
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
        if (status.isClosed() && resolvedAt == null) {
            this.resolvedAt = System.currentTimeMillis();
        }
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Long resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }

    public boolean isOpen() {
        return status == ReportStatus.OPEN;
    }

    public boolean isClosed() {
        return status.isClosed();
    }

    public void resolve(String adminId, String note, boolean actionTaken) {
        this.adminId = adminId;
        this.adminNote = note;
        this.status = actionTaken ? ReportStatus.RESOLVED : ReportStatus.DISMISSED;
        this.resolvedAt = System.currentTimeMillis();
    }
}
