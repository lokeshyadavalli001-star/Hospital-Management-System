package com.hms.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe sequence-based unique identifier generator.
 * Format: [PREFIX]-[SEQUENTIAL_NUMBER] (e.g., PAT-1001, DOC-2001, APT-3001, BIL-4001).
 */
public final class IdGenerator {

    private static final AtomicInteger PATIENT_SEQ = new AtomicInteger(1000);
    private static final AtomicInteger DOCTOR_SEQ = new AtomicInteger(2000);
    private static final AtomicInteger APPOINTMENT_SEQ = new AtomicInteger(3000);
    private static final AtomicInteger BILL_SEQ = new AtomicInteger(4000);
    private static final AtomicInteger USER_SEQ = new AtomicInteger(5000);

    private IdGenerator() {
    }

    public static String nextPatientId() {
        return "PAT-" + PATIENT_SEQ.incrementAndGet();
    }

    public static String nextDoctorId() {
        return "DOC-" + DOCTOR_SEQ.incrementAndGet();
    }

    public static String nextAppointmentId() {
        return "APT-" + APPOINTMENT_SEQ.incrementAndGet();
    }

    public static String nextBillId() {
        return "BIL-" + BILL_SEQ.incrementAndGet();
    }

    public static String nextUserId() {
        return "USR-" + USER_SEQ.incrementAndGet();
    }

    /**
     * Updates the sequence counter to prevent ID overlap when loading existing datasets.
     */
    public static void observeExistingId(String id) {
        if (id == null || !id.contains("-")) {
            return;
        }
        try {
            String[] parts = id.split("-", 2);
            String prefix = parts[0].toUpperCase();
            int num = Integer.parseInt(parts[1].trim());

            switch (prefix) {
                case "PAT":
                    PATIENT_SEQ.updateAndGet(curr -> Math.max(curr, num));
                    break;
                case "DOC":
                    DOCTOR_SEQ.updateAndGet(curr -> Math.max(curr, num));
                    break;
                case "APT":
                    APPOINTMENT_SEQ.updateAndGet(curr -> Math.max(curr, num));
                    break;
                case "BIL":
                    BILL_SEQ.updateAndGet(curr -> Math.max(curr, num));
                    break;
                case "USR":
                    USER_SEQ.updateAndGet(curr -> Math.max(curr, num));
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException ignored) {
            // Ignore custom-formatted historical IDs
        }
    }
}
