package com.hms.model.enums;

/**
 * Operational availability status of a medical consultant.
 */
public enum DoctorStatus {
    ACTIVE,
    ON_LEAVE,
    INACTIVE;

    public static DoctorStatus fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String clean = str.trim().toUpperCase();
        for (DoctorStatus s : values()) {
            if (s.name().equals(clean)) {
                return s;
            }
        }
        return null;
    }
}
