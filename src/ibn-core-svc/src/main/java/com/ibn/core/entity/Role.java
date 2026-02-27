package com.ibn.core.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "\"ROLE\"", schema = "INB")
public class Role {

    @Id
    @Column(name = "role_id", columnDefinition = "UUID")
    private UUID roleId;

    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;

    @Column(name = "description", length = 250)
    private String description;

    // Constructors
    public Role() {
    }

    public Role(UUID roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // Getters and Setters
    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
