package com.hms.model.enums;

/**
 * Payment reconciliation status for hospital bills and invoices.
 */
public enum PaymentStatus {
    PENDING,
    PARTIAL,
    PAID;

    public static PaymentStatus fromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String clean = str.trim().toUpperCase();
        for (PaymentStatus s : values()) {
            if (s.name().equals(clean)) {
                return s;
            }
        }
        return null;
    }
}
