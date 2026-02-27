package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TRANSFER_EXECUTION", schema = "INB")
public class TransferExecution {

    @Id
    @Column(name = "execution_id", columnDefinition = "UUID")
    private UUID executionId;

    @Column(name = "transfer_id", columnDefinition = "UUID", nullable = false)
    private UUID transferId;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo;

    @Column(name = "network", length = 20, nullable = false)
    private String network;

    @Column(name = "external_reference", length = 120)
    private String externalReference;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "failure_code", length = 40)
    private String failureCode;

    @ManyToOne
    @JoinColumn(name = "transfer_id", referencedColumnName = "transfer_id", insertable = false, updatable = false)
    private TransferInstruction transfer;

    // Constructors
    public TransferExecution() {
    }

    public TransferExecution(UUID executionId, UUID transferId, Integer attemptNo, String network, String status) {
        this.executionId = executionId;
        this.transferId = transferId;
        this.attemptNo = attemptNo;
        this.network = network;
        this.status = status;
    }

    // Getters and Setters
    public UUID getExecutionId() {
        return executionId;
    }

    public void setExecutionId(UUID executionId) {
        this.executionId = executionId;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo) {
        this.attemptNo = attemptNo;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(String failureCode) {
        this.failureCode = failureCode;
    }

    public TransferInstruction getTransfer() {
        return transfer;
    }

    public void setTransfer(TransferInstruction transfer) {
        this.transfer = transfer;
    }
}
