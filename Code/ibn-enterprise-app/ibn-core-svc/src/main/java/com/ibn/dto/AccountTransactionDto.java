package com.ibn.dto;

public class AccountTransactionDto {
    private String date;
    private String description;
    private String type;
    private double amount;

    public AccountTransactionDto() {
    }

    public AccountTransactionDto(String date, String description, String type, double amount) {
        this.date = date;
        this.description = description;
        this.type = type;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
