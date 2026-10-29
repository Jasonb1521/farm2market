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

import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.farm2market.app.ui.adapter.FarmerOrderAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class IncomingOrdersActivity extends AppCompatActivity implements FarmerOrderAdapter.OrderActionListener {

    private RecyclerView rvOrders;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private FarmerOrderAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_orders);

        initViews();
        setupRecyclerView();
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadOrders);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void setupRecyclerView() {
        adapter = new FarmerOrderAdapter(this, new ArrayList<>(), this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        apiService.getFarmerOrders(new ApiService.ApiCallback<JsonArray>() {
            @Override
            public void onSuccess(JsonArray result) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                displayOrders(result);
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(IncomingOrdersActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrders(JsonArray orders) {
        List<JsonObject> orderList = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            orderList.add(orders.get(i).getAsJsonObject());
        }

        adapter.updateOrders(orderList);

        tvEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAccept(String orderId) {
        updateStatus(orderId, "ACCEPTED");
    }

    @Override
    public void onReject(String orderId) {
        updateStatus(orderId, "REJECTED");
    }

    @Override
    public void onMarkPreparing(String orderId) {
        updateStatus(orderId, "PREPARING");
    }

    @Override
    public void onMarkReady(String orderId) {
        updateStatus(orderId, "READY_FOR_PICKUP");
    }

    @Override
    public void onViewDetails(String orderId) {
        Intent intent = new Intent(this, OrderFulfilmentActivity.class);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }

    private void updateStatus(String orderId, String status) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.updateOrderStatus(orderId, status, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(IncomingOrdersActivity.this, "Order updated", Toast.LENGTH_SHORT).show();
                loadOrders();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(IncomingOrdersActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
