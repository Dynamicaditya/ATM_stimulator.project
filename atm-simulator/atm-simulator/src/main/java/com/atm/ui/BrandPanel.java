package com.atm.ui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Blue gradient side panel used on the login and signup screens. */
public class BrandPanel extends JPanel {

    public BrandPanel(String headline, String... points) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(60, 44, 40, 44));

        JLabel logo = Theme.label("ATM", Font.BOLD, 56, Color.WHITE);
        JLabel bank = Theme.label("SIMULATOR", Font.BOLD, 20, new Color(0x93C5FD));
        JLabel head = Theme.label("<html><div style='width:260px'>" + headline + "</div></html>",
                Font.PLAIN, 17, Color.WHITE);
        add(logo);
        add(bank);
        add(javax.swing.Box.createVerticalStrut(36));
        add(head);
        add(javax.swing.Box.createVerticalStrut(28));
        for (String point : points) {
            JLabel l = Theme.label("<html><div style='width:260px'>\u2713  " + point + "</div></html>",
                    Font.PLAIN, 14, new Color(0xBFDBFE));
            add(l);
            add(javax.swing.Box.createVerticalStrut(12));
        }
        add(javax.swing.Box.createVerticalGlue());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, 0, getHeight(), Theme.PRIMARY_DARK));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
