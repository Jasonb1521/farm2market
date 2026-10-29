package com.farm2market.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farm2market.app.Farm2MarketApp;
import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.network.SocketClient;
import com.farm2market.app.ui.customer.CustomerHomeActivity;
import com.farm2market.app.ui.farmer.FarmerDashboardActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
        connectToServer();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Password reset coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void connectToServer() {
        showLoading(true);

        apiService.connect(new SocketClient.ConnectionListener() {
            @Override
            public void onConnected() {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected() {
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Connection failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        if (!apiService.isConnected()) {
            Toast.makeText(this, "Not connected to server. Reconnecting...", Toast.LENGTH_SHORT).show();
            connectToServer();
            return;
        }

        showLoading(true);

        apiService.login(email, password, new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                showLoading(false);
                handleLoginSuccess(result);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLoginSuccess(JsonObject response) {
        Farm2MarketApp app = Farm2MarketApp.getInstance();

        String token = response.get("token").getAsString();
        String userId = response.get("userId").getAsString();
        String name = response.get("name").getAsString();
        String email = response.get("email").getAsString();
        String role = response.get("role").getAsString();

        app.saveSession(token, userId, name, email, role);

        Intent intent;
        if ("FARMER".equals(role)) {
            intent = new Intent(this, FarmerDashboardActivity.class);
        } else {
            intent = new Intent(this, CustomerHomeActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
