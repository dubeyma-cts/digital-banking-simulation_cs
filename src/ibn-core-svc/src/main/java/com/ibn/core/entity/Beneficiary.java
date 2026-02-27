package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "BENEFICIARY", schema = "INB")
public class Beneficiary {

    @Id
    @Column(name = "beneficiary_id", columnDefinition = "UUID")
    private UUID beneficiaryId;

    @Column(name = "customer_id", columnDefinition = "UUID", nullable = false)
    private UUID customerId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "ifsc", length = 16)
    private String ifsc;

    @Column(name = "account_number", length = 30, nullable = false)
    private String accountNumber;

    @Column(name = "beneficiary_type", length = 20, nullable = false)
    private String beneficiaryType;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    // Constructors
    public Beneficiary() {
    }

    public Beneficiary(UUID beneficiaryId, UUID customerId, String name, String accountNumber) {
        this.beneficiaryId = beneficiaryId;
        this.customerId = customerId;
        this.name = name;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters
    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBeneficiaryType() {
        return beneficiaryType;
    }

    public void setBeneficiaryType(String beneficiaryType) {
        this.beneficiaryType = beneficiaryType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
