package com.atm.ui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import java.util.regex.Pattern;

/** Restricts what can be typed into a text field (digits only / money amount) and its length. */
public final class InputFilter extends DocumentFilter {

    private static final Pattern DIGITS = Pattern.compile("[0-9]*");
    private static final Pattern MONEY = Pattern.compile("[0-9]*(\\.[0-9]{0,2})?");

    private final int maxLength;
    private final Pattern pattern;

    private static final Pattern DATE_TEXT = Pattern.compile("[0-9-]*");

    private InputFilter(int maxLength, Pattern pattern) {
        this.maxLength = maxLength;
        this.pattern = pattern;
    }

    public static void digits(JTextComponent c, int maxLength) {
        ((AbstractDocument) c.getDocument()).setDocumentFilter(new InputFilter(maxLength, DIGITS));
    }

    /** Allows digits and '-' only (for yyyy-MM-dd input). */
    public static void dateText(JTextComponent c) {
        ((AbstractDocument) c.getDocument()).setDocumentFilter(new InputFilter(10, DATE_TEXT));
    }

    public static void money(JTextComponent c) {
        ((AbstractDocument) c.getDocument()).setDocumentFilter(new InputFilter(12, MONEY));
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
            throws BadLocationException {
        replace(fb, offset, 0, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String next = new StringBuilder(current)
                .replace(offset, offset + length, text == null ? "" : text).toString();
        if (next.length() <= maxLength && pattern.matcher(next).matches()) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
