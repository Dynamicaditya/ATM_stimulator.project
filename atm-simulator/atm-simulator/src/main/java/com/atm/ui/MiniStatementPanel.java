package com.atm.ui;

import com.atm.model.Transaction;
import com.atm.service.AccountService;
import com.atm.util.Session;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

/** Mini Statement: the last few transactions. */
public class MiniStatementPanel extends BasePanel {

    private final TransactionTable table = new TransactionTable();
    private final JLabel statusLabel = Theme.subtitle(" ");
    private final RoundedButton refreshButton = RoundedButton.primary("Refresh");

    public MiniStatementPanel(Session session) {
        super(session, "Mini Statement", "Your last " + AccountService.MINI_STATEMENT_SIZE
                + " transactions.", true);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(statusLabel, BorderLayout.WEST);
        bottom.add(refreshButton, BorderLayout.EAST);

        content.add(table.inScrollPane(), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        refreshButton.addActionListener(e -> load());
    }

    @Override
    public void onShow() {
        load();
    }

    private void load() {
        Async.run(this, refreshButton,
                () -> session.getAccountService().getMiniStatement(session.getAccountNumber()),
                (List<Transaction> list) -> {
                    table.setTransactions(list);
                    statusLabel.setText(list.isEmpty() ? "No transactions yet."
                            : "Showing " + list.size() + " most recent transaction(s).");
                }, null);
    }
}
