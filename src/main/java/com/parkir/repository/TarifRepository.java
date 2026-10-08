package com.parkir.repository;

import com.parkir.model.KonfigurasiTarif;

public interface TarifRepository {
    KonfigurasiTarif load();
    void save(KonfigurasiTarif konfigurasi);
}
