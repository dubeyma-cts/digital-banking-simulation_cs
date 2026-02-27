package com.ibn.core.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "BILL_PAYMENT", schema = "INB")
public class BillPayment {

    @Id
    @Column(name = "bill_payment_id", columnDefinition = "UUID")
    private UUID billPaymentId;

    @Column(name = "customer_id", columnDefinition = "UUID", nullable = false)
    private UUID customerId;

    @Column(name = "source_account_id", columnDefinition = "UUID", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "bill_account_id", columnDefinition = "UUID", nullable = false)
    private UUID billAccountId;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring;

    @Column(name = "schedule_id", columnDefinition = "UUID")
    private UUID scheduleId;

    @Column(name = "gateway", length = 30, nullable = false)
    private String gateway;

    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "idempotency_key_id", columnDefinition = "UUID", nullable = false)
    private UUID idempotencyKeyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reference", length = 64)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "source_account_id", referencedColumnName = "account_id", insertable = false, updatable = false)
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "bill_account_id", referencedColumnName = "bill_account_id", insertable = false, updatable = false)
    private BillAccount billAccount;

    @ManyToOne
    @JoinColumn(name = "schedule_id", referencedColumnName = "schedule_id", insertable = false, updatable = false)
    private PaymentSchedule schedule;

    // Constructors
    public BillPayment() {
    }

    public BillPayment(UUID billPaymentId, UUID customerId, UUID sourceAccountId, UUID billAccountId, BigDecimal amount) {
        this.billPaymentId = billPaymentId;
        this.customerId = customerId;
        this.sourceAccountId = sourceAccountId;
        this.billAccountId = billAccountId;
        this.amount = amount;
    }

    // Getters and Setters
    public UUID getBillPaymentId() {
        return billPaymentId;
    }

    public void setBillPaymentId(UUID billPaymentId) {
        this.billPaymentId = billPaymentId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public UUID getBillAccountId() {
        return billAccountId;
    }

    public void setBillAccountId(UUID billAccountId) {
        this.billAccountId = billAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public void setGatewayReference(String gatewayReference) {
        this.gatewayReference = gatewayReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getIdempotencyKeyId() {
        return idempotencyKeyId;
    }

    public void setIdempotencyKeyId(UUID idempotencyKeyId) {
        this.idempotencyKeyId = idempotencyKeyId;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public BillAccount getBillAccount() {
        return billAccount;
    }

    public void setBillAccount(BillAccount billAccount) {
        this.billAccount = billAccount;
    }

    public PaymentSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(PaymentSchedule schedule) {
        this.schedule = schedule;
    }
}
