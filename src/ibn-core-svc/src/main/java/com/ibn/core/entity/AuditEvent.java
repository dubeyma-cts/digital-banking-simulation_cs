package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "AUDIT_EVENT", schema = "INB")
public class AuditEvent {

    @Id
    @Column(name = "audit_id", columnDefinition = "UUID")
    private UUID auditId;

    @Column(name = "actor_user_id", columnDefinition = "UUID", nullable = false)
    private UUID actorUserId;

    @Column(name = "actor_role", length = 50, nullable = false)
    private String actorRole;

    @Column(name = "action", length = 80, nullable = false)
    private String action;

    @Column(name = "entity_type", length = 60, nullable = false)
    private String entityType;

    @Column(name = "entity_id", columnDefinition = "UUID", nullable = false)
    private UUID entityId;

    @Column(name = "outcome", length = 20, nullable = false)
    private String outcome;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "device_metadata", length = 400)
    private String deviceMetadata;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "details_json", columnDefinition = "CLOB")
    private String detailsJson;

    @ManyToOne
    @JoinColumn(name = "actor_user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User actor;

    // Constructors
    public AuditEvent() {
    }

    public AuditEvent(UUID auditId, UUID actorUserId, String action, String entityType, UUID entityId, String outcome) {
        this.auditId = auditId;
        this.actorUserId = actorUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.outcome = outcome;
    }

    // Getters and Setters
    public UUID getAuditId() {
        return auditId;
    }

    public void setAuditId(UUID auditId) {
        this.auditId = auditId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceMetadata() {
        return deviceMetadata;
    }

    public void setDeviceMetadata(String deviceMetadata) {
        this.deviceMetadata = deviceMetadata;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }
}
