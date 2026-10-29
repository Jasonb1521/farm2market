package com.farm2market.app.ui.farmer;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.farm2market.app.R;
import com.farm2market.app.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

public class AddEditProductActivity extends AppCompatActivity {

    private TextInputEditText etName, etDescription, etPrice, etStock;
    private Spinner spinnerCategory, spinnerUnit;
    private SwitchCompat switchOrganic;
    private Button btnSave;
    private ProgressBar progressBar;

    private ApiService apiService;
    private String productId;
    private boolean isEditing = false;

    private static final String[] CATEGORIES = {"VEGETABLES", "FRUITS", "GRAINS", "DAIRY", "PULSES", "HERBS", "EGGS", "OTHER"};
    private static final String[] UNITS = {"KG", "GRAM", "PIECE", "DOZEN", "LITRE", "BUNDLE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        productId = getIntent().getStringExtra("productId");
        isEditing = productId != null;

        initViews();
        setupSpinners();

        if (isEditing) {
            loadProduct();
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        switchOrganic = findViewById(R.id.switchOrganic);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiService.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());

        ((android.widget.TextView) findViewById(R.id.tvTitle))
                .setText(isEditing ? "Edit Product" : "Add Product");
        btnSave.setText(isEditing ? "Update" : "Add Product");
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
    }

    private void loadProduct() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProduct(productId, new ApiService.ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                progressBar.setVisibility(View.GONE);
                populateForm(result);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddEditProductActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateForm(JsonObject product) {
        etName.setText(getStringValue(product, "name"));
        etDescription.setText(getStringValue(product, "description"));

        if (product.has("price")) {
            etPrice.setText(String.valueOf(product.get("price").getAsDouble()));
        }
        if (product.has("stockQuantity")) {
            etStock.setText(String.valueOf(product.get("stockQuantity").getAsDouble()));
        }

        String category = getStringValue(product, "category");
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equalsIgnoreCase(category)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        String unit = getStringValue(product, "unit");
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(unit)) {
                spinnerUnit.setSelection(i);
                break;
            }
        }

        if (product.has("organic")) {
            switchOrganic.setChecked(product.get("organic").getAsBoolean());
        }
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError("Required");
            return;
        }
        if (stockStr.isEmpty()) {
            etStock.setError("Required");
            return;
        }

        double price, stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Double.parseDouble(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = CATEGORIES[spinnerCategory.getSelectedItemPosition()];
        String unit = UNITS[spinnerUnit.getSelectedItemPosition()];
        boolean organic = switchOrganic.isChecked();

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (isEditing) {
            apiService.updateProduct(productId, name, description, category, price, unit, stock, organic,
                    new ApiService.ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(AddEditProductActivity.this, "Product updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(AddEditProductActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            apiService.createProduct(name, description, category, price, unit, stock, organic,
                    new ApiService.ApiCallback<JsonObject>() {
                        @Override
                        public void onSuccess(JsonObject result) {
                            Toast.makeText(AddEditProductActivity.this, "Product created", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(AddEditProductActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String getStringValue(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
