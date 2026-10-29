package com.farm2market.admin.service;

import com.google.gson.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AdminClientService {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String sessionToken;
    private final Gson gson = new Gson();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        executor.shutdown();
    }

    private synchronized JsonObject sendRequest(String type, JsonElement payload) {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("type", type);
            if (sessionToken != null) {
                request.addProperty("token", sessionToken);
            }
            if (payload != null) {
                request.add("payload", payload);
            }

            String requestJson = gson.toJson(request);
            System.out.println("[Admin] Sending: " + requestJson);
            writer.println(requestJson);

            String response = reader.readLine();
            System.out.println("[Admin] Received: " + (response != null ? response.substring(0, Math.min(200, response.length())) : "null"));

            if (response == null) {
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("error", "No response from server");
                return error;
            }
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("[Admin] Error: " + e.getMessage());
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("error", e.getMessage());
            return error;
        }
    }

    public LoginResult login(String email, String password) {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);

        JsonObject response = sendRequest("LOGIN", payload);

        if (response.get("success").getAsBoolean()) {
            JsonObject data = response.getAsJsonObject("payload");
            String role = data.get("role").getAsString();

            if (!"ADMIN".equals(role)) {
                return new LoginResult(false, null, null, "Access denied: Admin role required");
            }

            sessionToken = data.get("token").getAsString();
            String name = data.get("name").getAsString();
            return new LoginResult(true, sessionToken, name, null);
        } else {
            String error = response.has("error") ? response.get("error").getAsString() : "Login failed";
            return new LoginResult(false, null, null, error);
        }
    }

    public void logout() {
        sendRequest("LOGOUT", null);
        sessionToken = null;
    }

    public void getPlatformSummary(Consumer<SummaryResult> callback) {
        executor.submit(() -> {
            try {
                JsonObject response = sendRequest("GET_PLATFORM_SUMMARY", null);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    JsonObject data = response.getAsJsonObject("payload");
                    callback.accept(new SummaryResult(true, new PlatformSummary(
                        getInt(data, "totalUsers"),
                        getInt(data, "customers"),
                        getInt(data, "farmers"),
                        getInt(data, "admins"),
                        getInt(data, "activeListings"),
                        getInt(data, "ordersToday"),
                        getInt(data, "ordersWeek"),
                        getInt(data, "ordersMonth"),
                        getDouble(data, "revenueToday"),
                        getDouble(data, "revenueWeek"),
                        getDouble(data, "revenueMonth"),
                        getInt(data, "pendingReports")
                    ), null));
                } else {
                    callback.accept(new SummaryResult(false, null, getError(response)));
                }
            } catch (Exception e) {
                System.err.println("[Admin] getPlatformSummary error: " + e.getMessage());
                e.printStackTrace();
                callback.accept(new SummaryResult(false, null, e.getMessage()));
            }
        });
    }

    public void getReports(String status, Consumer<ReportsResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            if (status != null) payload.addProperty("status", status);

            JsonObject response = sendRequest("GET_REPORTS", payload);
            if (response.get("success").getAsBoolean()) {
                List<ReportSummary> reports = new ArrayList<>();
                JsonArray arr = response.getAsJsonArray("payload");
                for (JsonElement el : arr) {
                    JsonObject r = el.getAsJsonObject();
                    reports.add(new ReportSummary(
                        getString(r, "reportId"),
                        getString(r, "targetType"),
                        getString(r, "targetId"),
                        getString(r, "reporterName"),
                        getString(r, "status"),
                        getString(r, "createdAt")
                    ));
                }
                callback.accept(new ReportsResult(true, reports, null));
            } else {
                callback.accept(new ReportsResult(false, null, getError(response)));
            }
        });
    }

    public void getReportDetails(String reportId, Consumer<ReportDetailResult> callback) {
        executor.submit(() -> {
            JsonObject response = sendRequest("GET_REPORT", new JsonPrimitive(reportId));
            if (response.get("success").getAsBoolean()) {
                JsonObject r = response.getAsJsonObject("payload");
                callback.accept(new ReportDetailResult(true, new ReportDetail(
                    getString(r, "reportId"),
                    getString(r, "targetType"),
                    getString(r, "targetId"),
                    getString(r, "reporterName"),
                    getString(r, "status"),
                    getString(r, "createdAt"),
                    getString(r, "reason")
                ), null));
            } else {
                callback.accept(new ReportDetailResult(false, null, getError(response)));
            }
        });
    }

    public void resolveReport(String reportId, String status, String note, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("reportId", reportId);
            payload.addProperty("status", status);
            payload.addProperty("adminNote", note);

            JsonObject response = sendRequest("RESOLVE_REPORT", payload);
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    public void getListings(String search, String category, String status, Consumer<ListingsResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            if (search != null) payload.addProperty("query", search);
            if (category != null) payload.addProperty("category", category);
            if (status != null) payload.addProperty("status", status);
            payload.addProperty("limit", 100);

            JsonObject response = sendRequest("LIST_PRODUCTS", payload);
            if (response.get("success").getAsBoolean()) {
                List<ListingSummary> listings = new ArrayList<>();
                JsonArray arr = response.getAsJsonArray("payload");
                for (JsonElement el : arr) {
                    JsonObject p = el.getAsJsonObject();
                    listings.add(new ListingSummary(
                        getString(p, "productId"),
                        getString(p, "name"),
                        getString(p, "farmerName"),
                        getString(p, "category"),
                        getDouble(p, "price"),
                        getString(p, "unit"),
                        getDouble(p, "stockQuantity"),
                        getString(p, "status")
                    ));
                }
                callback.accept(new ListingsResult(true, listings, null));
            } else {
                callback.accept(new ListingsResult(false, null, getError(response)));
            }
        });
    }

    public void getProductDetails(String productId, Consumer<ProductDetailResult> callback) {
        executor.submit(() -> {
            JsonObject response = sendRequest("GET_PRODUCT", new JsonPrimitive(productId));
            if (response.get("success").getAsBoolean()) {
                JsonObject p = response.getAsJsonObject("payload");
                callback.accept(new ProductDetailResult(true, new ProductDetail(
                    getString(p, "productId"),
                    getString(p, "name"),
                    getString(p, "category"),
                    getDouble(p, "price"),
                    getString(p, "unit"),
                    getDouble(p, "stockQuantity"),
                    getString(p, "status"),
                    getString(p, "farmerName"),
                    p.has("isOrganic") && p.get("isOrganic").getAsBoolean(),
                    getString(p, "createdAt"),
                    getString(p, "description")
                ), null));
            } else {
                callback.accept(new ProductDetailResult(false, null, getError(response)));
            }
        });
    }

    public void hideListing(String productId, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("productId", productId);
            payload.addProperty("status", "HIDDEN");

            JsonObject response = sendRequest("HIDE_LISTING", payload);
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    public void unhideListing(String productId, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("productId", productId);
            payload.addProperty("status", "ACTIVE");

            JsonObject response = sendRequest("UNHIDE_LISTING", payload);
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    public void deleteListing(String productId, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject response = sendRequest("DELETE_PRODUCT", new JsonPrimitive(productId));
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    public void getUsers(String search, String role, String status, Consumer<UsersResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            if (search != null) payload.addProperty("query", search);
            if (role != null) payload.addProperty("role", role);
            if (status != null) payload.addProperty("status", status);

            JsonObject response = sendRequest("GET_USERS", payload);
            if (response.get("success").getAsBoolean()) {
                List<UserSummary> users = new ArrayList<>();
                JsonArray arr = response.getAsJsonArray("payload");
                for (JsonElement el : arr) {
                    JsonObject u = el.getAsJsonObject();
                    users.add(new UserSummary(
                        getString(u, "userId"),
                        getString(u, "name"),
                        getString(u, "email"),
                        getString(u, "role"),
                        getString(u, "status"),
                        getString(u, "createdAt")
                    ));
                }
                callback.accept(new UsersResult(true, users, null));
            } else {
                callback.accept(new UsersResult(false, null, getError(response)));
            }
        });
    }

    public void getUserDetails(String userId, Consumer<UserDetailResult> callback) {
        executor.submit(() -> {
            JsonObject response = sendRequest("GET_USER", new JsonPrimitive(userId));
            if (response.get("success").getAsBoolean()) {
                JsonObject u = response.getAsJsonObject("payload");
                callback.accept(new UserDetailResult(true, new UserDetail(
                    getString(u, "userId"),
                    getString(u, "name"),
                    getString(u, "email"),
                    getString(u, "mobile"),
                    getString(u, "role"),
                    getString(u, "status"),
                    getString(u, "createdAt"),
                    getString(u, "farmName"),
                    getString(u, "location"),
                    getInt(u, "activeListings"),
                    getInt(u, "totalOrders"),
                    getDouble(u, "rating"),
                    getInt(u, "reviewCount"),
                    getDouble(u, "totalSpent")
                ), null));
            } else {
                callback.accept(new UserDetailResult(false, null, getError(response)));
            }
        });
    }

    public void getUserOrders(String userId, Consumer<OrdersResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("userId", userId);

            JsonObject response = sendRequest("GET_USER_ORDERS", payload);
            if (response.get("success").getAsBoolean()) {
                List<OrderSummary> orders = new ArrayList<>();
                JsonArray arr = response.getAsJsonArray("payload");
                for (JsonElement el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    orders.add(new OrderSummary(
                        getString(o, "orderId"),
                        getString(o, "createdAt"),
                        getDouble(o, "total"),
                        getString(o, "orderStatus")
                    ));
                }
                callback.accept(new OrdersResult(true, orders, null));
            } else {
                callback.accept(new OrdersResult(false, null, getError(response)));
            }
        });
    }

    public void getUserReports(String userId, Consumer<UserReportsResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("userId", userId);

            JsonObject response = sendRequest("GET_USER_REPORTS", payload);
            if (response.get("success").getAsBoolean()) {
                List<UserReportSummary> reports = new ArrayList<>();
                JsonArray arr = response.getAsJsonArray("payload");
                for (JsonElement el : arr) {
                    JsonObject r = el.getAsJsonObject();
                    reports.add(new UserReportSummary(
                        getString(r, "reportId"),
                        getString(r, "targetType"),
                        getString(r, "status"),
                        getString(r, "createdAt")
                    ));
                }
                callback.accept(new UserReportsResult(true, reports, null));
            } else {
                callback.accept(new UserReportsResult(false, null, getError(response)));
            }
        });
    }

    public void suspendUser(String userId, String reason, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("userId", userId);
            payload.addProperty("status", "SUSPENDED");
            payload.addProperty("reason", reason);

            JsonObject response = sendRequest("SUSPEND_USER", payload);
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    public void activateUser(String userId, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("userId", userId);
            payload.addProperty("status", "ACTIVE");

            JsonObject response = sendRequest("ACTIVATE_USER", payload);
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    public void exportOrdersCsv(String filePath, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("filePath", filePath);

            JsonObject response = sendRequest("EXPORT_ORDERS_CSV", payload);
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    public void exportSalesCsv(String filePath, Consumer<SimpleResult> callback) {
        executor.submit(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("filePath", filePath);

            JsonObject response = sendRequest("EXPORT_SALES_CSV", payload);
            callback.accept(new SimpleResult(response.get("success").getAsBoolean(), getError(response)));
        });
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private int getInt(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : 0;
    }

    private double getDouble(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsDouble() : 0.0;
    }

    private String getError(JsonObject response) {
        return response.has("error") && !response.get("error").isJsonNull()
            ? response.get("error").getAsString() : null;
    }

    // Record types for results
    public record LoginResult(boolean success, String token, String userName, String error) {}
    public record SimpleResult(boolean success, String error) {}
    public record SummaryResult(boolean success, PlatformSummary data, String error) {}
    public record ReportsResult(boolean success, List<ReportSummary> data, String error) {}
    public record ReportDetailResult(boolean success, ReportDetail data, String error) {}
    public record ListingsResult(boolean success, List<ListingSummary> data, String error) {}
    public record ProductDetailResult(boolean success, ProductDetail data, String error) {}
    public record UsersResult(boolean success, List<UserSummary> data, String error) {}
    public record UserDetailResult(boolean success, UserDetail data, String error) {}
    public record OrdersResult(boolean success, List<OrderSummary> data, String error) {}
    public record UserReportsResult(boolean success, List<UserReportSummary> data, String error) {}

    // Data records
    public record PlatformSummary(int totalUsers, int customers, int farmers, int admins,
        int activeListings, int ordersToday, int ordersWeek, int ordersMonth,
        double revenueToday, double revenueWeek, double revenueMonth, int pendingReports) {}

    public record ReportSummary(String id, String type, String targetId, String reporterName,
        String status, String createdAt) {}

    public record ReportDetail(String id, String type, String targetId, String reporterName,
        String status, String createdAt, String reason) {}

    public record ListingSummary(String id, String name, String farmerName, String category,
        double price, String unit, double stock, String status) {}

    public record ProductDetail(String id, String name, String category, double price, String unit,
        double stock, String status, String farmerName, boolean organic, String createdAt, String description) {}

    public record UserSummary(String id, String name, String email, String role, String status, String createdAt) {}

    public record UserDetail(String id, String name, String email, String mobile, String role, String status,
        String createdAt, String farmName, String location, int activeListings, int totalOrders,
        double rating, int reviewCount, double totalSpent) {}

    public record OrderSummary(String id, String date, double total, String status) {}

    public record UserReportSummary(String id, String type, String status, String date) {}
}
