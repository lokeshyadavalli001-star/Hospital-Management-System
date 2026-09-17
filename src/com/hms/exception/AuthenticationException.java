package com.hms.exception;

/**
 * Thrown during failed authentication, locked account, or unauthorized action.
 */
public class AuthenticationException extends HospitalException {
    public AuthenticationException(String message) {
        super(message);
    }
}
