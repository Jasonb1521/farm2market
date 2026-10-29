package com.farm2market.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Farm2MarketApp extends Application {

    private static Farm2MarketApp instance;
    private static final String PREFS_NAME = "Farm2MarketPrefs";
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static Farm2MarketApp getInstance() {
        return instance;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    // Session management
    public void saveSession(String token, String userId, String name, String email, String role) {
        prefs.edit()
                .putString("token", token)
                .putString("userId", userId)
                .putString("name", name)
                .putString("email", email)
                .putString("role", role)
                .putBoolean("isLoggedIn", true)
                .apply();
    }

    public void clearSession() {
        prefs.edit()
                .remove("token")
                .remove("userId")
                .remove("name")
                .remove("email")
                .remove("role")
                .putBoolean("isLoggedIn", false)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public String getToken() {
        return prefs.getString("token", null);
    }

    public String getUserId() {
        return prefs.getString("userId", null);
    }

    public String getUserName() {
        return prefs.getString("name", null);
    }

    public String getUserEmail() {
        return prefs.getString("email", null);
    }

    public String getUserRole() {
        return prefs.getString("role", "CUSTOMER");
    }

    public boolean isFarmer() {
        return "FARMER".equals(getUserRole());
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(getUserRole());
    }

    public void updateUserName(String name) {
        prefs.edit().putString("name", name).apply();
    }

    // Server configuration
    public String getServerHost() {
        return prefs.getString("serverHost", "localhost"); // Works with adb reverse
    }

    public int getServerPort() {
        return prefs.getInt("serverPort", 8888);
    }

    public void setServerConfig(String host, int port) {
        prefs.edit()
                .putString("serverHost", host)
                .putInt("serverPort", port)
                .apply();
    }
}
