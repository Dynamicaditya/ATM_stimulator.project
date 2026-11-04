package com.atm.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Flat rounded button with hover / pressed / disabled states. */
public class RoundedButton extends JButton {

    private final Color base;
    private boolean hover;

    public RoundedButton(String text, Color base, Color foreground) {
        super(text);
        this.base = base;
        setForeground(foreground);
        setFont(Theme.font(Font.BOLD, 14));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    public static RoundedButton primary(String text) {
        return new RoundedButton(text, Theme.ACCENT, Color.WHITE);
    }

    public static RoundedButton success(String text) {
        return new RoundedButton(text, Theme.SUCCESS, Color.WHITE);
    }

    public static RoundedButton danger(String text) {
        return new RoundedButton(text, Theme.DANGER, Color.WHITE);
    }

    public static RoundedButton secondary(String text) {
        return new RoundedButton(text, Theme.NEUTRAL, Theme.TEXT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill;
        if (!isEnabled()) {
            fill = new Color(0xCBD5E1);
        } else if (getModel().isPressed()) {
            fill = base.darker();
        } else if (hover) {
            fill = base.equals(Theme.NEUTRAL) ? new Color(0xD5DDE8) : base.brighter();
        } else {
            fill = base;
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }
}
