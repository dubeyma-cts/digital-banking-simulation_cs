package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "STATEMENT_REQUEST", schema = "INB")
public class StatementRequest {

    @Id
    @Column(name = "statement_req_id", columnDefinition = "UUID")
    private UUID statementReqId;

    @Column(name = "account_id", columnDefinition = "UUID", nullable = false)
    private UUID accountId;

    @Column(name = "request_type", length = 20, nullable = false)
    private String requestType;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "format", length = 10, nullable = false)
    private String format;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_by", columnDefinition = "UUID", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User creator;

    // Constructors
    public StatementRequest() {
    }

    public StatementRequest(UUID statementReqId, UUID accountId, String requestType, String format, String status) {
        this.statementReqId = statementReqId;
        this.accountId = accountId;
        this.requestType = requestType;
        this.format = format;
        this.status = status;
    }

    // Getters and Setters
    public UUID getStatementReqId() {
        return statementReqId;
    }

    public void setStatementReqId(UUID statementReqId) {
        this.statementReqId = statementReqId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
