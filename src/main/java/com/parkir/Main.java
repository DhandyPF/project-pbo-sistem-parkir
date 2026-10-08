package com.parkir;

import com.parkir.repository.KarcisRepository;
import com.parkir.repository.SlotRepository;
import com.parkir.repository.TarifRepository;
import com.parkir.repository.UserRepository;
import com.parkir.repository.csv.CsvKarcisRepository;
import com.parkir.repository.csv.CsvSlotRepository;
import com.parkir.repository.csv.CsvTarifRepository;
import com.parkir.repository.csv.CsvUserRepository;
import com.parkir.service.AuthService;
import com.parkir.service.LaporanService;
import com.parkir.service.ParkirService;
import com.parkir.ui.MainFrame;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // Look & Feel sistem platform
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Inisialisasi direktori data
        Path dataDir = Paths.get("data");

        // Dependency Injection
        UserRepository userRepo = new CsvUserRepository(dataDir.resolve("users.csv"));
        SlotRepository slotRepo = new CsvSlotRepository(dataDir.resolve("slots.csv"));
        TarifRepository tarifRepo = new CsvTarifRepository(dataDir.resolve("tarif.csv"));
        KarcisRepository karcisRepo = new CsvKarcisRepository(dataDir.resolve("karcis.csv"));

        AuthService authService = new AuthService(userRepo);
        ParkirService parkirService = new ParkirService(slotRepo, karcisRepo, tarifRepo);
        LaporanService laporanService = new LaporanService(karcisRepo);

        // Jalankan GUI di EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(
                    authService,
                    parkirService,
                    laporanService,
                    userRepo,
                    slotRepo,
                    tarifRepo
            );
            frame.setVisible(true);
        });
    }
}
