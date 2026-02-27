package com.ibn.dto;

public class FundTransferRequest {
    private String customerId;
    private String sourceAccountId;
    private String payeeAccountNumber;
    private String confirmPayeeAccountNumber;
    private Double amount;
    private String remarks;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getPayeeAccountNumber() {
        return payeeAccountNumber;
    }

    public void setPayeeAccountNumber(String payeeAccountNumber) {
        this.payeeAccountNumber = payeeAccountNumber;
    }

    public String getConfirmPayeeAccountNumber() {
        return confirmPayeeAccountNumber;
    }

    public void setConfirmPayeeAccountNumber(String confirmPayeeAccountNumber) {
        this.confirmPayeeAccountNumber = confirmPayeeAccountNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
