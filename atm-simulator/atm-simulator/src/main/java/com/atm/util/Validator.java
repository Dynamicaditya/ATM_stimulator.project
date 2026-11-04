package com.atm.util;

import com.atm.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/** All input validation rules live here. */
public final class Validator {

    public static final BigDecimal MAX_DEPOSIT = new BigDecimal("100000.00");
    public static final BigDecimal MAX_WITHDRAWAL = new BigDecimal("50000.00");
    public static final BigDecimal MAX_TRANSFER = new BigDecimal("200000.00");
    public static final BigDecimal WITHDRAWAL_MULTIPLE = new BigDecimal("100");

    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,79}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern ACCOUNT = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PIN = Pattern.compile("^[0-9]{4}$");

    private Validator() { }

    public static String name(String value) throws ValidationException {
        String v = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (!NAME.matcher(v).matches()) {
            throw new ValidationException("Enter a valid full name (letters only, at least 2 characters).");
        }
        return v;
    }

    public static String email(String value) throws ValidationException {
        String v = value == null ? "" : value.trim().toLowerCase();
        if (v.length() > 120 || !EMAIL.matcher(v).matches()) {
            throw new ValidationException("Enter a valid email address.");
        }
        return v;
    }

    public static String phone(String value) throws ValidationException {
        String v = value == null ? "" : value.trim();
        if (!PHONE.matcher(v).matches()) {
            throw new ValidationException("Enter a valid 10-digit mobile number.");
        }
        return v;
    }

    public static String accountNumber(String value) throws ValidationException {
        String v = value == null ? "" : value.trim();
        if (!ACCOUNT.matcher(v).matches()) {
            throw new ValidationException("Account number must be exactly 12 digits.");
        }
        return v;
    }

    public static void pin(char[] pin) throws ValidationException {
        if (pin == null || !PIN.matcher(new String(pin)).matches()) {
            throw new ValidationException("PIN must be exactly 4 digits.");
        }
    }

    /** A new PIN must be 4 digits and not trivially guessable (e.g. 1111, 1234). */
    public static void newPin(char[] pin) throws ValidationException {
        pin(pin);
        String s = new String(pin);
        boolean allSame = s.chars().distinct().count() == 1;
        boolean sequential = "0123456789".contains(s) || "9876543210".contains(s);
        if (allSame || sequential) {
            throw new ValidationException("Choose a stronger PIN (avoid repeated or sequential digits like 1111 or 1234).");
        }
    }

    /** Parses a money amount: positive, max 2 decimals. */
    public static BigDecimal amount(String text) throws ValidationException {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Please enter an amount.");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new ValidationException("Amount must be a valid number.");
        }
        if (value.signum() <= 0) {
            throw new ValidationException("Amount must be greater than zero.");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Amount can have at most 2 decimal places.");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static void deposit(BigDecimal amount) throws ValidationException {
        if (amount.compareTo(MAX_DEPOSIT) > 0) {
            throw new ValidationException("Maximum deposit per transaction is " + Format.money(MAX_DEPOSIT) + ".");
        }
    }

    public static void withdrawal(BigDecimal amount) throws ValidationException {
        if (amount.compareTo(MAX_WITHDRAWAL) > 0) {
            throw new ValidationException("Maximum withdrawal per transaction is " + Format.money(MAX_WITHDRAWAL) + ".");
        }
        if (amount.remainder(WITHDRAWAL_MULTIPLE).signum() != 0) {
            throw new ValidationException("Withdrawal amount must be a multiple of " + WITHDRAWAL_MULTIPLE + ".");
        }
    }

    public static void transfer(BigDecimal amount) throws ValidationException {
        if (amount.compareTo(MAX_TRANSFER) > 0) {
            throw new ValidationException("Maximum transfer per transaction is " + Format.money(MAX_TRANSFER) + ".");
        }
    }
}
