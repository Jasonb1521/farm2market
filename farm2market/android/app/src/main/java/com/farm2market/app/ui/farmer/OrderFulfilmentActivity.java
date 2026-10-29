package com.farm2market.app.ui.farmer;

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

public class OrderFulfilmentActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvDate, tvCustomer, tvPhone;
    private TextView tvAddress, tvTotal, tvPayment, tvNotes;
    private RecyclerView rvItems;
    private Button btnAction;
    private ProgressBar progressBar;
    private View layoutAddress;

    private OrderItemAdapter adapter;
    private ApiService apiService;
    private String orderId;
    private JsonObject order;
    private String currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_fulfilment);

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
        tvCustomer = findViewById(R.id.tvCustomer);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotal = findViewById(R.id.tvTotal);
        tvPayment = findViewById(R.id.tvPayment);
        tvNotes = findViewById(R.id.tvNotes);
        rvItems = findViewById(R.id.rvItems);
        btnAction = findViewById(R.id.btnAction);
        progressBar = findViewById(R.id.progressBar);
        layoutAddress = findViewById(R.id.layoutAddress);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnAction.setOnClickListener(v -> handleAction());
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
                Toast.makeText(OrderFulfilmentActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrder() {
        currentStatus = getStringValue("orderStatus");

        tvOrderId.setText("Order #" + orderId.substring(0, 8).toUpperCase());
        tvStatus.setText(currentStatus.replace("_", " "));

        long createdAt = order.has("createdAt") ? order.get("createdAt").getAsLong() : 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(createdAt)));

        tvCustomer.setText(getStringValue("customerName"));
        tvPhone.setText(getStringValue("customerPhone"));

        String fulfilment = getStringValue("fulfilmentMethod");
        if ("DELIVERY".equals(fulfilment) && order.has("deliveryAddress")) {
            JsonObject addr = order.getAsJsonObject("deliveryAddress");
            String address = String.format("%s, %s - %s",
                    addr.has("street") ? addr.get("street").getAsString() : "",
                    addr.has("city") ? addr.get("city").getAsString() : "",
                    addr.has("pincode") ? addr.get("pincode").getAsString() : "");
            tvAddress.setText(address);
            layoutAddress.setVisibility(View.VISIBLE);
        } else {
            layoutAddress.setVisibility(View.GONE);
        }

        double total = order.has("totalAmount") ? order.get("totalAmount").getAsDouble() : 0;
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

        // Configure action button
        configureActionButton();
    }

    private void configureActionButton() {
        switch (currentStatus) {
            case "PENDING":
                btnAction.setText("Accept Order");
                btnAction.setVisibility(View.VISIBLE);
                break;
            case "ACCEPTED":
                btnAction.setText("Start Preparing");
                btnAction.setVisibility(View.VISIBLE);
                break;
            case "PREPARING":
                btnAction.setText("Mark Ready");
                btnAction.setVisibility(View.VISIBLE);
                break;
            case "READY_FOR_PICKUP":
                btnAction.setText("Complete (Picked Up)");
                btnAction.setVisibility(View.VISIBLE);
                break;
            case "OUT_FOR_DELIVERY":
                btnAction.setText("Mark Delivered");
                btnAction.setVisibility(View.VISIBLE);
                break;
            default:
                btnAction.setVisibility(View.GONE);
        }
    }

    private void handleAction() {
        String nextStatus;
        switch (currentStatus) {
            case "PENDING": nextStatus = "ACCEPTED"; break;
            case "ACCEPTED": nextStatus = "PREPARING"; break;
            case "PREPARING": nextStatus = "READY_FOR_PICKUP"; break;
            case "READY_FOR_PICKUP": nextStatus = "PICKED_UP"; break;
            case "OUT_FOR_DELIVERY": nextStatus = "DELIVERED"; break;
            default: return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Update Order")
                .setMessage("Update order status to " + nextStatus.replace("_", " ") + "?")
                .setPositiveButton("Yes", (dialog, which) -> updateStatus(nextStatus))
                .setNegativeButton("No", null)
                .show();
    }

    private void updateStatus(String status) {
        progressBar.setVisibility(View.VISIBLE);
        btnAction.setEnabled(false);

        apiService.updateOrderStatus(orderId, status, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                btnAction.setEnabled(true);
                Toast.makeText(OrderFulfilmentActivity.this, "Order updated", Toast.LENGTH_SHORT).show();
                loadOrder();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnAction.setEnabled(true);
                Toast.makeText(OrderFulfilmentActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringValue(String key) {
        return order.has(key) && !order.get(key).isJsonNull() ? order.get(key).getAsString() : "";
    }
}
