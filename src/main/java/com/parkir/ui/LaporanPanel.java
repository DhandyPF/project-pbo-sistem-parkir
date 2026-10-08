package com.parkir.ui;

import com.parkir.model.JenisKendaraan;
import com.parkir.service.LaporanService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LaporanPanel extends JPanel {
    private final LaporanService laporanService;

    private JTextField tfTanggal;
    private JLabel lblTotalMasuk;
    private JLabel lblTotalKeluar;
    private JLabel lblTotalPendapatan;
    private DefaultTableModel tableModel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public LaporanPanel(LaporanService laporanService) {
        this.laporanService = laporanService;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel lblTitle = new JLabel("Laporan Harian Parkir (Admin)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        add(lblTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);

        // Filter Baris Atas
        JPanel filterCard = UITheme.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        JLabel lblTgl = new JLabel("Tanggal (YYYY-MM-DD):");
        lblTgl.setFont(UITheme.FONT_BOLD);

        tfTanggal = new JTextField(LocalDate.now().format(DATE_FMT), 10);
        tfTanggal.setFont(UITheme.FONT_BODY);

        JButton btnTampilkan = UITheme.createPrimaryButton("Tampilkan Laporan");
        btnTampilkan.addActionListener(e -> muatLaporan());

        JButton btnHariIni = UITheme.createSecondaryButton("Hari Ini");
        btnHariIni.addActionListener(e -> {
            tfTanggal.setText(LocalDate.now().format(DATE_FMT));
            muatLaporan();
        });

        filterCard.add(lblTgl);
        filterCard.add(tfTanggal);
        filterCard.add(btnTampilkan);
        filterCard.add(btnHariIni);
        content.add(filterCard, BorderLayout.NORTH);

        // Center: Kartu Ringkasan + Tabel Rincian
        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);

        JPanel summaryCards = new JPanel(new GridLayout(1, 3, 16, 16));
        summaryCards.setOpaque(false);

        JPanel c1 = UITheme.createCard();
        c1.setLayout(new BoxLayout(c1, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel("KENDARAAN MASUK");
        t1.setFont(UITheme.FONT_SMALL);
        t1.setForeground(UITheme.TEXT_MUTED);
        lblTotalMasuk = new JLabel("0");
        lblTotalMasuk.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotalMasuk.setForeground(UITheme.PRIMARY);
        c1.add(t1);
        c1.add(Box.createVerticalStrut(6));
        c1.add(lblTotalMasuk);

        JPanel c2 = UITheme.createCard();
        c2.setLayout(new BoxLayout(c2, BoxLayout.Y_AXIS));
        JLabel t2 = new JLabel("KENDARAAN KELUAR");
        t2.setFont(UITheme.FONT_SMALL);
        t2.setForeground(UITheme.TEXT_MUTED);
        lblTotalKeluar = new JLabel("0");
        lblTotalKeluar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotalKeluar.setForeground(UITheme.TEXT_MAIN);
        c2.add(t2);
        c2.add(Box.createVerticalStrut(6));
        c2.add(lblTotalKeluar);

        JPanel c3 = UITheme.createCard();
        c3.setLayout(new BoxLayout(c3, BoxLayout.Y_AXIS));
        JLabel t3 = new JLabel("TOTAL PENDAPATAN");
        t3.setFont(UITheme.FONT_SMALL);
        t3.setForeground(UITheme.TEXT_MUTED);
        lblTotalPendapatan = new JLabel(RUPIAH.format(0));
        lblTotalPendapatan.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotalPendapatan.setForeground(UITheme.SUCCESS);
        c3.add(t3);
        c3.add(Box.createVerticalStrut(6));
        c3.add(lblTotalPendapatan);

        summaryCards.add(c1);
        summaryCards.add(c2);
        summaryCards.add(c3);
        body.add(summaryCards, BorderLayout.NORTH);

        // Tabel Rincian per Jenis
        String[] cols = {"Jenis Kendaraan", "Jumlah Masuk", "Jumlah Keluar", "Pendapatan"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        UITheme.formatTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        body.add(scrollPane, BorderLayout.CENTER);

        content.add(body, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    public void refresh() {
        muatLaporan();
    }

    private void muatLaporan() {
        String inputTgl = tfTanggal.getText().trim();
        LocalDate tgl;
        try {
            tgl = LocalDate.parse(inputTgl, DATE_FMT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah. Gunakan YYYY-MM-DD", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LaporanService.LaporanHarian lap = laporanService.laporanHarian(tgl);

        lblTotalMasuk.setText(String.valueOf(lap.getTotalMasuk()));
        lblTotalKeluar.setText(String.valueOf(lap.getTotalKeluar()));
        lblTotalPendapatan.setText(RUPIAH.format(lap.getTotalPendapatan()));

        tableModel.setRowCount(0);
        for (JenisKendaraan j : JenisKendaraan.values()) {
            LaporanService.RincianJenis r = lap.getPerJenis().get(j);
            tableModel.addRow(new Object[]{
                    j.name(),
                    r.getJumlahMasuk(),
                    r.getJumlahKeluar(),
                    RUPIAH.format(r.getTotalPendapatan())
            });
        }
    }
}
