package com.atm.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/** Colours, fonts and small component factories so the whole app looks consistent. */
public final class Theme {

    public static final Color PRIMARY = new Color(0x1E3A8A);
    public static final Color PRIMARY_DARK = new Color(0x0F1F4D);
    public static final Color ACCENT = new Color(0x2563EB);
    public static final Color BG = new Color(0xF1F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color BORDER = new Color(0xCBD5E1);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color NEUTRAL = new Color(0xE2E8F0);

    private Theme() { }

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", font(Font.PLAIN, 13));
        UIManager.put("Label.font", font(Font.PLAIN, 14));
        UIManager.put("ComboBox.font", font(Font.PLAIN, 14));
    }

    // ------------------------------------------------------------ labels

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static JLabel title(String text) { return label(text, Font.BOLD, 24, TEXT); }

    public static JLabel subtitle(String text) { return label(text, Font.PLAIN, 14, MUTED); }

    public static JLabel fieldLabel(String text) { return label(text, Font.BOLD, 12, MUTED); }

    // ------------------------------------------------------------ inputs

    public static JTextField textField(int columns) {
        JTextField f = new JTextField(columns);
        styleInput(f);
        return f;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        styleInput(f);
        return f;
    }

    private static void styleInput(javax.swing.text.JTextComponent f) {
        f.setFont(font(Font.PLAIN, 15));
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
    }

    /** A label stacked above a component. */
    public static JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(fieldLabel(label.toUpperCase()), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------ containers

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));
        return p;
    }

    public static JPanel verticalBox() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }

    // ------------------------------------------------------------ dialogs

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static JLabel centered(String text, int style, int size, Color color) {
        JLabel l = label(text, style, size, color);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }
}
