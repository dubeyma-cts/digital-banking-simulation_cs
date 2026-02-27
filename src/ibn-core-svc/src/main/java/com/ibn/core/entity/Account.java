package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ACCOUNT", schema = "INB")
public class Account {

    @Id
    @Column(name = "account_id", columnDefinition = "UUID")
    private UUID accountId;

    @Column(name = "customer_id", columnDefinition = "UUID", nullable = false)
    private UUID customerId;

    @Column(name = "account_number", length = 30, nullable = false)
    private String accountNumber;

    @Column(name = "account_type", length = 20, nullable = false)
    private String accountType;

    @Column(name = "branch_code", length = 20, nullable = false)
    private String branchCode;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "opened_at", nullable = false)
    private LocalDate openedAt;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    // Constructors
    public Account() {
    }

    public Account(UUID accountId, UUID customerId, String accountNumber, String accountType, String currency) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currency = currency;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDate openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDate getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDate closedAt) {
        this.closedAt = closedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
