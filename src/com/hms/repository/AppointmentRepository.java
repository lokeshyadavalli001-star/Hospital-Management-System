package com.hms.repository;

import com.hms.model.Appointment;
import com.hms.model.enums.AppointmentStatus;
import com.hms.util.DateTimeUtil;
import com.hms.util.FileUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository handling outpatient appointment scheduling persistence and collision queries.
 */
public class AppointmentRepository extends AbstractCsvRepository<Appointment> {

    public AppointmentRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected String getCsvHeader() {
        return "id,patientId,doctorId,appointmentDate,appointmentTime,reason,status,consultationNotes,createdAt";
    }

    @Override
    protected String serialize(Appointment a) {
        return FileUtil.toCsvLine(Arrays.asList(
                a.getId(),
                a.getPatientId(),
                a.getDoctorId(),
                DateTimeUtil.formatDate(a.getAppointmentDate()),
                DateTimeUtil.formatTime(a.getAppointmentTime()),
                a.getReason() != null ? a.getReason() : "",
                a.getStatus().name(),
                a.getConsultationNotes() != null ? a.getConsultationNotes() : "",
                DateTimeUtil.formatDateTime(a.getCreatedAt())
        ));
    }

    @Override
    protected Appointment deserialize(List<String> tokens) {
        if (tokens.size() < 9) {
            return null;
        }
        String id = tokens.get(0);
        String patientId = tokens.get(1);
        String doctorId = tokens.get(2);
        LocalDate date = DateTimeUtil.parseDate(tokens.get(3));
        LocalTime time = DateTimeUtil.parseTime(tokens.get(4));
        String reason = tokens.get(5);
        AppointmentStatus status = AppointmentStatus.fromString(tokens.get(6));
        String notes = tokens.get(7);
        LocalDateTime createdAt = DateTimeUtil.parseDateTime(tokens.get(8));

        return new Appointment(id, patientId, doctorId, date, time, reason, status, notes, createdAt);
    }

    public List<Appointment> findByDoctorId(String doctorId) {
        if (doctorId == null) return List.of();
        return findAll().stream()
                .filter(a -> a.getDoctorId().equalsIgnoreCase(doctorId.trim()))
                .collect(Collectors.toList());
    }

    public List<Appointment> findByPatientId(String patientId) {
        if (patientId == null) return List.of();
        return findAll().stream()
                .filter(a -> a.getPatientId().equalsIgnoreCase(patientId.trim()))
                .collect(Collectors.toList());
    }

    public List<Appointment> findByDate(LocalDate date) {
        if (date == null) return List.of();
        return findAll().stream()
                .filter(a -> date.equals(a.getAppointmentDate()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if doctor already has an active (SCHEDULED/RESCHEDULED) appointment at the specified date & time.
     */
    public Optional<Appointment> findDoctorConflict(String doctorId, LocalDate date, LocalTime time, String excludeId) {
        return findAll().stream()
                .filter(a -> (excludeId == null || !a.getId().equalsIgnoreCase(excludeId)))
                .filter(a -> a.getDoctorId().equalsIgnoreCase(doctorId))
                .filter(a -> a.getAppointmentDate().equals(date))
                .filter(a -> a.getAppointmentTime().equals(time))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.RESCHEDULED)
                .findFirst();
    }

    /**
     * Checks if patient already has an active appointment at the exact same slot.
     */
    public Optional<Appointment> findPatientConflict(String patientId, LocalDate date, LocalTime time, String excludeId) {
        return findAll().stream()
                .filter(a -> (excludeId == null || !a.getId().equalsIgnoreCase(excludeId)))
                .filter(a -> a.getPatientId().equalsIgnoreCase(patientId))
                .filter(a -> a.getAppointmentDate().equals(date))
                .filter(a -> a.getAppointmentTime().equals(time))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.RESCHEDULED)
                .findFirst();
    }
}
