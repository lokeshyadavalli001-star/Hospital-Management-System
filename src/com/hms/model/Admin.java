package com.hms.model;

import com.hms.model.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Represents a System Administrator with unconstrained system configuration privileges.
 */
public class Admin extends User {
    private String departmentAccess;

    public Admin(String id, String username, String passwordHash, String salt,
                 String fullName, String email, String phone,
                 boolean active, LocalDateTime createdAt, String departmentAccess) {
        super(id, username, passwordHash, salt, fullName, email, phone, UserRole.ADMIN, active, createdAt);
        this.departmentAccess = (departmentAccess == null || departmentAccess.trim().isEmpty())
                ? "ALL_DEPARTMENTS" : departmentAccess;
    }

    public String getDepartmentAccess() {
        return departmentAccess;
    }

    public void setDepartmentAccess(String departmentAccess) {
        this.departmentAccess = departmentAccess;
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator with full access to accounts, master records, reports, and security audit logs.";
    }
}
