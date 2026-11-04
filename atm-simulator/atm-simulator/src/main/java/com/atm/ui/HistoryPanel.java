package com.atm.ui;

import com.atm.exception.ValidationException;
import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.service.AccountService;
import com.atm.util.Session;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Full Transaction History with type and date filters. */
public class HistoryPanel extends BasePanel {

    private static final String ALL = "All types";

    private final JComboBox<Object> typeBox = new JComboBox<>();
    private final JTextField fromField = Theme.textField(9);
    private final JTextField toField = Theme.textField(9);
    private final RoundedButton searchButton = RoundedButton.primary("Search");
    private final RoundedButton clearButton = RoundedButton.secondary("Clear");
    private final TransactionTable table = new TransactionTable();
    private final JLabel statusLabel = Theme.subtitle(" ");

    public HistoryPanel(Session session) {
        super(session, "Transaction History", "Every deposit, withdrawal and transfer on your account.", true);

        typeBox.addItem(ALL);
        for (TransactionType t : TransactionType.values()) {
            typeBox.addItem(t);
        }
        typeBox.setPreferredSize(new java.awt.Dimension(170, 40));
        fromField.setToolTipText("yyyy-MM-dd");
        toField.setToolTipText("yyyy-MM-dd");
        InputFilter.dateText(fromField);
        InputFilter.dateText(toField);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filters.setOpaque(false);
        filters.add(Theme.labeled("Type", typeBox));
        filters.add(Theme.labeled("From (yyyy-mm-dd)", fromField));
        filters.add(Theme.labeled("To (yyyy-mm-dd)", toField));
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(searchButton);
        buttons.add(clearButton);
        JPanel buttonWrap = new JPanel(new BorderLayout());
        buttonWrap.setOpaque(false);
        buttonWrap.add(javax.swing.Box.createVerticalStrut(22), BorderLayout.NORTH);
        buttonWrap.add(buttons, BorderLayout.CENTER);
        filters.add(buttonWrap);

        content.add(filters, BorderLayout.NORTH);
        content.add(table.inScrollPane(), BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> load());
        clearButton.addActionListener(e -> {
            typeBox.setSelectedIndex(0);
            fromField.setText("");
            toField.setText("");
            load();
        });
    }

    @Override
    public void onShow() {
        load();
    }

    private void load() {
        final TransactionType type;
        final LocalDate from;
        final LocalDate to;
        try {
            Object selected = typeBox.getSelectedItem();
            type = selected instanceof TransactionType ? (TransactionType) selected : null;
            from = parseDate(fromField.getText(), "From");
            to = parseDate(toField.getText(), "To");
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
            return;
        }
        Async.run(this, searchButton,
                () -> session.getAccountService().getHistory(session.getAccountNumber(), type, from, to),
                (List<Transaction> list) -> {
                    table.setTransactions(list);
                    String note = list.size() >= AccountService.HISTORY_LIMIT
                            ? " (showing the latest " + AccountService.HISTORY_LIMIT + ")" : "";
                    statusLabel.setText(list.isEmpty() ? "No transactions found."
                            : list.size() + " transaction(s)" + note);
                }, null);
    }

    private static LocalDate parseDate(String text, String name) throws ValidationException {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("'" + name + "' date must be in yyyy-MM-dd format, e.g. 2026-01-31.");
        }
    }
}
