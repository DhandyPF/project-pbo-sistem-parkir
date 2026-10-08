package com.parkir.repository;

import com.parkir.model.SlotParkir;
import java.util.List;
import java.util.Optional;

public interface SlotRepository {
    List<SlotParkir> findAll();
    Optional<SlotParkir> findByKode(String kode);
    void save(SlotParkir slot);
    void saveAll(List<SlotParkir> slots);
    void delete(String kode);
}
