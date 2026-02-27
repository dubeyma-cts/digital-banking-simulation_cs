package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "PAYMENT_SCHEDULE", schema = "INB")
public class PaymentSchedule {

    @Id
    @Column(name = "schedule_id", columnDefinition = "UUID")
    private UUID scheduleId;

    @Column(name = "bill_account_id", columnDefinition = "UUID", nullable = false)
    private UUID billAccountId;

    @Column(name = "frequency", length = 20, nullable = false)
    private String frequency;

    @Column(name = "day_of_month", nullable = false)
    private Integer dayOfMonth;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "bill_account_id", referencedColumnName = "bill_account_id", insertable = false, updatable = false)
    private BillAccount billAccount;

    // Constructors
    public PaymentSchedule() {
    }

    public PaymentSchedule(UUID scheduleId, UUID billAccountId, String frequency, Integer dayOfMonth, String status) {
        this.scheduleId = scheduleId;
        this.billAccountId = billAccountId;
        this.frequency = frequency;
        this.dayOfMonth = dayOfMonth;
        this.status = status;
    }

    // Getters and Setters
    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UUID getBillAccountId() {
        return billAccountId;
    }

    public void setBillAccountId(UUID billAccountId) {
        this.billAccountId = billAccountId;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BillAccount getBillAccount() {
        return billAccount;
    }

    public void setBillAccount(BillAccount billAccount) {
        this.billAccount = billAccount;
    }
}
