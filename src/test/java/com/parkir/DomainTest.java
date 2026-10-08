package com.parkir;

import com.parkir.model.Admin;
import com.parkir.model.Fitur;
import com.parkir.model.JenisKendaraan;
import com.parkir.model.KonfigurasiTarif;
import com.parkir.model.Mobil;
import com.parkir.model.Motor;
import com.parkir.model.Petugas;
import com.parkir.model.TarifProgresif;
import com.parkir.model.Truk;
import com.parkir.model.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class DomainTest {

    @Test
    void testUserDanHakAkses() {
        Admin admin = new Admin("admin", User.hashPassword("admin123"), "Admin Test");
        Petugas petugas = new Petugas("budi", User.hashPassword("budi123"), "Budi");

        assertTrue(admin.cocokPassword("admin123"));
        assertFalse(admin.cocokPassword("salah"));
        for (Fitur f : Fitur.values()) {
            assertTrue(admin.bolehMengakses(f));
        }

        assertTrue(petugas.bolehMengakses(Fitur.CATAT_MASUK));
        assertTrue(petugas.bolehMengakses(Fitur.CATAT_KELUAR));
        assertTrue(petugas.bolehMengakses(Fitur.LIHAT_PARKIR));
        assertFalse(petugas.bolehMengakses(Fitur.KELOLA_TARIF));
        assertFalse(petugas.bolehMengakses(Fitur.KELOLA_SLOT));
        assertFalse(petugas.bolehMengakses(Fitur.KELOLA_PETUGAS));
        assertFalse(petugas.bolehMengakses(Fitur.LIHAT_LAPORAN));
    }

    @Test
    void testHitungBiayaSesuaiSpesifikasi() {
        KonfigurasiTarif cfg = new KonfigurasiTarif();
        // Default:
        // Motor: 2000, 1000, maks 10000
        // Mobil: 5000, 3000, maks 30000
        // Truk: 8000, 5000, maks 60000
        Mobil mobil = new Mobil("B 1234 CD");
        Motor motor = new Motor("AD 1234 AB");

        // Mobil 3 jam 20 menit -> 4 jam -> 5.000 + 3 * 3.000 = 14.000
        assertEquals(14000, mobil.hitungBiaya(Duration.ofHours(3).plusMinutes(20), cfg));

        // Motor 12 jam -> 2.000 + 11 * 1.000 = 13.000, dibatasi -> 10.000
        assertEquals(10000, motor.hitungBiaya(Duration.ofHours(12), cfg));

        // Motor tepat 24 jam -> 1 blok, sisa 0 -> 10.000
        assertEquals(10000, motor.hitungBiaya(Duration.ofHours(24), cfg));

        // Motor 25 jam -> 1 blok 24 jam (10.000) + 1 jam (2.000) = 12.000
        assertEquals(12000, motor.hitungBiaya(Duration.ofHours(25), cfg));

        // Durasi 0 menit -> dibulatkan 1 jam -> tarif jam pertama (2.000)
        assertEquals(2000, motor.hitungBiaya(Duration.ofMinutes(0), cfg));

        // Durasi 1 menit -> dibulatkan 1 jam
        assertEquals(2000, motor.hitungBiaya(Duration.ofMinutes(1), cfg));

        // Durasi 60 menit -> 1 jam
        assertEquals(2000, motor.hitungBiaya(Duration.ofMinutes(60), cfg));

        // Durasi 61 menit -> 2 jam -> 2.000 + 1.000 = 3.000
        assertEquals(3000, motor.hitungBiaya(Duration.ofMinutes(61), cfg));
    }

    @Test
    void testValidasiTarifProgresif() {
        assertThrows(RuntimeException.class, () -> new TarifProgresif(-1000, 1000, 10000));
        assertThrows(RuntimeException.class, () -> new TarifProgresif(5000, 1000, 3000)); // maks < jamPertama
    }
}
