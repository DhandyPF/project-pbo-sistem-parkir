package com.parkir.model;

import java.time.Duration;
import java.util.Objects;

public abstract class Kendaraan {
    private final String platNomor;

    public Kendaraan(String platNomor) {
        this.platNomor = Objects.requireNonNull(platNomor, "platNomor tidak boleh null").trim();
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public abstract JenisKendaraan getJenis();

    public long hitungBiaya(Duration durasi, KonfigurasiTarif konfigurasiTarif) {
        Objects.requireNonNull(durasi, "durasi tidak boleh null");
        Objects.requireNonNull(konfigurasiTarif, "konfigurasiTarif tidak boleh null");
        TarifStrategy strategy = konfigurasiTarif.getStrategi(getJenis());
        return strategy.hitungBiaya(durasi.toMinutes());
    }

    @Override
    public String toString() {
        return getJenis() + " [" + platNomor + "]";
    }
}
