package com.atm.ui;

import com.atm.service.AccountService;
import com.atm.service.AuthService;
import com.atm.util.Session;
import com.atm.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Login screen. */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final AccountService accountService = new AccountService();

    private final JTextField accountField = Theme.textField(22);
    private final JPasswordField pinField = Theme.passwordField(22);
    private final RoundedButton loginButton = RoundedButton.primary("Login");

    public LoginFrame(String prefillAccount) {
        super("ATM Simulator - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setResizable(false);
        setLocationRelativeTo(null);

        InputFilter.digits(accountField, 12);
        InputFilter.digits(pinField, 4);
        if (prefillAccount != null) {
            accountField.setText(prefillAccount);
        }

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(new BrandPanel("Secure, simple banking at your fingertips.",
                "PINs are salted and hashed (PBKDF2)",
                "Account locks after 3 wrong PIN attempts",
                "Every transaction is recorded safely"));
        root.add(buildForm());
        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(e -> login());
    }

    /** Shows a fresh login window. */
    public static void open(String prefillAccount) {
        SwingUtilities.invokeLater(() -> new LoginFrame(prefillAccount).setVisible(true));
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(70, 56, 40, 56));

        form.add(Theme.title("Welcome back"));
        form.add(Box.createVerticalStrut(6));
        form.add(Theme.subtitle("Login with your account number and PIN."));
        form.add(Box.createVerticalStrut(32));
        form.add(Theme.labeled("Account number", accountField));
        form.add(Box.createVerticalStrut(18));
        form.add(Theme.labeled("4-digit PIN", pinField));
        form.add(Box.createVerticalStrut(28));

        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 46));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(24));

        JPanel signupRow = new JPanel(new BorderLayout());
        signupRow.setOpaque(false);
        signupRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel link = Theme.label("<html>New here? <u>Create an account</u></html>",
                Font.BOLD, 14, Theme.ACCENT);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new SignupFrame().setVisible(true));
            }
        });
        signupRow.add(link, BorderLayout.WEST);
        signupRow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
        form.add(signupRow);
        form.add(Box.createVerticalGlue());
        return form;
    }

    private void login() {
        String accountNumber = accountField.getText();
        char[] pin = pinField.getPassword();
        try {
            Validator.accountNumber(accountNumber);
            Validator.pin(pin);
        } catch (com.atm.exception.ValidationException ex) {
            Theme.showError(this, ex.getMessage());
            return;
        }
        Async.run(this, loginButton,
                () -> authService.login(accountNumber, pin),
                account -> {
                    Session session = new Session(account, authService, accountService);
                    new DashboardFrame(session).setVisible(true);
                    dispose();
                },
                () -> pinField.setText(""));
    }
}
