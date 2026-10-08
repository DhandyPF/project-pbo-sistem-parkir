package com.parkir.ui;

import com.parkir.exception.ParkirException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.Karcis;
import com.parkir.model.User;
import com.parkir.service.ParkirService;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class KendaraanMasukPanel extends JPanel {
    private final ParkirService parkirService;
    private final Runnable onDataChanged;
    private User currentUser;

    private JTextField tfPlat;
    private JComboBox<JenisKendaraan> cbJenis;
    private JButton btnSimpan;

    private JPanel hasilPanel;
    private JLabel lblHasilId;
    private JLabel lblHasilSlot;
    private JLabel lblHasilWaktu;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public KendaraanMasukPanel(ParkirService parkirService, Runnable onDataChanged) {
        this.parkirService = parkirService;
        this.onDataChanged = onDataChanged;
        initComponents();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel lblTitle = new JLabel("Catat Kendaraan Masuk");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        add(lblTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 20));
        content.setOpaque(false);

        // Card Form Kiri
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblPlat = new JLabel("Plat Nomor (contoh: AD 1234 AB)");
        lblPlat.setFont(UITheme.FONT_BOLD);
        lblPlat.setForeground(UITheme.TEXT_MAIN);
        lblPlat.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfPlat = new JTextField();
        tfPlat.setFont(new Font("Consolas", Font.BOLD, 16));
        tfPlat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblJenis = new JLabel("Jenis Kendaraan");
        lblJenis.setFont(UITheme.FONT_BOLD);
        lblJenis.setForeground(UITheme.TEXT_MAIN);
        lblJenis.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbJenis = new JComboBox<>(JenisKendaraan.values());
        cbJenis.setFont(UITheme.FONT_BODY);
        cbJenis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnSimpan = UITheme.createPrimaryButton("Cetak Karcis & Masuk");
        btnSimpan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSimpan.addActionListener(e -> prosesMasuk());

        formCard.add(lblPlat);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(tfPlat);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(lblJenis);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(cbJenis);
        formCard.add(Box.createVerticalStrut(24));
        formCard.add(btnSimpan);
        formCard.add(Box.createVerticalGlue());

        // Card Hasil Kanan
        hasilPanel = UITheme.createCard();
        hasilPanel.setLayout(new BoxLayout(hasilPanel, BoxLayout.Y_AXIS));

        JLabel lblHasilTitle = new JLabel("Karcis Parkir Terakhir");
        lblHasilTitle.setFont(UITheme.FONT_HEADER);
        lblHasilTitle.setForeground(UITheme.PRIMARY);
        lblHasilTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblHasilId = new JLabel("ID Karcis: -");
        lblHasilId.setFont(UITheme.FONT_BOLD);
        lblHasilId.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblHasilSlot = new JLabel("Slot Ditetapkan: -");
        lblHasilSlot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHasilSlot.setForeground(UITheme.SUCCESS);
        lblHasilSlot.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblHasilWaktu = new JLabel("Waktu Masuk: -");
        lblHasilWaktu.setFont(UITheme.FONT_BODY);
        lblHasilWaktu.setAlignmentX(Component.LEFT_ALIGNMENT);

        hasilPanel.add(lblHasilTitle);
        hasilPanel.add(Box.createVerticalStrut(16));
        hasilPanel.add(lblHasilId);
        hasilPanel.add(Box.createVerticalStrut(10));
        hasilPanel.add(lblHasilSlot);
        hasilPanel.add(Box.createVerticalStrut(10));
        hasilPanel.add(lblHasilWaktu);
        hasilPanel.add(Box.createVerticalGlue());

        content.add(formCard);
        content.add(hasilPanel);
        add(content, BorderLayout.CENTER);
    }

    private void prosesMasuk() {
        String plat = tfPlat.getText();
        JenisKendaraan jenis = (JenisKendaraan) cbJenis.getSelectedItem();
        String petugas = (currentUser != null) ? currentUser.getUsername() : "petugas";

        try {
            Karcis karcis = parkirService.catatMasuk(plat, jenis, petugas);
            lblHasilId.setText("ID Karcis: " + karcis.getId());
            lblHasilSlot.setText("Slot Ditetapkan: " + karcis.getSlot().getKode());
            lblHasilWaktu.setText("Waktu Masuk: " + karcis.getWaktuMasuk().format(TIME_FMT));

            tfPlat.setText("");
            if (onDataChanged != null) onDataChanged.run();

            JOptionPane.showMessageDialog(this,
                    "Kendaraan berhasil dicatat masuk!\nSlot: " + karcis.getSlot().getKode() + "\nKarcis: " + karcis.getId(),
                    "Berhasil Masuk",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (ParkirException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Masuk", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
