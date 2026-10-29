package com.farm2market.app.ui.common;

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
import com.farm2market.app.ui.adapter.NotificationAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private NotificationAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnMarkAllRead).setOnClickListener(v -> markAllAsRead());

        swipeRefresh.setOnRefreshListener(this::loadNotifications);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, new ArrayList<>());
        adapter.setOnItemClickListener(notification -> {
            String notificationId = notification.has("notificationId")
                    ? notification.get("notificationId").getAsString() : "";
            markAsRead(notificationId);
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        apiService.getNotifications(new ApiService.ApiCallback<JsonArray>() {
            @Override
            public void onSuccess(JsonArray result) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                displayNotifications(result);
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNotifications(JsonArray notifications) {
        List<JsonObject> notificationList = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i++) {
            notificationList.add(notifications.get(i).getAsJsonObject());
        }

        adapter.updateNotifications(notificationList);

        tvEmpty.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(notificationList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void markAsRead(String notificationId) {
        apiService.markNotificationRead(notificationId, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadNotifications();
            }

            @Override
            public void onError(String error) {
                // Ignore
            }
        });
    }

    private void markAllAsRead() {
        apiService.markAllNotificationsRead(new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(NotificationsActivity.this, "All marked as read", Toast.LENGTH_SHORT).show();
                loadNotifications();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
