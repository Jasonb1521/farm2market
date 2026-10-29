package com.farm2market.server.io;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AuditLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "audit.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static AuditLogger instance;
    private final BlockingQueue<LogEntry> logQueue;
    private final Thread writerThread;
    private volatile boolean running;

    private AuditLogger() {
        this.logQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.writerThread = new Thread(this::processLogs, "AuditLogger");
        this.writerThread.setDaemon(true);

        new File(LOG_DIR).mkdirs();
        this.writerThread.start();
    }

    public static synchronized AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    public void log(String eventType, String userId, String targetType, String targetId, String details) {
        logQueue.offer(new LogEntry(
            LocalDateTime.now(),
            eventType,
            userId,
            targetType,
            targetId,
            details,
            null
        ));
    }

    public void log(String eventType, String userId, String details) {
        log(eventType, userId, null, null, details);
    }

    public void logWithIp(String eventType, String userId, String details, String ipAddress) {
        logQueue.offer(new LogEntry(
            LocalDateTime.now(),
            eventType,
            userId,
            null,
            null,
            details,
            ipAddress
        ));
    }

    public void logLogin(String userId, String ipAddress, boolean success) {
        log(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED", userId, "SESSION", null,
            success ? "User logged in" : "Login attempt failed");
    }

    public void logLogout(String userId) {
        log("LOGOUT", userId, "SESSION", null, "User logged out");
    }

    public void logRegister(String userId, String role) {
        log("REGISTER", userId, "USER", userId, "New " + role + " registered");
    }

    public void logOrderCreated(String userId, String orderId, double total) {
        log("ORDER_CREATED", userId, "ORDER", orderId,
            String.format("Order created, total: ₹%.2f", total));
    }

    public void logOrderStatusChange(String userId, String orderId, String oldStatus, String newStatus) {
        log("ORDER_STATUS_CHANGE", userId, "ORDER", orderId,
            String.format("Status changed: %s -> %s", oldStatus, newStatus));
    }

    public void logProductCreated(String userId, String productId, String productName) {
        log("PRODUCT_CREATED", userId, "PRODUCT", productId,
            "Product created: " + productName);
    }

    public void logProductUpdated(String userId, String productId, String changes) {
        log("PRODUCT_UPDATED", userId, "PRODUCT", productId, changes);
    }

    public void logProductDeleted(String userId, String productId, String productName) {
        log("PRODUCT_DELETED", userId, "PRODUCT", productId,
            "Product deleted: " + productName);
    }

    public void logReportCreated(String userId, String reportId, String targetType, String targetId) {
        log("REPORT_CREATED", userId, targetType, targetId,
            "Report created: " + reportId);
    }

    public void logReportResolved(String adminId, String reportId, String resolution) {
        log("REPORT_RESOLVED", adminId, "REPORT", reportId,
            "Report " + resolution.toLowerCase());
    }

    public void logUserSuspended(String adminId, String userId, String reason) {
        log("USER_SUSPENDED", adminId, "USER", userId, "User suspended: " + reason);
    }

    public void logUserActivated(String adminId, String userId) {
        log("USER_ACTIVATED", adminId, "USER", userId, "User activated");
    }

    public void logListingHidden(String adminId, String productId) {
        log("LISTING_HIDDEN", adminId, "PRODUCT", productId, "Listing hidden by admin");
    }

    public void logError(String context, String error) {
        log("ERROR", null, null, null, context + ": " + error);
    }

    private void processLogs() {
        String logPath = LOG_DIR + File.separator + LOG_FILE;

        while (running || !logQueue.isEmpty()) {
            try {
                LogEntry entry = logQueue.take();
                writeLog(logPath, entry);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void writeLog(String logPath, LogEntry entry) {
        try (FileWriter fw = new FileWriter(logPath, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.printf("%s|%s|%s|%s|%s|%s|%s%n",
                entry.timestamp.format(TIMESTAMP_FORMAT),
                entry.eventType,
                entry.userId != null ? entry.userId : "-",
                entry.targetType != null ? entry.targetType : "-",
                entry.targetId != null ? entry.targetId : "-",
                entry.details != null ? entry.details.replace("|", "\\|") : "-",
                entry.ipAddress != null ? entry.ipAddress : "-"
            );
        } catch (IOException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        writerThread.interrupt();
        try {
            writerThread.join(5000);
        } catch (InterruptedException ignored) {}
    }

    private record LogEntry(
        LocalDateTime timestamp,
        String eventType,
        String userId,
        String targetType,
        String targetId,
        String details,
        String ipAddress
    ) {}
}
