package com.farm2market.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.farm2market.app.R;
import com.google.gson.JsonObject;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface CartItemListener {
        void onQuantityChanged(String cartItemId, int newQuantity);
        void onRemoveItem(String cartItemId);
    }

    private final Context context;
    private List<JsonObject> items;
    private final CartItemListener listener;

    public CartAdapter(Context context, List<JsonObject> items, CartItemListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<JsonObject> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject item = items.get(position);

        String cartItemId = getStringValue(item, "cartItemId");
        String productName = getStringValue(item, "productName");
        double price = item.has("priceSnapshot") ? item.get("priceSnapshot").getAsDouble() : 0;
        int quantity = item.has("quantity") ? item.get("quantity").getAsInt() : 1;
        String unit = getStringValue(item, "unit").toLowerCase();

        holder.tvName.setText(productName);
        holder.tvPrice.setText(String.format("₹%.2f/%s", price, unit));
        holder.tvQuantity.setText(String.valueOf(quantity));
        holder.tvItemTotal.setText(String.format("₹%.2f", price * quantity));

        String imageUrl = getStringValue(item, "productImage");
        if (!imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_farm)
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.ic_farm);
        }

        holder.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                listener.onQuantityChanged(cartItemId, quantity - 1);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            listener.onQuantityChanged(cartItemId, quantity + 1);
        });

        holder.btnRemove.setOnClickListener(v -> {
            listener.onRemoveItem(cartItemId);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String getStringValue(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvItemTotal;
        ImageButton btnMinus, btnPlus, btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
