package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CHEQUE_STATUS_HISTORY", schema = "INB")
public class ChequeStatusHistory {

    @Id
    @Column(name = "history_id", columnDefinition = "UUID")
    private UUID historyId;

    @Column(name = "cheque_deposit_id", columnDefinition = "UUID", nullable = false)
    private UUID chequeDepositId;

    @Column(name = "old_status", length = 30, nullable = false)
    private String oldStatus;

    @Column(name = "new_status", length = 30, nullable = false)
    private String newStatus;

    @Column(name = "changed_by", columnDefinition = "UUID", nullable = false)
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "remarks", length = 250)
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "cheque_deposit_id", referencedColumnName = "cheque_deposit_id", insertable = false, updatable = false)
    private ChequeDeposit chequeDeposit;

    @ManyToOne
    @JoinColumn(name = "changed_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User changedByUser;

    // Constructors
    public ChequeStatusHistory() {
    }

    public ChequeStatusHistory(UUID historyId, UUID chequeDepositId, String oldStatus, String newStatus) {
        this.historyId = historyId;
        this.chequeDepositId = chequeDepositId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    // Getters and Setters
    public UUID getHistoryId() {
        return historyId;
    }

    public void setHistoryId(UUID historyId) {
        this.historyId = historyId;
    }

    public UUID getChequeDepositId() {
        return chequeDepositId;
    }

    public void setChequeDepositId(UUID chequeDepositId) {
        this.chequeDepositId = chequeDepositId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ChequeDeposit getChequeDeposit() {
        return chequeDeposit;
    }

    public void setChequeDeposit(ChequeDeposit chequeDeposit) {
        this.chequeDeposit = chequeDeposit;
    }

    public User getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(User changedByUser) {
        this.changedByUser = changedByUser;
    }
}
