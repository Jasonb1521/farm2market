package com.farm2market.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.farm2market.app.Farm2MarketApp;
import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.ui.adapter.ProductAdapter;
import com.farm2market.app.ui.auth.LoginActivity;
import com.farm2market.app.ui.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class CustomerHomeActivity extends AppCompatActivity {

    private SearchView searchView;
    private ChipGroup chipCategories;
    private RecyclerView rvProducts;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvUserName;
    private BottomNavigationView bottomNav;

    private ProductAdapter adapter;
    private ApiService apiService;
    private String selectedCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        initViews();
        setupRecyclerView();
        setupListeners();
        connectAndLoad();
    }

    private void connectAndLoad() {
        showLoading(true);
        if (!apiService.isConnected()) {
            apiService.connect(new com.farm2market.app.network.SocketClient.ConnectionListener() {
                @Override
                public void onConnected() {
                    loadProducts();
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
            loadProducts();
        }
    }

    private void initViews() {
        searchView = findViewById(R.id.searchView);
        chipCategories = findViewById(R.id.chipCategories);
        rvProducts = findViewById(R.id.rvProducts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvUserName = findViewById(R.id.tvUserName);

        apiService = ApiService.getInstance();
        bottomNav = findViewById(R.id.bottomNav);

        // Set user greeting
        String name = Farm2MarketApp.getInstance().getUserName();
        tvUserName.setText("Hello, " + (name != null ? name : "Guest"));

        // Setup categories
        setupCategories();
        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void setupCategories() {
        String[] categories = {"All", "Vegetables", "Fruits", "Grains", "Dairy", "Pulses", "Herbs", "Eggs"};

        for (int i = 0; i < categories.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(categories[i]);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            final int index = i;
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = index == 0 ? null : categories[index].toUpperCase();
                    loadProducts();
                }
            });
            chipCategories.addView(chip);
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this, new ArrayList<>());
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("productId", product.get("productId").getAsString());
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadProducts);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadProducts();
                }
                return true;
            }
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void loadProducts() {
        showLoading(true);

        if (selectedCategory != null) {
            apiService.searchProducts(null, selectedCategory, null, null, 50, 0,
                new ApiService.ApiCallback<JsonArray>() {
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
        } else {
            apiService.getProducts(50, 0, new ApiService.ApiCallback<JsonArray>() {
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
    }

    private void searchProducts(String query) {
        showLoading(true);

        apiService.searchProducts(query, selectedCategory, null, null, 50, 0,
            new ApiService.ApiCallback<JsonArray>() {
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
        for (int i = 0; i < products.size(); i++) {
            productList.add(products.get(i).getAsJsonObject());
        }

        adapter.updateProducts(productList);
        tvEmpty.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(productList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void logout() {
        apiService.logout(new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Farm2MarketApp.getInstance().clearSession();
                apiService.disconnect();

                Intent intent = new Intent(CustomerHomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Farm2MarketApp.getInstance().clearSession();
                Intent intent = new Intent(CustomerHomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showLoading(boolean show) {
        swipeRefresh.setRefreshing(false);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}
