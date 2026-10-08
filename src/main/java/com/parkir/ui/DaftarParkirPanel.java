package com.parkir.ui;

import com.parkir.model.Karcis;
import com.parkir.service.ParkirService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DaftarParkirPanel extends JPanel {
    private final ParkirService parkirService;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public DaftarParkirPanel(ParkirService parkirService) {
        this.parkirService = parkirService;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitle = new JLabel("Daftar Kendaraan Sedang Parkir");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JButton btnRefresh = UITheme.createSecondaryButton("Segarkan");
        btnRefresh.addActionListener(e -> refresh());

        top.add(lblTitle, BorderLayout.WEST);
        top.add(btnRefresh, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        String[] cols = {"ID Karcis", "Plat Nomor", "Jenis", "Slot", "Waktu Masuk", "Lama Parkir", "Petugas"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UITheme.formatTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        add(scrollPane, BorderLayout.CENTER);

        lblStatus = new JLabel("Belum ada data");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.TEXT_MUTED);
        add(lblStatus, BorderLayout.SOUTH);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<Karcis> aktifList = parkirService.daftarParkirAktif();
        LocalDateTime now = LocalDateTime.now();

        for (Karcis k : aktifList) {
            Duration d = Duration.between(k.getWaktuMasuk(), now);
            long jam = d.toHours();
            long menit = d.toMinutesPart();
            String durasiStr = jam + "j " + menit + "m";

            tableModel.addRow(new Object[]{
                    k.getId(),
                    k.getKendaraan().getPlatNomor(),
                    k.getKendaraan().getJenis().name(),
                    k.getSlot().getKode(),
                    k.getWaktuMasuk().format(TIME_FMT),
                    durasiStr,
                    k.getPetugas()
            });
        }

        if (aktifList.isEmpty()) {
            lblStatus.setText("Belum ada data kendaraan yang sedang parkir");
        } else {
            lblStatus.setText("Total kendaraan parkir: " + aktifList.size());
        }
    }
}
