package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "BILL_ACCOUNT", schema = "INB")
public class BillAccount {

    @Id
    @Column(name = "bill_account_id", columnDefinition = "UUID")
    private UUID billAccountId;

    @Column(name = "customer_id", columnDefinition = "UUID", nullable = false)
    private UUID customerId;

    @Column(name = "biller_id", columnDefinition = "UUID", nullable = false)
    private UUID billerId;

    @Column(name = "consumer_number", length = 60, nullable = false)
    private String consumerNumber;

    @Column(name = "nickname", length = 80)
    private String nickname;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "biller_id", referencedColumnName = "biller_id", insertable = false, updatable = false)
    private Biller biller;

    // Constructors
    public BillAccount() {
    }

    public BillAccount(UUID billAccountId, UUID customerId, UUID billerId, String consumerNumber, String status) {
        this.billAccountId = billAccountId;
        this.customerId = customerId;
        this.billerId = billerId;
        this.consumerNumber = consumerNumber;
        this.status = status;
    }

    // Getters and Setters
    public UUID getBillAccountId() {
        return billAccountId;
    }

    public void setBillAccountId(UUID billAccountId) {
        this.billAccountId = billAccountId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getBillerId() {
        return billerId;
    }

    public void setBillerId(UUID billerId) {
        this.billerId = billerId;
    }

    public String getConsumerNumber() {
        return consumerNumber;
    }

    public void setConsumerNumber(String consumerNumber) {
        this.consumerNumber = consumerNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public Biller getBiller() {
        return biller;
    }

    public void setBiller(Biller biller) {
        this.biller = biller;
    }
}
