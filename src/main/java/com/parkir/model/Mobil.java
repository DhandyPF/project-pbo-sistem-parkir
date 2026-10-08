package com.parkir.model;

public class Mobil extends Kendaraan {
    public Mobil(String platNomor) {
        super(platNomor);
    }

    @Override
    public JenisKendaraan getJenis() {
        return JenisKendaraan.MOBIL;
    }
}
