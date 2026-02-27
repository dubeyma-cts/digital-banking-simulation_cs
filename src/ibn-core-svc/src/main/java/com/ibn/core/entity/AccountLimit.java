package com.ibn.core.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ACCOUNT_LIMIT", schema = "INB")
public class AccountLimit {

    @Id
    @Column(name = "limit_id", columnDefinition = "UUID")
    private UUID limitId;

    @Column(name = "account_id", columnDefinition = "UUID", nullable = false)
    private UUID accountId;

    @Column(name = "limit_type", length = 40, nullable = false)
    private String limitType;

    @Column(name = "limit_value", precision = 18, scale = 2, nullable = false)
    private BigDecimal limitValue;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "configured_by", columnDefinition = "UUID", nullable = false)
    private UUID configuredBy;

    @Column(name = "configured_at", nullable = false)
    private LocalDateTime configuredAt;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "configured_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User configuredByUser;

    // Constructors
    public AccountLimit() {
    }

    public AccountLimit(UUID limitId, UUID accountId, String limitType, BigDecimal limitValue) {
        this.limitId = limitId;
        this.accountId = accountId;
        this.limitType = limitType;
        this.limitValue = limitValue;
    }

    // Getters and Setters
    public UUID getLimitId() {
        return limitId;
    }

    public void setLimitId(UUID limitId) {
        this.limitId = limitId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public BigDecimal getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(BigDecimal limitValue) {
        this.limitValue = limitValue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public UUID getConfiguredBy() {
        return configuredBy;
    }

    public void setConfiguredBy(UUID configuredBy) {
        this.configuredBy = configuredBy;
    }

    public LocalDateTime getConfiguredAt() {
        return configuredAt;
    }

    public void setConfiguredAt(LocalDateTime configuredAt) {
        this.configuredAt = configuredAt;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public User getConfiguredByUser() {
        return configuredByUser;
    }

    public void setConfiguredByUser(User configuredByUser) {
        this.configuredByUser = configuredByUser;
    }
}
