package com.ibn.dto;

/**
 * DTO for remaining login attempts response
 */
public class RemainingAttemptsResponse {
    private String customerId;
    private int remainingAttempts;
    private String message;

    public RemainingAttemptsResponse() {
    }

    public RemainingAttemptsResponse(String customerId, int remainingAttempts, String message) {
        this.customerId = customerId;
        this.remainingAttempts = remainingAttempts;
        this.message = message;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RemainingAttemptsResponse{" +
                "customerId='" + customerId + '\'' +
                ", remainingAttempts=" + remainingAttempts +
                ", message='" + message + '\'' +
                '}';
    }
}
