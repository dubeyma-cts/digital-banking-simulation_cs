package com.ibn.dto;

public class PayeeValidationResponse {
    private boolean valid;
    private String accountHolderName;
    private String accountId;
    private String message;

    public PayeeValidationResponse() {
    }

    public PayeeValidationResponse(boolean valid, String accountHolderName, String accountId, String message) {
        this.valid = valid;
        this.accountHolderName = accountHolderName;
        this.accountId = accountId;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
