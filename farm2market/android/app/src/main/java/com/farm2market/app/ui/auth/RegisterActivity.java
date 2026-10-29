package com.farm2market.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farm2market.app.Farm2MarketApp;
import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.ui.customer.CustomerHomeActivity;
import com.farm2market.app.ui.farmer.FarmerDashboardActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etMobile, etPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void attemptRegister() {
        String name = getText(etName);
        String email = getText(etEmail);
        String mobile = getText(etMobile);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        // Validation
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        String role = rgRole.getCheckedRadioButtonId() == R.id.rbFarmer ? "FARMER" : "CUSTOMER";

        if (!apiService.isConnected()) {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        apiService.register(name, email, mobile, password, role, new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                showLoading(false);
                handleRegisterSuccess(result);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleRegisterSuccess(JsonObject response) {
        Farm2MarketApp app = Farm2MarketApp.getInstance();

        String token = response.get("token").getAsString();
        String userId = response.get("userId").getAsString();
        String name = response.get("name").getAsString();
        String email = response.get("email").getAsString();
        String role = response.get("role").getAsString();

        app.saveSession(token, userId, name, email, role);

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

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

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}
