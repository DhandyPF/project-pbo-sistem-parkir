package com.parkir.model;

import java.util.Objects;

public class SlotParkir {
    private final String kode;
    private final JenisKendaraan jenis;
    private boolean terisi;

    public SlotParkir(String kode, JenisKendaraan jenis, boolean terisi) {
        this.kode = Objects.requireNonNull(kode, "kode tidak boleh null").trim();
        this.jenis = Objects.requireNonNull(jenis, "jenis tidak boleh null");
        this.terisi = terisi;
    }

    public String getKode() {
        return kode;
    }

    public JenisKendaraan getJenis() {
        return jenis;
    }

    public boolean isTerisi() {
        return terisi;
    }

    public void setTerisi(boolean terisi) {
        this.terisi = terisi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlotParkir slot)) return false;
        return kode.equalsIgnoreCase(slot.kode);
    }

    @Override
    public int hashCode() {
        return kode.toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return kode + " (" + jenis + ", " + (terisi ? "Terisi" : "Kosong") + ")";
    }
}
