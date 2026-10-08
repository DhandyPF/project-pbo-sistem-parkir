package com.parkir.model;

public class Truk extends Kendaraan {
    public Truk(String platNomor) {
        super(platNomor);
    }

    @Override
    public JenisKendaraan getJenis() {
        return JenisKendaraan.TRUK;
    }
}
