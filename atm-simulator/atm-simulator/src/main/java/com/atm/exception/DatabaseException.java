package com.atm.exception;

/** Wraps SQLException so that technical details never reach the UI. */
public class DatabaseException extends ATMException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message, Throwable cause) { super(message, cause); }
}
