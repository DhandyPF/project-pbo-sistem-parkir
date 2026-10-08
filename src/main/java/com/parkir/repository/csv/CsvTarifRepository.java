package com.parkir.repository.csv;

import com.parkir.exception.DataRusakException;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.KonfigurasiTarif;
import com.parkir.model.TarifProgresif;
import com.parkir.model.TarifStrategy;
import com.parkir.repository.TarifRepository;
import com.parkir.util.CsvUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvTarifRepository implements TarifRepository {
    private static final String HEADER = "jenis,jamPertama,jamBerikutnya,maksHarian";
    private final Path filePath;

    public CsvTarifRepository(Path filePath) {
        this.filePath = filePath;
        if (!Files.exists(filePath)) {
            // Tulis default bawaan
            save(new KonfigurasiTarif());
        }
    }

    @Override
    public synchronized KonfigurasiTarif load() {
        if (!Files.exists(filePath)) {
            KonfigurasiTarif defaultCfg = new KonfigurasiTarif();
            save(defaultCfg);
            return defaultCfg;
        }

        List<String[]> rows = CsvUtil.readRows(filePath, 4, true);
        KonfigurasiTarif cfg = new KonfigurasiTarif();
        for (String[] row : rows) {
            String jenisStr = row[0];
            try {
                JenisKendaraan jenis = JenisKendaraan.valueOf(jenisStr.toUpperCase());
                long jamPertama = Long.parseLong(row[1]);
                long jamBerikutnya = Long.parseLong(row[2]);
                long maksHarian = Long.parseLong(row[3]);
                cfg.setStrategi(jenis, new TarifProgresif(jamPertama, jamBerikutnya, maksHarian));
            } catch (Exception e) {
                throw new DataRusakException("Format tarif tidak valid di " + filePath.getFileName() + ": " + e.getMessage(), e);
            }
        }
        return cfg;
    }

    @Override
    public synchronized void save(KonfigurasiTarif konfigurasi) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<JenisKendaraan, TarifStrategy> entry : konfigurasi.getAll().entrySet()) {
            TarifStrategy s = entry.getValue();
            lines.add(String.join(",",
                    entry.getKey().name(),
                    String.valueOf(s.getJamPertama()),
                    String.valueOf(s.getJamBerikutnya()),
                    String.valueOf(s.getMaksHarian())
            ));
        }
        CsvUtil.writeRowsAtomically(filePath, HEADER, lines);
    }
}
