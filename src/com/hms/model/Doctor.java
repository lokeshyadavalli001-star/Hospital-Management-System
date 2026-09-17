package com.hms.model;

import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.UserRole;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a medical consultant/physician.
 * Extends User to inherit identity and credentials while encapsulating clinical attributes.
 */
public class Doctor extends User {
    private String specialization;
    private String department;
    private double consultationFee;
    private DoctorStatus status;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private String qualification;

    public Doctor(String id, String username, String passwordHash, String salt,
                  String fullName, String email, String phone,
                  boolean active, LocalDateTime createdAt,
                  String specialization, String department, double consultationFee,
                  DoctorStatus status, LocalTime availableFrom, LocalTime availableTo,
                  String qualification) {
        super(id, username, passwordHash, salt, fullName, email, phone, UserRole.DOCTOR, active, createdAt);
        this.specialization = specialization;
        this.department = department;
        this.consultationFee = consultationFee;
        this.status = (status != null) ? status : DoctorStatus.ACTIVE;
        this.availableFrom = (availableFrom != null) ? availableFrom : LocalTime.of(9, 0);
        this.availableTo = (availableTo != null) ? availableTo : LocalTime.of(17, 0);
        this.qualification = (qualification != null) ? qualification : "MBBS, MD";
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public DoctorStatus getStatus() {
        return status;
    }

    public void setStatus(DoctorStatus status) {
        this.status = status;
        // Keep active synchronized
        setActive(status == DoctorStatus.ACTIVE);
    }

    public LocalTime getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalTime availableFrom) {
        this.availableFrom = availableFrom;
    }

    public LocalTime getAvailableTo() {
        return availableTo;
    }

    public void setAvailableTo(LocalTime availableTo) {
        this.availableTo = availableTo;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public boolean isAvailableAt(LocalTime time) {
        if (status != DoctorStatus.ACTIVE || !isActive()) {
            return false;
        }
        return (time.equals(availableFrom) || time.isAfter(availableFrom))
                && (time.equals(availableTo) || time.isBefore(availableTo));
    }

    @Override
    public String getRoleDescription() {
        return String.format("Consultant Physician (%s, %s) with clinical consultation and diagnosis privileges.",
                specialization, department);
    }

    @Override
    public boolean matches(String query) {
        if (super.matches(query)) {
            return true;
        }
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.toLowerCase();
        return (specialization != null && specialization.toLowerCase().contains(q))
                || (department != null && department.toLowerCase().contains(q))
                || (qualification != null && qualification.toLowerCase().contains(q));
    }
}
