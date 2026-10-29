package com.farm2market.server.io;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReceiptWriter {

    private static final String RECEIPTS_DIR = "receipts";
    private static final int RECEIPT_WIDTH = 48;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public ReceiptWriter() {
        new File(RECEIPTS_DIR).mkdirs();
    }

    public String generateReceipt(Map<String, Object> order) throws IOException {
        String orderId = getString(order, "orderId");
        String fileName = RECEIPTS_DIR + File.separator + "receipt_" + orderId + ".txt";

        StringBuilder receipt = new StringBuilder();

        receipt.append(center("FARM2MARKET")).append("\n");
        receipt.append(center("Order Receipt")).append("\n");
        receipt.append(repeat("=", RECEIPT_WIDTH)).append("\n\n");

        receipt.append(String.format("Order ID: %s%n", orderId.substring(0, Math.min(orderId.length(), 8)).toUpperCase()));
        receipt.append(String.format("Date: %s%n", formatDate(order.get("createdAt"))));
        receipt.append("\n");

        receipt.append("Customer: ").append(getString(order, "customerName")).append("\n");

        String fulfilment = getString(order, "fulfilmentMethod");
        receipt.append("Fulfilment: ").append(fulfilment).append("\n");

        if ("DELIVERY".equals(fulfilment)) {
            receipt.append("Address: ").append(getString(order, "deliveryAddress")).append("\n");
        }

        receipt.append("\n");
        receipt.append(repeat("-", RECEIPT_WIDTH)).append("\n");
        receipt.append("From: ").append(getString(order, "farmerName")).append("\n");
        receipt.append(repeat("-", RECEIPT_WIDTH)).append("\n\n");

        receipt.append(String.format("%-24s %6s %10s%n", "ITEM", "QTY", "AMOUNT"));
        receipt.append(repeat("-", RECEIPT_WIDTH)).append("\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        if (items != null) {
            for (Map<String, Object> item : items) {
                String name = getString(item, "productName");
                if (name.length() > 22) {
                    name = name.substring(0, 20) + "..";
                }
                double qty = getDouble(item, "quantity");
                String unit = getString(item, "unit");
                double lineTotal = getDouble(item, "lineTotal");

                receipt.append(String.format("%-24s %5.1f%s %9.2f%n",
                    name, qty, unit.substring(0, Math.min(1, unit.length())), lineTotal));
            }
        }

        receipt.append(repeat("-", RECEIPT_WIDTH)).append("\n");

        double subtotal = getDouble(order, "subtotal");
        double deliveryFee = getDouble(order, "deliveryFee");
        double total = getDouble(order, "total");

        receipt.append(String.format("%34s: %9.2f%n", "Subtotal", subtotal));
        if (deliveryFee > 0) {
            receipt.append(String.format("%34s: %9.2f%n", "Delivery Fee", deliveryFee));
        }
        receipt.append(repeat("=", RECEIPT_WIDTH)).append("\n");
        receipt.append(String.format("%34s: %9.2f%n", "TOTAL", total));
        receipt.append(repeat("=", RECEIPT_WIDTH)).append("\n\n");

        receipt.append("Payment: ").append(formatPaymentMethod(getString(order, "paymentMethod"))).append("\n");
        receipt.append("Status: ").append(getString(order, "paymentStatus")).append("\n\n");

        String notes = getString(order, "customerNote");
        if (!notes.isEmpty()) {
            receipt.append("Note: ").append(notes).append("\n\n");
        }

        receipt.append(repeat("-", RECEIPT_WIDTH)).append("\n");
        receipt.append(center("Thank you for shopping local!")).append("\n");
        receipt.append(center("Support your local farmers")).append("\n");
        receipt.append(repeat("-", RECEIPT_WIDTH)).append("\n");

        String receiptText = receipt.toString();

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(receiptText);
        }

        return receiptText;
    }

    public String generateSummaryReceipt(String farmerId, String farmerName,
                                          List<Map<String, Object>> orders,
                                          LocalDateTime from, LocalDateTime to) throws IOException {

        String fileName = RECEIPTS_DIR + File.separator + "summary_" + farmerId + "_" +
            from.toLocalDate() + "_" + to.toLocalDate() + ".txt";

        StringBuilder receipt = new StringBuilder();

        receipt.append(center("FARM2MARKET")).append("\n");
        receipt.append(center("Sales Summary")).append("\n");
        receipt.append(repeat("=", RECEIPT_WIDTH)).append("\n\n");

        receipt.append("Farmer: ").append(farmerName).append("\n");
        receipt.append(String.format("Period: %s to %s%n",
            from.toLocalDate().format(DateTimeFormatter.ISO_DATE),
            to.toLocalDate().format(DateTimeFormatter.ISO_DATE)));
        receipt.append("\n");

        receipt.append(repeat("-", RECEIPT_WIDTH)).append("\n");

        int totalOrders = orders.size();
        int completedOrders = (int) orders.stream()
            .filter(o -> "COMPLETED".equals(getString(o, "orderStatus")))
            .count();
        double totalRevenue = orders.stream()
            .filter(o -> "COMPLETED".equals(getString(o, "orderStatus")))
            .mapToDouble(o -> getDouble(o, "total"))
            .sum();

        receipt.append(String.format("Total Orders: %d%n", totalOrders));
        receipt.append(String.format("Completed Orders: %d%n", completedOrders));
        receipt.append(String.format("Total Revenue: ₹%.2f%n", totalRevenue));

        receipt.append("\n");
        receipt.append(repeat("=", RECEIPT_WIDTH)).append("\n");

        String receiptText = receipt.toString();

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(receiptText);
        }

        return receiptText;
    }

    private String center(String text) {
        int padding = (RECEIPT_WIDTH - text.length()) / 2;
        if (padding <= 0) return text;
        return repeat(" ", padding) + text;
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private String formatDate(Object dateValue) {
        if (dateValue instanceof Long) {
            return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli((Long) dateValue),
                java.time.ZoneId.systemDefault()
            ).format(DATE_FORMAT);
        } else if (dateValue instanceof String) {
            return (String) dateValue;
        }
        return LocalDateTime.now().format(DATE_FORMAT);
    }

    private String formatPaymentMethod(String method) {
        return switch (method) {
            case "CASH_ON_DELIVERY" -> "Cash on Delivery";
            case "PAY_ON_PICKUP" -> "Pay on Pickup";
            case "DEMO" -> "Demo Payment";
            default -> method;
        };
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}
