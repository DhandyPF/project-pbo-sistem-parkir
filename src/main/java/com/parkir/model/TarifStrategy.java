package com.parkir.model;

public interface TarifStrategy {
    long hitungBiaya(long menit);
    long getJamPertama();
    long getJamBerikutnya();
    long getMaksHarian();
}
