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

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private final Context context;
    private List<JsonObject> items;

    public OrderItemAdapter(Context context, List<JsonObject> items) {
        this.context = context;
        this.items = items;
    }

    public void updateItems(List<JsonObject> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position);

        holder.tvName.setText(getStringValue(item, "productName"));

        double quantity = item.has("quantity") ? item.get("quantity").getAsDouble() : 0;
        double price = item.has("priceSnapshot") ? item.get("priceSnapshot").getAsDouble() : 0;
        String unit = getStringValue(item, "unit").toLowerCase();

        holder.tvQuantity.setText(String.format("%.0f %s × ₹%.2f", quantity, unit, price));
        holder.tvTotal.setText(String.format("₹%.2f", quantity * price));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String getStringValue(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvTotal;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}
