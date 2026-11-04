package com.atm.exception;

/** Thrown for invalid credentials. */
public class AuthenticationException extends ATMException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) { super(message); }
}
