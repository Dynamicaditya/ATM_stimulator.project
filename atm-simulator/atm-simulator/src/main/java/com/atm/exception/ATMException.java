package com.atm.exception;

/** Base checked exception for all business-level errors. Messages are safe to show to the user. */
public class ATMException extends Exception {
    private static final long serialVersionUID = 1L;

    public ATMException(String message) { super(message); }

    public ATMException(String message, Throwable cause) { super(message, cause); }
}
