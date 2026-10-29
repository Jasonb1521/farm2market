package com.farm2market.app.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.ui.adapter.OrderItemAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvDate, tvFarmer, tvAddress;
    private TextView tvSubtotal, tvDelivery, tvTotal, tvPayment, tvNotes;
    private RecyclerView rvItems;
    private Button btnCancel, btnReview;
    private ProgressBar progressBar;
    private View layoutAddress;

    private OrderItemAdapter adapter;
    private ApiService apiService;
    private String orderId;
    private JsonObject order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadOrder();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvDate = findViewById(R.id.tvDate);
        tvFarmer = findViewById(R.id.tvFarmer);
        tvAddress = findViewById(R.id.tvAddress);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvTotal = findViewById(R.id.tvTotal);
        tvPayment = findViewById(R.id.tvPayment);
        tvNotes = findViewById(R.id.tvNotes);
        rvItems = findViewById(R.id.rvItems);
        btnCancel = findViewById(R.id.btnCancel);
        btnReview = findViewById(R.id.btnReview);
        progressBar = findViewById(R.id.progressBar);
        layoutAddress = findViewById(R.id.layoutAddress);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> confirmCancel());
        btnReview.setOnClickListener(v -> openReview());
    }

    private void setupRecyclerView() {
        adapter = new OrderItemAdapter(this, new ArrayList<>());
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);
    }

    private void loadOrder() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getOrder(orderId, new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                progressBar.setVisibility(View.GONE);
                order = result;
                displayOrder();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrder() {
        tvOrderId.setText("Order #" + orderId.substring(0, 8).toUpperCase());

        String status = getStringValue("orderStatus");
        tvStatus.setText(status.replace("_", " "));
        setStatusColor(status);

        long createdAt = order.has("createdAt") ? order.get("createdAt").getAsLong() : 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(createdAt)));

        tvFarmer.setText("From: " + getStringValue("farmerName"));

        String fulfilment = getStringValue("fulfilmentMethod");
        if ("DELIVERY".equals(fulfilment) && order.has("deliveryAddress")) {
            // deliveryAddress can be a String or JsonObject
            String address;
            if (order.get("deliveryAddress").isJsonObject()) {
                JsonObject addr = order.getAsJsonObject("deliveryAddress");
                address = String.format("%s, %s - %s",
                        addr.has("street") ? addr.get("street").getAsString() : "",
                        addr.has("city") ? addr.get("city").getAsString() : "",
                        addr.has("pincode") ? addr.get("pincode").getAsString() : "");
            } else {
                address = order.get("deliveryAddress").getAsString();
            }
            tvAddress.setText(address);
            layoutAddress.setVisibility(View.VISIBLE);
        } else {
            layoutAddress.setVisibility(View.GONE);
        }

        double subtotal = order.has("subtotal") ? order.get("subtotal").getAsDouble() : 0;
        double deliveryFee = order.has("deliveryFee") ? order.get("deliveryFee").getAsDouble() : 0;
        double total = order.has("totalAmount") ? order.get("totalAmount").getAsDouble() : 0;

        tvSubtotal.setText(String.format("₹%.2f", subtotal));
        tvDelivery.setText(deliveryFee > 0 ? String.format("₹%.2f", deliveryFee) : "FREE");
        tvTotal.setText(String.format("₹%.2f", total));

        String payment = getStringValue("paymentMethod").replace("_", " ");
        tvPayment.setText(payment);

        String notes = getStringValue("notes");
        tvNotes.setVisibility(notes.isEmpty() ? View.GONE : View.VISIBLE);
        tvNotes.setText(notes);

        // Order items
        if (order.has("items")) {
            JsonArray items = order.getAsJsonArray("items");
            List<JsonObject> itemList = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                itemList.add(items.get(i).getAsJsonObject());
            }
            adapter.updateItems(itemList);
        }

        // Show/hide action buttons
        boolean canCancel = "PENDING".equals(status) || "ACCEPTED".equals(status);
        boolean canReview = "DELIVERED".equals(status) || "PICKED_UP".equals(status);
        btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        btnReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
    }

    private void setStatusColor(String status) {
        int color;
        switch (status) {
            case "PENDING": color = R.color.status_pending; break;
            case "ACCEPTED": color = R.color.status_accepted; break;
            case "PREPARING": color = R.color.status_preparing; break;
            case "READY_FOR_PICKUP":
            case "OUT_FOR_DELIVERY": color = R.color.status_ready; break;
            case "DELIVERED":
            case "PICKED_UP": color = R.color.status_completed; break;
            case "CANCELLED":
            case "REJECTED": color = R.color.status_cancelled; break;
            default: color = R.color.text_secondary;
        }
        tvStatus.setTextColor(getResources().getColor(color, null));
    }

    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelOrder())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelOrder() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.cancelOrder(orderId, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderDetailActivity.this, "Order cancelled", Toast.LENGTH_SHORT).show();
                loadOrder();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openReview() {
        // TODO: Open review activity
        Toast.makeText(this, "Review feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private String getStringValue(String key) {
        return order.has(key) && !order.get(key).isJsonNull() ? order.get(key).getAsString() : "";
    }
}
