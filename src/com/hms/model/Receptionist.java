package com.hms.model;

import com.hms.model.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Front-desk receptionist responsible for patient registration, appointment scheduling, and billing intake.
 */
public class Receptionist extends User {
    private String shift;
    private String deskNumber;

    public Receptionist(String id, String username, String passwordHash, String salt,
                        String fullName, String email, String phone,
                        boolean active, LocalDateTime createdAt,
                        String shift, String deskNumber) {
        super(id, username, passwordHash, salt, fullName, email, phone, UserRole.RECEPTIONIST, active, createdAt);
        this.shift = (shift != null) ? shift : "DAY";
        this.deskNumber = (deskNumber != null) ? deskNumber : "FRONT-01";
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getDeskNumber() {
        return deskNumber;
    }

    public void setDeskNumber(String deskNumber) {
        this.deskNumber = deskNumber;
    }

    @Override
    public String getRoleDescription() {
        return "Receptionist with authority over patient registration, scheduling appointments, and generating invoices.";
    }
}
