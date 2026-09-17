package com.hms.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation utility enforcing business rules and format integrity across HMS.
 */
public final class InputValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,13}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Set<String> BLOOD_GROUPS = new HashSet<>(
            Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    );

    private InputValidator() {
        // Utility class
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String clean = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= 130;
    }

    public static boolean isValidBloodGroup(String bg) {
        if (bg == null) return false;
        return BLOOD_GROUPS.contains(bg.trim().toUpperCase());
    }

    public static boolean isPositive(double amount) {
        return amount > 0.0;
    }

    public static boolean isNonNegative(double amount) {
        return amount >= 0.0;
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) return false;
        try {
            LocalDate.parse(dateStr.trim(), DateTimeUtil.DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidTime(String timeStr) {
        if (timeStr == null) return false;
        try {
            LocalTime.parse(timeStr.trim(), DateTimeUtil.TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
