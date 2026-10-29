package com.farm2market.core.exception;

public class UnauthorizedActionException extends Farm2MarketException {
    private final String userId;
    private final String action;
    private final String resourceType;
    private final String resourceId;

    public UnauthorizedActionException(String message) {
        super(message, "UNAUTHORIZED");
        this.userId = null;
        this.action = null;
        this.resourceType = null;
        this.resourceId = null;
    }

    public UnauthorizedActionException(String userId, String action) {
        super(String.format("User %s is not authorized to %s", userId, action), "UNAUTHORIZED");
        this.userId = userId;
        this.action = action;
        this.resourceType = null;
        this.resourceId = null;
    }

    public UnauthorizedActionException(String userId, String action, String resourceType, String resourceId) {
        super(String.format("User %s is not authorized to %s %s %s",
                userId, action, resourceType, resourceId), "UNAUTHORIZED");
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
