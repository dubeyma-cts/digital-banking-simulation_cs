package com.ibn.dto;

public class FundTransferResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String reference;
    private String fromAccountNumber;
    private String payeeAccountNumber;
    private String payeeAccountHolderName;
    private double amount;
    private String currency;
    private String transferredAt;

    public FundTransferResponse() {
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getPayeeAccountNumber() {
        return payeeAccountNumber;
    }

    public void setPayeeAccountNumber(String payeeAccountNumber) {
        this.payeeAccountNumber = payeeAccountNumber;
    }

    public String getPayeeAccountHolderName() {
        return payeeAccountHolderName;
    }

    public void setPayeeAccountHolderName(String payeeAccountHolderName) {
        this.payeeAccountHolderName = payeeAccountHolderName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(String transferredAt) {
        this.transferredAt = transferredAt;
    }
}
