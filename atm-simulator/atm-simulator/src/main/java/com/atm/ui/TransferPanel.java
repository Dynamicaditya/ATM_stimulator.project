package com.atm.ui;

import com.atm.exception.ValidationException;
import com.atm.util.Format;
import com.atm.util.Session;
import com.atm.util.Validator;

import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.math.BigDecimal;

/** Money Transfer to another account in the same bank. */
public class TransferPanel extends BasePanel {

    private final JTextField recipientField = Theme.textField(24);
    private final JTextField amountField = Theme.textField(24);
    private final JPasswordField pinField = Theme.passwordField(24);
    private final RoundedButton transferButton = RoundedButton.primary("Transfer Money");

    public TransferPanel(Session session) {
        super(session, "Money Transfer", "Send money instantly to any account (max "
                + Format.money(Validator.MAX_TRANSFER) + " per transaction).", false);

        InputFilter.digits(recipientField, 12);
        InputFilter.money(amountField);
        InputFilter.digits(pinField, 4);
        addField("Recipient account number (12 digits)", recipientField);
        addField("Amount (\u20B9)", amountField);
        addField("Confirm with your PIN", pinField);
        addComponent(transferButton);

        transferButton.addActionListener(e -> startTransfer());
        pinField.addActionListener(e -> startTransfer());
    }

    private void startTransfer() {
        final String recipient;
        final BigDecimal amount;
        try {
            recipient = Validator.accountNumber(recipientField.getText());
            amount = Validator.amount(amountField.getText());
            Validator.transfer(amount);
            Validator.pin(pinField.getPassword());
            if (recipient.equals(session.getAccountNumber())) {
                throw new ValidationException("You cannot transfer money to your own account.");
            }
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
            return;
        }
        final char[] pin = pinField.getPassword();

        // Step 1: verify PIN and look up the recipient's name (so the user can double-check it).
        Async.run(this, transferButton,
                () -> {
                    session.getAuthService().login(session.getAccountNumber(), pin);
                    return session.getAccountService().lookupHolderName(recipient);
                },
                holderName -> {
                    pinField.setText("");
                    boolean ok = Theme.confirm(this, "Transfer " + Format.money(amount) + " to\n"
                            + holderName + " (" + recipient + ")?");
                    if (ok) {
                        completeTransfer(recipient, amount, holderName);
                    }
                },
                () -> pinField.setText(""));
    }

    // Step 2: PIN already verified, perform the transfer atomically.
    private void completeTransfer(String recipient, BigDecimal amount, String holderName) {
        Async.run(this, transferButton,
                () -> session.getAccountService().transfer(session.getAccountNumber(), recipient, amount),
                newBalance -> {
                    recipientField.setText("");
                    amountField.setText("");
                    Theme.showSuccess(this, Format.money(amount) + " sent to " + holderName + ".\n"
                            + "Remaining balance: " + Format.money(newBalance));
                }, null);
    }
}
