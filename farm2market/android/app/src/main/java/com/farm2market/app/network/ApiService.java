package com.farm2market.app.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ApiService {

    private static ApiService instance;
    private final SocketClient client;
    private final Gson gson;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    private ApiService() {
        this.client = SocketClient.getInstance();
        this.gson = new Gson();
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    // ==================== AUTH ====================

    public void login(String email, String password, ApiCallback<JsonObject> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);

        client.sendRequest("LOGIN", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void register(String name, String email, String mobile, String password, String role,
                         ApiCallback<JsonObject> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("name", name);
        payload.addProperty("email", email);
        payload.addProperty("mobile", mobile);
        payload.addProperty("password", password);
        payload.addProperty("role", role);

        client.sendRequest("REGISTER", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void logout(ApiCallback<Void> callback) {
        client.sendRequest("LOGOUT", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== PRODUCTS ====================

    public void getProducts(int limit, int offset, ApiCallback<JsonArray> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("limit", limit);
        payload.addProperty("offset", offset);

        client.sendRequest("LIST_PRODUCTS", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void searchProducts(String query, String category, Double minPrice, Double maxPrice,
                               int limit, int offset, ApiCallback<JsonArray> callback) {
        JsonObject payload = new JsonObject();
        if (query != null) payload.addProperty("query", query);
        if (category != null) payload.addProperty("category", category);
        if (minPrice != null) payload.addProperty("minPrice", minPrice);
        if (maxPrice != null) payload.addProperty("maxPrice", maxPrice);
        payload.addProperty("limit", limit);
        payload.addProperty("offset", offset);
        payload.addProperty("inStock", true);

        client.sendRequest("SEARCH_PRODUCTS", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getProduct(String productId, ApiCallback<JsonObject> callback) {
        client.sendRequest("GET_PRODUCT", productId, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void createProduct(JsonObject product, ApiCallback<JsonObject> callback) {
        client.sendRequest("CREATE_PRODUCT", product, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getFarmerProducts(ApiCallback<JsonArray> callback) {
        client.sendRequest("GET_FARMER_PRODUCTS", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getCategories(ApiCallback<JsonArray> callback) {
        client.sendRequest("GET_CATEGORIES", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== PROFILE ====================

    public void getProfile(ApiCallback<JsonObject> callback) {
        client.sendRequest("GET_PROFILE", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getFarmerProfile(String farmerId, ApiCallback<JsonObject> callback) {
        client.sendRequest("GET_FARMER_PROFILE", farmerId, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== CART ====================

    public void addToCart(String productId, int quantity, ApiCallback<Void> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("productId", productId);
        payload.addProperty("quantity", quantity);

        client.sendRequest("ADD_TO_CART", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getCart(ApiCallback<JsonObject> callback) {
        client.sendRequest("GET_CART", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== PRODUCT MANAGEMENT ====================

    public void createProduct(String name, String description, String category,
                              double price, String unit, double stock, boolean organic,
                              ApiCallback<JsonObject> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("name", name);
        payload.addProperty("description", description);
        payload.addProperty("category", category);
        payload.addProperty("price", price);
        payload.addProperty("unit", unit);
        payload.addProperty("stockQuantity", stock);
        payload.addProperty("organic", organic);

        client.sendRequest("CREATE_PRODUCT", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void updateProduct(String productId, String name, String description, String category,
                              double price, String unit, double stock, boolean organic,
                              ApiCallback<Void> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("productId", productId);
        payload.addProperty("name", name);
        payload.addProperty("description", description);
        payload.addProperty("category", category);
        payload.addProperty("price", price);
        payload.addProperty("unit", unit);
        payload.addProperty("stockQuantity", stock);
        payload.addProperty("organic", organic);

        client.sendRequest("UPDATE_PRODUCT", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== CART OPERATIONS ====================

    public void updateCartItem(String cartItemId, int quantity, ApiCallback<Void> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("cartItemId", cartItemId);
        payload.addProperty("quantity", quantity);

        client.sendRequest("UPDATE_CART_ITEM", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void removeFromCart(String cartItemId, ApiCallback<Void> callback) {
        client.sendRequest("REMOVE_FROM_CART", cartItemId, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== ORDERS ====================

    public void placeOrder(JsonObject orderRequest, ApiCallback<JsonObject> callback) {
        client.sendRequest("PLACE_ORDER", orderRequest, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getOrders(ApiCallback<JsonArray> callback) {
        client.sendRequest("GET_CUSTOMER_ORDERS", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getOrder(String orderId, ApiCallback<JsonObject> callback) {
        client.sendRequest("GET_ORDER", orderId, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void cancelOrder(String orderId, ApiCallback<Void> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("orderId", orderId);
        payload.addProperty("status", "CANCELLED");

        client.sendRequest("UPDATE_ORDER_STATUS", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getFarmerOrders(ApiCallback<JsonArray> callback) {
        client.sendRequest("GET_FARMER_ORDERS", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void updateOrderStatus(String orderId, String status, ApiCallback<Void> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("orderId", orderId);
        payload.addProperty("status", status);

        client.sendRequest("UPDATE_ORDER_STATUS", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== PROFILE UPDATES ====================

    public void updateProfile(String name, String mobile, ApiCallback<Void> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("name", name);
        payload.addProperty("mobile", mobile);

        client.sendRequest("UPDATE_PROFILE", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== NOTIFICATIONS ====================

    public void getNotifications(ApiCallback<JsonArray> callback) {
        client.sendRequest("GET_NOTIFICATIONS", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void markNotificationRead(String notificationId, ApiCallback<Void> callback) {
        client.sendRequest("MARK_NOTIFICATION_READ", notificationId, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void markAllNotificationsRead(ApiCallback<Void> callback) {
        client.sendRequest("MARK_ALL_NOTIFICATIONS_READ", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== REVIEWS ====================

    public void submitReview(String orderId, String productId, int rating, String comment,
                             ApiCallback<Void> callback) {
        JsonObject payload = new JsonObject();
        payload.addProperty("orderId", orderId);
        payload.addProperty("productId", productId);
        payload.addProperty("rating", rating);
        payload.addProperty("comment", comment);

        client.sendRequest("SUBMIT_REVIEW", payload, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getProductReviews(String productId, ApiCallback<JsonArray> callback) {
        client.sendRequest("GET_PRODUCT_REVIEWS", productId, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonArray("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getFarmerStats(ApiCallback<JsonObject> callback) {
        client.sendRequest("GET_FARMER_STATS", null, new SocketClient.ResponseCallback() {
            @Override
            public void onResponse(JsonObject response) {
                callback.onSuccess(response.getAsJsonObject("payload"));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== CONNECTION ====================

    public void connect(SocketClient.ConnectionListener listener) {
        client.connect(listener);
    }

    public void disconnect() {
        client.disconnect();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void setNotificationListener(SocketClient.NotificationListener listener) {
        client.setNotificationListener(listener);
    }
}
