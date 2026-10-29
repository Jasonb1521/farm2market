package com.farm2market.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.farm2market.app.Farm2MarketApp;
import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.ui.auth.LoginActivity;
import com.farm2market.app.ui.common.NotificationsActivity;
import com.google.gson.JsonObject;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvMobile, tvRole;
    private Button btnEditProfile, btnNotifications, btnHelp, btnLogout;
    private ProgressBar progressBar;

    private ApiService apiService;
    private JsonObject profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvMobile = findViewById(R.id.tvMobile);
        tvRole = findViewById(R.id.tvRole);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnHelp = findViewById(R.id.btnHelp);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });

        btnHelp.setOnClickListener(v -> {
            // TODO: Help screen
            Toast.makeText(this, "Help & Support coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProfile(new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                progressBar.setVisibility(View.GONE);
                profile = result;
                displayProfile();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                // Use cached data
                displayCachedProfile();
            }
        });
    }

    private void displayProfile() {
        tvName.setText(getStringValue("name"));
        tvEmail.setText(getStringValue("email"));
        tvMobile.setText(getStringValue("mobile"));

        String role = getStringValue("role");
        tvRole.setText(role.substring(0, 1) + role.substring(1).toLowerCase());
    }

    private void displayCachedProfile() {
        Farm2MarketApp app = Farm2MarketApp.getInstance();
        tvName.setText(app.getUserName() != null ? app.getUserName() : "User");
        tvEmail.setText("Loading...");
        tvMobile.setText("");
        tvRole.setText(app.getUserRole() != null ? app.getUserRole() : "");
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        apiService.logout(new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                clearAndExit();
            }

            @Override
            public void onError(String error) {
                clearAndExit();
            }
        });
    }

    private void clearAndExit() {
        Farm2MarketApp.getInstance().clearSession();
        apiService.disconnect();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getStringValue(String key) {
        return profile != null && profile.has(key) && !profile.get(key).isJsonNull()
                ? profile.get(key).getAsString() : "";
    }
}
