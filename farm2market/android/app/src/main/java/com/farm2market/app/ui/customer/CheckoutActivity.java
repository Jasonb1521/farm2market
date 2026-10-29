package com.farm2market.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

public class CheckoutActivity extends AppCompatActivity {

    private TextInputEditText etAddress, etCity, etPincode, etPhone, etNotes;
    private RadioGroup rgPayment, rgFulfilment;
    private TextView tvSubtotal, tvDelivery, tvTotal;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private ApiService apiService;
    private double subtotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        subtotal = getIntent().getDoubleExtra("subtotal", 0);

        initViews();
        updateTotals();
    }

    private void initViews() {
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etPincode = findViewById(R.id.etPincode);
        etPhone = findViewById(R.id.etPhone);
        etNotes = findViewById(R.id.etNotes);
        rgPayment = findViewById(R.id.rgPayment);
        rgFulfilment = findViewById(R.id.rgFulfilment);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        rgFulfilment.setOnCheckedChangeListener((group, checkedId) -> updateTotals());
    }

    private void updateTotals() {
        boolean isDelivery = rgFulfilment.getCheckedRadioButtonId() == R.id.rbDelivery;
        double delivery = isDelivery ? (subtotal > 500 ? 0 : 40) : 0;
        double total = subtotal + delivery;

        tvSubtotal.setText(String.format("₹%.2f", subtotal));
        tvDelivery.setText(delivery > 0 ? String.format("₹%.2f", delivery) : "FREE");
        tvTotal.setText(String.format("₹%.2f", total));
    }

    private void placeOrder() {
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        boolean isDelivery = rgFulfilment.getCheckedRadioButtonId() == R.id.rbDelivery;

        if (isDelivery) {
            if (address.isEmpty()) {
                etAddress.setError("Required for delivery");
                return;
            }
            if (city.isEmpty()) {
                etCity.setError("Required");
                return;
            }
            if (pincode.isEmpty()) {
                etPincode.setError("Required");
                return;
            }
        }

        if (phone.isEmpty() || phone.length() < 10) {
            etPhone.setError("Valid phone required");
            return;
        }

        String paymentMethod = rgPayment.getCheckedRadioButtonId() == R.id.rbCOD
                ? "CASH_ON_DELIVERY" : "PAY_ON_PICKUP";
        String fulfilmentMethod = isDelivery ? "DELIVERY" : "PICKUP";

        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        JsonObject orderRequest = new JsonObject();
        if (isDelivery) {
            JsonObject addressObj = new JsonObject();
            addressObj.addProperty("street", address);
            addressObj.addProperty("city", city);
            addressObj.addProperty("pincode", pincode);
            orderRequest.add("deliveryAddress", addressObj);
        }
        orderRequest.addProperty("phone", phone);
        orderRequest.addProperty("notes", notes);
        orderRequest.addProperty("paymentMethod", paymentMethod);
        orderRequest.addProperty("fulfilmentMethod", fulfilmentMethod);

        apiService.placeOrder(orderRequest, new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();

                String orderId = result.has("orderId") ? result.get("orderId").getAsString() : "";
                Intent intent = new Intent(CheckoutActivity.this, OrderDetailActivity.class);
                intent.putExtra("orderId", orderId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
