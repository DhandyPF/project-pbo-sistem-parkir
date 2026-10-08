package com.parkir.repository.csv;

import com.parkir.exception.DataRusakException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.Karcis;
import com.parkir.model.Kendaraan;
import com.parkir.model.Mobil;
import com.parkir.model.Motor;
import com.parkir.model.SlotParkir;
import com.parkir.model.StatusKarcis;
import com.parkir.model.Truk;
import com.parkir.repository.KarcisRepository;
import com.parkir.util.CsvUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CsvKarcisRepository implements KarcisRepository {
    private static final String HEADER = "id,plat,jenis,slot,waktuMasuk,waktuKeluar,biaya,status,petugas";
    private static final DateTimeFormatter ID_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final Path filePath;
    private final Map<String, Karcis> cache = new LinkedHashMap<>();

    public CsvKarcisRepository(Path filePath) {
        this.filePath = filePath;
        initAndLoad();
    }

    private void initAndLoad() {
        if (!Files.exists(filePath)) {
            flush();
            return;
        }

        List<String[]> rows = CsvUtil.readRows(filePath, 9, true);
        cache.clear();
        for (String[] row : rows) {
            String id = row[0];
            String plat = row[1];
            String jenisStr = row[2];
            String slotKode = row[3];
            String masukStr = row[4];
            String keluarStr = row[5];
            String biayaStr = row[6];
            String statusStr = row[7];
            String petugas = row[8];

            try {
                JenisKendaraan jenis = JenisKendaraan.valueOf(jenisStr.toUpperCase());
                Kendaraan k = switch (jenis) {
                    case MOTOR -> new Motor(plat);
                    case MOBIL -> new Mobil(plat);
                    case TRUK -> new Truk(plat);
                };

                StatusKarcis status = StatusKarcis.valueOf(statusStr.toUpperCase());
                SlotParkir slot = new SlotParkir(slotKode, jenis, status == StatusKarcis.AKTIF);
                LocalDateTime masuk = LocalDateTime.parse(masukStr);
                LocalDateTime keluar = keluarStr.isEmpty() ? null : LocalDateTime.parse(keluarStr);
                Long biaya = biayaStr.isEmpty() ? null : Long.parseLong(biayaStr);

                Karcis karcis = new Karcis(id, k, slot, masuk, keluar, biaya, status, petugas);
                cache.put(id, karcis);
            } catch (Exception e) {
                throw new DataRusakException("Baris karcis rusak di " + filePath.getFileName() + ": " + e.getMessage(), e);
            }
        }
    }

    private synchronized void flush() {
        List<String> lines = new ArrayList<>();
        for (Karcis k : cache.values()) {
            lines.add(String.join(",",
                    k.getId(),
                    k.getKendaraan().getPlatNomor(),
                    k.getKendaraan().getJenis().name(),
                    k.getSlot().getKode(),
                    k.getWaktuMasuk().toString(),
                    k.getWaktuKeluar() != null ? k.getWaktuKeluar().toString() : "",
                    k.getBiaya() != null ? k.getBiaya().toString() : "",
                    k.getStatus().name(),
                    k.getPetugas()
            ));
        }
        CsvUtil.writeRowsAtomically(filePath, HEADER, lines);
    }

    @Override
    public synchronized List<Karcis> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized Optional<Karcis> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(cache.get(id.trim()));
    }

    @Override
    public synchronized Optional<Karcis> findAktifByPlat(String platNomor) {
        if (platNomor == null) return Optional.empty();
        String normalized = platNomor.replaceAll("\\s+", "").toUpperCase();
        for (Karcis k : cache.values()) {
            if (k.getStatus() == StatusKarcis.AKTIF) {
                String kPlat = k.getKendaraan().getPlatNomor().replaceAll("\\s+", "").toUpperCase();
                if (kPlat.equals(normalized)) {
                    return Optional.of(k);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized List<Karcis> findAktifAll() {
        List<Karcis> result = new ArrayList<>();
        for (Karcis k : cache.values()) {
            if (k.getStatus() == StatusKarcis.AKTIF) {
                result.add(k);
            }
        }
        return result;
    }

    @Override
    public synchronized List<Karcis> findByTanggalMasuk(LocalDate tanggal) {
        List<Karcis> result = new ArrayList<>();
        for (Karcis k : cache.values()) {
            if (k.getWaktuMasuk().toLocalDate().equals(tanggal)) {
                result.add(k);
            }
        }
        return result;
    }

    @Override
    public synchronized void save(Karcis karcis) {
        cache.put(karcis.getId(), karcis);
        flush();
    }

    @Override
    public synchronized String generateNextId(LocalDate tanggal) {
        // Format: PKR-yyyyMMdd-NNNN
        String prefix = "PKR-" + tanggal.format(ID_DATE_FMT) + "-";
        int maxSeq = 0;
        for (String id : cache.keySet()) {
            if (id.startsWith(prefix)) {
                String seqStr = id.substring(prefix.length());
                try {
                    int seq = Integer.parseInt(seqStr);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%04d", prefix, maxSeq + 1);
    }
}
