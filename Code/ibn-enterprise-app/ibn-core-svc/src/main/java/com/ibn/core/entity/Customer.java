package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CUSTOMER", schema = "INB")
public class Customer {

    @Id
    @Column(name = "customer_id", columnDefinition = "UUID")
    private UUID customerId;

    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "default_pwd", length = 255, nullable = false)
    private String defaultPwd;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "address_line3", length = 255)
    private String addressLine3;

    @Column(name = "city", length = 120, nullable = false)
    private String city;

    @Column(name = "state", length = 120, nullable = false)
    private String state;

    @Column(name = "zip", nullable = false)
    private Integer zip;

    @Column(name = "account_type", length = 50, nullable = false)
    private String accountType;

    @Column(name = "pan_number", length = 30, nullable = false)
    private String panNumber;

    @Column(name = "addhaar_num", nullable = false)
    private Integer addhaarNum;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "pan_enc")
    private byte[] panEncrypted;

    @Column(name = "aadhaar_hash", nullable = false, length = 64)
    private byte[] aadhaarHash;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "email", length = 254, nullable = false)
    private String email;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    @Column(name = "onboarding_status", length = 30, nullable = false)
    private String onboardingStatus;

    @Column(name = "approved_by", columnDefinition = "UUID")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User approver;

    // Constructors
    public Customer() {
    }

    public Customer(UUID customerId, String fullName, String phone, String email, String onboardingStatus) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.onboardingStatus = onboardingStatus;
    }

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDefaultPwd() {
        return defaultPwd;
    }

    public void setDefaultPwd(String defaultPwd) {
        this.defaultPwd = defaultPwd;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getZip() {
        return zip;
    }

    public void setZip(Integer zip) {
        this.zip = zip;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public Integer getAddhaarNum() {
        return addhaarNum;
    }

    public void setAddhaarNum(Integer addhaarNum) {
        this.addhaarNum = addhaarNum;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public byte[] getPanEncrypted() {
        return panEncrypted;
    }

    public void setPanEncrypted(byte[] panEncrypted) {
        this.panEncrypted = panEncrypted;
    }

    public byte[] getAadhaarHash() {
        return aadhaarHash;
    }

    public void setAadhaarHash(byte[] aadhaarHash) {
        this.aadhaarHash = aadhaarHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getOnboardingStatus() {
        return onboardingStatus;
    }

    public void setOnboardingStatus(String onboardingStatus) {
        this.onboardingStatus = onboardingStatus;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
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

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }
}
