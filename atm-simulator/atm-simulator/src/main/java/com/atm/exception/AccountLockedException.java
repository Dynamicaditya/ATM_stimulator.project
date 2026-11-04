package com.atm.exception;

/** Thrown when an account is locked after too many failed PIN attempts. */
public class AccountLockedException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public AccountLockedException(String message) { super(message); }
}
