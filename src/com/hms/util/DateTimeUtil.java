package com.hms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Standardized date and time formatting and parsing utilities.
 */
public final class DateTimeUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
    }

    public static String formatDate(LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "";
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        return LocalDate.parse(text.trim(), DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        return (time != null) ? time.format(TIME_FORMATTER) : "";
    }

    public static LocalTime parseTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        return LocalTime.parse(text.trim(), TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return (dateTime != null) ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(text.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Fallback try ISO format
            return LocalDateTime.parse(text.trim());
        }
    }
}
