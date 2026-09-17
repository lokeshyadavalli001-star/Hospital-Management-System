package com.hms.service;

import com.hms.exception.AuthenticationException;
import com.hms.exception.InvalidInputException;
import com.hms.model.Admin;
import com.hms.model.Doctor;
import com.hms.model.Receptionist;
import com.hms.model.User;
import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.UserRole;
import com.hms.repository.UserRepository;
import com.hms.util.IdGenerator;
import com.hms.util.InputValidator;
import com.hms.util.PasswordUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing user identity, access privileges, and authentication.
 */
public class UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public User authenticate(String username, String rawPassword) throws AuthenticationException {
        if (!InputValidator.isNotEmpty(username) || !InputValidator.isNotEmpty(rawPassword)) {
            throw new AuthenticationException("Username and password are required.");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            auditService.log("AUTH", "LOGIN_FAILED", username, "User does not exist");
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            auditService.log("AUTH", "LOGIN_BLOCKED", username, "User account is deactivated");
            throw new AuthenticationException("Account is deactivated. Please contact an administrator.");
        }

        boolean valid = PasswordUtil.verifyPassword(rawPassword, user.getSalt(), user.getPasswordHash());
        if (!valid) {
            auditService.log("AUTH", "LOGIN_FAILED", username, "Password mismatch");
            throw new AuthenticationException("Invalid username or password.");
        }

        auditService.log("AUTH", "LOGIN_SUCCESS", username, "Role: " + user.getRole());
        return user;
    }

    public User createUser(String username, String rawPassword, String fullName,
                           String email, String phone, UserRole role,
                           String extra1, String extra2, String actor) throws InvalidInputException {
        if (!InputValidator.isNotEmpty(username) || username.length() < 3) {
            throw new InvalidInputException("Username must be at least 3 characters long.");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new InvalidInputException("Username '" + username + "' is already registered.");
        }
        if (!InputValidator.isNotEmpty(rawPassword) || rawPassword.length() < 4) {
            throw new InvalidInputException("Password must be at least 4 characters long.");
        }
        if (!InputValidator.isNotEmpty(fullName)) {
            throw new InvalidInputException("Full name cannot be empty.");
        }
        if (role == null) {
            throw new InvalidInputException("User role must be specified.");
        }

        String id = IdGenerator.nextUserId();
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(rawPassword, salt);
        LocalDateTime now = LocalDateTime.now();

        User user;
        if (role == UserRole.ADMIN) {
            user = new Admin(id, username, hash, salt, fullName, email, phone, true, now, extra1);
        } else if (role == UserRole.RECEPTIONIST) {
            user = new Receptionist(id, username, hash, salt, fullName, email, phone, true, now, extra1, extra2);
        } else if (role == UserRole.DOCTOR) {
            user = new Doctor(id, username, hash, salt, fullName, email, phone, true, now,
                    (extra1 != null && !extra1.isEmpty()) ? extra1 : "General Medicine",
                    (extra2 != null && !extra2.isEmpty()) ? extra2 : "OPD",
                    500.0, DoctorStatus.ACTIVE, LocalTime.of(9, 0), LocalTime.of(17, 0), "MBBS");
        } else {
            user = new Admin(id, username, hash, salt, fullName, email, phone, true, now, "GENERAL");
        }

        userRepository.save(user);
        auditService.log("USER", "CREATE", actor, "Created user " + username + " with role " + role);
        return user;
    }

    public void changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException, InvalidInputException {
        User user = authenticate(username, oldPassword);
        if (!InputValidator.isNotEmpty(newPassword) || newPassword.length() < 4) {
            throw new InvalidInputException("New password must be at least 4 characters long.");
        }

        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);
        user.setSalt(newSalt);
        user.setPasswordHash(newHash);
        userRepository.save(user);
        auditService.log("USER", "PASSWORD_CHANGE", username, "Password updated successfully");
    }

    public void setUserActiveStatus(String userId, boolean active, String actor) throws InvalidInputException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new InvalidInputException("User ID not found: " + userId);
        }
        User user = userOpt.get();
        user.setActive(active);
        userRepository.save(user);
        auditService.log("USER", active ? "ACTIVATE" : "DEACTIVATE", actor, "User ID: " + userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
}
