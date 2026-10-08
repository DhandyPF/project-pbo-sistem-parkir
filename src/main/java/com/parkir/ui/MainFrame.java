package com.parkir.ui;

import com.parkir.model.Fitur;
import com.parkir.model.User;
import com.parkir.repository.SlotRepository;
import com.parkir.repository.TarifRepository;
import com.parkir.repository.UserRepository;
import com.parkir.service.AuthService;
import com.parkir.service.LaporanService;
import com.parkir.service.ParkirService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final AuthService authService;
    private final ParkirService parkirService;
    private final LaporanService laporanService;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final TarifRepository tarifRepository;

    private User currentUser;

    private JPanel rootCardPanel;
    private CardLayout rootCardLayout;

    private LoginPanel loginPanel;
    private JPanel appContainerPanel;
    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout contentCardLayout;

    // View panels
    private DashboardPanel dashboardPanel;
    private KendaraanMasukPanel masukPanel;
    private KendaraanKeluarPanel keluarPanel;
    private DaftarParkirPanel daftarParkirPanel;
    private KelolaTarifPanel kelolaTarifPanel;
    private KelolaSlotPanel kelolaSlotPanel;
    private KelolaPetugasPanel kelolaPetugasPanel;
    private LaporanPanel laporanPanel;

    private JLabel lblUserBadge;
    private final Map<Fitur, JButton> menuButtons = new HashMap<>();

    public MainFrame(AuthService authService,
                     ParkirService parkirService,
                     LaporanService laporanService,
                     UserRepository userRepository,
                     SlotRepository slotRepository,
                     TarifRepository tarifRepository) {
        this.authService = authService;
        this.parkirService = parkirService;
        this.laporanService = laporanService;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.tarifRepository = tarifRepository;

        initFrame();
        initViews();
    }

    private void initFrame() {
        setTitle("Sistem Parkir Modern");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
    }

    private void initViews() {
        rootCardLayout = new CardLayout();
        rootCardPanel = new JPanel(rootCardLayout);

        // 1. Login View
        loginPanel = new LoginPanel(authService, this::onLoginSuccess);
        rootCardPanel.add(loginPanel, "LOGIN");

        // 2. App Main View (Sidebar + Content)
        appContainerPanel = new JPanel(new BorderLayout());

        // Sidebar
        sidebarPanel = createSidebar();
        appContainerPanel.add(sidebarPanel, BorderLayout.WEST);

        // Content Area
        contentCardLayout = new CardLayout();
        mainContentPanel = new JPanel(contentCardLayout);

        Runnable refreshAll = this::refreshActiveViews;

        dashboardPanel = new DashboardPanel(parkirService);
        masukPanel = new KendaraanMasukPanel(parkirService, refreshAll);
        keluarPanel = new KendaraanKeluarPanel(parkirService, refreshAll);
        daftarParkirPanel = new DaftarParkirPanel(parkirService);
        kelolaTarifPanel = new KelolaTarifPanel(tarifRepository, refreshAll);
        kelolaSlotPanel = new KelolaSlotPanel(slotRepository, refreshAll);
        kelolaPetugasPanel = new KelolaPetugasPanel(userRepository);
        laporanPanel = new LaporanPanel(laporanService);

        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(masukPanel, Fitur.CATAT_MASUK.name());
        mainContentPanel.add(keluarPanel, Fitur.CATAT_KELUAR.name());
        mainContentPanel.add(daftarParkirPanel, Fitur.LIHAT_PARKIR.name());
        mainContentPanel.add(kelolaTarifPanel, Fitur.KELOLA_TARIF.name());
        mainContentPanel.add(kelolaSlotPanel, Fitur.KELOLA_SLOT.name());
        mainContentPanel.add(kelolaPetugasPanel, Fitur.KELOLA_PETUGAS.name());
        mainContentPanel.add(laporanPanel, Fitur.LIHAT_LAPORAN.name());

        appContainerPanel.add(mainContentPanel, BorderLayout.CENTER);
        rootCardPanel.add(appContainerPanel, "APP");

        setContentPane(rootCardPanel);
        rootCardLayout.show(rootCardPanel, "LOGIN");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, Integer.MAX_VALUE));
        sidebar.setBackground(UITheme.BG_DARK);
        sidebar.setLayout(new BorderLayout());

        // Header Sidebar: Logo & App Title
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        JLabel lblLogo = new JLabel("PARKIR SYSTEM");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLogo.setForeground(Color.WHITE);

        lblUserBadge = new JLabel("Petugas: -");
        lblUserBadge.setFont(UITheme.FONT_SMALL);
        lblUserBadge.setForeground(UITheme.TEXT_MUTED);

        top.add(lblLogo);
        top.add(Box.createVerticalStrut(4));
        top.add(lblUserBadge);

        sidebar.add(top, BorderLayout.NORTH);

        // Menu Navigasi
        JPanel menuList = new JPanel();
        menuList.setOpaque(false);
        menuList.setLayout(new BoxLayout(menuList, BoxLayout.Y_AXIS));
        menuList.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Tombol Dashboard selalu ada untuk semua peran
        JButton btnDash = createMenuButton("Dashboard", e -> switchContent("DASHBOARD"));
        menuList.add(btnDash);
        menuList.add(Box.createVerticalStrut(4));

        // Tombol per fitur (dikontrol oleh hak akses)
        addMenuFeature(menuList, Fitur.CATAT_MASUK, "Kendaraan Masuk");
        addMenuFeature(menuList, Fitur.CATAT_KELUAR, "Kendaraan Keluar");
        addMenuFeature(menuList, Fitur.LIHAT_PARKIR, "Daftar Parkir");
        addMenuFeature(menuList, Fitur.KELOLA_TARIF, "Kelola Tarif");
        addMenuFeature(menuList, Fitur.KELOLA_SLOT, "Kelola Slot");
        addMenuFeature(menuList, Fitur.KELOLA_PETUGAS, "Kelola Petugas");
        addMenuFeature(menuList, Fitur.LIHAT_LAPORAN, "Laporan Harian");

        sidebar.add(menuList, BorderLayout.CENTER);

        // Bottom: Tombol Keluar / Logout
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setBorder(BorderFactory.createEmptyBorder(12, 10, 16, 10));

        JButton btnLogout = UITheme.createDangerButton("Keluar (Logout)");
        btnLogout.addActionListener(e -> logout());
        bot.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(bot, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addMenuFeature(JPanel container, Fitur fitur, String label) {
        JButton btn = createMenuButton(label, e -> switchContent(fitur.name()));
        menuButtons.put(fitur, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    private JButton createMenuButton(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UITheme.BG_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    private void switchContent(String cardName) {
        contentCardLayout.show(mainContentPanel, cardName);
        refreshActiveViews();
    }

    private void onLoginSuccess(User user) {
        this.currentUser = user;
        lblUserBadge.setText(user.getNama() + " (" + user.getPeran() + ")");

        // Context pengguna ke panel yang membutuhkan
        masukPanel.setCurrentUser(user);
        kelolaPetugasPanel.setCurrentUser(user);

        // Aturan penting sesuai dokumen: Hak akses ditentukan oleh bolehMengakses(Fitur)
        // bukan if peran yang tersebar di GUI
        for (Map.Entry<Fitur, JButton> entry : menuButtons.entrySet()) {
            boolean boleh = user.bolehMengakses(entry.getKey());
            entry.getValue().setVisible(boleh);
        }

        refreshActiveViews();
        switchContent("DASHBOARD");
        rootCardLayout.show(rootCardPanel, "APP");
    }

    private void logout() {
        this.currentUser = null;
        loginPanel.resetForm();
        rootCardLayout.show(rootCardPanel, "LOGIN");
    }

    private void refreshActiveViews() {
        dashboardPanel.refresh();
        daftarParkirPanel.refresh();
        kelolaTarifPanel.refresh();
        kelolaSlotPanel.refresh();
        kelolaPetugasPanel.refresh();
        laporanPanel.refresh();
    }
}
