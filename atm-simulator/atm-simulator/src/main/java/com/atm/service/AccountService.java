package com.atm.service;

import com.atm.config.DatabaseConfig;
import com.atm.dao.AccountDAO;
import com.atm.dao.TransactionDAO;
import com.atm.exception.ATMException;
import com.atm.exception.InsufficientFundsException;
import com.atm.exception.ValidationException;
import com.atm.model.Account;
import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.util.PinHasher;
import com.atm.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Banking operations. Every money movement runs in one DB transaction with row locks. */
public class AccountService {

    public static final int MINI_STATEMENT_SIZE = 5;
    public static final int HISTORY_LIMIT = 500;

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // ------------------------------------------------------------------ reads

    public Account getAccount(String accountNumber) throws ATMException {
        return DatabaseConfig.query(con -> {
            Account a = accountDAO.findByNumber(con, accountNumber);
            if (a == null) {
                throw new ATMException("Account not found.");
            }
            return a;
        });
    }

    public BigDecimal getBalance(String accountNumber) throws ATMException {
        return getAccount(accountNumber).getBalance();
    }

    /** Returns the holder's name for a 12-digit account number, used to confirm a transfer recipient. */
    public String lookupHolderName(String accountNumber) throws ATMException {
        String number = Validator.accountNumber(accountNumber);
        return DatabaseConfig.query(con -> {
            Account a = accountDAO.findByNumber(con, number);
            if (a == null) {
                throw new ValidationException("Recipient account not found.");
            }
            return a.getFullName();
        });
    }

    public List<Transaction> getMiniStatement(String accountNumber) throws ATMException {
        return DatabaseConfig.query(con ->
                transactionDAO.findRecent(con, accountNumber, MINI_STATEMENT_SIZE));
    }

    public List<Transaction> getHistory(String accountNumber, TransactionType type,
                                        LocalDate from, LocalDate to) throws ATMException {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("'From' date must not be after 'To' date.");
        }
        return DatabaseConfig.query(con ->
                transactionDAO.search(con, accountNumber, type, from, to, HISTORY_LIMIT));
    }

    // ------------------------------------------------------------------ writes

    /** @return the new balance */
    public BigDecimal deposit(String accountNumber, BigDecimal amount) throws ATMException {
        Validator.deposit(amount);
        return DatabaseConfig.inTransaction(con -> {
            Account a = requireAccount(accountDAO.findByNumberForUpdate(con, accountNumber));
            BigDecimal newBalance = a.getBalance().add(amount);
            accountDAO.updateBalance(con, accountNumber, newBalance);
            transactionDAO.insert(con, new Transaction(accountNumber, TransactionType.DEPOSIT,
                    amount, newBalance, null, "Cash deposit"));
            return newBalance;
        });
    }

    /** @return the new balance */
    public BigDecimal withdraw(String accountNumber, BigDecimal amount) throws ATMException {
        Validator.withdrawal(amount);
        return DatabaseConfig.inTransaction(con -> {
            Account a = requireAccount(accountDAO.findByNumberForUpdate(con, accountNumber));
            if (a.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance. Available: "
                        + com.atm.util.Format.money(a.getBalance()));
            }
            BigDecimal newBalance = a.getBalance().subtract(amount);
            accountDAO.updateBalance(con, accountNumber, newBalance);
            transactionDAO.insert(con, new Transaction(accountNumber, TransactionType.WITHDRAWAL,
                    amount, newBalance, null, "Cash withdrawal"));
            return newBalance;
        });
    }

    /** @return the sender's new balance */
    public BigDecimal transfer(String fromAccount, String toAccount, BigDecimal amount) throws ATMException {
        String to = Validator.accountNumber(toAccount);
        if (fromAccount.equals(to)) {
            throw new ValidationException("You cannot transfer money to your own account.");
        }
        Validator.transfer(amount);

        return DatabaseConfig.inTransaction(con -> {
            // Lock rows in a fixed order so two opposite transfers can never deadlock.
            Account first;
            Account second;
            if (fromAccount.compareTo(to) < 0) {
                first = accountDAO.findByNumberForUpdate(con, fromAccount);
                second = accountDAO.findByNumberForUpdate(con, to);
            } else {
                second = accountDAO.findByNumberForUpdate(con, to);
                first = accountDAO.findByNumberForUpdate(con, fromAccount);
            }
            Account sender = fromAccount.compareTo(to) < 0 ? first : second;
            Account receiver = fromAccount.compareTo(to) < 0 ? second : first;

            if (sender == null) {
                throw new ATMException("Your account was not found.");
            }
            if (sender.isLocked()) {
                throw new ATMException("This account is locked. Please contact the bank.");
            }
            if (receiver == null) {
                throw new ValidationException("Recipient account not found.");
            }
            if (receiver.isLocked()) {
                throw new ValidationException("Recipient account cannot receive transfers right now.");
            }
            if (sender.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance. Available: "
                        + com.atm.util.Format.money(sender.getBalance()));
            }

            BigDecimal senderBalance = sender.getBalance().subtract(amount);
            BigDecimal receiverBalance = receiver.getBalance().add(amount);
            accountDAO.updateBalance(con, sender.getAccountNumber(), senderBalance);
            accountDAO.updateBalance(con, receiver.getAccountNumber(), receiverBalance);

            transactionDAO.insert(con, new Transaction(sender.getAccountNumber(), TransactionType.TRANSFER_OUT,
                    amount, senderBalance, receiver.getAccountNumber(),
                    "Transfer to " + receiver.getFullName()));
            transactionDAO.insert(con, new Transaction(receiver.getAccountNumber(), TransactionType.TRANSFER_IN,
                    amount, receiverBalance, sender.getAccountNumber(),
                    "Transfer from " + sender.getFullName()));
            return senderBalance;
        });
    }

    /**
     * Changes the PIN. The caller is expected to have verified the old PIN
     * (see {@link AuthService#login}) – the old PIN is re-checked here as well.
     */
    public void changePin(String accountNumber, char[] oldPin, char[] newPin, char[] confirmPin)
            throws ATMException {
        try {
            Validator.pin(oldPin);
            Validator.newPin(newPin);
            if (!java.util.Arrays.equals(newPin, confirmPin)) {
                throw new ValidationException("New PIN and confirmation PIN do not match.");
            }
            if (java.util.Arrays.equals(oldPin, newPin)) {
                throw new ValidationException("New PIN must be different from the current PIN.");
            }
            DatabaseConfig.inTransaction(con -> {
                Account a = requireAccount(accountDAO.findByNumberForUpdate(con, accountNumber));
                if (!PinHasher.verify(oldPin, a.getPinSalt(), a.getPinHash())) {
                    throw new com.atm.exception.AuthenticationException("Current PIN is incorrect.");
                }
                String salt = PinHasher.generateSalt();
                accountDAO.updatePin(con, accountNumber, PinHasher.hash(newPin, salt), salt);
                return Boolean.TRUE;
            });
        } finally {
            PinHasher.wipe(oldPin);
            PinHasher.wipe(newPin);
            PinHasher.wipe(confirmPin);
        }
    }

    private Account requireAccount(Account a) throws ATMException {
        if (a == null) {
            throw new ATMException("Account not found.");
        }
        if (a.isLocked()) {
            throw new ATMException("This account is locked. Please contact the bank.");
        }
        return a;
    }
}
