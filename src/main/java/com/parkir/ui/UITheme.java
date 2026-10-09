package com.parkir.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class UITheme {
    private UITheme() {}

    // Palette: Slate dan Indigo
    public static final Color BG_DARK = new Color(15, 23, 42);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_APP = new Color(241, 245, 249);
    public static final Color PRIMARY = new Color(79, 70, 229);
    public static final Color PRIMARY_HOVER = new Color(67, 56, 202);
    public static final Color SUCCESS = new Color(5, 150, 105);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color DANGER_HOVER = new Color(185, 28, 28);
    public static final Color TEXT_MAIN = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TABLE_HEADER_BG = new Color(241, 245, 249);
    public static final Color TABLE_ROW_ALT = new Color(248, 250, 252);
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);
    public static final Color SIDEBAR_TEXT = new Color(203, 213, 225);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 14);

    private static final int RADIUS = 12;

    // ---------- Tombol ----------
    private static class RoundButton extends JButton {
        private Color base;
        private final Color hover;
        private final Color line;
        private final Color normalBase;
        private boolean over;

        RoundButton(String text, Color fg, Color base, Color hover, Color line) {
            super(text);
            this.base = base;
            this.normalBase = base;
            this.hover = hover;
            this.line = line;
            setFont(FONT_BOLD);
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMargin(new Insets(8, 18, 8, 18));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(getPreferredSize().width + 8, 40));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    over = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    over = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            base = active ? PRIMARY : normalBase;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = (over && base == normalBase) ? hover : base;
            if (fill != null) {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
            }
            if (line != null) {
                g2.setColor(line);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton createPrimaryButton(String text) {
        return new RoundButton(text, Color.WHITE, PRIMARY, PRIMARY_HOVER, null);
    }

    public static JButton createSecondaryButton(String text) {
        return new RoundButton(text, TEXT_MAIN, Color.WHITE, new Color(241, 245, 249), BORDER);
    }

    public static JButton createDangerButton(String text) {
        return new RoundButton(text, Color.WHITE, DANGER, DANGER_HOVER, null);
    }

    public static JButton createMenuButton(String text) {
        JButton btn = new RoundButton(text, SIDEBAR_TEXT, BG_DARK, SIDEBAR_HOVER, null);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(10, 16, 10, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(200, 42));
        return btn;
    }

    public static void setMenuActive(JButton btn, boolean active) {
        if (btn instanceof RoundButton) {
            ((RoundButton) btn).setActive(active);
            btn.setForeground(active ? Color.WHITE : SIDEBAR_TEXT);
        }
    }

    // ---------- Kartu ----------
    private static class RoundCard extends JPanel {
        RoundCard() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG_CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS + 4, RADIUS + 4);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS + 4, RADIUS + 4);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel createCard() {
        return new RoundCard();
    }

    // ---------- Input ----------
    public static void styleField(JTextComponent field) {
        Border b = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
        field.setBorder(b);
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_MAIN);
    }

    // ---------- Tabel ----------
    public static void formatTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT_MAIN);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_MAIN);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ROW_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}