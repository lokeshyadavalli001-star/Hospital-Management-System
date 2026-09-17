package com.hms.repository;

import com.hms.model.Patient;
import com.hms.model.enums.Gender;
import com.hms.util.DateTimeUtil;
import com.hms.util.FileUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository handling patient entity serialization and custom queries.
 */
public class PatientRepository extends AbstractCsvRepository<Patient> {

    public PatientRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected String getCsvHeader() {
        return "id,name,age,gender,phone,email,address,bloodGroup,emergencyContact,registrationDate,active";
    }

    @Override
    protected String serialize(Patient p) {
        return FileUtil.toCsvLine(Arrays.asList(
                p.getId(),
                p.getName(),
                String.valueOf(p.getAge()),
                p.getGender().name(),
                p.getPhone(),
                p.getEmail() != null ? p.getEmail() : "",
                p.getAddress() != null ? p.getAddress() : "",
                p.getBloodGroup() != null ? p.getBloodGroup() : "",
                p.getEmergencyContact() != null ? p.getEmergencyContact() : "",
                DateTimeUtil.formatDate(p.getRegistrationDate()),
                String.valueOf(p.isActive())
        ));
    }

    @Override
    protected Patient deserialize(List<String> tokens) {
        if (tokens.size() < 11) {
            return null;
        }
        String id = tokens.get(0);
        String name = tokens.get(1);
        int age = Integer.parseInt(tokens.get(2));
        Gender gender = Gender.fromString(tokens.get(3));
        String phone = tokens.get(4);
        String email = tokens.get(5);
        String address = tokens.get(6);
        String bloodGroup = tokens.get(7);
        String emergencyContact = tokens.get(8);
        LocalDate regDate = DateTimeUtil.parseDate(tokens.get(9));
        boolean active = Boolean.parseBoolean(tokens.get(10));

        return new Patient(id, name, age, gender, phone, email, address, bloodGroup, emergencyContact, regDate, active);
    }

    public Optional<Patient> findByPhone(String phone) {
        if (phone == null) return Optional.empty();
        String clean = phone.replaceAll("[\\s-]", "");
        return findAll().stream()
                .filter(p -> p.getPhone() != null && p.getPhone().replaceAll("[\\s-]", "").equals(clean))
                .findFirst();
    }

    public List<Patient> findByName(String name) {
        if (name == null || name.trim().isEmpty()) return findAll();
        String clean = name.trim().toLowerCase();
        return findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(clean))
                .collect(Collectors.toList());
    }

    public List<Patient> findActivePatients() {
        return findAll().stream()
                .filter(Patient::isActive)
                .collect(Collectors.toList());
    }
}
