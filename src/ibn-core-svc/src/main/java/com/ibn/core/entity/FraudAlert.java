package com.ibn.core.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "FRAUD_ALERT", schema = "INB")
public class FraudAlert {

    @Id
    @Column(name = "fraud_alert_id", columnDefinition = "UUID")
    private UUID fraudAlertId;

    @Column(name = "entity_type", length = 40, nullable = false)
    private String entityType;

    @Column(name = "entity_id", columnDefinition = "UUID", nullable = false)
    private UUID entityId;

    @Column(name = "risk_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal riskScore;

    @Column(name = "rule_code", length = 50, nullable = false)
    private String ruleCode;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_by", columnDefinition = "UUID")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "outcome", length = 30)
    private String outcome;

    @Column(name = "notes", length = 500)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "reviewed_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User reviewer;

    // Constructors
    public FraudAlert() {
    }

    public FraudAlert(UUID fraudAlertId, String entityType, UUID entityId, BigDecimal riskScore, String status) {
        this.fraudAlertId = fraudAlertId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.riskScore = riskScore;
        this.status = status;
    }

    // Getters and Setters
    public UUID getFraudAlertId() {
        return fraudAlertId;
    }

    public void setFraudAlertId(UUID fraudAlertId) {
        this.fraudAlertId = fraudAlertId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
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

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }
}
