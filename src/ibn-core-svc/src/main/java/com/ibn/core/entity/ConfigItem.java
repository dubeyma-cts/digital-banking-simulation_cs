package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CONFIG_ITEM", schema = "INB")
public class ConfigItem {

    @Id
    @Column(name = "config_id", columnDefinition = "UUID")
    private UUID configId;

    @Column(name = "config_key", length = 100, nullable = false)
    private String configKey;

    @Column(name = "scope_type", length = 20, nullable = false)
    private String scopeType;

    @Column(name = "scope_id", length = 60)
    private String scopeId;

    @Column(name = "value_json", columnDefinition = "CLOB", nullable = false)
    private String valueJson;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "created_by", columnDefinition = "UUID", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User creator;

    // Constructors
    public ConfigItem() {
    }

    public ConfigItem(UUID configId, String configKey, String scopeType, String valueJson, String status) {
        this.configId = configId;
        this.configKey = configKey;
        this.scopeType = scopeType;
        this.valueJson = valueJson;
        this.status = status;
    }

    // Getters and Setters
    public UUID getConfigId() {
        return configId;
    }

    public void setConfigId(UUID configId) {
        this.configId = configId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getValueJson() {
        return valueJson;
    }

    public void setValueJson(String valueJson) {
        this.valueJson = valueJson;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
