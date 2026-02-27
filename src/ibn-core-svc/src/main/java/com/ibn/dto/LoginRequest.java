package com.ibn.dto;

/**
 * DTO for login request containing customer credentials
 */
public class LoginRequest {
    private String customerId;
    private String password;
    private String captcha;

    public LoginRequest() {
    }

    public LoginRequest(String customerId, String password) {
        this.customerId = customerId;
        this.password = password;
    }

    public LoginRequest(String customerId, String password, String captcha) {
        this.customerId = customerId;
        this.password = password;
        this.captcha = captcha;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "customerId='" + customerId + '\'' +
                ", password='***'" +
                ", captcha='" + captcha + '\'' +
                '}';
    }
}
