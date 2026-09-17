package com.hms.repository;

import com.hms.model.Admin;
import com.hms.model.Doctor;
import com.hms.model.Receptionist;
import com.hms.model.User;
import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.UserRole;
import com.hms.util.DateTimeUtil;
import com.hms.util.FileUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository persisting system user credentials and polymorphic User subclasses.
 */
public class UserRepository extends AbstractCsvRepository<User> {

    public UserRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected String getCsvHeader() {
        return "id,username,passwordHash,salt,fullName,email,phone,role,active,createdAt,extra1,extra2";
    }

    @Override
    protected String serialize(User u) {
        String extra1 = "";
        String extra2 = "";

        if (u instanceof Admin) {
            extra1 = ((Admin) u).getDepartmentAccess();
        } else if (u instanceof Receptionist) {
            Receptionist rec = (Receptionist) u;
            extra1 = rec.getShift();
            extra2 = rec.getDeskNumber();
        } else if (u instanceof Doctor) {
            Doctor doc = (Doctor) u;
            extra1 = doc.getSpecialization();
            extra2 = doc.getDepartment();
        }

        return FileUtil.toCsvLine(Arrays.asList(
                u.getId(),
                u.getUsername(),
                u.getPasswordHash(),
                u.getSalt(),
                u.getFullName(),
                u.getEmail() != null ? u.getEmail() : "",
                u.getPhone() != null ? u.getPhone() : "",
                u.getRole().name(),
                String.valueOf(u.isActive()),
                DateTimeUtil.formatDateTime(u.getCreatedAt()),
                extra1,
                extra2
        ));
    }

    @Override
    protected User deserialize(List<String> tokens) {
        if (tokens.size() < 10) {
            return null;
        }
        String id = tokens.get(0);
        String username = tokens.get(1);
        String passwordHash = tokens.get(2);
        String salt = tokens.get(3);
        String fullName = tokens.get(4);
        String email = tokens.get(5);
        String phone = tokens.get(6);
        UserRole role = UserRole.fromString(tokens.get(7));
        boolean active = Boolean.parseBoolean(tokens.get(8));
        LocalDateTime createdAt = DateTimeUtil.parseDateTime(tokens.get(9));
        String extra1 = tokens.size() > 10 ? tokens.get(10) : "";
        String extra2 = tokens.size() > 11 ? tokens.get(11) : "";

        if (role == UserRole.ADMIN) {
            return new Admin(id, username, passwordHash, salt, fullName, email, phone, active, createdAt, extra1);
        } else if (role == UserRole.RECEPTIONIST) {
            return new Receptionist(id, username, passwordHash, salt, fullName, email, phone, active, createdAt, extra1, extra2);
        } else if (role == UserRole.DOCTOR) {
            return new Doctor(id, username, passwordHash, salt, fullName, email, phone, active, createdAt,
                    extra1.isEmpty() ? "General Medicine" : extra1,
                    extra2.isEmpty() ? "OPD" : extra2,
                    500.0, DoctorStatus.ACTIVE, LocalTime.of(9, 0), LocalTime.of(17, 0), "MBBS");
        } else {
            // Default fallback
            return new Admin(id, username, passwordHash, salt, fullName, email, phone, active, createdAt, "DEFAULT");
        }
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String clean = username.trim();
        return findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(clean))
                .findFirst();
    }

    public List<User> findByRole(UserRole role) {
        if (role == null) return findAll();
        return findAll().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }
}
