package com.ibn.core.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "BILLER", schema = "INB")
public class Biller {

    @Id
    @Column(name = "biller_id", columnDefinition = "UUID")
    private UUID billerId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "api_reference", length = 100, nullable = false)
    private String apiReference;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Biller() {
    }

    public Biller(UUID billerId, String name, String apiReference, Boolean active) {
        this.billerId = billerId;
        this.name = name;
        this.apiReference = apiReference;
        this.active = active;
    }

    // Getters and Setters
    public UUID getBillerId() {
        return billerId;
    }

    public void setBillerId(UUID billerId) {
        this.billerId = billerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getApiReference() {
        return apiReference;
    }

    public void setApiReference(String apiReference) {
        this.apiReference = apiReference;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
