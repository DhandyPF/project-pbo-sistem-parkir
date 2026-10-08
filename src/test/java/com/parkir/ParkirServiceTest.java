package com.parkir;

import com.parkir.exception.ParkirPenuhException;
import com.parkir.exception.PlatSudahParkirException;
import com.parkir.exception.PlatTidakValidException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.Karcis;
import com.parkir.model.KonfigurasiTarif;
import com.parkir.model.SlotParkir;
import com.parkir.model.StatusKarcis;
import com.parkir.repository.KarcisRepository;
import com.parkir.repository.SlotRepository;
import com.parkir.repository.TarifRepository;
import com.parkir.repository.csv.CsvKarcisRepository;
import com.parkir.repository.csv.CsvSlotRepository;
import com.parkir.repository.csv.CsvTarifRepository;
import com.parkir.service.LaporanService;
import com.parkir.service.ParkirService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParkirServiceTest {

    @TempDir
    Path tempDir;

    private SlotRepository slotRepo;
    private KarcisRepository karcisRepo;
    private TarifRepository tarifRepo;
    private ParkirService parkirService;
    private LaporanService laporanService;

    @BeforeEach
    void setUp() {
        slotRepo = new CsvSlotRepository(tempDir.resolve("slots.csv"));
        karcisRepo = new CsvKarcisRepository(tempDir.resolve("karcis.csv"));
        tarifRepo = new CsvTarifRepository(tempDir.resolve("tarif.csv"));
        parkirService = new ParkirService(slotRepo, karcisRepo, tarifRepo);
        laporanService = new LaporanService(karcisRepo);
    }

    @Test
    void testCatatMasukDanKeluarBerhasil() {
        LocalDateTime masuk = LocalDateTime.of(2026, 10, 9, 8, 0);
        Karcis k = parkirService.catatMasuk("B 1234 CD", JenisKendaraan.MOBIL, "petugas1", masuk);

        assertNotNull(k);
        assertEquals("B 1234 CD", k.getKendaraan().getPlatNomor());
        assertEquals(StatusKarcis.AKTIF, k.getStatus());
        assertEquals("C-01", k.getSlot().getKode());
        assertTrue(k.getSlot().isTerisi());

        // Cek slot di repo terisi
        SlotParkir slotDiRepo = slotRepo.findByKode("C-01").orElseThrow();
        assertTrue(slotDiRepo.isTerisi());

        // Keluar setelah 3 jam 20 menit (11:20) -> durasi dibulatkan 4 jam -> biaya 14.000
        LocalDateTime keluar = masuk.plusHours(3).plusMinutes(20);
        Karcis kKeluar = parkirService.catatKeluar(k.getId(), keluar);

        assertEquals(StatusKarcis.SELESAI, kKeluar.getStatus());
        assertEquals(14000L, kKeluar.getBiaya());
        assertEquals(keluar, kKeluar.getWaktuKeluar());

        // Slot harus bebas kembali
        SlotParkir slotBebas = slotRepo.findByKode("C-01").orElseThrow();
        assertFalse(slotBebas.isTerisi());
    }

    @Test
    void testNormalisasiPlatDanValidasi() {
        assertThrows(PlatTidakValidException.class, () -> parkirService.normalisasiPlat(""));
        assertThrows(PlatTidakValidException.class, () -> parkirService.normalisasiPlat("123456"));
        assertEquals("AD 1234 AB", parkirService.normalisasiPlat("  ad   1234   ab  "));
    }

    @Test
    void testPlatSudahParkirDitolak() {
        parkirService.catatMasuk("B 1111 AA", JenisKendaraan.MOBIL, "petugas1");
        assertThrows(PlatSudahParkirException.class, () ->
                parkirService.catatMasuk("b 1111 aa", JenisKendaraan.MOBIL, "petugas1"));
    }

    @Test
    void testParkirPenuhException() {
        // Truk default 4 slot (T-01 s/d T-04)
        for (int i = 1; i <= 4; i++) {
            parkirService.catatMasuk("B " + (9000 + i) + " TR", JenisKendaraan.TRUK, "petugas1");
        }
        assertThrows(ParkirPenuhException.class, () ->
                parkirService.catatMasuk("B 9999 TR", JenisKendaraan.TRUK, "petugas1"));
    }

    @Test
    void testLaporanHarian() {
        LocalDate today = LocalDate.of(2026, 10, 9);
        LocalDateTime masuk = LocalDateTime.of(today, java.time.LocalTime.of(8, 0));
        Karcis k1 = parkirService.catatMasuk("B 1001 CD", JenisKendaraan.MOBIL, "petugas1", masuk);
        parkirService.catatKeluar(k1.getId(), masuk.plusHours(2)); // Mobil 2 jam: 5000 + 3000 = 8000

        LaporanService.LaporanHarian lap = laporanService.laporanHarian(today);
        assertEquals(1, lap.getTotalMasuk());
        assertEquals(1, lap.getTotalKeluar());
        assertEquals(8000L, lap.getTotalPendapatan());
        assertEquals(8000L, lap.getPerJenis().get(JenisKendaraan.MOBIL).getTotalPendapatan());
    }
}
