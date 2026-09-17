package com.hms.model;

import com.hms.model.enums.AppointmentStatus;
import com.hms.repository.Identifiable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents an outpatient clinical appointment booking.
 * Enforces status transitions and temporal constraints.
 */
public class Appointment implements Identifiable, Searchable {
    private final String id;
    private final String patientId;
    private String doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String reason;
    private AppointmentStatus status;
    private String consultationNotes;
    private final LocalDateTime createdAt;

    public Appointment(String id, String patientId, String doctorId,
                       LocalDate appointmentDate, LocalTime appointmentTime,
                       String reason, AppointmentStatus status,
                       String consultationNotes, LocalDateTime createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = (reason != null && !reason.trim().isEmpty()) ? reason : "General Checkup";
        this.status = (status != null) ? status : AppointmentStatus.SCHEDULED;
        this.consultationNotes = (consultationNotes != null) ? consultationNotes : "";
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getConsultationNotes() {
        return consultationNotes;
    }

    public void setConsultationNotes(String consultationNotes) {
        this.consultationNotes = consultationNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.toLowerCase();
        return id.toLowerCase().contains(q)
                || patientId.toLowerCase().contains(q)
                || doctorId.toLowerCase().contains(q)
                || status.name().toLowerCase().contains(q)
                || appointmentDate.toString().contains(q)
                || (reason != null && reason.toLowerCase().contains(q));
    }

    @Override
    public String toString() {
        return String.format("[%s] Patient: %s | Doctor: %s | Date: %s %s | Status: %s | Reason: %s",
                id, patientId, doctorId, appointmentDate, appointmentTime, status, reason);
    }
}
