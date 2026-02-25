package com.ibn.dto;

/**
 * DTO for account status response
 */
public class AccountStatusResponse {
    private String customerId;
    private String status;
    private boolean locked;
    private String message;
    private int remainingAttempts;

    public AccountStatusResponse() {
    }

    public AccountStatusResponse(String customerId, String status, boolean locked, String message) {
        this.customerId = customerId;
        this.status = status;
        this.locked = locked;
        this.message = message;
    }

    public AccountStatusResponse(String customerId, String status, boolean locked, String message, int remainingAttempts) {
        this.customerId = customerId;
        this.status = status;
        this.locked = locked;
        this.message = message;
        this.remainingAttempts = remainingAttempts;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    @Override
    public String toString() {
        return "AccountStatusResponse{" +
                "customerId='" + customerId + '\'' +
                ", status='" + status + '\'' +
                ", locked=" + locked +
                ", message='" + message + '\'' +
                ", remainingAttempts=" + remainingAttempts +
                '}';
    }
}
