package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"TRANSACTION\"", schema = "INB")
public class Transaction {

    @Id
    @Column(name = "txn_id", columnDefinition = "UUID")
    private UUID txnId;

    @Column(name = "account_id", columnDefinition = "UUID", nullable = false)
    private UUID accountId;

    @Column(name = "txn_type", length = 30, nullable = false)
    private String txnType;

    @Column(name = "direction", length = 1, nullable = false)
    private String direction;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private java.math.BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "reference", length = 64)
    private String reference;

    @Column(name = "related_entity_type", length = 40)
    private String relatedEntityType;

    @Column(name = "related_entity_id", columnDefinition = "UUID")
    private UUID relatedEntityId;

    @Column(name = "narration", length = 250)
    private String narration;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    private Account account;

    // Constructors
    public Transaction() {
    }

    public Transaction(UUID txnId, UUID accountId, String txnType, String direction, java.math.BigDecimal amount) {
        this.txnId = txnId;
        this.accountId = accountId;
        this.txnType = txnType;
        this.direction = direction;
        this.amount = amount;
    }

    // Getters and Setters
    public UUID getTxnId() {
        return txnId;
    }

    public void setTxnId(UUID txnId) {
        this.txnId = txnId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public UUID getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(UUID relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
