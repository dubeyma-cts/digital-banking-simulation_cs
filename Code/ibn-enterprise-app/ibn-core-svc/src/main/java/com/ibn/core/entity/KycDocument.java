package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "KYC_DOCUMENT", schema = "INB")
public class KycDocument {

    @Id
    @Column(name = "kyc_doc_id", columnDefinition = "UUID")
    private UUID kycDocId;

    @Column(name = "customer_id", columnDefinition = "UUID", nullable = false)
    private UUID customerId;

    @Column(name = "doc_type", length = 30, nullable = false)
    private String docType;

    @Column(name = "object_uri", length = 500, nullable = false)
    private String objectUri;

    @Column(name = "content_hash", nullable = false, length = 32)
    private byte[] contentHash;

    @Column(name = "mime_type", length = 80, nullable = false)
    private String mimeType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by", columnDefinition = "UUID")
    private UUID uploadedBy;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "verified_by", columnDefinition = "UUID")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User uploadedByUser;

    @ManyToOne
    @JoinColumn(name = "verified_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User verifiedByUser;

    // Constructors
    public KycDocument() {
    }

    public KycDocument(UUID kycDocId, UUID customerId, String docType, String status) {
        this.kycDocId = kycDocId;
        this.customerId = customerId;
        this.docType = docType;
        this.status = status;
    }

    // Getters and Setters
    public UUID getKycDocId() {
        return kycDocId;
    }

    public void setKycDocId(UUID kycDocId) {
        this.kycDocId = kycDocId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UUID uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(UUID verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getUploadedByUser() {
        return uploadedByUser;
    }

    public void setUploadedByUser(User uploadedByUser) {
        this.uploadedByUser = uploadedByUser;
    }

    public User getVerifiedByUser() {
        return verifiedByUser;
    }

    public void setVerifiedByUser(User verifiedByUser) {
        this.verifiedByUser = verifiedByUser;
    }
}
