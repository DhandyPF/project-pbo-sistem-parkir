package com.parkir.model;

public class Admin extends User {
    public Admin(String username, String passwordHash, String nama) {
        super(username, passwordHash, nama);
    }

    @Override
    public Peran getPeran() {
        return Peran.ADMIN;
    }

    @Override
    public boolean bolehMengakses(Fitur fitur) {
        // Admin boleh mengakses semua fitur
        return fitur != null;
    }
}
