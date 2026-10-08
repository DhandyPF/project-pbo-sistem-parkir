package com.parkir.ui;

import com.parkir.exception.ParkirException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.KonfigurasiTarif;
import com.parkir.model.TarifProgresif;
import com.parkir.model.TarifStrategy;
import com.parkir.repository.TarifRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class KelolaTarifPanel extends JPanel {
    private final TarifRepository tarifRepository;
    private final Runnable onDataChanged;

    private JTable table;
    private DefaultTableModel tableModel;

    private JComboBox<JenisKendaraan> cbJenis;
    private JTextField tfJamPertama;
    private JTextField tfJamBerikutnya;
    private JTextField tfMaksHarian;
    private JButton btnSimpan;

    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public KelolaTarifPanel(TarifRepository tarifRepository, Runnable onDataChanged) {
        this.tarifRepository = tarifRepository;
        this.onDataChanged = onDataChanged;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel lblTitle = new JLabel("Kelola Tarif Parkir (Admin)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        add(lblTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 20));
        content.setOpaque(false);

        // Tabel Kiri
        String[] cols = {"Jenis Kendaraan", "Jam Pertama", "Jam Berikutnya", "Maksimal/Hari"};
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
        content.add(scrollPane);

        // Form Ubah Kanan
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblFormTitle = new JLabel("Perbarui Tarif");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        lblFormTitle.setForeground(UITheme.PRIMARY);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblJenis = new JLabel("Pilih Jenis Kendaraan");
        lblJenis.setFont(UITheme.FONT_BOLD);
        lblJenis.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbJenis = new JComboBox<>(JenisKendaraan.values());
        cbJenis.setFont(UITheme.FONT_BODY);
        cbJenis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblP1 = new JLabel("Tarif Jam Pertama (Rp)");
        lblP1.setFont(UITheme.FONT_BOLD);
        lblP1.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfJamPertama = new JTextField();
        tfJamPertama.setFont(UITheme.FONT_BODY);
        tfJamPertama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblP2 = new JLabel("Tarif Jam Berikutnya (Rp)");
        lblP2.setFont(UITheme.FONT_BOLD);
        lblP2.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfJamBerikutnya = new JTextField();
        tfJamBerikutnya.setFont(UITheme.FONT_BODY);
        tfJamBerikutnya.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblMax = new JLabel("Maksimal Harian (Rp)");
        lblMax.setFont(UITheme.FONT_BOLD);
        lblMax.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfMaksHarian = new JTextField();
        tfMaksHarian.setFont(UITheme.FONT_BODY);
        tfMaksHarian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnSimpan = UITheme.createPrimaryButton("Simpan Perubahan");
        btnSimpan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSimpan.addActionListener(e -> simpanTarif());

        // Sinkronkan input form saat ganti combo box
        cbJenis.addActionListener(e -> isiFormDariPilihan());

        formCard.add(lblFormTitle);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(lblJenis);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(cbJenis);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblP1);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(tfJamPertama);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblP2);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(tfJamBerikutnya);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblMax);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(tfMaksHarian);
        formCard.add(Box.createVerticalStrut(20));
        formCard.add(btnSimpan);
        formCard.add(Box.createVerticalGlue());

        content.add(formCard);
        add(content, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        KonfigurasiTarif cfg = tarifRepository.load();

        for (Map.Entry<JenisKendaraan, TarifStrategy> entry : cfg.getAll().entrySet()) {
            TarifStrategy s = entry.getValue();
            tableModel.addRow(new Object[]{
                    entry.getKey().name(),
                    RUPIAH.format(s.getJamPertama()),
                    RUPIAH.format(s.getJamBerikutnya()),
                    RUPIAH.format(s.getMaksHarian())
            });
        }
        isiFormDariPilihan();
    }

    private void isiFormDariPilihan() {
        JenisKendaraan selected = (JenisKendaraan) cbJenis.getSelectedItem();
        if (selected == null) return;
        KonfigurasiTarif cfg = tarifRepository.load();
        TarifStrategy s = cfg.getStrategi(selected);
        tfJamPertama.setText(String.valueOf(s.getJamPertama()));
        tfJamBerikutnya.setText(String.valueOf(s.getJamBerikutnya()));
        tfMaksHarian.setText(String.valueOf(s.getMaksHarian()));
    }

    private void simpanTarif() {
        JenisKendaraan selected = (JenisKendaraan) cbJenis.getSelectedItem();
        try {
            long p1 = Long.parseLong(tfJamPertama.getText().trim());
            long p2 = Long.parseLong(tfJamBerikutnya.getText().trim());
            long max = Long.parseLong(tfMaksHarian.getText().trim());

            KonfigurasiTarif cfg = tarifRepository.load();
            cfg.setStrategi(selected, new TarifProgresif(p1, p2, max));
            tarifRepository.save(cfg);

            refresh();
            if (onDataChanged != null) onDataChanged.run();

            JOptionPane.showMessageDialog(this, "Tarif " + selected + " berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nilai tarif harus berupa angka bulat", "Peringatan", JOptionPane.WARNING_MESSAGE);
        } catch (ParkirException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Simpan", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
