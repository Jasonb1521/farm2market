package com.farm2market.app.ui.farmer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.farm2market.app.Farm2MarketApp;
import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.ui.adapter.ProductAdapter;
import com.farm2market.app.ui.auth.LoginActivity;
import com.farm2market.app.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class FarmerDashboardActivity extends AppCompatActivity {

    private TextView tvUserName, tvActiveListings, tvTotalOrders, tvEmpty;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private FloatingActionButton btnAddProduct;
    private BottomNavigationView bottomNav;

    private ProductAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        initViews();
        setupRecyclerView();
        setupListeners();
        connectAndLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiService.isConnected()) {
            loadDashboard();
        }
    }

    private void connectAndLoad() {
        showLoading(true);
        if (!apiService.isConnected()) {
            apiService.connect(new com.farm2market.app.network.SocketClient.ConnectionListener() {
                @Override
                public void onConnected() {
                    loadDashboard();
                }

                @Override
                public void onDisconnected() {
                    showLoading(false);
                    showError("Disconnected from server");
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    showError("Connection failed: " + error);
                }
            });
        } else {
            loadDashboard();
        }
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvActiveListings = findViewById(R.id.tvActiveListings);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvProducts = findViewById(R.id.rvProducts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        apiService = ApiService.getInstance();
        bottomNav = findViewById(R.id.bottomNav);

        String name = Farm2MarketApp.getInstance().getUserName();
        tvUserName.setText("Hello, " + (name != null ? name : "Farmer"));

        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_products) {
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, IncomingOrdersActivity.class));
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, SalesSummaryActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this, new ArrayList<>());
        adapter.setFarmerMode(true);
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            intent.putExtra("productId", product.get("productId").getAsString());
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadDashboard);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void loadDashboard() {
        showLoading(true);

        apiService.getFarmerProducts(new ApiService.ApiCallback<JsonArray>() {
            @Override
            public void onSuccess(JsonArray result) {
                showLoading(false);
                displayProducts(result);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                showError(error);
            }
        });
    }

    private void displayProducts(JsonArray products) {
        List<JsonObject> productList = new ArrayList<>();
        int activeCount = 0;

        for (int i = 0; i < products.size(); i++) {
            JsonObject product = products.get(i).getAsJsonObject();
            productList.add(product);

            String status = product.has("status") ? product.get("status").getAsString() : "ACTIVE";
            if ("ACTIVE".equals(status)) {
                activeCount++;
            }
        }

        adapter.updateProducts(productList);
        tvActiveListings.setText(String.valueOf(activeCount));
        tvTotalOrders.setText("0"); // TODO: Load actual order count

        tvEmpty.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(productList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void logout() {
        apiService.logout(new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                performLogout();
            }

            @Override
            public void onError(String error) {
                performLogout();
            }
        });
    }

    private void performLogout() {
        Farm2MarketApp.getInstance().clearSession();
        apiService.disconnect();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        swipeRefresh.setRefreshing(false);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}
