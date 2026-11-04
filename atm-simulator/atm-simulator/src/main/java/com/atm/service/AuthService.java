package com.atm.service;

import com.atm.config.DatabaseConfig;
import com.atm.dao.AccountDAO;
import com.atm.dao.TransactionDAO;
import com.atm.dao.UserDAO;
import com.atm.exception.ATMException;
import com.atm.exception.AccountLockedException;
import com.atm.exception.AuthenticationException;
import com.atm.exception.ValidationException;
import com.atm.model.Account;
import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.model.User;
import com.atm.util.PinHasher;
import com.atm.util.Validator;

import java.math.BigDecimal;
import java.security.SecureRandom;

/** Login, signup and PIN re-verification. */
public class AuthService {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    private static final BigDecimal MIN_OPENING_DEPOSIT = BigDecimal.ZERO;

    private final UserDAO userDAO = new UserDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final SecureRandom random = new SecureRandom();

    /**
     * Verifies account number + PIN. Failed attempts are counted and the account
     * is locked after {@value #MAX_FAILED_ATTEMPTS} wrong PINs.
     * Also used to re-confirm the PIN before sensitive operations.
     */
    public Account login(String accountNumber, char[] pin) throws ATMException {
        try {
            String number = Validator.accountNumber(accountNumber);
            Validator.pin(pin);

            // The outcome is decided inside the transaction, but the exception is thrown
            // after commit so the failed-attempt counter is persisted.
            Object[] outcome = DatabaseConfig.inTransaction(con -> {
                Account account = accountDAO.findByNumberForUpdate(con, number);
                if (account == null) {
                    return new Object[] {null, new AuthenticationException("Invalid account number or PIN.")};
                }
                if (account.isLocked()) {
                    return new Object[] {null, new AccountLockedException(
                            "This account is locked after too many wrong PIN attempts. Please contact the bank.")};
                }
                if (PinHasher.verify(pin, account.getPinSalt(), account.getPinHash())) {
                    if (account.getFailedAttempts() != 0) {
                        accountDAO.updateFailedAttempts(con, number, 0, false);
                    }
                    return new Object[] {account, null};
                }
                int attempts = account.getFailedAttempts() + 1;
                boolean lock = attempts >= MAX_FAILED_ATTEMPTS;
                accountDAO.updateFailedAttempts(con, number, attempts, lock);
                if (lock) {
                    return new Object[] {null, new AccountLockedException(
                            "Too many wrong PIN attempts. Your account has been locked.")};
                }
                int left = MAX_FAILED_ATTEMPTS - attempts;
                return new Object[] {null, new AuthenticationException(
                        "Invalid account number or PIN. " + left + " attempt(s) left.")};
            });

            if (outcome[1] != null) {
                throw (ATMException) outcome[1];
            }
            return (Account) outcome[0];
        } finally {
            PinHasher.wipe(pin);
        }
    }

    /** Creates a user + account. Returns the new account (including its generated number). */
    public Account signup(String fullName, String email, String phone, char[] pin, char[] confirmPin,
                          String openingDeposit) throws ATMException {
        try {
            String name = Validator.name(fullName);
            String mail = Validator.email(email);
            String mobile = Validator.phone(phone);
            Validator.newPin(pin);
            if (!java.util.Arrays.equals(pin, confirmPin)) {
                throw new ValidationException("PIN and confirmation PIN do not match.");
            }
            BigDecimal deposit = MIN_OPENING_DEPOSIT;
            if (openingDeposit != null && !openingDeposit.isBlank()) {
                deposit = Validator.amount(openingDeposit);
                Validator.deposit(deposit);
            }
            final BigDecimal opening = deposit;

            return DatabaseConfig.inTransaction(con -> {
                if (userDAO.emailExists(con, mail)) {
                    throw new ValidationException("An account with this email already exists.");
                }
                if (userDAO.phoneExists(con, mobile)) {
                    throw new ValidationException("An account with this mobile number already exists.");
                }
                User user = new User(name, mail, mobile);
                long userId = userDAO.insert(con, user);

                String accountNumber = generateUniqueAccountNumber(con);
                String salt = PinHasher.generateSalt();
                String hash = PinHasher.hash(pin, salt);
                accountDAO.insert(con, accountNumber, userId, hash, salt, opening);

                if (opening.signum() > 0) {
                    transactionDAO.insert(con, new Transaction(accountNumber, TransactionType.DEPOSIT,
                            opening, opening, null, "Opening deposit"));
                }
                return accountDAO.findByNumber(con, accountNumber);
            });
        } finally {
            PinHasher.wipe(pin);
            PinHasher.wipe(confirmPin);
        }
    }

    private String generateUniqueAccountNumber(java.sql.Connection con) throws java.sql.SQLException, ATMException {
        for (int i = 0; i < 20; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(1 + random.nextInt(9));
            for (int d = 1; d < 12; d++) {
                sb.append(random.nextInt(10));
            }
            String candidate = sb.toString();
            if (!accountDAO.exists(con, candidate)) {
                return candidate;
            }
        }
        throw new ATMException("Could not generate a unique account number. Please try again.");
    }
}
