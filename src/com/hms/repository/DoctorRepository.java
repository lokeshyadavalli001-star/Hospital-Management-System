package com.hms.repository;

import com.hms.model.Doctor;
import com.hms.model.enums.DoctorStatus;
import com.hms.util.DateTimeUtil;
import com.hms.util.FileUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository handling medical practitioner persistence and specialized queries.
 */
public class DoctorRepository extends AbstractCsvRepository<Doctor> {

    public DoctorRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected String getCsvHeader() {
        return "id,username,passwordHash,salt,fullName,email,phone,active,createdAt,specialization,department,consultationFee,status,availableFrom,availableTo,qualification";
    }

    @Override
    protected String serialize(Doctor d) {
        return FileUtil.toCsvLine(Arrays.asList(
                d.getId(),
                d.getUsername(),
                d.getPasswordHash() != null ? d.getPasswordHash() : "",
                d.getSalt() != null ? d.getSalt() : "",
                d.getFullName(),
                d.getEmail() != null ? d.getEmail() : "",
                d.getPhone() != null ? d.getPhone() : "",
                String.valueOf(d.isActive()),
                DateTimeUtil.formatDateTime(d.getCreatedAt()),
                d.getSpecialization(),
                d.getDepartment(),
                String.valueOf(d.getConsultationFee()),
                d.getStatus().name(),
                DateTimeUtil.formatTime(d.getAvailableFrom()),
                DateTimeUtil.formatTime(d.getAvailableTo()),
                d.getQualification() != null ? d.getQualification() : ""
        ));
    }

    @Override
    protected Doctor deserialize(List<String> tokens) {
        if (tokens.size() < 16) {
            return null;
        }
        String id = tokens.get(0);
        String username = tokens.get(1);
        String passwordHash = tokens.get(2);
        String salt = tokens.get(3);
        String fullName = tokens.get(4);
        String email = tokens.get(5);
        String phone = tokens.get(6);
        boolean active = Boolean.parseBoolean(tokens.get(7));
        LocalDateTime createdAt = DateTimeUtil.parseDateTime(tokens.get(8));
        String specialization = tokens.get(9);
        String department = tokens.get(10);
        double fee = Double.parseDouble(tokens.get(11));
        DoctorStatus status = DoctorStatus.fromString(tokens.get(12));
        LocalTime availableFrom = DateTimeUtil.parseTime(tokens.get(13));
        LocalTime availableTo = DateTimeUtil.parseTime(tokens.get(14));
        String qualification = tokens.get(15);

        return new Doctor(id, username, passwordHash, salt, fullName, email, phone,
                active, createdAt, specialization, department, fee, status, availableFrom, availableTo, qualification);
    }

    public List<Doctor> findByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) return findAll();
        String clean = department.trim().toLowerCase();
        return findAll().stream()
                .filter(d -> d.getDepartment().toLowerCase().contains(clean))
                .collect(Collectors.toList());
    }

    public List<Doctor> findBySpecialization(String spec) {
        if (spec == null || spec.trim().isEmpty()) return findAll();
        String clean = spec.trim().toLowerCase();
        return findAll().stream()
                .filter(d -> d.getSpecialization().toLowerCase().contains(clean))
                .collect(Collectors.toList());
    }

    public List<Doctor> findActiveDoctors() {
        return findAll().stream()
                .filter(d -> d.getStatus() == DoctorStatus.ACTIVE && d.isActive())
                .collect(Collectors.toList());
    }

    public Optional<Doctor> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return findAll().stream()
                .filter(d -> d.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst();
    }
}
