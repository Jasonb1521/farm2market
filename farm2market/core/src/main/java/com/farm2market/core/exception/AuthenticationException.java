package com.farm2market.core.exception;

public class AuthenticationException extends Farm2MarketException {

    public enum Reason {
        INVALID_CREDENTIALS("Invalid email or password"),
        ACCOUNT_SUSPENDED("Your account has been suspended"),
        ACCOUNT_DELETED("This account no longer exists"),
        SESSION_EXPIRED("Your session has expired. Please login again"),
        INVALID_TOKEN("Invalid or expired token"),
        EMAIL_ALREADY_EXISTS("An account with this email already exists"),
        MOBILE_ALREADY_EXISTS("An account with this mobile number already exists");

        private final String message;

        Reason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final Reason reason;

    public AuthenticationException(Reason reason) {
        super(reason.getMessage(), "AUTH_ERROR");
        this.reason = reason;
    }

    public AuthenticationException(String message) {
        super(message, "AUTH_ERROR");
        this.reason = null;
    }

    public AuthenticationException(Reason reason, Throwable cause) {
        super(reason.getMessage(), "AUTH_ERROR", cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
