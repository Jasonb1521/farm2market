package com.farm2market.server.io;

import java.io.*;
import java.util.*;

public class CsvImporter {

    public List<Map<String, Object>> importProducts(String filePath, String farmerId) throws IOException {
        List<Map<String, Object>> products = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Empty file");
            }

            String[] columns = parseCsvLine(header);
            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < columns.length; i++) {
                columnIndex.put(columns[i].toLowerCase().trim(), i);
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = parseCsvLine(line);
                    Map<String, Object> product = new HashMap<>();

                    product.put("farmerId", farmerId);
                    product.put("name", getValue(values, columnIndex, "name"));
                    product.put("description", getValue(values, columnIndex, "description"));
                    product.put("category", getValue(values, columnIndex, "category").toUpperCase());
                    product.put("price", parseDouble(getValue(values, columnIndex, "price")));
                    product.put("unit", getValue(values, columnIndex, "unit").toUpperCase());
                    product.put("stockQuantity", parseDouble(getValue(values, columnIndex, "stock")));
                    product.put("minOrderQuantity", parseDouble(getValue(values, columnIndex, "min_order", "1")));
                    product.put("isOrganic", parseBoolean(getValue(values, columnIndex, "organic", "false")));
                    product.put("isSeasonal", parseBoolean(getValue(values, columnIndex, "seasonal", "false")));
                    product.put("isLocallyGrown", parseBoolean(getValue(values, columnIndex, "local", "true")));

                    if (product.get("name") == null || ((String)product.get("name")).isEmpty()) {
                        throw new IllegalArgumentException("Name is required");
                    }
                    if ((Double)product.get("price") <= 0) {
                        throw new IllegalArgumentException("Price must be positive");
                    }

                    products.add(product);
                } catch (Exception e) {
                    throw new IOException("Error on line " + lineNum + ": " + e.getMessage());
                }
            }
        }

        return products;
    }

    public List<Map<String, Object>> importUsers(String filePath) throws IOException {
        List<Map<String, Object>> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Empty file");
            }

            String[] columns = parseCsvLine(header);
            Map<String, Integer> columnIndex = new HashMap<>();
            for (int i = 0; i < columns.length; i++) {
                columnIndex.put(columns[i].toLowerCase().trim(), i);
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] values = parseCsvLine(line);
                    Map<String, Object> user = new HashMap<>();

                    user.put("name", getValue(values, columnIndex, "name"));
                    user.put("email", getValue(values, columnIndex, "email"));
                    user.put("mobile", getValue(values, columnIndex, "mobile"));
                    user.put("password", getValue(values, columnIndex, "password"));
                    user.put("role", getValue(values, columnIndex, "role").toUpperCase());

                    if (user.get("name") == null || ((String)user.get("name")).isEmpty()) {
                        throw new IllegalArgumentException("Name is required");
                    }
                    if (user.get("email") == null || ((String)user.get("email")).isEmpty()) {
                        throw new IllegalArgumentException("Email is required");
                    }

                    users.add(user);
                } catch (Exception e) {
                    throw new IOException("Error on line " + lineNum + ": " + e.getMessage());
                }
            }
        }

        return users;
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());

        return values.toArray(new String[0]);
    }

    private String getValue(String[] values, Map<String, Integer> columnIndex, String... keys) {
        for (String key : keys) {
            Integer index = columnIndex.get(key.toLowerCase());
            if (index != null && index < values.length) {
                String value = values[index].trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return keys.length > 1 ? keys[keys.length - 1] : "";
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", "").replace("₹", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean parseBoolean(String value) {
        if (value == null || value.isEmpty()) return false;
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1");
    }
}
