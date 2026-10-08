package com.parkir.ui;

import com.parkir.exception.ParkirException;
import com.parkir.model.Karcis;
import com.parkir.service.ParkirService;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class KendaraanKeluarPanel extends JPanel {
    private final ParkirService parkirService;
    private final Runnable onDataChanged;

    private JTextField tfQuery;
    private JButton btnProses;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public KendaraanKeluarPanel(ParkirService parkirService, Runnable onDataChanged) {
        this.parkirService = parkirService;
        this.onDataChanged = onDataChanged;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel lblTitle = new JLabel("Catat Kendaraan Keluar & Pembayaran");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);

        JPanel card = UITheme.createCard();
        card.setPreferredSize(new Dimension(500, 240));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblPrompt = new JLabel("Masukkan Nomor Karcis atau Plat Nomor:");
        lblPrompt.setFont(UITheme.FONT_BOLD);
        lblPrompt.setForeground(UITheme.TEXT_MAIN);
        lblPrompt.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfQuery = new JTextField();
        tfQuery.setFont(new Font("Consolas", Font.BOLD, 18));
        tfQuery.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        btnProses = UITheme.createPrimaryButton("Hitung Biaya & Selesaikan Parkir");
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnProses.addActionListener(e -> prosesKeluar());

        card.add(Box.createVerticalStrut(10));
        card.add(lblPrompt);
        card.add(Box.createVerticalStrut(10));
        card.add(tfQuery);
        card.add(Box.createVerticalStrut(20));
        card.add(btnProses);

        centerPanel.add(card);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void prosesKeluar() {
        String query = tfQuery.getText();
        if (query == null || query.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor karcis atau plat nomor harus diisi", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Karcis selesai = parkirService.catatKeluar(query);
            tfQuery.setText("");
            if (onDataChanged != null) onDataChanged.run();

            tampilkanStrukDialog(selesai);
        } catch (ParkirException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Keluar", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tampilkanStrukDialog(Karcis k) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Struk Parkir", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(380, 480);
        dialog.setLocationRelativeTo(this);

        Duration durasi = Duration.between(k.getWaktuMasuk(), k.getWaktuKeluar());
        long totalMenit = durasi.toMinutes();
        long jam = totalMenit / 60;
        long menit = totalMenit % 60;

        JTextArea ta = new JTextArea();
        ta.setFont(UITheme.FONT_MONO);
        ta.setEditable(false);
        ta.setBackground(new Color(254, 252, 232)); // Struk paper tone
        ta.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        StringBuilder sb = new StringBuilder();
        sb.append("====================================\n");
        sb.append("           STRUK PARKIR             \n");
        sb.append("====================================\n\n");
        sb.append(String.format("ID Karcis   : %s\n", k.getId()));
        sb.append(String.format("Plat Nomor  : %s\n", k.getKendaraan().getPlatNomor()));
        sb.append(String.format("Jenis       : %s\n", k.getKendaraan().getJenis()));
        sb.append(String.format("Slot Parkir : %s\n", k.getSlot().getKode()));
        sb.append(String.format("Petugas     : %s\n", k.getPetugas()));
        sb.append("------------------------------------\n");
        sb.append(String.format("Masuk       : %s\n", k.getWaktuMasuk().format(TIME_FMT)));
        sb.append(String.format("Keluar      : %s\n", k.getWaktuKeluar().format(TIME_FMT)));
        sb.append(String.format("Durasi      : %d jam %d menit\n", jam, menit));
        sb.append("------------------------------------\n");
        sb.append(String.format("TOTAL BIAYA : %s\n", RUPIAH.format(k.getBiaya())));
        sb.append("====================================\n");
        sb.append("   Terima kasih atas kunjungan Anda \n");
        sb.append("====================================\n");

        ta.setText(sb.toString());

        JButton btnTutup = UITheme.createPrimaryButton("Tutup");
        btnTutup.addActionListener(e -> dialog.dispose());

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.add(btnTutup);

        dialog.add(new JScrollPane(ta), BorderLayout.CENTER);
        dialog.add(bot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
