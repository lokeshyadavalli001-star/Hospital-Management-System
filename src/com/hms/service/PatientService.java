package com.hms.service;

import com.hms.exception.InvalidInputException;
import com.hms.exception.PatientNotFoundException;
import com.hms.model.Patient;
import com.hms.model.enums.Gender;
import com.hms.repository.PatientRepository;
import com.hms.util.IdGenerator;
import com.hms.util.InputValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing patient registration, updates, searching, and demographic validation.
 */
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuditService auditService;

    public PatientService(PatientRepository patientRepository, AuditService auditService) {
        this.patientRepository = patientRepository;
        this.auditService = auditService;
    }

    public Patient registerPatient(String name, int age, Gender gender, String phone,
                                   String email, String address, String bloodGroup,
                                   String emergencyContact, String actor) throws InvalidInputException {
        validateDemographics(name, age, phone, email, bloodGroup);

        String id = IdGenerator.nextPatientId();
        Patient patient = new Patient(id, name.trim(), age, gender, phone.trim(),
                email != null ? email.trim() : "",
                address != null ? address.trim() : "",
                bloodGroup != null ? bloodGroup.trim().toUpperCase() : "N/A",
                emergencyContact != null ? emergencyContact.trim() : "",
                LocalDate.now(), true);

        patientRepository.save(patient);
        auditService.log("PATIENT", "REGISTER", actor, "Registered patient " + id + " (" + name + ")");
        return patient;
    }

    public Patient updatePatient(String id, String name, int age, Gender gender, String phone,
                                 String email, String address, String bloodGroup,
                                 String emergencyContact, boolean active, String actor)
            throws PatientNotFoundException, InvalidInputException {
        Patient existing = getPatientById(id);
        validateDemographics(name, age, phone, email, bloodGroup);

        existing.setName(name.trim());
        existing.setAge(age);
        existing.setGender(gender);
        existing.setPhone(phone.trim());
        existing.setEmail(email != null ? email.trim() : "");
        existing.setAddress(address != null ? address.trim() : "");
        existing.setBloodGroup(bloodGroup != null ? bloodGroup.trim().toUpperCase() : "N/A");
        existing.setEmergencyContact(emergencyContact != null ? emergencyContact.trim() : "");
        existing.setActive(active);

        patientRepository.save(existing);
        auditService.log("PATIENT", "UPDATE", actor, "Updated patient " + id);
        return existing;
    }

    public void deactivatePatient(String id, String actor) throws PatientNotFoundException {
        Patient patient = getPatientById(id);
        patient.setActive(false);
        patientRepository.save(patient);
        auditService.log("PATIENT", "DEACTIVATE", actor, "Deactivated patient " + id);
    }

    public Patient getPatientById(String id) throws PatientNotFoundException {
        if (id == null) {
            throw new PatientNotFoundException("Patient ID cannot be null.");
        }
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("No patient found with ID: " + id));
    }

    public Patient findByPhone(String phone) throws PatientNotFoundException {
        return patientRepository.findByPhone(phone)
                .orElseThrow(() -> new PatientNotFoundException("No patient found with phone number: " + phone));
    }

    public List<Patient> searchPatients(String query) {
        return patientRepository.search(query);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> getActivePatients() {
        return patientRepository.findActivePatients();
    }

    private void validateDemographics(String name, int age, String phone, String email, String bloodGroup)
            throws InvalidInputException {
        if (!InputValidator.isNotEmpty(name)) {
            throw new InvalidInputException("Patient name cannot be empty.");
        }
        if (!InputValidator.isValidAge(age)) {
            throw new InvalidInputException("Age must be between 0 and 130 years.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new InvalidInputException("Invalid phone number format. Must be 10-13 digits.");
        }
        if (InputValidator.isNotEmpty(email) && !InputValidator.isValidEmail(email)) {
            throw new InvalidInputException("Invalid email address format.");
        }
        if (InputValidator.isNotEmpty(bloodGroup) && !bloodGroup.equalsIgnoreCase("N/A")
                && !InputValidator.isValidBloodGroup(bloodGroup)) {
            throw new InvalidInputException("Invalid blood group. Allowed: A+, A-, B+, B-, AB+, AB-, O+, O-.");
        }
    }
}
