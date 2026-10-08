package com.parkir.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Karcis {
    private final String id;
    private final Kendaraan kendaraan;
    private final SlotParkir slot;
    private final LocalDateTime waktuMasuk;
    private LocalDateTime waktuKeluar;
    private Long biaya;
    private StatusKarcis status;
    private final String petugas; // username pencatat

    public Karcis(String id, Kendaraan kendaraan, SlotParkir slot,
                  LocalDateTime waktuMasuk, LocalDateTime waktuKeluar,
                  Long biaya, StatusKarcis status, String petugas) {
        this.id = Objects.requireNonNull(id, "id tidak boleh null").trim();
        this.kendaraan = Objects.requireNonNull(kendaraan, "kendaraan tidak boleh null");
        this.slot = Objects.requireNonNull(slot, "slot tidak boleh null");
        this.waktuMasuk = Objects.requireNonNull(waktuMasuk, "waktuMasuk tidak boleh null");
        this.waktuKeluar = waktuKeluar;
        this.biaya = biaya;
        this.status = Objects.requireNonNull(status, "status tidak boleh null");
        this.petugas = Objects.requireNonNull(petugas, "petugas tidak boleh null").trim();
    }

    public static Karcis baru(String id, Kendaraan kendaraan, SlotParkir slot,
                              LocalDateTime waktuMasuk, String petugas) {
        return new Karcis(id, kendaraan, slot, waktuMasuk, null, null, StatusKarcis.AKTIF, petugas);
    }

    public String getId() {
        return id;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public SlotParkir getSlot() {
        return slot;
    }

    public LocalDateTime getWaktuMasuk() {
        return waktuMasuk;
    }

    public LocalDateTime getWaktuKeluar() {
        return waktuKeluar;
    }

    public Long getBiaya() {
        return biaya;
    }

    public StatusKarcis getStatus() {
        return status;
    }

    public String getPetugas() {
        return petugas;
    }

    public void selesaikan(LocalDateTime waktuKeluar, long biaya) {
        this.waktuKeluar = Objects.requireNonNull(waktuKeluar, "waktuKeluar tidak boleh null");
        this.biaya = biaya;
        this.status = StatusKarcis.SELESAI;
    }
}
