package com.atm.ui;

import com.atm.exception.ValidationException;
import com.atm.util.Format;
import com.atm.util.Session;
import com.atm.util.Validator;

import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.math.BigDecimal;

/** Withdrawal (PIN is re-confirmed before money leaves the account). */
public class WithdrawPanel extends BasePanel {

    private final JTextField amountField = Theme.textField(24);
    private final JPasswordField pinField = Theme.passwordField(24);
    private final RoundedButton withdrawButton = RoundedButton.danger("Withdraw Cash");

    public WithdrawPanel(Session session) {
        super(session, "Withdrawal", "Multiples of " + Validator.WITHDRAWAL_MULTIPLE + ", max "
                + Format.money(Validator.MAX_WITHDRAWAL) + " per transaction.", false);

        InputFilter.money(amountField);
        InputFilter.digits(pinField, 4);
        addField("Amount (\u20B9)", amountField);
        addComponent(DepositPanel.quickAmounts(amountField, "500", "1000", "2000", "5000", "10000"));
        addField("Confirm with your PIN", pinField);
        addComponent(withdrawButton);

        withdrawButton.addActionListener(e -> withdraw());
        pinField.addActionListener(e -> withdraw());
    }

    private void withdraw() {
        final BigDecimal amount;
        try {
            amount = Validator.amount(amountField.getText());
            Validator.withdrawal(amount);
            Validator.pin(pinField.getPassword());
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
            return;
        }
        final char[] pin = pinField.getPassword();
        Async.run(this, withdrawButton,
                () -> {
                    session.getAuthService().login(session.getAccountNumber(), pin); // re-verify PIN
                    return session.getAccountService().withdraw(session.getAccountNumber(), amount);
                },
                newBalance -> {
                    amountField.setText("");
                    pinField.setText("");
                    Theme.showSuccess(this, "Please collect your cash: " + Format.money(amount) + "\n"
                            + "Remaining balance: " + Format.money(newBalance));
                },
                () -> pinField.setText(""));
    }
}
