package com.farm2market.app.ui.farmer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.google.gson.JsonObject;

import java.text.NumberFormat;
import java.util.Locale;

public class SalesSummaryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;

    private TextView tvTotalRevenue;
    private TextView tvTotalOrders;
    private TextView tvPendingOrders;
    private TextView tvCompletedOrders;
    private TextView tvCancelledOrders;
    private TextView tvActiveProducts;
    private TextView tvOutOfStock;
    private TextView tvAvgRating;
    private TextView tvTotalReviews;

    private ApiService apiService;
    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_summary);

        apiService = ApiService.getInstance();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        initViews();
        setupListeners();
        loadStats();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        tvCancelledOrders = findViewById(R.id.tvCancelledOrders);
        tvActiveProducts = findViewById(R.id.tvActiveProducts);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadStats);
    }

    private void loadStats() {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        apiService.getFarmerStats(new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject stats) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    displayStats(stats);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SalesSummaryActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayStats(JsonObject stats) {
        double totalRevenue = stats.has("totalRevenue") ? stats.get("totalRevenue").getAsDouble() : 0;
        int totalOrders = stats.has("totalOrders") ? stats.get("totalOrders").getAsInt() : 0;
        int pendingOrders = stats.has("pendingOrders") ? stats.get("pendingOrders").getAsInt() : 0;
        int completedOrders = stats.has("completedOrders") ? stats.get("completedOrders").getAsInt() : 0;
        int cancelledOrders = stats.has("cancelledOrders") ? stats.get("cancelledOrders").getAsInt() : 0;
        int activeProducts = stats.has("activeProducts") ? stats.get("activeProducts").getAsInt() : 0;
        int outOfStock = stats.has("outOfStockProducts") ? stats.get("outOfStockProducts").getAsInt() : 0;
        double avgRating = stats.has("avgRating") ? stats.get("avgRating").getAsDouble() : 0;
        int totalReviews = stats.has("totalReviews") ? stats.get("totalReviews").getAsInt() : 0;

        tvTotalRevenue.setText(currencyFormat.format(totalRevenue));
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
        tvCompletedOrders.setText(String.valueOf(completedOrders));
        tvCancelledOrders.setText(String.valueOf(cancelledOrders));
        tvActiveProducts.setText(String.valueOf(activeProducts));
        tvOutOfStock.setText(String.valueOf(outOfStock));
        tvAvgRating.setText(String.format(Locale.US, "%.1f", avgRating));
        tvTotalReviews.setText(String.valueOf(totalReviews));
    }
}
