package com.hms.model.enums;

/**
 * State lifecycle of an outpatient hospital appointment.
 */
public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    RESCHEDULED;

    public static AppointmentStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        String clean = statusStr.trim().toUpperCase();
        for (AppointmentStatus s : values()) {
            if (s.name().equals(clean)) {
                return s;
            }
        }
        return null;
    }
}
