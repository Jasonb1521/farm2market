package com.farm2market.core.service;

import com.farm2market.core.exception.InvalidInputException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "^[6-9]\\d{9}$"
    );

    private static final Pattern PINCODE_PATTERN = Pattern.compile(
            "^\\d{6}$"
    );

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    public void validateEmail(String email) throws InvalidInputException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("email", "Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("email", "Invalid email format");
        }
    }

    public void validateMobile(String mobile) throws InvalidInputException {
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new InvalidInputException("mobile", "Mobile number is required");
        }
        String cleaned = mobile.replaceAll("[^0-9]", "");
        if (!MOBILE_PATTERN.matcher(cleaned).matches()) {
            throw new InvalidInputException("mobile", "Invalid mobile number. Must be 10 digits starting with 6-9");
        }
    }

    public void validatePassword(String password) throws InvalidInputException {
        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("password", "Password is required");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException("password",
                    String.format("Password must be at least %d characters", MIN_PASSWORD_LENGTH));
        }
    }

    public void validateName(String name) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("name", "Name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException("name",
                    String.format("Name must not exceed %d characters", MAX_NAME_LENGTH));
        }
    }

    public void validatePrice(double price) throws InvalidInputException {
        if (price <= 0) {
            throw new InvalidInputException("price", "Price must be greater than 0");
        }
        if (price > 1000000) {
            throw new InvalidInputException("price", "Price seems too high");
        }
    }

    public void validateQuantity(double quantity) throws InvalidInputException {
        if (quantity < 0) {
            throw new InvalidInputException("quantity", "Quantity cannot be negative");
        }
    }

    public void validateStock(double stock) throws InvalidInputException {
        if (stock < 0) {
            throw new InvalidInputException("stock", "Stock cannot be negative");
        }
    }

    public void validatePincode(String pincode) throws InvalidInputException {
        if (pincode == null || pincode.trim().isEmpty()) {
            throw new InvalidInputException("pincode", "Pincode is required");
        }
        if (!PINCODE_PATTERN.matcher(pincode.trim()).matches()) {
            throw new InvalidInputException("pincode", "Invalid pincode. Must be 6 digits");
        }
    }

    public void validateRequired(String value, String fieldName) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName, fieldName + " is required");
        }
    }

    public void validateDescription(String description) throws InvalidInputException {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidInputException("description",
                    String.format("Description must not exceed %d characters", MAX_DESCRIPTION_LENGTH));
        }
    }

    public void validateRating(int rating) throws InvalidInputException {
        if (rating < 1 || rating > 5) {
            throw new InvalidInputException("rating", "Rating must be between 1 and 5");
        }
    }

    public ValidationResult validate(Runnable... validations) {
        Map<String, String> errors = new HashMap<>();
        for (Runnable validation : validations) {
            try {
                validation.run();
            } catch (InvalidInputException e) {
                if (e.getField() != null) {
                    errors.put(e.getField(), e.getMessage());
                }
            }
        }
        return new ValidationResult(errors);
    }

    public static class ValidationResult {
        private final Map<String, String> errors;

        public ValidationResult(Map<String, String> errors) {
            this.errors = errors;
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public void throwIfInvalid() throws InvalidInputException {
            if (!isValid()) {
                throw new InvalidInputException(errors);
            }
        }
    }
}
