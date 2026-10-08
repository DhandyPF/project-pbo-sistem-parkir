package com.parkir.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class KonfigurasiTarif {
    private final Map<JenisKendaraan, TarifStrategy> strategiMap = new EnumMap<>(JenisKendaraan.class);

    public KonfigurasiTarif() {
        // Default awal sesuai dokumen:
        // Motor: 2.000 / 1.000 / 10.000
        // Mobil: 5.000 / 3.000 / 30.000
        // Truk: 8.000 / 5.000 / 60.000
        strategiMap.put(JenisKendaraan.MOTOR, new TarifProgresif(2000, 1000, 10000));
        strategiMap.put(JenisKendaraan.MOBIL, new TarifProgresif(5000, 3000, 30000));
        strategiMap.put(JenisKendaraan.TRUK, new TarifProgresif(8000, 5000, 60000));
    }

    public void setStrategi(JenisKendaraan jenis, TarifStrategy strategy) {
        Objects.requireNonNull(jenis, "jenis tidak boleh null");
        Objects.requireNonNull(strategy, "strategy tidak boleh null");
        strategiMap.put(jenis, strategy);
    }

    public TarifStrategy getStrategi(JenisKendaraan jenis) {
        TarifStrategy strategy = strategiMap.get(jenis);
        if (strategy == null) {
            throw new IllegalStateException("Strategi tarif belum ditentukan untuk " + jenis);
        }
        return strategy;
    }

    public Map<JenisKendaraan, TarifStrategy> getAll() {
        return new EnumMap<>(strategiMap);
    }
}
