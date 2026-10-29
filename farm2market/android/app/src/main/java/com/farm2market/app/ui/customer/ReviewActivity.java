package com.farm2market.app.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.google.gson.JsonObject;

public class ReviewActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT_NAME = "product_name";

    private ImageButton btnBack;
    private TextView tvProductName;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private ApiService apiService;
    private String orderId;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        apiService = ApiService.getInstance();

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        String productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);

        initViews();
        setupListeners();

        if (productName != null) {
            tvProductName.setText(productName);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvProductName = findViewById(R.id.tvProductName);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            btnSubmit.setEnabled(rating > 0);
        });

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        apiService.submitReview(orderId, productId, rating, comment, new ApiService.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ReviewActivity.this, "Review submitted! Thank you!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ReviewActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading && ratingBar.getRating() > 0);
        ratingBar.setIsIndicator(loading);
        etComment.setEnabled(!loading);
    }
}
