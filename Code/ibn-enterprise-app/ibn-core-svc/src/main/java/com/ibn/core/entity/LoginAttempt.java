package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "LOGIN_ATTEMPT", schema = "INB")
public class LoginAttempt {

    @Id
    @Column(name = "attempt_id", columnDefinition = "UUID")
    private UUID attemptId;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "attempt_at", nullable = false)
    private LocalDateTime attemptAt;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "device_fingerprint", length = 200)
    private String deviceFingerprint;

    @Column(name = "outcome", length = 20, nullable = false)
    private String outcome;

    @Column(name = "failure_reason", length = 50)
    private String failureReason;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    // Constructors
    public LoginAttempt() {
    }

    public LoginAttempt(UUID attemptId, String username, String outcome) {
        this.attemptId = attemptId;
        this.username = username;
        this.outcome = outcome;
    }

    // Getters and Setters
    public UUID getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getAttemptAt() {
        return attemptAt;
    }

    public void setAttemptAt(LocalDateTime attemptAt) {
        this.attemptAt = attemptAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
