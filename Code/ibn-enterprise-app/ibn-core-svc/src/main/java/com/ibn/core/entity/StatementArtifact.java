package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "STATEMENT_ARTIFACT", schema = "INB")
public class StatementArtifact {

    @Id
    @Column(name = "artifact_id", columnDefinition = "UUID")
    private UUID artifactId;

    @Column(name = "statement_req_id", columnDefinition = "UUID", nullable = false)
    private UUID statementReqId;

    @Column(name = "object_uri", length = 500, nullable = false)
    private String objectUri;

    @Column(name = "content_hash", nullable = false, length = 32)
    private byte[] contentHash;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "mime_type", length = 80, nullable = false)
    private String mimeType;

    @ManyToOne
    @JoinColumn(name = "statement_req_id", referencedColumnName = "statement_req_id", insertable = false, updatable = false)
    private StatementRequest statementRequest;

    // Constructors
    public StatementArtifact() {
    }

    public StatementArtifact(UUID artifactId, UUID statementReqId, String objectUri, String mimeType) {
        this.artifactId = artifactId;
        this.statementReqId = statementReqId;
        this.objectUri = objectUri;
        this.mimeType = mimeType;
    }

    // Getters and Setters
    public UUID getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(UUID artifactId) {
        this.artifactId = artifactId;
    }

    public UUID getStatementReqId() {
        return statementReqId;
    }

    public void setStatementReqId(UUID statementReqId) {
        this.statementReqId = statementReqId;
    }

    public String getObjectUri() {
        return objectUri;
    }

    public void setObjectUri(String objectUri) {
        this.objectUri = objectUri;
    }

    public byte[] getContentHash() {
        return contentHash;
    }

    public void setContentHash(byte[] contentHash) {
        this.contentHash = contentHash;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public StatementRequest getStatementRequest() {
        return statementRequest;
    }

    public void setStatementRequest(StatementRequest statementRequest) {
        this.statementRequest = statementRequest;
    }
}
