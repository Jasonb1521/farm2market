package com.farm2market.core.enums;

public enum ReportStatus {
    OPEN("Open", "Report is pending review"),
    REVIEWED("Reviewed", "Report is under review"),
    RESOLVED("Resolved", "Report has been resolved with action taken"),
    DISMISSED("Dismissed", "Report was dismissed");

    private final String displayName;
    private final String description;

    ReportStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isClosed() {
        return this == RESOLVED || this == DISMISSED;
    }
}
