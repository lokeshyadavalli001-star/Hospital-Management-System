package com.hms.exception;

/**
 * Thrown when payment amounts are invalid, exceed balance, or are zero/negative.
 */
public class InsufficientPaymentException extends HospitalException {
    public InsufficientPaymentException(String message) {
        super(message);
    }
}
