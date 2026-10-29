package com.farm2market.app.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvName, tvPrice, tvFarmer, tvDescription, tvStock, tvCategory;
    private Button btnAddToCart;
    private ProgressBar progressBar;

    private ApiService apiService;
    private String productId;
    private JsonObject product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            finish();
            return;
        }

        initViews();
        loadProduct();
    }

    private void initViews() {
        ivProduct = findViewById(R.id.ivProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvFarmer = findViewById(R.id.tvFarmer);
        tvDescription = findViewById(R.id.tvDescription);
        tvStock = findViewById(R.id.tvStock);
        tvCategory = findViewById(R.id.tvCategory);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProduct() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProduct(productId, new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                progressBar.setVisibility(View.GONE);
                product = result;
                displayProduct();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProduct() {
        tvName.setText(getStringValue("name"));

        double price = product.has("price") ? product.get("price").getAsDouble() : 0;
        String unit = getStringValue("unit").toLowerCase();
        tvPrice.setText(String.format("₹%.2f/%s", price, unit));

        tvFarmer.setText("By " + getStringValue("farmerName"));
        tvDescription.setText(getStringValue("description"));
        tvCategory.setText(getStringValue("category"));

        double stock = product.has("stockQuantity") ? product.get("stockQuantity").getAsDouble() : 0;
        tvStock.setText(String.format("%.1f %s available", stock, unit));

        String imageUrl = null;
        if (product.has("imageUrls")) {
            JsonArray images = product.getAsJsonArray("imageUrls");
            if (images != null && images.size() > 0) {
                imageUrl = images.get(0).getAsString();
            }
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_farm)
                    .error(R.drawable.ic_farm)
                    .centerCrop()
                    .into(ivProduct);
        }

        btnAddToCart.setEnabled(stock > 0);
        btnAddToCart.setText(stock > 0 ? "Add to Cart" : "Out of Stock");
    }

    private void addToCart() {
        btnAddToCart.setEnabled(false);

        apiService.addToCart(productId, 1, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                btnAddToCart.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                btnAddToCart.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringValue(String key) {
        return product.has(key) && !product.get(key).isJsonNull() ? product.get(key).getAsString() : "";
    }
}
