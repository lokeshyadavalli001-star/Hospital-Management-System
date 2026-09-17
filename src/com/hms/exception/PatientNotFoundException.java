package com.hms.exception;

/**
 * Thrown when a requested patient record cannot be located by ID or criteria.
 */
public class PatientNotFoundException extends HospitalException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
