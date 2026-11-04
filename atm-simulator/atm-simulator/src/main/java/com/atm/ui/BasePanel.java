package com.atm.ui;

import com.atm.util.Session;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

/** Common layout for every feature screen: title + subtitle on top, a white card below. */
public abstract class BasePanel extends JPanel {

    protected final Session session;
    /** The white card that holds the screen's content. */
    protected final JPanel content = Theme.card();

    /**
     * @param stretch true if the card should fill the available space (tables),
     *                false for compact forms.
     */
    protected BasePanel(Session session, String title, String subtitle, boolean stretch) {
        super(new BorderLayout(0, 18));
        this.session = session;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = Theme.verticalBox();
        header.add(Theme.title(title));
        header.add(javax.swing.Box.createRigidArea(new Dimension(0, 4)));
        header.add(Theme.subtitle(subtitle));
        add(header, BorderLayout.NORTH);

        if (stretch) {
            content.setLayout(new BorderLayout(0, 12));
            add(content, BorderLayout.CENTER);
        } else {
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(spacer(460, 0));
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(content);
            add(wrapper, BorderLayout.CENTER);
        }
    }

    /** Adds a labelled field to a form card (non-stretch panels). */
    protected void addField(String label, JComponent field) {
        content.add(Theme.labeled(label, field));
        content.add(spacer(0, 16));
    }

    protected void addComponent(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(c);
        content.add(spacer(0, 16));
    }

    /** Invisible left-aligned spacer (Box.createRigidArea is centre-aligned and would shift the form). */
    private static JPanel spacer(int width, int height) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(width, height));
        p.setMaximumSize(new Dimension(width == 0 ? Integer.MAX_VALUE : width, height == 0 ? Integer.MAX_VALUE : height));
        p.setMinimumSize(new Dimension(width, height));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Called every time the screen becomes visible – override to reload data. */
    public void onShow() { }
}
