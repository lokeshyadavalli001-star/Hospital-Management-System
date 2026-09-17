package com.hms.exception;

/**
 * Thrown when terminal input fails syntax or domain validation rules.
 */
public class InvalidInputException extends HospitalException {
    public InvalidInputException(String message) {
        super(message);
    }
}
