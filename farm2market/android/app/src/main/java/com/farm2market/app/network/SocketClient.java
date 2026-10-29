package com.farm2market.app.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.farm2market.app.Farm2MarketApp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketClient {

    private static final String TAG = "SocketClient";
    private static SocketClient instance;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Gson gson;
    private final AtomicBoolean connected;
    private final ConcurrentHashMap<String, ResponseCallback> pendingRequests;
    private NotificationListener notificationListener;

    public interface ResponseCallback {
        void onResponse(JsonObject response);
        void onError(String error);
    }

    public interface NotificationListener {
        void onNotification(JsonObject notification);
    }

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    private SocketClient() {
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
        this.connected = new AtomicBoolean(false);
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    public void connect(ConnectionListener listener) {
        executor.execute(() -> {
            try {
                Farm2MarketApp app = Farm2MarketApp.getInstance();
                String host = app.getServerHost();
                int port = app.getServerPort();

                Log.d(TAG, "Connecting to " + host + ":" + port);

                socket = new Socket(host, port);
                socket.setSoTimeout(60000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                connected.set(true);

                Log.d(TAG, "Connected successfully");
                mainHandler.post(listener::onConnected);

                // Start reading responses
                startReading();

            } catch (IOException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                mainHandler.post(() -> listener.onError(e.getMessage()));
            }
        });
    }

    private void startReading() {
        executor.execute(() -> {
            try {
                String line;
                while (connected.get() && (line = reader.readLine()) != null) {
                    final String response = line;
                    handleResponse(response);
                }
            } catch (IOException e) {
                if (connected.get()) {
                    Log.e(TAG, "Read error: " + e.getMessage());
                    disconnect();
                }
            }
        });
    }

    private void handleResponse(String responseJson) {
        try {
            JsonObject response = gson.fromJson(responseJson, JsonObject.class);
            String type = response.has("type") ? response.get("type").getAsString() : "";
            String requestId = response.has("requestId") ? response.get("requestId").getAsString() : "";

            // Handle push notifications
            if ("NOTIFICATION_PUSH".equals(type)) {
                if (notificationListener != null) {
                    mainHandler.post(() -> notificationListener.onNotification(response));
                }
                return;
            }

            // Handle regular responses
            ResponseCallback callback = pendingRequests.remove(requestId);
            if (callback != null) {
                mainHandler.post(() -> {
                    boolean success = response.has("success") && response.get("success").getAsBoolean();
                    if (success) {
                        callback.onResponse(response);
                    } else {
                        String error = "Unknown error";
                        if (response.has("error")) {
                            JsonObject errorObj = response.getAsJsonObject("error");
                            error = errorObj.has("message") ? errorObj.get("message").getAsString() : error;
                        }
                        callback.onError(error);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response: " + e.getMessage());
        }
    }

    public void sendRequest(String type, Object payload, ResponseCallback callback) {
        if (!connected.get()) {
            mainHandler.post(() -> callback.onError("Not connected to server"));
            return;
        }

        executor.execute(() -> {
            try {
                String requestId = System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);

                JsonObject request = new JsonObject();
                request.addProperty("type", type);
                request.addProperty("requestId", requestId);
                request.addProperty("timestamp", System.currentTimeMillis());

                String token = Farm2MarketApp.getInstance().getToken();
                if (token != null) {
                    request.addProperty("token", token);
                }

                if (payload != null) {
                    request.add("payload", gson.toJsonTree(payload));
                }

                pendingRequests.put(requestId, callback);

                String json = gson.toJson(request);
                writer.println(json);
                writer.flush();

                Log.d(TAG, "Sent: " + type);

            } catch (Exception e) {
                Log.e(TAG, "Send error: " + e.getMessage());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void disconnect() {
        connected.set(false);

        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Disconnect error: " + e.getMessage());
        }

        Log.d(TAG, "Disconnected");
    }

    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }
}
