package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CONFIG_APPROVAL", schema = "INB")
public class ConfigApproval {

    @Id
    @Column(name = "approval_id", columnDefinition = "UUID")
    private UUID approvalId;

    @Column(name = "config_id", columnDefinition = "UUID", nullable = false)
    private UUID configId;

    @Column(name = "requested_by", columnDefinition = "UUID", nullable = false)
    private UUID requestedBy;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_by", columnDefinition = "UUID")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "comment", length = 250)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "config_id", referencedColumnName = "config_id", insertable = false, updatable = false)
    private ConfigItem config;

    @ManyToOne
    @JoinColumn(name = "requested_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "approved_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User approver;

    // Constructors
    public ConfigApproval() {
    }

    public ConfigApproval(UUID approvalId, UUID configId, UUID requestedBy, String status) {
        this.approvalId = approvalId;
        this.configId = configId;
        this.requestedBy = requestedBy;
        this.status = status;
    }

    // Getters and Setters
    public UUID getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(UUID approvalId) {
        this.approvalId = approvalId;
    }

    public UUID getConfigId() {
        return configId;
    }

    public void setConfigId(UUID configId) {
        this.configId = configId;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ConfigItem getConfig() {
        return config;
    }

    public void setConfig(ConfigItem config) {
        this.config = config;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }
}
