package com.ibn.dto;

/**
 * DTO for CAPTCHA validation request
 */
public class CaptchaRequest {
    private String captcha;

    public CaptchaRequest() {
    }

    public CaptchaRequest(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    @Override
    public String toString() {
        return "CaptchaRequest{" +
                "captcha='" + captcha + '\'' +
                '}';
    }
}
