package com.farm2market.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.farm2market.app.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private List<JsonObject> products;
    private OnItemClickListener listener;
    private boolean farmerMode = false;

    public interface OnItemClickListener {
        void onItemClick(JsonObject product);
    }

    public ProductAdapter(Context context, List<JsonObject> products) {
        this.context = context;
        this.products = products;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFarmerMode(boolean farmerMode) {
        this.farmerMode = farmerMode;
    }

    public void updateProducts(List<JsonObject> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                farmerMode ? R.layout.item_product_farmer : R.layout.item_product,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject product = products.get(position);

        holder.tvName.setText(getStringValue(product, "name"));

        double price = product.has("price") ? product.get("price").getAsDouble() : 0;
        String unit = getStringValue(product, "unit").toLowerCase();
        holder.tvPrice.setText(String.format("₹%.2f/%s", price, unit));

        if (holder.tvFarmer != null) {
            String farmerName = getStringValue(product, "farmerName");
            if (!farmerName.isEmpty()) {
                holder.tvFarmer.setText(farmerName);
                holder.tvFarmer.setVisibility(View.VISIBLE);
            } else {
                holder.tvFarmer.setVisibility(View.GONE);
            }
        }

        if (holder.tvStock != null) {
            double stock = product.has("stockQuantity") ? product.get("stockQuantity").getAsDouble() : 0;
            holder.tvStock.setText(String.format("Stock: %.1f %s", stock, unit));
        }

        if (holder.tvStatus != null) {
            String status = getStringValue(product, "status");
            holder.tvStatus.setText(status);
            holder.tvStatus.setVisibility(View.VISIBLE);
        }

        // Load image
        String imageUrl = null;
        if (product.has("imageUrls")) {
            JsonArray images = product.getAsJsonArray("imageUrls");
            if (images != null && images.size() > 0) {
                imageUrl = images.get(0).getAsString();
            }
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_farm)
                    .error(R.drawable.ic_farm)
                    .centerCrop()
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.ic_farm);
        }

        // Tags
        if (holder.tvOrganic != null) {
            boolean organic = product.has("organic") && product.get("organic").getAsBoolean();
            holder.tvOrganic.setVisibility(organic ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    private String getStringValue(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvFarmer, tvStock, tvStatus, tvOrganic;

        ViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvFarmer = itemView.findViewById(R.id.tvFarmer);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrganic = itemView.findViewById(R.id.tvOrganic);
        }
    }
}
