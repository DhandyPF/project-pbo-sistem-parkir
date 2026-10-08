package com.parkir.ui;

import com.parkir.exception.ParkirException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.SlotParkir;
import com.parkir.repository.SlotRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class KelolaSlotPanel extends JPanel {
    private final SlotRepository slotRepository;
    private final Runnable onDataChanged;

    private JTable table;
    private DefaultTableModel tableModel;

    private JComboBox<JenisKendaraan> cbJenis;
    private JButton btnTambah;
    private JButton btnKurangi;

    public KelolaSlotPanel(SlotRepository slotRepository, Runnable onDataChanged) {
        this.slotRepository = slotRepository;
        this.onDataChanged = onDataChanged;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel lblTitle = new JLabel("Kelola Kapasitas Slot Parkir (Admin)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        add(lblTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);

        // Kontrol atas
        JPanel controlCard = UITheme.createCard();
        controlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        JLabel lblPilih = new JLabel("Pilih Jenis Kendaraan:");
        lblPilih.setFont(UITheme.FONT_BOLD);
        cbJenis = new JComboBox<>(JenisKendaraan.values());
        cbJenis.setFont(UITheme.FONT_BODY);

        btnTambah = UITheme.createPrimaryButton("+ Tambah 1 Slot");
        btnTambah.addActionListener(e -> tambahSlot());

        btnKurangi = UITheme.createDangerButton("- Kurangi 1 Slot Kosong");
        btnKurangi.addActionListener(e -> kurangiSlot());

        controlCard.add(lblPilih);
        controlCard.add(cbJenis);
        controlCard.add(btnTambah);
        controlCard.add(btnKurangi);
        content.add(controlCard, BorderLayout.NORTH);

        // Tabel slot
        String[] cols = {"Kode Slot", "Jenis Kendaraan", "Status"};
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
        content.add(scrollPane, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<SlotParkir> slots = slotRepository.findAll();
        slots.sort(Comparator.comparing(SlotParkir::getKode));

        for (SlotParkir s : slots) {
            tableModel.addRow(new Object[]{
                    s.getKode(),
                    s.getJenis().name(),
                    s.isTerisi() ? "TERISI" : "KOSONG"
            });
        }
    }

    private void tambahSlot() {
        JenisKendaraan jenis = (JenisKendaraan) cbJenis.getSelectedItem();
        List<SlotParkir> slots = slotRepository.findAll();

        String prefix = switch (jenis) {
            case MOTOR -> "M-";
            case MOBIL -> "C-";
            case TRUK -> "T-";
        };

        int maxNum = 0;
        for (SlotParkir s : slots) {
            if (s.getKode().startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(s.getKode().substring(prefix.length()));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }

        String nextKode = String.format("%s%02d", prefix, maxNum + 1);
        SlotParkir baru = new SlotParkir(nextKode, jenis, false);
        slotRepository.save(baru);

        refresh();
        if (onDataChanged != null) onDataChanged.run();
        JOptionPane.showMessageDialog(this, "Slot " + nextKode + " berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void kurangiSlot() {
        JenisKendaraan jenis = (JenisKendaraan) cbJenis.getSelectedItem();
        List<SlotParkir> slots = slotRepository.findAll();

        String prefix = switch (jenis) {
            case MOTOR -> "M-";
            case MOBIL -> "C-";
            case TRUK -> "T-";
        };

        // Cari slot kosong dengan nomor urut paling besar untuk jenis tersebut
        SlotParkir target = slots.stream()
                .filter(s -> s.getKode().startsWith(prefix) && !s.isTerisi())
                .max(Comparator.comparing(SlotParkir::getKode))
                .orElse(null);

        if (target == null) {
            JOptionPane.showMessageDialog(this,
                    "Tidak ada slot kosong yang dapat dikurangi untuk jenis " + jenis + ".\nSlot yang sedang terisi tidak dapat dihapus.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        slotRepository.delete(target.getKode());
        refresh();
        if (onDataChanged != null) onDataChanged.run();
        JOptionPane.showMessageDialog(this, "Slot " + target.getKode() + " berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }
}
