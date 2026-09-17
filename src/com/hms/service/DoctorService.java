package com.hms.service;

import com.hms.exception.DoctorNotFoundException;
import com.hms.exception.InvalidInputException;
import com.hms.model.Doctor;
import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.UserRole;
import com.hms.repository.DoctorRepository;
import com.hms.repository.UserRepository;
import com.hms.util.IdGenerator;
import com.hms.util.InputValidator;
import com.hms.util.PasswordUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service managing physician profiles, clinical availability, and department listings.
 */
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository, AuditService auditService) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public Doctor addDoctor(String username, String rawPassword, String fullName,
                            String email, String phone, String specialization,
                            String department, double consultationFee, DoctorStatus status,
                            LocalTime availableFrom, LocalTime availableTo,
                            String qualification, String actor) throws InvalidInputException {
        if (!InputValidator.isNotEmpty(fullName)) {
            throw new InvalidInputException("Doctor full name is required.");
        }
        if (!InputValidator.isNotEmpty(specialization)) {
            throw new InvalidInputException("Specialization cannot be empty.");
        }
        if (!InputValidator.isNotEmpty(department)) {
            throw new InvalidInputException("Department cannot be empty.");
        }
        if (!InputValidator.isNonNegative(consultationFee)) {
            throw new InvalidInputException("Consultation fee cannot be negative.");
        }
        if (availableFrom != null && availableTo != null && !availableTo.isAfter(availableFrom)) {
            throw new InvalidInputException("Available end time must be after start time.");
        }

        String docId = IdGenerator.nextDoctorId();
        String safeUsername = InputValidator.isNotEmpty(username) ? username.trim() : ("doc_" + docId.toLowerCase().replace("-", ""));
        String safePassword = InputValidator.isNotEmpty(rawPassword) ? rawPassword : "Doctor@123";

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(safePassword, salt);
        boolean isActive = (status == DoctorStatus.ACTIVE);

        Doctor doctor = new Doctor(docId, safeUsername, hash, salt, fullName.trim(),
                email != null ? email.trim() : "",
                phone != null ? phone.trim() : "",
                isActive, LocalDateTime.now(),
                specialization.trim(), department.trim(), consultationFee,
                status != null ? status : DoctorStatus.ACTIVE,
                availableFrom != null ? availableFrom : LocalTime.of(9, 0),
                availableTo != null ? availableTo : LocalTime.of(17, 0),
                qualification != null ? qualification.trim() : "MBBS");

        doctorRepository.save(doctor);
        userRepository.save(doctor); // Synchronize with user login accounts

        auditService.log("DOCTOR", "ADD", actor, "Added doctor " + docId + " (" + fullName + ")");
        return doctor;
    }

    public Doctor updateDoctor(String id, String fullName, String email, String phone,
                               String specialization, String department, double consultationFee,
                               DoctorStatus status, LocalTime availableFrom, LocalTime availableTo,
                               String qualification, String actor) throws DoctorNotFoundException, InvalidInputException {
        Doctor existing = getDoctorById(id);

        if (!InputValidator.isNotEmpty(fullName)) {
            throw new InvalidInputException("Doctor full name cannot be empty.");
        }
        if (!InputValidator.isNonNegative(consultationFee)) {
            throw new InvalidInputException("Consultation fee cannot be negative.");
        }
        if (availableFrom != null && availableTo != null && !availableTo.isAfter(availableFrom)) {
            throw new InvalidInputException("Available end time must be after start time.");
        }

        existing.setFullName(fullName.trim());
        existing.setEmail(email != null ? email.trim() : "");
        existing.setPhone(phone != null ? phone.trim() : "");
        existing.setSpecialization(specialization != null ? specialization.trim() : existing.getSpecialization());
        existing.setDepartment(department != null ? department.trim() : existing.getDepartment());
        existing.setConsultationFee(consultationFee);
        existing.setStatus(status);
        existing.setAvailableFrom(availableFrom);
        existing.setAvailableTo(availableTo);
        existing.setQualification(qualification);

        doctorRepository.save(existing);
        userRepository.save(existing);

        auditService.log("DOCTOR", "UPDATE", actor, "Updated doctor profile " + id);
        return existing;
    }

    public void setDoctorStatus(String doctorId, DoctorStatus status, String actor) throws DoctorNotFoundException {
        Doctor doctor = getDoctorById(doctorId);
        doctor.setStatus(status);
        doctorRepository.save(doctor);
        userRepository.save(doctor);
        auditService.log("DOCTOR", "STATUS_CHANGE", actor, "Changed status of " + doctorId + " to " + status);
    }

    public Doctor getDoctorById(String id) throws DoctorNotFoundException {
        if (id == null) {
            throw new DoctorNotFoundException("Doctor ID cannot be null.");
        }
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("No doctor found with ID: " + id));
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findActiveDoctors();
    }

    public List<Doctor> getDoctorsByDepartment(String dept) {
        return doctorRepository.findByDepartment(dept);
    }

    public List<Doctor> getDoctorsBySpecialization(String spec) {
        return doctorRepository.findBySpecialization(spec);
    }

    public List<Doctor> searchDoctors(String query) {
        return doctorRepository.search(query);
    }
}
