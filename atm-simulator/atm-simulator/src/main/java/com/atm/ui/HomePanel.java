package com.atm.ui;

import com.atm.util.Session;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.function.Consumer;

/** Dashboard home: quick-access tiles for every feature. */
public class HomePanel extends BasePanel {

    public HomePanel(Session session, Consumer<String> navigate) {
        super(session, "Hello, " + firstName(session.getAccount().getFullName()),
                "What would you like to do today?", true);

        JPanel tiles = new JPanel(new GridLayout(2, 4, 16, 16));
        tiles.setOpaque(false);
        tiles.add(tile("Balance Enquiry", "Check your balance", DashboardFrame.BALANCE, new Color(0x2563EB), navigate));
        tiles.add(tile("Deposit", "Add cash", DashboardFrame.DEPOSIT, new Color(0x16A34A), navigate));
        tiles.add(tile("Withdrawal", "Take cash out", DashboardFrame.WITHDRAW, new Color(0xDC2626), navigate));
        tiles.add(tile("Money Transfer", "Send to another account", DashboardFrame.TRANSFER, new Color(0x7C3AED), navigate));
        tiles.add(tile("Mini Statement", "Last 5 transactions", DashboardFrame.STATEMENT, new Color(0x0891B2), navigate));
        tiles.add(tile("Transaction History", "Search and filter", DashboardFrame.HISTORY, new Color(0xEA580C), navigate));
        tiles.add(tile("Change PIN", "Update your PIN", DashboardFrame.PIN, new Color(0x475569), navigate));
        tiles.add(securityTip());
        content.add(tiles, BorderLayout.NORTH);
    }

    private static RoundedButton tile(String title, String hint, String key, Color color,
                                      Consumer<String> navigate) {
        RoundedButton b = new RoundedButton("<html><div style='text-align:center'>" + title
                + "<br><span style='font-size:10px;font-weight:normal'>" + hint + "</span></div></html>",
                color, Color.WHITE);
        b.addActionListener(e -> navigate.accept(key));
        b.setPreferredSize(new java.awt.Dimension(200, 120));
        return b;
    }

    private static javax.swing.JComponent securityTip() {
        javax.swing.JLabel tip = Theme.label("<html><div style='text-align:center;width:150px'><b>Stay safe</b><br>"
                + "Never share your PIN. We will never ask for it.</div></html>",
                java.awt.Font.PLAIN, 12, Theme.MUTED);
        tip.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tip.setOpaque(true);
        tip.setBackground(Theme.BG);
        return tip;
    }

    private static String firstName(String fullName) {
        int space = fullName.indexOf(' ');
        return space > 0 ? fullName.substring(0, space) : fullName;
    }
}
