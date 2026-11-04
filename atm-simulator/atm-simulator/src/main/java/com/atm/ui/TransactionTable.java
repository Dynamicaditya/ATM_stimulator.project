package com.atm.ui;

import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.util.Format;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** Styled table that lists transactions (used by Mini Statement and Transaction History). */
public class TransactionTable extends JTable {

    private static final String[] COLUMNS = {"Date & Time", "Type", "Details", "Amount", "Balance"};
    private final Model model = new Model();

    public TransactionTable() {
        setModel(model);
        setRowHeight(36);
        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));
        setFont(Theme.font(Font.PLAIN, 13));
        setForeground(Theme.TEXT);
        setSelectionBackground(new Color(0xDBEAFE));
        setSelectionForeground(Theme.TEXT);
        setFillsViewportHeight(true);
        setAutoCreateRowSorter(false);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setDefaultRenderer(new HeaderRenderer());
        getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));

        getColumnModel().getColumn(0).setPreferredWidth(170);
        getColumnModel().getColumn(1).setPreferredWidth(130);
        getColumnModel().getColumn(2).setPreferredWidth(240);
        getColumnModel().getColumn(3).setPreferredWidth(130);
        getColumnModel().getColumn(4).setPreferredWidth(130);

        for (int c = 0; c < 3; c++) {
            getColumnModel().getColumn(c).setCellRenderer(new PaddedRenderer(SwingConstants.LEFT));
        }
        getColumnModel().getColumn(3).setCellRenderer(new AmountRenderer());
        getColumnModel().getColumn(4).setCellRenderer(new PaddedRenderer(SwingConstants.RIGHT));
    }

    public void setTransactions(List<Transaction> transactions) {
        model.set(transactions);
    }

    public JScrollPane inScrollPane() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        sp.setColumnHeaderView(getTableHeader());
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private static boolean isCredit(Transaction t) {
        return t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.TRANSFER_IN;
    }

    // ---------------------------------------------------------------- model

    private static final class Model extends AbstractTableModel {
        private List<Transaction> rows = new ArrayList<>();

        void set(List<Transaction> data) {
            rows = new ArrayList<>(data);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int c) { return COLUMNS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override
        public Object getValueAt(int row, int col) {
            Transaction t = rows.get(row);
            switch (col) {
                case 0: return Format.dateTime(t.getCreatedAt());
                case 1: return t.getType().getLabel();
                case 2: return t.getDescription() == null ? "" : t.getDescription();
                case 3: return (isCredit(t) ? "+ " : "- ") + Format.money(t.getAmount());
                case 4: return Format.money(t.getBalanceAfter());
                default: return "";
            }
        }
    }

    // ---------------------------------------------------------------- renderers

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setBackground(Theme.PRIMARY);
            setForeground(Color.WHITE);
            setFont(Theme.font(Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(table, value, false, false, row, col);
            setBackground(Theme.PRIMARY);
            setForeground(Color.WHITE);
            setFont(Theme.font(Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(col >= 3 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return this;
        }
    }

    private static final class PaddedRenderer extends DefaultTableCellRenderer {
        private final int alignment;

        PaddedRenderer(int alignment) { this.alignment = alignment; }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(alignment);
            return this;
        }
    }

    private static final class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            l.setHorizontalAlignment(SwingConstants.RIGHT);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            l.setFont(Theme.font(Font.BOLD, 13));
            String text = String.valueOf(value);
            l.setForeground(text.startsWith("+") ? Theme.SUCCESS : Theme.DANGER);
            return l;
        }
    }
}
