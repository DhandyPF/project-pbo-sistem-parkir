package com.parkir.repository.csv;

import com.parkir.exception.DataRusakException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.SlotParkir;
import com.parkir.repository.SlotRepository;
import com.parkir.util.CsvUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CsvSlotRepository implements SlotRepository {
    private static final String HEADER = "kode,jenis,terisi";
    private final Path filePath;
    private final Map<String, SlotParkir> cache = new LinkedHashMap<>();

    public CsvSlotRepository(Path filePath) {
        this.filePath = filePath;
        initAndLoad();
    }

    private void initAndLoad() {
        if (!Files.exists(filePath)) {
            // Default sesuai dokumen: 20 Motor, 10 Mobil, 4 Truk
            List<SlotParkir> defaults = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                defaults.add(new SlotParkir(String.format("M-%02d", i), JenisKendaraan.MOTOR, false));
            }
            for (int i = 1; i <= 10; i++) {
                defaults.add(new SlotParkir(String.format("C-%02d", i), JenisKendaraan.MOBIL, false));
            }
            for (int i = 1; i <= 4; i++) {
                defaults.add(new SlotParkir(String.format("T-%02d", i), JenisKendaraan.TRUK, false));
            }
            for (SlotParkir s : defaults) {
                cache.put(s.getKode().toUpperCase(), s);
            }
            flush();
            return;
        }

        List<String[]> rows = CsvUtil.readRows(filePath, 3, true);
        cache.clear();
        for (String[] row : rows) {
            String kode = row[0];
            String jenisStr = row[1];
            boolean terisi = Boolean.parseBoolean(row[2]);

            JenisKendaraan jenis;
            try {
                jenis = JenisKendaraan.valueOf(jenisStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new DataRusakException("Jenis kendaraan '" + jenisStr + "' tidak valid di " + filePath.getFileName());
            }

            SlotParkir slot = new SlotParkir(kode, jenis, terisi);
            cache.put(kode.toUpperCase(), slot);
        }
    }

    private synchronized void flush() {
        List<String> lines = new ArrayList<>();
        for (SlotParkir s : cache.values()) {
            lines.add(String.join(",",
                    s.getKode(),
                    s.getJenis().name(),
                    String.valueOf(s.isTerisi())
            ));
        }
        CsvUtil.writeRowsAtomically(filePath, HEADER, lines);
    }

    @Override
    public synchronized List<SlotParkir> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized Optional<SlotParkir> findByKode(String kode) {
        if (kode == null) return Optional.empty();
        return Optional.ofNullable(cache.get(kode.trim().toUpperCase()));
    }

    @Override
    public synchronized void save(SlotParkir slot) {
        cache.put(slot.getKode().toUpperCase(), slot);
        flush();
    }

    @Override
    public synchronized void saveAll(List<SlotParkir> slots) {
        for (SlotParkir slot : slots) {
            cache.put(slot.getKode().toUpperCase(), slot);
        }
        flush();
    }

    @Override
    public synchronized void delete(String kode) {
        if (kode == null) return;
        cache.remove(kode.trim().toUpperCase());
        flush();
    }
}
