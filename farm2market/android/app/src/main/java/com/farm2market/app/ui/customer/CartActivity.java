package com.farm2market.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.ui.adapter.CartAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView rvCartItems;
    private TextView tvEmpty, tvSubtotal, tvDelivery, tvTotal;
    private Button btnCheckout;
    private ProgressBar progressBar;
    private View layoutSummary;

    private CartAdapter adapter;
    private ApiService apiService;
    private double subtotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupRecyclerView();
        loadCart();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        progressBar = findViewById(R.id.progressBar);
        layoutSummary = findViewById(R.id.layoutSummary);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this, new ArrayList<>(), this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(adapter);
    }

    private void loadCart() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getCart(new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                progressBar.setVisibility(View.GONE);
                displayCart(result);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCart(JsonObject cart) {
        List<JsonObject> items = new ArrayList<>();
        subtotal = 0;

        if (cart.has("items")) {
            JsonArray itemsArray = cart.getAsJsonArray("items");
            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject item = itemsArray.get(i).getAsJsonObject();
                items.add(item);

                double price = item.has("priceSnapshot") ? item.get("priceSnapshot").getAsDouble() : 0;
                double quantity = item.has("quantity") ? item.get("quantity").getAsDouble() : 0;
                subtotal += price * quantity;
            }
        }

        adapter.updateItems(items);

        boolean hasItems = !items.isEmpty();
        tvEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        rvCartItems.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        layoutSummary.setVisibility(hasItems ? View.VISIBLE : View.GONE);

        updateTotals();
    }

    private void updateTotals() {
        double delivery = subtotal > 500 ? 0 : 40; // Free delivery over ₹500
        double total = subtotal + delivery;

        tvSubtotal.setText(String.format("₹%.2f", subtotal));
        tvDelivery.setText(delivery > 0 ? String.format("₹%.2f", delivery) : "FREE");
        tvTotal.setText(String.format("₹%.2f", total));
    }

    @Override
    public void onQuantityChanged(String cartItemId, int newQuantity) {
        apiService.updateCartItem(cartItemId, newQuantity, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadCart();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRemoveItem(String cartItemId) {
        apiService.removeFromCart(cartItemId, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadCart();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToCheckout() {
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("subtotal", subtotal);
        startActivity(intent);
    }
}
