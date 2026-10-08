package com.parkir.ui;

import com.parkir.model.JenisKendaraan;
import com.parkir.model.User;
import com.parkir.service.ParkirService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final ParkirService parkirService;
    private final JPanel cardsPanel;
    private JLabel lblTotalParkir;

    public DashboardPanel(ParkirService parkirService) {
        this.parkirService = parkirService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Dashboard Kapasitas Parkir");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        lblTotalParkir = new JLabel("Total Parkir: 0");
        lblTotalParkir.setFont(UITheme.FONT_HEADER);
        lblTotalParkir.setForeground(UITheme.PRIMARY);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblTotalParkir, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Cards panel untuk slot per jenis
        cardsPanel = new JPanel(new GridLayout(1, 3, 16, 16));
        cardsPanel.setOpaque(false);
        add(cardsPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        cardsPanel.removeAll();
        Map<JenisKendaraan, ParkirService.RingkasanSlotItem> ringkasan = parkirService.ringkasanSlot();

        int totalTerisi = 0;
        for (JenisKendaraan jenis : JenisKendaraan.values()) {
            ParkirService.RingkasanSlotItem item = ringkasan.get(jenis);
            if (item != null) {
                totalTerisi += item.getTerisi();
                cardsPanel.add(createJenisCard(item));
            }
        }
        lblTotalParkir.setText("Total Kendaraan Parkir: " + totalTerisi);

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel createJenisCard(ParkirService.RingkasanSlotItem item) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblJenis = new JLabel(item.getJenis().name());
        lblJenis.setFont(UITheme.FONT_HEADER);
        lblJenis.setForeground(UITheme.PRIMARY);
        lblJenis.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblKosong = new JLabel(String.valueOf(item.getKosong()));
        lblKosong.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblKosong.setForeground(item.getKosong() > 0 ? UITheme.SUCCESS : UITheme.DANGER);
        lblKosong.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblKet = new JLabel("Slot Kosong Tersisa");
        lblKet.setFont(UITheme.FONT_SMALL);
        lblKet.setForeground(UITheme.TEXT_MUTED);
        lblKet.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTerisi = new JLabel("Terisi: " + item.getTerisi() + " / " + item.getKapasitas());
        lblTerisi.setFont(UITheme.FONT_BODY);
        lblTerisi.setForeground(UITheme.TEXT_MAIN);
        lblTerisi.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblJenis);
        card.add(Box.createVerticalStrut(12));
        card.add(lblKosong);
        card.add(lblKet);
        card.add(Box.createVerticalStrut(16));
        card.add(sep);
        card.add(Box.createVerticalStrut(12));
        card.add(lblTerisi);

        return card;
    }
}
