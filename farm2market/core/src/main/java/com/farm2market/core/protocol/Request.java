package com.farm2market.core.protocol;

public class Request {
    private MessageType type;
    private String token;
    private String requestId;
    private long timestamp;
    private Object payload;

    public Request() {
        this.timestamp = System.currentTimeMillis();
        this.requestId = generateRequestId();
    }

    public Request(MessageType type) {
        this();
        this.type = type;
    }

    public Request(MessageType type, Object payload) {
        this();
        this.type = type;
        this.payload = payload;
    }

    public Request(MessageType type, String token, Object payload) {
        this();
        this.type = type;
        this.token = token;
        this.payload = payload;
    }

    private String generateRequestId() {
        return String.format("%d-%d", System.currentTimeMillis(), (int)(Math.random() * 10000));
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public boolean hasToken() {
        return token != null && !token.isEmpty();
    }
}
