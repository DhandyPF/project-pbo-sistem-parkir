package com.parkir.repository;

import com.parkir.model.Karcis;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KarcisRepository {
    List<Karcis> findAll();
    Optional<Karcis> findById(String id);
    Optional<Karcis> findAktifByPlat(String platNomor);
    List<Karcis> findAktifAll();
    List<Karcis> findByTanggalMasuk(LocalDate tanggal);
    void save(Karcis karcis);
    String generateNextId(LocalDate tanggal);
}
