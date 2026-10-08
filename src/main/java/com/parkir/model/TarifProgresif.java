package com.parkir.model;

import com.parkir.exception.ParkirException;

public class TarifProgresif implements TarifStrategy {
    private final long jamPertama;
    private final long jamBerikutnya;
    private final long maksHarian;

    public TarifProgresif(long jamPertama, long jamBerikutnya, long maksHarian) {
        if (jamPertama < 0 || jamBerikutnya < 0 || maksHarian < 0) {
            throw new ParkirException("Komponen tarif tidak boleh bernilai negatif");
        }
        if (maksHarian < jamPertama) {
            throw new ParkirException("Tarif maksimum harian (" + maksHarian + ") harus >= jam pertama (" + jamPertama + ")");
        }
        this.jamPertama = jamPertama;
        this.jamBerikutnya = jamBerikutnya;
        this.maksHarian = maksHarian;
    }

    @Override
    public long getJamPertama() {
        return jamPertama;
    }

    @Override
    public long getJamBerikutnya() {
        return jamBerikutnya;
    }

    @Override
    public long getMaksHarian() {
        return maksHarian;
    }

    @Override
    public long hitungBiaya(long menit) {
        if (menit < 0) {
            throw new ParkirException("Durasi menit tidak boleh negatif");
        }
        // Aturan bisnis: pembulatan ke atas per jam, minimal 1 jam (0 menit dibulatkan 1 jam)
        long jam;
        if (menit == 0) {
            jam = 1;
        } else {
            jam = (menit + 59) / 60;
        }

        // Untuk durasi > 24 jam: blok = jam / 24, sisa = jam % 24
        // biaya = blok * maksHarian + (sisa > 0 ? min(maksHarian, jamPertama + (sisa - 1) * jamBerikutnya) : 0)
        long blok = jam / 24;
        long sisa = jam % 24;

        long biayaBlok = blok * maksHarian;
        long biayaSisa = 0;
        if (sisa > 0) {
            long biayaParsial = jamPertama + (sisa - 1) * jamBerikutnya;
            biayaSisa = Math.min(maksHarian, biayaParsial);
        }

        return biayaBlok + biayaSisa;
    }
}
