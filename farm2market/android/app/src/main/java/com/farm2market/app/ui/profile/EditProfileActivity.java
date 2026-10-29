package com.farm2market.app.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farm2market.app.Farm2MarketApp;
import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etMobile;
    private Button btnSave;
    private ProgressBar progressBar;

    private ApiService apiService;
    private JsonObject currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadProfile();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        // Email is not editable
        etEmail.setEnabled(false);
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProfile(new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    currentProfile = result;
                    populateForm();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void populateForm() {
        etName.setText(getStringValue("name"));
        etEmail.setText(getStringValue("email"));
        etMobile.setText(getStringValue("mobile"));
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        if (mobile.isEmpty() || mobile.length() < 10) {
            etMobile.setError("Valid mobile number required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        apiService.updateProfile(name, mobile, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Farm2MarketApp.getInstance().updateUserName(name);
                    Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getStringValue(String key) {
        return currentProfile != null && currentProfile.has(key) && !currentProfile.get(key).isJsonNull()
                ? currentProfile.get(key).getAsString() : "";
    }
}
