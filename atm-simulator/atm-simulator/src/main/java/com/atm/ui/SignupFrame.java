package com.atm.ui;

import com.atm.model.Account;
import com.atm.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

/** Signup: creates the user, the account number and the hashed PIN. */
public class SignupFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private final JTextField nameField = Theme.textField(16);
    private final JTextField emailField = Theme.textField(16);
    private final JTextField phoneField = Theme.textField(16);
    private final JTextField depositField = Theme.textField(16);
    private final JPasswordField pinField = Theme.passwordField(16);
    private final JPasswordField confirmField = Theme.passwordField(16);
    private final RoundedButton createButton = RoundedButton.success("Create Account");
    private final RoundedButton backButton = RoundedButton.secondary("Back to Login");

    public SignupFrame() {
        super("ATM Simulator - Create Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 620);
        setResizable(false);
        setLocationRelativeTo(null);

        InputFilter.digits(phoneField, 10);
        InputFilter.money(depositField);
        InputFilter.digits(pinField, 4);
        InputFilter.digits(confirmField, 4);

        JPanel root = new JPanel(new BorderLayout());
        BrandPanel brand = new BrandPanel("Open your account in under a minute.",
                "A unique 12-digit account number is generated for you",
                "Choose a 4-digit PIN (we only store a secure hash)",
                "Optional opening deposit");
        brand.setPreferredSize(new java.awt.Dimension(340, 0));
        root.add(brand, BorderLayout.WEST);
        root.add(buildForm(), BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(createButton);

        createButton.addActionListener(e -> create());
        backButton.addActionListener(e -> {
            dispose();
            LoginFrame.open(null);
        });
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(44, 40, 30, 40));

        form.add(Theme.title("Create your account"));
        form.add(Box.createVerticalStrut(6));
        form.add(Theme.subtitle("Fill in your details below."));
        form.add(Box.createVerticalStrut(26));

        JPanel grid = new JPanel(new GridLayout(3, 2, 18, 18));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.add(Theme.labeled("Full name", nameField));
        grid.add(Theme.labeled("Email", emailField));
        grid.add(Theme.labeled("Mobile (10 digits)", phoneField));
        grid.add(Theme.labeled("Opening deposit (\u20B9, optional)", depositField));
        grid.add(Theme.labeled("Choose 4-digit PIN", pinField));
        grid.add(Theme.labeled("Confirm PIN", confirmField));
        grid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 260));
        form.add(grid);
        form.add(Box.createVerticalStrut(28));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 14, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        buttons.add(createButton);
        buttons.add(backButton);
        buttons.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 46));
        form.add(buttons);
        form.add(Box.createVerticalGlue());
        return form;
    }

    private void create() {
        final String name = nameField.getText();
        final String email = emailField.getText();
        final String phone = phoneField.getText();
        final String deposit = depositField.getText();
        final char[] pin = pinField.getPassword();
        final char[] confirm = confirmField.getPassword();

        Async.run(this, createButton,
                () -> authService.signup(name, email, phone, pin, confirm, deposit),
                (Account account) -> {
                    JLabel msg = new JLabel("<html><b>Account created successfully!</b><br><br>"
                            + "Your account number is:<br>"
                            + "<span style='font-size:20px;color:#1E3A8A'><b>" + account.getAccountNumber()
                            + "</b></span><br><br>Please note it down. You need it to log in.</html>");
                    javax.swing.JOptionPane.showMessageDialog(this, msg, "Welcome, " + account.getFullName(),
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    LoginFrame.open(account.getAccountNumber());
                },
                () -> {
                    pinField.setText("");
                    confirmField.setText("");
                });
    }
}
