package com.hms.service;

import com.hms.exception.AppointmentConflictException;
import com.hms.exception.DoctorNotFoundException;
import com.hms.exception.HospitalException;
import com.hms.exception.InvalidInputException;
import com.hms.exception.PatientNotFoundException;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.enums.AppointmentStatus;
import com.hms.model.enums.DoctorStatus;
import com.hms.repository.AppointmentRepository;
import com.hms.util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service orchestrating clinical appointments, collision checking, and lifecycle state changes.
 */
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AuditService auditService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientService patientService,
                              DoctorService doctorService,
                              AuditService auditService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.auditService = auditService;
    }

    public Appointment bookAppointment(String patientId, String doctorId,
                                       LocalDate appointmentDate, LocalTime appointmentTime,
                                       String reason, String actor) throws HospitalException {
        Patient patient = patientService.getPatientById(patientId);
        if (!patient.isActive()) {
            throw new InvalidInputException("Cannot book appointment: Patient " + patientId + " is deactivated.");
        }

        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor.getStatus() != DoctorStatus.ACTIVE || !doctor.isActive()) {
            throw new InvalidInputException("Cannot book appointment: Doctor " + doctor.getFullName()
                    + " is currently " + doctor.getStatus() + ".");
        }

        if (appointmentDate == null || appointmentDate.isBefore(LocalDate.now())) {
            throw new InvalidInputException("Appointment date cannot be in the past.");
        }

        if (appointmentTime == null) {
            throw new InvalidInputException("Appointment time is required.");
        }

        if (!doctor.isAvailableAt(appointmentTime)) {
            throw new InvalidInputException(String.format(
                    "Doctor %s is only available between %s and %s.",
                    doctor.getFullName(), doctor.getAvailableFrom(), doctor.getAvailableTo()));
        }

        // Check doctor collision
        Optional<Appointment> docConflict = appointmentRepository.findDoctorConflict(
                doctorId, appointmentDate, appointmentTime, null);
        if (docConflict.isPresent()) {
            throw new AppointmentConflictException(String.format(
                    "Doctor %s is already booked on %s at %s (Appt: %s).",
                    doctor.getFullName(), appointmentDate, appointmentTime, docConflict.get().getId()));
        }

        // Check patient collision
        Optional<Appointment> patConflict = appointmentRepository.findPatientConflict(
                patientId, appointmentDate, appointmentTime, null);
        if (patConflict.isPresent()) {
            throw new AppointmentConflictException(String.format(
                    "Patient %s already has an active appointment on %s at %s (Appt: %s).",
                    patient.getName(), appointmentDate, appointmentTime, patConflict.get().getId()));
        }

        String apptId = IdGenerator.nextAppointmentId();
        Appointment appointment = new Appointment(
                apptId, patientId, doctorId, appointmentDate, appointmentTime,
                reason, AppointmentStatus.SCHEDULED, "", LocalDateTime.now()
        );

        appointmentRepository.save(appointment);
        auditService.log("APPOINTMENT", "BOOK", actor,
                String.format("Booked %s for Patient %s with Doctor %s on %s %s",
                        apptId, patientId, doctorId, appointmentDate, appointmentTime));
        return appointment;
    }

    public Appointment rescheduleAppointment(String appointmentId, LocalDate newDate,
                                             LocalTime newTime, String actor) throws HospitalException {
        Appointment appointment = getAppointmentById(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidInputException("Cannot reschedule a cancelled appointment.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidInputException("Cannot reschedule an already completed appointment.");
        }
        if (newDate == null || newDate.isBefore(LocalDate.now())) {
            throw new InvalidInputException("New appointment date cannot be in the past.");
        }

        Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId());
        if (!doctor.isAvailableAt(newTime)) {
            throw new InvalidInputException(String.format(
                    "Doctor %s is only available between %s and %s.",
                    doctor.getFullName(), doctor.getAvailableFrom(), doctor.getAvailableTo()));
        }

        // Check doctor conflict
        Optional<Appointment> docConflict = appointmentRepository.findDoctorConflict(
                appointment.getDoctorId(), newDate, newTime, appointmentId);
        if (docConflict.isPresent()) {
            throw new AppointmentConflictException(String.format(
                    "Doctor %s has another booking on %s at %s.",
                    doctor.getFullName(), newDate, newTime));
        }

        // Check patient conflict
        Optional<Appointment> patConflict = appointmentRepository.findPatientConflict(
                appointment.getPatientId(), newDate, newTime, appointmentId);
        if (patConflict.isPresent()) {
            throw new AppointmentConflictException("Patient has another appointment at that time.");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        appointmentRepository.save(appointment);
        auditService.log("APPOINTMENT", "RESCHEDULE", actor,
                String.format("Rescheduled %s to %s %s", appointmentId, newDate, newTime));
        return appointment;
    }

    public void cancelAppointment(String appointmentId, String reason, String actor) throws HospitalException {
        Appointment appointment = getAppointmentById(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidInputException("Cannot cancel an appointment that is already COMPLETED.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidInputException("Appointment is already CANCELLED.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            String updated = (appointment.getConsultationNotes().isEmpty() ? "" : appointment.getConsultationNotes() + " | ")
                    + "Cancellation reason: " + reason.trim();
            appointment.setConsultationNotes(updated);
        }

        appointmentRepository.save(appointment);
        auditService.log("APPOINTMENT", "CANCEL", actor, "Cancelled appointment " + appointmentId);
    }

    public void completeAppointment(String appointmentId, String notes, String actor) throws HospitalException {
        Appointment appointment = getAppointmentById(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidInputException("Cannot complete an appointment that has been CANCELLED.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        if (notes != null && !notes.trim().isEmpty()) {
            appointment.setConsultationNotes(notes.trim());
        }

        appointmentRepository.save(appointment);
        auditService.log("APPOINTMENT", "COMPLETE", actor, "Completed appointment " + appointmentId);
    }

    public Appointment getAppointmentById(String id) throws InvalidInputException {
        if (id == null) {
            throw new InvalidInputException("Appointment ID cannot be null.");
        }
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException("No appointment found with ID: " + id));
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByDoctor(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }

    public List<Appointment> getTodaysAppointments() {
        return appointmentRepository.findByDate(LocalDate.now());
    }

    public List<Appointment> getUpcomingAppointments() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate().isEqual(today) || a.getAppointmentDate().isAfter(today))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.RESCHEDULED)
                .collect(Collectors.toList());
    }

    public List<Appointment> searchAppointments(String query) {
        return appointmentRepository.search(query);
    }
}
