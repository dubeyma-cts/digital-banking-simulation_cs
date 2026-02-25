package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CHEQUE_DEPOSIT", schema = "INB")
public class ChequeDeposit {

    @Id
    @Column(name = "cheque_deposit_id", columnDefinition = "UUID")
    private UUID chequeDepositId;

    @Column(name = "customer_id", columnDefinition = "UUID", nullable = false)
    private UUID customerId;

    @Column(name = "account_id", columnDefinition = "UUID", nullable = false)
    private UUID accountId;

    @Column(name = "depositor_name", length = 150)
    private String depositorName;

    @Column(name = "cheque_number", length = 30, nullable = false)
    private String chequeNumber;

    @Column(name = "cheque_date")
    private LocalDate chequeDate;

    @Column(name = "drawer_bank", length = 200)
    private String drawerBank;

    @Column(name = "branch_name", length = 200)
    private String branchName;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private java.math.BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "current_status", length = 30, nullable = false)
    private String currentStatus;

    @Column(name = "clearance_sla_days", nullable = false)
    private Integer clearanceSLADays;

    @Column(name = "bounce_penalty_amount", precision = 18, scale = 2)
    private java.math.BigDecimal bouncePenaltyAmount;

    @Column(name = "remarks", length = 250)
    private String remarks;

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;

    @Lob
    @Column(name = "attmt_doc")
    private byte[] attmtDoc;

    @Column(name = "attachment_blob_url", length = 1024)
    private String attachmentBlobUrl;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    private Account account;

    // Constructors
    public ChequeDeposit() {
    }

    public ChequeDeposit(UUID chequeDepositId, UUID customerId, UUID accountId, String chequeNumber, String currentStatus) {
        this.chequeDepositId = chequeDepositId;
        this.customerId = customerId;
        this.accountId = accountId;
        this.chequeNumber = chequeNumber;
        this.currentStatus = currentStatus;
    }

    // Getters and Setters
    public UUID getChequeDepositId() {
        return chequeDepositId;
    }

    public void setChequeDepositId(UUID chequeDepositId) {
        this.chequeDepositId = chequeDepositId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public String getDepositorName() {
        return depositorName;
    }

    public void setDepositorName(String depositorName) {
        this.depositorName = depositorName;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public LocalDate getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(LocalDate chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getDrawerBank() {
        return drawerBank;
    }

    public void setDrawerBank(String drawerBank) {
        this.drawerBank = drawerBank;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Integer getClearanceSLADays() {
        return clearanceSLADays;
    }

    public void setClearanceSLADays(Integer clearanceSLADays) {
        this.clearanceSLADays = clearanceSLADays;
    }

    public java.math.BigDecimal getBouncePenaltyAmount() {
        return bouncePenaltyAmount;
    }

    public void setBouncePenaltyAmount(java.math.BigDecimal bouncePenaltyAmount) {
        this.bouncePenaltyAmount = bouncePenaltyAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public byte[] getAttmtDoc() {
        return attmtDoc;
    }

    public void setAttmtDoc(byte[] attmtDoc) {
        this.attmtDoc = attmtDoc;
    }

    public String getAttachmentBlobUrl() {
        return attachmentBlobUrl;
    }

    public void setAttachmentBlobUrl(String attachmentBlobUrl) {
        this.attachmentBlobUrl = attachmentBlobUrl;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
