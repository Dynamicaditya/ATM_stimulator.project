package com.atm.ui;

import com.atm.exception.ValidationException;
import com.atm.util.Format;
import com.atm.util.Session;
import com.atm.util.Validator;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.math.BigDecimal;

/** Deposit. */
public class DepositPanel extends BasePanel {

    private final JTextField amountField = Theme.textField(24);
    private final RoundedButton depositButton = RoundedButton.success("Deposit Money");

    public DepositPanel(Session session) {
        super(session, "Deposit", "Add cash to your account (max "
                + Format.money(Validator.MAX_DEPOSIT) + " per transaction).", false);

        InputFilter.money(amountField);
        addField("Amount (\u20B9)", amountField);
        addComponent(quickAmounts(amountField, "500", "1000", "5000", "10000"));
        addComponent(depositButton);

        depositButton.addActionListener(e -> deposit());
        amountField.addActionListener(e -> deposit());
    }

    static JPanel quickAmounts(JTextField target, String... values) {
        JPanel p = new JPanel();
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.X_AXIS));
        p.setOpaque(false);
        for (String v : values) {
            RoundedButton b = RoundedButton.secondary("\u20B9 " + v);
            b.setBorder(javax.swing.BorderFactory.createEmptyBorder(9, 14, 9, 14));
            b.addActionListener(e -> target.setText(v));
            p.add(b);
            p.add(javax.swing.Box.createHorizontalStrut(8));
        }
        return p;
    }

    private void deposit() {
        BigDecimal amount;
        try {
            amount = Validator.amount(amountField.getText());
            Validator.deposit(amount);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
            return;
        }
        final BigDecimal value = amount;
        Async.run(this, depositButton,
                () -> session.getAccountService().deposit(session.getAccountNumber(), value),
                newBalance -> {
                    amountField.setText("");
                    Theme.showSuccess(this, Format.money(value) + " deposited successfully.\n"
                            + "New balance: " + Format.money(newBalance));
                }, null);
    }
}
