package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RECONCILIATION_ITEM", schema = "INB")
public class ReconciliationItem {

    @Id
    @Column(name = "recon_item_id", columnDefinition = "UUID")
    private UUID reconItemId;

    @Column(name = "recon_run_id", columnDefinition = "UUID", nullable = false)
    private UUID reconRunId;

    @Column(name = "txn_id", columnDefinition = "UUID", nullable = false)
    private UUID txnId;

    @Column(name = "match_status", length = 20, nullable = false)
    private String matchStatus;

    @Column(name = "notes", length = 250)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "recon_run_id", referencedColumnName = "recon_run_id", insertable = false, updatable = false)
    private ReconciliationRun reconciliationRun;

    @ManyToOne
    @JoinColumn(name = "txn_id", referencedColumnName = "txn_id", insertable = false, updatable = false)
    private Transaction transaction;

    // Constructors
    public ReconciliationItem() {
    }

    public ReconciliationItem(UUID reconItemId, UUID reconRunId, UUID txnId, String matchStatus) {
        this.reconItemId = reconItemId;
        this.reconRunId = reconRunId;
        this.txnId = txnId;
        this.matchStatus = matchStatus;
    }

    // Getters and Setters
    public UUID getReconItemId() {
        return reconItemId;
    }

    public void setReconItemId(UUID reconItemId) {
        this.reconItemId = reconItemId;
    }

    public UUID getReconRunId() {
        return reconRunId;
    }

    public void setReconRunId(UUID reconRunId) {
        this.reconRunId = reconRunId;
    }

    public UUID getTxnId() {
        return txnId;
    }

    public void setTxnId(UUID txnId) {
        this.txnId = txnId;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ReconciliationRun getReconciliationRun() {
        return reconciliationRun;
    }

    public void setReconciliationRun(ReconciliationRun reconciliationRun) {
        this.reconciliationRun = reconciliationRun;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
