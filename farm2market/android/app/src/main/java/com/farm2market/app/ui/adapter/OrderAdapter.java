package com.farm2market.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.farm2market.app.R;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(JsonObject order);
    }

    private final Context context;
    private List<JsonObject> orders;
    private OnItemClickListener listener;

    public OrderAdapter(Context context, List<JsonObject> orders) {
        this.context = context;
        this.orders = orders;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateOrders(List<JsonObject> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject order = orders.get(position);

        String orderId = getStringValue(order, "orderId");
        String displayId = orderId.length() > 8 ? orderId.substring(orderId.length() - 8) : orderId;
        holder.tvOrderId.setText("#" + displayId.toUpperCase());

        String status = getStringValue(order, "orderStatus");
        holder.tvStatus.setText(status.replace("_", " "));
        setStatusColor(holder.tvStatus, status);

        long createdAt = order.has("createdAt") ? order.get("createdAt").getAsLong() : 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(createdAt)));

        double total = order.has("total") ? order.get("total").getAsDouble() :
                      (order.has("totalAmount") ? order.get("totalAmount").getAsDouble() : 0);
        holder.tvTotal.setText(String.format("₹%.2f", total));

        int itemCount = 0;
        if (order.has("items") && order.get("items").isJsonArray()) {
            itemCount = order.getAsJsonArray("items").size();
        } else if (order.has("itemCount")) {
            itemCount = order.get("itemCount").getAsInt();
        }
        holder.tvItemCount.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));

        holder.tvFarmer.setText(getStringValue(order, "farmerName"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(order);
            }
        });
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
        TextView tvOrderId, tvStatus, tvDate, tvTotal, tvItemCount, tvFarmer;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvFarmer = itemView.findViewById(R.id.tvFarmer);
        }
    }
}
