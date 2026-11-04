package com.atm.exception;

/** Thrown when an account does not hold enough balance for an operation. */
public class InsufficientFundsException extends ATMException {
    private static final long serialVersionUID = 1L;

    public InsufficientFundsException(String message) { super(message); }
}
