package com.ibn.dto;

/**
 * DTO for CAPTCHA validation response
 */
public class CaptchaResponse {
    private boolean valid;
    private String message;

    public CaptchaResponse() {
    }

    public CaptchaResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CaptchaResponse{" +
                "valid=" + valid +
                ", message='" + message + '\'' +
                '}';
    }
}
