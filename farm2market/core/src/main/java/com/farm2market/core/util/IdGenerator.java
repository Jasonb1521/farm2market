package com.farm2market.core.util;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis());

    private IdGenerator() {}

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateUserId() {
        return "USR" + generateShortId();
    }

    public static String generateProductId() {
        return "PRD" + generateShortId();
    }

    public static String generateOrderId() {
        return "ORD" + generateShortId();
    }

    public static String generateCartId() {
        return "CRT" + generateShortId();
    }

    public static String generateReviewId() {
        return "REV" + generateShortId();
    }

    public static String generateNotificationId() {
        return "NTF" + generateShortId();
    }

    public static String generateReportId() {
        return "RPT" + generateShortId();
    }

    public static String generateAddressId() {
        return "ADR" + generateShortId();
    }

    public static String generateSessionToken() {
        return generateRandomString(AppConstants.TOKEN_LENGTH);
    }

    private static String generateShortId() {
        long timestamp = System.currentTimeMillis();
        long counter = COUNTER.incrementAndGet();
        return String.format("%d%04d", timestamp % 100000000, counter % 10000);
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public static String generateOrderNumber() {
        long timestamp = System.currentTimeMillis();
        return String.format("F2M%d%04d", timestamp % 100000, RANDOM.nextInt(10000));
    }
}
