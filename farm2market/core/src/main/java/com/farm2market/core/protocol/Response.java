package com.farm2market.core.protocol;

public class Response {
    private MessageType type;
    private String requestId;
    private boolean success;
    private long timestamp;
    private Object payload;
    private ErrorInfo error;

    public Response() {
        this.timestamp = System.currentTimeMillis();
    }

    public Response(MessageType type, String requestId, boolean success) {
        this();
        this.type = type;
        this.requestId = requestId;
        this.success = success;
    }

    public static Response success(MessageType type, String requestId, Object payload) {
        Response response = new Response(type, requestId, true);
        response.setPayload(payload);
        return response;
    }

    public static Response error(String requestId, String errorCode, String message) {
        Response response = new Response(MessageType.ERROR_RESPONSE, requestId, false);
        response.setError(new ErrorInfo(errorCode, message));
        return response;
    }

    public static Response error(String requestId, ErrorInfo error) {
        Response response = new Response(MessageType.ERROR_RESPONSE, requestId, false);
        response.setError(error);
        return response;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public ErrorInfo getError() {
        return error;
    }

    public void setError(ErrorInfo error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }

    public static class ErrorInfo {
        private String code;
        private String message;
        private Object details;

        public ErrorInfo() {}

        public ErrorInfo(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public ErrorInfo(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getDetails() { return details; }
        public void setDetails(Object details) { this.details = details; }
    }
}
