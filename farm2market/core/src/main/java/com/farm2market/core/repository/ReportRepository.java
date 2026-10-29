package com.farm2market.core.repository;

import com.farm2market.core.enums.ReportStatus;
import com.farm2market.core.enums.ReportTargetType;
import com.farm2market.core.model.Report;
import java.util.List;

public interface ReportRepository extends Repository<Report, String> {

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByStatus(ReportStatus status, int limit, int offset);

    List<Report> findByTargetType(ReportTargetType targetType);

    List<Report> findByTargetId(String targetId);

    List<Report> findByReporterId(String reporterId);

    List<Report> findOpen();

    List<Report> findOpen(int limit, int offset);

    void updateStatus(String reportId, ReportStatus status, String adminId, String note);

    long countByStatus(ReportStatus status);

    long countOpen();

    boolean existsByReporterAndTarget(String reporterId, ReportTargetType targetType, String targetId);
}
