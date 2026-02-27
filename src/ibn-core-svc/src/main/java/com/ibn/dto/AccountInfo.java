package com.ibn.dto;

public class AccountInfo {
    private String accountNumber;
    private double balance;
    private String customerId;

    public AccountInfo() {}

    public AccountInfo(String accountNumber, double balance, String customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
