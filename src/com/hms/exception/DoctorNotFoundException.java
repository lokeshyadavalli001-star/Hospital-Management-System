package com.hms.exception;

/**
 * Thrown when a doctor record is missing or inactive during scheduling.
 */
public class DoctorNotFoundException extends HospitalException {
    public DoctorNotFoundException(String message) {
        super(message);
    }
}
