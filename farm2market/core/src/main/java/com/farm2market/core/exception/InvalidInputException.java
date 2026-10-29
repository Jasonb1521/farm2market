package com.farm2market.core.exception;

import java.util.Collections;
import java.util.Map;

public class InvalidInputException extends Farm2MarketException {
    private final String field;
    private final Map<String, String> fieldErrors;

    public InvalidInputException(String message) {
        super(message, "INVALID_INPUT");
        this.field = null;
        this.fieldErrors = Collections.emptyMap();
    }

    public InvalidInputException(String field, String message) {
        super(String.format("Invalid %s: %s", field, message), "INVALID_INPUT");
        this.field = field;
        this.fieldErrors = Map.of(field, message);
    }

    public InvalidInputException(Map<String, String> fieldErrors) {
        super("Validation failed for multiple fields", "INVALID_INPUT");
        this.field = null;
        this.fieldErrors = fieldErrors;
    }

    public String getField() {
        return field;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasMultipleErrors() {
        return fieldErrors.size() > 1;
    }
}
