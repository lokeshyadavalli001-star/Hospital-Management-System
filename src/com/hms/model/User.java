package com.hms.model;

import com.hms.model.enums.UserRole;
import com.hms.repository.Identifiable;

import java.time.LocalDateTime;

/**
 * Abstract base class representing an authenticated actor within the Hospital Management System.
 * Demonstrates Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class User implements Identifiable, Searchable {
    private final String id;
    private final String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private String phone;
    private final UserRole role;
    private boolean active;
    private final LocalDateTime createdAt;

    public User(String id, String username, String passwordHash, String salt,
                String fullName, String email, String phone, UserRole role,
                boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Polymorphic method to obtain a description of role authority.
     */
    public abstract String getRoleDescription();

    @Override
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.toLowerCase();
        return id.toLowerCase().contains(q)
                || username.toLowerCase().contains(q)
                || fullName.toLowerCase().contains(q)
                || (email != null && email.toLowerCase().contains(q))
                || (phone != null && phone.toLowerCase().contains(q));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s | Active: %s",
                role, fullName, username, id, active ? "Yes" : "No");
    }
}
