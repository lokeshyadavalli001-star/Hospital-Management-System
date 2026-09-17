package com.hms.exception;

/**
 * Thrown when an appointment booking conflicts with an existing slot for doctor or patient.
 */
public class AppointmentConflictException extends HospitalException {
    public AppointmentConflictException(String message) {
        super(message);
    }
}
