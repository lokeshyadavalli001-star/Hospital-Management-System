package com.hms.model.enums;

/**
 * Represents the gender identity for patients and medical personnel.
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    /**
     * Parse string to Gender case-insensitively with friendly error handling.
     *
     * @param value raw text
     * @return matching Gender or null if invalid
     */
    public static Gender fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim().toUpperCase();
        for (Gender g : values()) {
            if (g.name().equals(clean)) {
                return g;
            }
        }
        return null;
    }
}
