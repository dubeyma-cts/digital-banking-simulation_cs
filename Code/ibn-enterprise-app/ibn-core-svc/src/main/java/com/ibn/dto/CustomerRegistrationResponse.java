package com.ibn.dto;

public class CustomerRegistrationResponse {
    private boolean success;
    private String message;
    private String customerId;

    public CustomerRegistrationResponse() {
    }

    public CustomerRegistrationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CustomerRegistrationResponse(boolean success, String message, String customerId) {
        this.success = success;
        this.message = message;
        this.customerId = customerId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
