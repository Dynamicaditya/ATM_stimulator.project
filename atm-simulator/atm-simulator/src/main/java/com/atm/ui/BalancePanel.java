package com.atm.ui;

import com.atm.model.Account;
import com.atm.util.Format;
import com.atm.util.Session;

import javax.swing.JLabel;
import java.awt.Font;
import java.time.LocalDateTime;

/** Balance Enquiry. */
public class BalancePanel extends BasePanel {

    private final JLabel balanceLabel = Theme.label("\u20B9 --", Font.BOLD, 40, Theme.PRIMARY);
    private final JLabel asOfLabel = Theme.subtitle(" ");
    private final JLabel holderLabel = Theme.label(" ", Font.BOLD, 15, Theme.TEXT);
    private final JLabel accountLabel = Theme.subtitle(" ");
    private final RoundedButton refreshButton = RoundedButton.primary("Refresh Balance");

    public BalancePanel(Session session) {
        super(session, "Balance Enquiry", "Your available balance, right now.", false);

        addComponent(Theme.fieldLabel("AVAILABLE BALANCE"));
        addComponent(balanceLabel);
        addComponent(asOfLabel);
        addComponent(holderLabel);
        addComponent(accountLabel);
        addComponent(refreshButton);

        refreshButton.addActionListener(e -> refresh());
    }

    @Override
    public void onShow() {
        refresh();
    }

    private void refresh() {
        Async.run(this, refreshButton,
                () -> session.getAccountService().getAccount(session.getAccountNumber()),
                (Account a) -> {
                    balanceLabel.setText(Format.money(a.getBalance()));
                    holderLabel.setText(a.getFullName());
                    accountLabel.setText("Account No: " + a.getAccountNumber());
                    asOfLabel.setText("As of " + Format.dateTime(LocalDateTime.now()));
                }, null);
    }
}
