package com.atm.exception;

/** Thrown when user input fails validation. */
public class ValidationException extends ATMException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) { super(message); }
}
