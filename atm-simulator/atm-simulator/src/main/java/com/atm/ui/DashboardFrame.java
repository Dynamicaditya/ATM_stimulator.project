package com.atm.ui;

import com.atm.model.Account;
import com.atm.util.Session;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main window after login: sidebar navigation + card-switched feature screens. */
public class DashboardFrame extends JFrame {

    static final String HOME = "Home";
    static final String BALANCE = "Balance Enquiry";
    static final String DEPOSIT = "Deposit";
    static final String WITHDRAW = "Withdrawal";
    static final String TRANSFER = "Money Transfer";
    static final String STATEMENT = "Mini Statement";
    static final String HISTORY = "Transaction History";
    static final String PIN = "Change PIN";

    private static final int IDLE_TIMEOUT_MS = 3 * 60 * 1000;

    private final Session session;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, BasePanel> panels = new LinkedHashMap<>();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final Timer idleTimer;
    private final AWTEventListener activityListener;
    private boolean loggedOut;

    public DashboardFrame(Session session) {
        super("ATM Simulator - Dashboard");
        this.session = session;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        cards.setBackground(Theme.BG);
        register(HOME, new HomePanel(session, this::show));
        register(BALANCE, new BalancePanel(session));
        register(DEPOSIT, new DepositPanel(session));
        register(WITHDRAW, new WithdrawPanel(session));
        register(TRANSFER, new TransferPanel(session));
        register(STATEMENT, new MiniStatementPanel(session));
        register(HISTORY, new HistoryPanel(session));
        register(PIN, new PinChangePanel(session));

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout());
        right.add(buildTopBar(), BorderLayout.NORTH);
        right.add(cards, BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        // Auto logout after a period of inactivity.
        idleTimer = new Timer(IDLE_TIMEOUT_MS, e -> {
            Theme.showInfo(this, "You were logged out due to inactivity.");
            logout(false);
        });
        idleTimer.setRepeats(false);
        activityListener = event -> idleTimer.restart();
        Toolkit.getDefaultToolkit().addAWTEventListener(activityListener,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        idleTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { stopIdleWatch(); }
        });

        show(HOME);
    }

    private void register(String key, BasePanel panel) {
        panels.put(key, panel);
        cards.add(panel, key);
    }

    void show(String key) {
        cardLayout.show(cards, key);
        navButtons.forEach((k, b) -> b.setSelectedItem(k.equals(key)));
        panels.get(key).onShow();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.PRIMARY_DARK);
        side.setPreferredSize(new Dimension(240, 0));
        side.setBorder(BorderFactory.createEmptyBorder(24, 14, 20, 14));

        JLabel brand = Theme.label("ATM SIMULATOR", Font.BOLD, 18, Color.WHITE);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        side.add(brand);
        side.add(Box.createVerticalStrut(28));

        for (String key : panels.keySet()) {
            NavButton b = new NavButton(key);
            b.addActionListener(e -> show(key));
            navButtons.put(key, b);
            side.add(b);
            side.add(Box.createVerticalStrut(4));
        }
        side.add(Box.createVerticalGlue());

        NavButton logout = new NavButton("Logout");
        logout.setForeground(new Color(0xFCA5A5));
        logout.addActionListener(e -> {
            if (Theme.confirm(this, "Do you want to log out?")) {
                logout(false);
            }
        });
        side.add(logout);
        return side;
    }

    private JPanel buildTopBar() {
        Account a = session.getAccount();
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(14, 32, 14, 32)));

        JPanel left = Theme.verticalBox();
        left.add(Theme.label(a.getFullName(), Font.BOLD, 16, Theme.TEXT));
        left.add(Theme.label("Account " + a.getMaskedNumber(), Font.PLAIN, 12, Theme.MUTED));
        bar.add(left, BorderLayout.WEST);
        bar.add(Theme.label("Auto-logout after 3 min of inactivity", Font.PLAIN, 12, Theme.MUTED),
                BorderLayout.EAST);
        return bar;
    }

    private void stopIdleWatch() {
        idleTimer.stop();
        Toolkit.getDefaultToolkit().removeAWTEventListener(activityListener);
    }

    private void logout(boolean unused) {
        if (loggedOut) {
            return;
        }
        loggedOut = true;
        stopIdleWatch();
        dispose();
        LoginFrame.open(null);
    }

    // ---------------------------------------------------------------- sidebar button

    private static final class NavButton extends JButton {
        private boolean selectedItem;
        private boolean hover;

        NavButton(String text) {
            super(text);
            setFont(Theme.font(Font.PLAIN, 14));
            setForeground(Color.WHITE);
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(LEFT_ALIGNMENT);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setSelectedItem(boolean value) {
            selectedItem = value;
            setFont(Theme.font(value ? Font.BOLD : Font.PLAIN, 14));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (selectedItem) {
                g.setColor(Theme.ACCENT);
            } else if (hover) {
                g.setColor(new Color(0x1E3A8A));
            } else {
                g.setColor(Theme.PRIMARY_DARK);
            }
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g);
        }
    }
}
