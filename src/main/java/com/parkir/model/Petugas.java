package com.parkir.model;

public class Petugas extends User {
    public Petugas(String username, String passwordHash, String nama) {
        super(username, passwordHash, nama);
    }

    @Override
    public Peran getPeran() {
        return Peran.PETUGAS;
    }

    @Override
    public boolean bolehMengakses(Fitur fitur) {
        if (fitur == null) return false;
        return switch (fitur) {
            case CATAT_MASUK, CATAT_KELUAR, LIHAT_PARKIR -> true;
            case KELOLA_TARIF, KELOLA_SLOT, KELOLA_PETUGAS, LIHAT_LAPORAN -> false;
        };
    }
}
