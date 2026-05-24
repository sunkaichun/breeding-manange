package com.wens.breeding.app.baseapp;

public final class BaseAppErrorResponse {
    private final String status;
    private final String message;

    public BaseAppErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
