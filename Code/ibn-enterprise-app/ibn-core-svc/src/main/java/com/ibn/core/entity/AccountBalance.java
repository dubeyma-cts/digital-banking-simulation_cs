package com.ibn.core.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ACCOUNT_BALANCE", schema = "INB")
public class AccountBalance {

    @Id
    @Column(name = "account_id", columnDefinition = "UUID")
    private UUID accountId;

    @Column(name = "available_balance", precision = 18, scale = 2, nullable = false)
    private BigDecimal availableBalance;

    @Column(name = "ledger_balance", precision = 18, scale = 2, nullable = false)
    private BigDecimal ledgerBalance;

    @Column(name = "as_of", nullable = false)
    private LocalDateTime asOf;

    @Column(name = "overdraft_used", precision = 18, scale = 2, nullable = false)
    private BigDecimal overdraftUsed;

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    private Account account;

    // Constructors
    public AccountBalance() {
    }

    public AccountBalance(UUID accountId, BigDecimal availableBalance, BigDecimal ledgerBalance) {
        this.accountId = accountId;
        this.availableBalance = availableBalance;
        this.ledgerBalance = ledgerBalance;
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }

    public void setLedgerBalance(BigDecimal ledgerBalance) {
        this.ledgerBalance = ledgerBalance;
    }

    public LocalDateTime getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDateTime asOf) {
        this.asOf = asOf;
    }

    public BigDecimal getOverdraftUsed() {
        return overdraftUsed;
    }

    public void setOverdraftUsed(BigDecimal overdraftUsed) {
        this.overdraftUsed = overdraftUsed;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
