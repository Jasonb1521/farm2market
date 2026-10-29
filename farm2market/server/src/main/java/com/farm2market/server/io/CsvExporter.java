package com.farm2market.server.io;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CsvExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportOrders(List<Map<String, Object>> orders, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Order ID,Customer,Farmer,Date,Subtotal,Delivery Fee,Total,Status,Payment Method,Payment Status,Fulfilment\n");

            for (Map<String, Object> order : orders) {
                writer.write(String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%s,%s,%s,%s\n",
                    escapeCsv(getString(order, "orderId")),
                    escapeCsv(getString(order, "customerName")),
                    escapeCsv(getString(order, "farmerName")),
                    getString(order, "createdAt"),
                    getDouble(order, "subtotal"),
                    getDouble(order, "deliveryFee"),
                    getDouble(order, "total"),
                    getString(order, "orderStatus"),
                    getString(order, "paymentMethod"),
                    getString(order, "paymentStatus"),
                    getString(order, "fulfilmentMethod")
                ));
            }
        }
    }

    public void exportOrdersDetailed(List<Map<String, Object>> orders, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Order ID,Customer,Farmer,Product,Quantity,Unit Price,Line Total,Order Total,Status,Date\n");

            for (Map<String, Object> order : orders) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        writer.write(String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%s,%s\n",
                            escapeCsv(getString(order, "orderId")),
                            escapeCsv(getString(order, "customerName")),
                            escapeCsv(getString(order, "farmerName")),
                            escapeCsv(getString(item, "productName")),
                            getDouble(item, "quantity"),
                            getDouble(item, "unitPrice"),
                            getDouble(item, "lineTotal"),
                            getDouble(order, "total"),
                            getString(order, "orderStatus"),
                            getString(order, "createdAt")
                        ));
                    }
                }
            }
        }
    }

    public void exportSales(String farmerId, List<Map<String, Object>> orders,
                            LocalDate from, LocalDate to, String filePath) throws IOException {

        Map<String, SalesAggregate> productSales = orders.stream()
            .flatMap(order -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
                return items != null ? items.stream() : java.util.stream.Stream.empty();
            })
            .collect(Collectors.groupingBy(
                item -> getString(item, "productId"),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        String name = getString(list.get(0), "productName");
                        double totalQty = list.stream().mapToDouble(i -> getDouble(i, "quantity")).sum();
                        double totalRevenue = list.stream().mapToDouble(i -> getDouble(i, "lineTotal")).sum();
                        return new SalesAggregate(name, totalQty, totalRevenue, list.size());
                    }
                )
            ));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Sales Report\n");
            writer.write(String.format("Period: %s to %s\n", from, to));
            writer.write(String.format("Total Orders: %d\n\n", orders.size()));
            writer.write("Product ID,Product Name,Quantity Sold,Revenue,Order Count\n");

            double totalRevenue = 0;
            for (var entry : productSales.entrySet()) {
                var agg = entry.getValue();
                writer.write(String.format("%s,%s,%.2f,%.2f,%d\n",
                    entry.getKey(),
                    escapeCsv(agg.name),
                    agg.quantity,
                    agg.revenue,
                    agg.orderCount
                ));
                totalRevenue += agg.revenue;
            }

            writer.write(String.format("\nTotal Revenue,,,%.2f,\n", totalRevenue));
        }
    }

    public void exportUsers(List<Map<String, Object>> users, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("User ID,Name,Email,Mobile,Role,Status,Created\n");

            for (Map<String, Object> user : users) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsv(getString(user, "userId")),
                    escapeCsv(getString(user, "name")),
                    escapeCsv(getString(user, "email")),
                    escapeCsv(getString(user, "mobile")),
                    getString(user, "role"),
                    getString(user, "status"),
                    getString(user, "createdAt")
                ));
            }
        }
    }

    public void exportProducts(List<Map<String, Object>> products, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Product ID,Name,Category,Price,Unit,Stock,Status,Farmer,Created\n");

            for (Map<String, Object> product : products) {
                writer.write(String.format("%s,%s,%s,%.2f,%s,%.2f,%s,%s,%s\n",
                    escapeCsv(getString(product, "productId")),
                    escapeCsv(getString(product, "name")),
                    getString(product, "category"),
                    getDouble(product, "price"),
                    getString(product, "unit"),
                    getDouble(product, "stockQuantity"),
                    getString(product, "status"),
                    escapeCsv(getString(product, "farmerName")),
                    getString(product, "createdAt")
                ));
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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

    private record SalesAggregate(String name, double quantity, double revenue, int orderCount) {}
}
