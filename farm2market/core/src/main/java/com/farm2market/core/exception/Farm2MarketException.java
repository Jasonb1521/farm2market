package com.farm2market.core.exception;

public class Farm2MarketException extends RuntimeException {
    private final String errorCode;

    public Farm2MarketException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }

    public Farm2MarketException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public Farm2MarketException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
    }

    public Farm2MarketException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
