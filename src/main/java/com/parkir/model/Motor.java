package com.parkir.model;

public class Motor extends Kendaraan {
    public Motor(String platNomor) {
        super(platNomor);
    }

    @Override
    public JenisKendaraan getJenis() {
        return JenisKendaraan.MOTOR;
    }
}
