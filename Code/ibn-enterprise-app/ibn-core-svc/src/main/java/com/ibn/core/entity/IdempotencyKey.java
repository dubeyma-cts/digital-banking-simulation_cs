package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "IDEMPOTENCY_KEY", schema = "INB")
public class IdempotencyKey {

    @Id
    @Column(name = "key_id", columnDefinition = "UUID")
    private UUID keyId;

    @Column(name = "owner_user_id", columnDefinition = "UUID", nullable = false)
    private UUID ownerUserId;

    @Column(name = "key_hash", nullable = false, length = 32)
    private byte[] keyHash;

    @Column(name = "scope", length = 30, nullable = false)
    private String scope;

    @Column(name = "request_fingerprint", nullable = false, length = 32)
    private byte[] requestFingerprint;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "response_ref", length = 200)
    private String responseRef;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "owner_user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User owner;

    // Constructors
    public IdempotencyKey() {
    }

    public IdempotencyKey(UUID keyId, UUID ownerUserId, String scope, String status) {
        this.keyId = keyId;
        this.ownerUserId = ownerUserId;
        this.scope = scope;
        this.status = status;
    }

    // Getters and Setters
    public UUID getKeyId() {
        return keyId;
    }

    public void setKeyId(UUID keyId) {
        this.keyId = keyId;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public byte[] getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(byte[] keyHash) {
        this.keyHash = keyHash;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public byte[] getRequestFingerprint() {
        return requestFingerprint;
    }

    public void setRequestFingerprint(byte[] requestFingerprint) {
        this.requestFingerprint = requestFingerprint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseRef() {
        return responseRef;
    }

    public void setResponseRef(String responseRef) {
        this.responseRef = responseRef;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
