package com.atm.ui;

import com.atm.exception.ValidationException;
import com.atm.util.Session;
import com.atm.util.Validator;

import javax.swing.JPasswordField;

/** PIN Change. */
public class PinChangePanel extends BasePanel {

    private final JPasswordField oldPin = Theme.passwordField(24);
    private final JPasswordField newPin = Theme.passwordField(24);
    private final JPasswordField confirmPin = Theme.passwordField(24);
    private final RoundedButton changeButton = RoundedButton.primary("Change PIN");

    public PinChangePanel(Session session) {
        super(session, "Change PIN", "Use a 4-digit PIN that is hard to guess. Never share it with anyone.", false);

        InputFilter.digits(oldPin, 4);
        InputFilter.digits(newPin, 4);
        InputFilter.digits(confirmPin, 4);
        addField("Current PIN", oldPin);
        addField("New PIN", newPin);
        addField("Confirm new PIN", confirmPin);
        addComponent(changeButton);

        changeButton.addActionListener(e -> change());
        confirmPin.addActionListener(e -> change());
    }

    @Override
    public void onShow() {
        clear();
    }

    private void clear() {
        oldPin.setText("");
        newPin.setText("");
        confirmPin.setText("");
    }

    private void change() {
        try {
            Validator.pin(oldPin.getPassword());
            Validator.newPin(newPin.getPassword());
            if (!java.util.Arrays.equals(newPin.getPassword(), confirmPin.getPassword())) {
                throw new ValidationException("New PIN and confirmation PIN do not match.");
            }
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
            return;
        }
        final char[] current = oldPin.getPassword();
        final char[] verifyCopy = oldPin.getPassword();
        final char[] fresh = newPin.getPassword();
        final char[] confirm = confirmPin.getPassword();

        Async.run(this, changeButton,
                () -> {
                    // Counts wrong attempts towards the lock-out rule, then updates the hash.
                    session.getAuthService().login(session.getAccountNumber(), verifyCopy);
                    session.getAccountService().changePin(session.getAccountNumber(), current, fresh, confirm);
                    return Boolean.TRUE;
                },
                ok -> {
                    clear();
                    Theme.showSuccess(this, "Your PIN has been changed successfully.");
                },
                this::clear);
    }
}
