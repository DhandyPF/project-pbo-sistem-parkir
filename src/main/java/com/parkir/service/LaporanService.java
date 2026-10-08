package com.parkir.service;

import com.parkir.model.JenisKendaraan;
import com.parkir.model.Karcis;
import com.parkir.model.StatusKarcis;
import com.parkir.repository.KarcisRepository;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LaporanService {
    private final KarcisRepository karcisRepository;

    public LaporanService(KarcisRepository karcisRepository) {
        this.karcisRepository = Objects.requireNonNull(karcisRepository);
    }

    public static class RincianJenis {
        private int jumlahMasuk;
        private int jumlahKeluar;
        private long totalPendapatan;

        public int getJumlahMasuk() {
            return jumlahMasuk;
        }

        public int getJumlahKeluar() {
            return jumlahKeluar;
        }

        public long getTotalPendapatan() {
            return totalPendapatan;
        }
    }

    public static class LaporanHarian {
        private final LocalDate tanggal;
        private int totalMasuk;
        private int totalKeluar;
        private long totalPendapatan;
        private final Map<JenisKendaraan, RincianJenis> perJenis = new EnumMap<>(JenisKendaraan.class);

        public LaporanHarian(LocalDate tanggal) {
            this.tanggal = tanggal;
            for (JenisKendaraan j : JenisKendaraan.values()) {
                perJenis.put(j, new RincianJenis());
            }
        }

        public LocalDate getTanggal() {
            return tanggal;
        }

        public int getTotalMasuk() {
            return totalMasuk;
        }

        public int getTotalKeluar() {
            return totalKeluar;
        }

        public long getTotalPendapatan() {
            return totalPendapatan;
        }

        public Map<JenisKendaraan, RincianJenis> getPerJenis() {
            return perJenis;
        }
    }

    public LaporanHarian laporanHarian(LocalDate tanggal) {
        Objects.requireNonNull(tanggal, "Tanggal tidak boleh null");
        LaporanHarian lap = new LaporanHarian(tanggal);

        // Ambil semua karcis yang masuk atau selesai pada tanggal tersebut
        List<Karcis> semua = karcisRepository.findAll();
        for (Karcis k : semua) {
            JenisKendaraan jk = k.getKendaraan().getJenis();
            RincianJenis r = lap.getPerJenis().get(jk);

            if (k.getWaktuMasuk().toLocalDate().equals(tanggal)) {
                lap.totalMasuk++;
                r.jumlahMasuk++;
            }

            if (k.getStatus() == StatusKarcis.SELESAI && k.getWaktuKeluar() != null
                    && k.getWaktuKeluar().toLocalDate().equals(tanggal)) {
                lap.totalKeluar++;
                r.jumlahKeluar++;
                long b = k.getBiaya() != null ? k.getBiaya() : 0L;
                lap.totalPendapatan += b;
                r.totalPendapatan += b;
            }
        }

        return lap;
    }
}
