package com.ibn.dto;

/**
 * DTO for login response containing authentication details
 */
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String userId;
    private String email;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, String token, String userId, String email) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.userId = userId;
        this.email = email;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
