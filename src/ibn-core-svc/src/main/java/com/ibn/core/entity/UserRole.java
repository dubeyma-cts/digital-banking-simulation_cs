package com.ibn.core.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "USER_ROLE", schema = "INB")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Id
    @Column(name = "role_id", columnDefinition = "UUID")
    private UUID roleId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", columnDefinition = "UUID")
    private UUID assignedBy;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", insertable = false, updatable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "assigned_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User assignedByUser;

    // Constructors
    public UserRole() {
    }

    public UserRole(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UUID assignedBy) {
        this.assignedBy = assignedBy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getAssignedByUser() {
        return assignedByUser;
    }

    public void setAssignedByUser(User assignedByUser) {
        this.assignedByUser = assignedByUser;
    }
}
