package com.farm2market.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.farm2market.app.Farm2MarketApp;
import com.farm2market.app.R;
import com.farm2market.app.ui.customer.CustomerHomeActivity;
import com.farm2market.app.ui.farmer.FarmerDashboardActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, SPLASH_DELAY);
    }

    private void navigateNext() {
        Farm2MarketApp app = Farm2MarketApp.getInstance();
        Intent intent;

        if (app.isLoggedIn()) {
            if (app.isFarmer()) {
                intent = new Intent(this, FarmerDashboardActivity.class);
            } else {
                intent = new Intent(this, CustomerHomeActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
