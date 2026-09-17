package com.hms.model.enums;

/**
 * System user roles enforcing Role-Based Access Control (RBAC).
 */
public enum UserRole {
    ADMIN("Administrator", "Full system privileges, configuration, user management, and auditing."),
    RECEPTIONIST("Receptionist", "Front-desk operations: patient registration, scheduling, and billing."),
    DOCTOR("Doctor", "Clinical consultation, appointment review, and prescription/treatment notes."),
    PATIENT("Patient", "Self-service access: personal appointments and invoice review.");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return null;
        }
        String clean = roleStr.trim().toUpperCase();
        for (UserRole r : values()) {
            if (r.name().equals(clean)) {
                return r;
            }
        }
        return null;
    }
}
