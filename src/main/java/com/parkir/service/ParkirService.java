package com.parkir.service;

import com.parkir.exception.KarcisTidakDitemukanException;
import com.parkir.exception.ParkirException;
import com.parkir.exception.ParkirPenuhException;
import com.parkir.exception.PlatSudahParkirException;
import com.parkir.exception.PlatTidakValidException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.Karcis;
import com.parkir.model.Kendaraan;
import com.parkir.model.KonfigurasiTarif;
import com.parkir.model.Mobil;
import com.parkir.model.Motor;
import com.parkir.model.SlotParkir;
import com.parkir.model.StatusKarcis;
import com.parkir.model.Truk;
import com.parkir.repository.KarcisRepository;
import com.parkir.repository.SlotRepository;
import com.parkir.repository.TarifRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class ParkirService {
    // Format plat Indonesia: 1-2 huruf, 1-4 angka, 0-3 huruf (contoh: AD 1234 AB, B 123 CD, D 1 A)
    private static final Pattern PLAT_PATTERN = Pattern.compile("^[A-Z]{1,2}\\s+[0-9]{1,4}(\\s+[A-Z]{1,3})?$");

    private final SlotRepository slotRepository;
    private final KarcisRepository karcisRepository;
    private final TarifRepository tarifRepository;

    public ParkirService(SlotRepository slotRepository,
                         KarcisRepository karcisRepository,
                         TarifRepository tarifRepository) {
        this.slotRepository = Objects.requireNonNull(slotRepository);
        this.karcisRepository = Objects.requireNonNull(karcisRepository);
        this.tarifRepository = Objects.requireNonNull(tarifRepository);
    }

    public static String normalisasiPlat(String plat) {
        if (plat == null) {
            throw new PlatTidakValidException("Plat nomor tidak boleh kosong");
        }
        String clean = plat.trim().toUpperCase().replaceAll("\\s+", " ");
        if (!PLAT_PATTERN.matcher(clean).matches()) {
            throw new PlatTidakValidException("Format plat nomor tidak valid (contoh format: AD 1234 AB)");
        }
        return clean;
    }

    public synchronized Karcis catatMasuk(String rawPlat, JenisKendaraan jenis, String petugasUsername) {
        return catatMasuk(rawPlat, jenis, petugasUsername, LocalDateTime.now());
    }

    public synchronized Karcis catatMasuk(String rawPlat, JenisKendaraan jenis, String petugasUsername, LocalDateTime waktuMasuk) {
        Objects.requireNonNull(jenis, "Jenis kendaraan tidak boleh null");
        Objects.requireNonNull(petugasUsername, "Petugas tidak boleh null");
        Objects.requireNonNull(waktuMasuk, "Waktu masuk tidak boleh null");

        String plat = normalisasiPlat(rawPlat);

        // Cek apakah plat sedang aktif parkir
        if (karcisRepository.findAktifByPlat(plat).isPresent()) {
            throw new PlatSudahParkirException("Kendaraan dengan plat " + plat + " sudah tercatat sedang parkir");
        }

        // Cari slot kosong untuk jenis tersebut dengan kode terkecil
        List<SlotParkir> slots = slotRepository.findAll();
        SlotParkir slotKosong = slots.stream()
                .filter(s -> s.getJenis() == jenis && !s.isTerisi())
                .min(Comparator.comparing(SlotParkir::getKode))
                .orElseThrow(() -> new ParkirPenuhException("Slot parkir untuk " + jenis + " sudah penuh"));

        // Buat kendaraan dan karcis
        Kendaraan kendaraan = switch (jenis) {
            case MOTOR -> new Motor(plat);
            case MOBIL -> new Mobil(plat);
            case TRUK -> new Truk(plat);
        };

        String idKarcis = karcisRepository.generateNextId(waktuMasuk.toLocalDate());
        Karcis karcis = Karcis.baru(idKarcis, kendaraan, slotKosong, waktuMasuk, petugasUsername);

        // Update slot & simpan
        slotKosong.setTerisi(true);
        slotRepository.save(slotKosong);
        karcisRepository.save(karcis);

        return karcis;
    }

    public synchronized Karcis catatKeluar(String platAtauIdKarcis) {
        return catatKeluar(platAtauIdKarcis, LocalDateTime.now());
    }

    public synchronized Karcis catatKeluar(String platAtauIdKarcis, LocalDateTime waktuKeluar) {
        if (platAtauIdKarcis == null || platAtauIdKarcis.trim().isEmpty()) {
            throw new KarcisTidakDitemukanException("Nomor karcis atau plat nomor tidak boleh kosong");
        }
        Objects.requireNonNull(waktuKeluar, "Waktu keluar tidak boleh null");

        String query = platAtauIdKarcis.trim();

        // Cari berdasarkan ID karcis dulu, jika tidak ketemu cari berdasarkan plat aktif
        Optional<Karcis> karcisOpt = karcisRepository.findById(query);
        if (karcisOpt.isEmpty() || karcisOpt.get().getStatus() != StatusKarcis.AKTIF) {
            karcisOpt = karcisRepository.findAktifByPlat(query);
        }

        if (karcisOpt.isEmpty() || karcisOpt.get().getStatus() != StatusKarcis.AKTIF) {
            throw new KarcisTidakDitemukanException("Tidak ditemukan karcis aktif untuk: " + query);
        }

        Karcis karcis = karcisOpt.get();

        if (waktuKeluar.isBefore(karcis.getWaktuMasuk())) {
            throw new ParkirException("Waktu keluar tidak boleh mendahului waktu masuk (" +
                    karcis.getWaktuMasuk() + ")");
        }

        Duration durasi = Duration.between(karcis.getWaktuMasuk(), waktuKeluar);
        KonfigurasiTarif konfigurasi = tarifRepository.load();
        long biaya = karcis.getKendaraan().hitungBiaya(durasi, konfigurasi);

        karcis.selesaikan(waktuKeluar, biaya);

        // Bebaskan slot
        SlotParkir slot = karcis.getSlot();
        Optional<SlotParkir> slotReal = slotRepository.findByKode(slot.getKode());
        if (slotReal.isPresent()) {
            slotReal.get().setTerisi(false);
            slotRepository.save(slotReal.get());
        } else {
            slot.setTerisi(false);
            slotRepository.save(slot);
        }

        karcisRepository.save(karcis);
        return karcis;
    }

    public synchronized List<Karcis> daftarParkirAktif() {
        return karcisRepository.findAktifAll();
    }

    public static class RingkasanSlotItem {
        private final JenisKendaraan jenis;
        private final int kapasitas;
        private final int terisi;

        public RingkasanSlotItem(JenisKendaraan jenis, int kapasitas, int terisi) {
            this.jenis = jenis;
            this.kapasitas = kapasitas;
            this.terisi = terisi;
        }

        public JenisKendaraan getJenis() {
            return jenis;
        }

        public int getKapasitas() {
            return kapasitas;
        }

        public int getTerisi() {
            return terisi;
        }

        public int getKosong() {
            return kapasitas - terisi;
        }
    }

    public synchronized Map<JenisKendaraan, RingkasanSlotItem> ringkasanSlot() {
        Map<JenisKendaraan, RingkasanSlotItem> result = new EnumMap<>(JenisKendaraan.class);
        List<SlotParkir> slots = slotRepository.findAll();

        for (JenisKendaraan j : JenisKendaraan.values()) {
            int kap = 0;
            int isi = 0;
            for (SlotParkir s : slots) {
                if (s.getJenis() == j) {
                    kap++;
                    if (s.isTerisi()) isi++;
                }
            }
            result.put(j, new RingkasanSlotItem(j, kap, isi));
        }
        return result;
    }
}
