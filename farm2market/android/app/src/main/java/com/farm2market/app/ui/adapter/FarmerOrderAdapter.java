package com.farm2market.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.farm2market.app.R;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FarmerOrderAdapter extends RecyclerView.Adapter<FarmerOrderAdapter.ViewHolder> {

    public interface OrderActionListener {
        void onAccept(String orderId);
        void onReject(String orderId);
        void onMarkPreparing(String orderId);
        void onMarkReady(String orderId);
        void onViewDetails(String orderId);
    }

    private final Context context;
    private List<JsonObject> orders;
    private final OrderActionListener listener;

    public FarmerOrderAdapter(Context context, List<JsonObject> orders, OrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    public void updateOrders(List<JsonObject> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farmer_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject order = orders.get(position);
        String orderId = getStringValue(order, "orderId");
        String status = getStringValue(order, "orderStatus");

        holder.tvOrderId.setText("#" + orderId.substring(0, 8).toUpperCase());
        holder.tvStatus.setText(status.replace("_", " "));
        setStatusColor(holder.tvStatus, status);

        holder.tvCustomer.setText(getStringValue(order, "customerName"));

        long createdAt = order.has("createdAt") ? order.get("createdAt").getAsLong() : 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(createdAt)));

        double total = order.has("totalAmount") ? order.get("totalAmount").getAsDouble() : 0;
        holder.tvTotal.setText(String.format("₹%.2f", total));

        int itemCount = order.has("itemCount") ? order.get("itemCount").getAsInt() : 0;
        holder.tvItems.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));

        // Configure buttons based on status
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);
        holder.btnPreparing.setVisibility(View.GONE);
        holder.btnReady.setVisibility(View.GONE);

        switch (status) {
            case "PENDING":
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                break;
            case "ACCEPTED":
                holder.btnPreparing.setVisibility(View.VISIBLE);
                break;
            case "PREPARING":
                holder.btnReady.setVisibility(View.VISIBLE);
                break;
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(orderId));
        holder.btnReject.setOnClickListener(v -> listener.onReject(orderId));
        holder.btnPreparing.setOnClickListener(v -> listener.onMarkPreparing(orderId));
        holder.btnReady.setOnClickListener(v -> listener.onMarkReady(orderId));
        holder.itemView.setOnClickListener(v -> listener.onViewDetails(orderId));
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    private void setStatusColor(TextView tv, String status) {
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
        tv.setTextColor(context.getResources().getColor(color, null));
    }

    private String getStringValue(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvCustomer, tvDate, tvTotal, tvItems;
        Button btnAccept, btnReject, btnPreparing, btnReady;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvItems = itemView.findViewById(R.id.tvItems);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnPreparing = itemView.findViewById(R.id.btnPreparing);
            btnReady = itemView.findViewById(R.id.btnReady);
        }
    }
}
