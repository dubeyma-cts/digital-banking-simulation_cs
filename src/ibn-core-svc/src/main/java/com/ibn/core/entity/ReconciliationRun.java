package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RECONCILIATION_RUN", schema = "INB")
public class ReconciliationRun {

    @Id
    @Column(name = "recon_run_id", columnDefinition = "UUID")
    private UUID reconRunId;

    @Column(name = "run_type", length = 20, nullable = false)
    private String runType;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "generated_by", columnDefinition = "UUID", nullable = false)
    private UUID generatedBy;

    @Column(name = "report_object_uri", length = 500)
    private String reportObjectUri;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @ManyToOne
    @JoinColumn(name = "generated_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User generator;

    // Constructors
    public ReconciliationRun() {
    }

    public ReconciliationRun(UUID reconRunId, String runType, LocalDate businessDate, String status) {
        this.reconRunId = reconRunId;
        this.runType = runType;
        this.businessDate = businessDate;
        this.status = status;
    }

    // Getters and Setters
    public UUID getReconRunId() {
        return reconRunId;
    }

    public void setReconRunId(UUID reconRunId) {
        this.reconRunId = reconRunId;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public UUID getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(UUID generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getReportObjectUri() {
        return reportObjectUri;
    }

    public void setReportObjectUri(String reportObjectUri) {
        this.reportObjectUri = reportObjectUri;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public User getGenerator() {
        return generator;
    }

    public void setGenerator(User generator) {
        this.generator = generator;
    }
}
