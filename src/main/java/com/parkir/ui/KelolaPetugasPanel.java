package com.parkir.ui;

import com.parkir.exception.ParkirException;
import com.parkir.model.Admin;
import com.parkir.model.Peran;
import com.parkir.model.Petugas;
import com.parkir.model.User;
import com.parkir.repository.UserRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class KelolaPetugasPanel extends JPanel {
    private final UserRepository userRepository;
    private User currentUser;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField tfUsername;
    private JPasswordField tfPassword;
    private JTextField tfNama;
    private JComboBox<Peran> cbPeran;
    private JButton btnTambah;
    private JButton btnHapus;

    public KelolaPetugasPanel(UserRepository userRepository) {
        this.userRepository = userRepository;
        initComponents();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JLabel lblTitle = new JLabel("Kelola Akun Pengguna & Petugas (Admin)");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);
        add(lblTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 20));
        content.setOpaque(false);

        // Tabel Kiri
        String[] cols = {"Username", "Nama", "Peran"};
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

        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setOpaque(false);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        btnHapus = UITheme.createDangerButton("Hapus Akun Terpilih");
        btnHapus.addActionListener(e -> hapusAkun());
        leftPanel.add(btnHapus, BorderLayout.SOUTH);

        content.add(leftPanel);

        // Form Tambah Kanan
        JPanel formCard = UITheme.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblFormTitle = new JLabel("Tambah / Ubah Akun");
        lblFormTitle.setFont(UITheme.FONT_HEADER);
        lblFormTitle.setForeground(UITheme.PRIMARY);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblU = new JLabel("Username");
        lblU.setFont(UITheme.FONT_BOLD);
        lblU.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfUsername = new JTextField();
        tfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblP = new JLabel("Password");
        lblP.setFont(UITheme.FONT_BOLD);
        lblP.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfPassword = new JPasswordField();
        tfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblN = new JLabel("Nama Lengkap");
        lblN.setFont(UITheme.FONT_BOLD);
        lblN.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfNama = new JTextField();
        tfNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblR = new JLabel("Peran");
        lblR.setFont(UITheme.FONT_BOLD);
        lblR.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbPeran = new JComboBox<>(Peran.values());
        cbPeran.setSelectedItem(Peran.PETUGAS);
        cbPeran.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnTambah = UITheme.createPrimaryButton("Simpan Akun");
        btnTambah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnTambah.addActionListener(e -> simpanAkun());

        formCard.add(lblFormTitle);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(lblU);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(tfUsername);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblP);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(tfPassword);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblN);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(tfNama);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(lblR);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(cbPeran);
        formCard.add(Box.createVerticalStrut(20));
        formCard.add(btnTambah);
        formCard.add(Box.createVerticalGlue());

        content.add(formCard);
        add(content, BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<User> list = userRepository.findAll();
        for (User u : list) {
            tableModel.addRow(new Object[]{u.getUsername(), u.getNama(), u.getPeran().name()});
        }
    }

    private void simpanAkun() {
        String u = tfUsername.getText().trim();
        String p = new String(tfPassword.getPassword()).trim();
        String n = tfNama.getText().trim();
        Peran peran = (Peran) cbPeran.getSelectedItem();

        if (u.isEmpty() || p.isEmpty() || n.isEmpty() || peran == null) {
            JOptionPane.showMessageDialog(this, "Semua kolom akun harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hash = User.hashPassword(p);
        User user = (peran == Peran.ADMIN)
                ? new Admin(u, hash, n)
                : new Petugas(u, hash, n);

        userRepository.save(user);
        tfUsername.setText("");
        tfPassword.setText("");
        tfNama.setText("");
        refresh();

        JOptionPane.showMessageDialog(this, "Akun " + u + " berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void hapusAkun() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih akun yang ingin dihapus pada tabel", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String targetUsername = (String) tableModel.getValueAt(selectedRow, 0);

        // Aturan bisnis: Admin tidak boleh menghapus akunnya sendiri
        if (currentUser != null && currentUser.getUsername().equalsIgnoreCase(targetUsername)) {
            JOptionPane.showMessageDialog(this, "Admin tidak dapat menghapus akunnya sendiri yang sedang aktif!", "Ditolak", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus akun '" + targetUsername + "'?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            userRepository.delete(targetUsername);
            refresh();
            JOptionPane.showMessageDialog(this, "Akun " + targetUsername + " berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
