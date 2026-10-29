package com.farm2market.core.util;

public final class AppConstants {

    private AppConstants() {}

    // Server
    public static final int DEFAULT_SERVER_PORT = 8888;
    public static final int MAX_CONNECTIONS = 100;
    public static final int CONNECTION_TIMEOUT_MS = 30000;
    public static final int READ_TIMEOUT_MS = 60000;

    // Session
    public static final long SESSION_DURATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days
    public static final int TOKEN_LENGTH = 64;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Limits
    public static final int MAX_CART_ITEMS = 50;
    public static final int MAX_IMAGES_PER_PRODUCT = 5;
    public static final int MAX_RECENT_SEARCHES = 10;
    public static final int MAX_UNDO_ACTIONS = 20;

    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    public static final int MAX_ADDRESS_LENGTH = 500;

    // Currency
    public static final String CURRENCY_SYMBOL = "₹";
    public static final String CURRENCY_CODE = "INR";

    // Date formats
    public static final String DATE_FORMAT = "dd MMM yyyy";
    public static final String TIME_FORMAT = "hh:mm a";
    public static final String DATETIME_FORMAT = "dd MMM yyyy, hh:mm a";

    // Default values
    public static final double DEFAULT_DELIVERY_FEE = 30.0;
    public static final double MIN_ORDER_FOR_FREE_DELIVERY = 500.0;

    // File paths
    public static final String AUDIT_LOG_FILE = "audit.log";
    public static final String EXPORT_DIRECTORY = "exports";
}
