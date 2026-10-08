# Sistem Parkir — Dokumen Desain

**Tanggal:** 2026-10-09
**Konteks:** Tugas UAS Pemrograman Berorientasi Objek (kelompok 5 orang)
**Teknologi:** Java 21, Swing (GUI), JUnit 5 (pengujian logika)
**Status:** Draft untuk direview kelompok

---

## 1. Ringkasan

Sistem Parkir adalah aplikasi desktop untuk mengelola parkir kendaraan. Petugas mencatat kendaraan masuk dan keluar, sistem menghitung biaya berdasarkan jenis kendaraan dan lama parkir, lalu mencetak struk. Admin mengatur tarif, slot, dan akun petugas, serta melihat laporan.

Tujuan utama proyek adalah menunjukkan penerapan konsep OOP (encapsulation, inheritance, abstraction, polymorphism) pada aplikasi GUI yang utuh, sederhana, dan mudah didemokan.

## 2. Ruang Lingkup

**Termasuk (MVP):**
- Login dengan dua peran: Admin dan Petugas
- Pencatatan kendaraan masuk (Motor, Mobil, Truk) beserta penetapan slot otomatis
- Pencatatan kendaraan keluar, penghitungan biaya, dan struk
- Tarif progresif per jenis kendaraan yang dapat diubah Admin
- Daftar kendaraan yang sedang parkir
- Kelola akun petugas, kelola tarif, kelola slot (Admin)
- Laporan harian (Admin)
- Penyimpanan data ke file CSV

**Tidak termasuk:**
- Member, diskon, dan denda karcis hilang
- Pembayaran non-tunai atau integrasi perangkat keras (gerbang, printer, kamera)
- Denah parkir bergambar
- Multi-lokasi atau jaringan
- Pemulihan password

**Cadangan scope (dipangkas lebih dulu jika waktu mepet):**
1. Layar Kelola Slot (kapasitas ditetapkan tetap di konfigurasi awal)
2. Layar Kelola Petugas (akun petugas diisi langsung di `users.csv`)
3. Rincian laporan per jenis kendaraan (cukup total harian)

## 3. Peran dan Hak Akses

| Fitur | Petugas | Admin |
|---|---|---|
| Login | ✓ | ✓ |
| Dashboard ringkasan slot | ✓ | ✓ |
| Catat kendaraan masuk | ✓ | ✓ |
| Catat kendaraan keluar | ✓ | ✓ |
| Lihat daftar kendaraan parkir | ✓ | ✓ |
| Kelola tarif | – | ✓ |
| Kelola slot | – | ✓ |
| Kelola akun petugas | – | ✓ |
| Lihat laporan | – | ✓ |

Hak akses ditentukan oleh method `bolehMengakses(Fitur)` pada tiap subclass `User`, bukan oleh `if` peran yang tersebar di GUI.

## 4. Alur Penggunaan

1. Pengguna login. Menu yang tampil mengikuti peran.
2. **Masuk:** petugas mengisi plat nomor dan memilih jenis kendaraan. Sistem memvalidasi plat, memilih slot kosong pertama yang sesuai jenis, membuat karcis (waktu masuk = waktu sistem), lalu menandai slot terisi.
3. **Keluar:** petugas memasukkan plat atau nomor karcis. Sistem mengambil karcis aktif, mencatat waktu keluar, menghitung durasi dan biaya, menampilkan struk, menandai karcis selesai, dan membebaskan slot.
4. **Admin:** mengatur tarif, slot, dan petugas, serta membuka laporan per tanggal.

## 5. Arsitektur

```
GUI (Swing)  ──►  ParkirService / AuthService / LaporanService
                          │
          Domain: User, Kendaraan, TarifStrategy, Karcis, SlotParkir
                          │
        Repository (interface): UserRepository, KarcisRepository,
                                SlotRepository, TarifRepository
                          │
                 Csv...Repository  (SQLite opsional)
```

Aturan penting:
- **Domain dan service tidak boleh mengimpor kelas Swing atau AWT.** Dengan begitu logika dapat diuji dengan JUnit tanpa GUI.
- GUI hanya memanggil service dan menangkap exception, tidak memuat aturan bisnis.
- Penyimpanan diakses hanya lewat interface Repository, sehingga penggantian CSV ke database tidak mengubah kode lain.

## 6. Desain Class

### 6.1 Pengguna

**`User` (abstract)**
- Atribut private: `username`, `passwordHash`, `nama`
- Method publik: getter, `cocokPassword(String)`
- Method abstract: `getPeran()`, `bolehMengakses(Fitur)`

**`Admin`** dan **`Petugas`** extends `User`. `Fitur` adalah enum (`CATAT_MASUK`, `CATAT_KELUAR`, `LIHAT_PARKIR`, `KELOLA_TARIF`, `KELOLA_SLOT`, `KELOLA_PETUGAS`, `LIHAT_LAPORAN`).

**`AuthService`**: `login(username, password)` mengembalikan `User` atau melempar `LoginGagalException`.

### 6.2 Kendaraan dan Tarif

**`JenisKendaraan`** (enum): `MOTOR`, `MOBIL`, `TRUK`.

**`Kendaraan` (abstract)**
- Atribut private: `platNomor`
- Method abstract: `getJenis()`
- Method: `hitungBiaya(Duration, KonfigurasiTarif)` yang mengambil strategi tarif untuk jenisnya
- Subclass: `Motor`, `Mobil`, `Truk`

**`TarifStrategy` (interface)**: `hitungBiaya(long menit)`
- `TarifProgresif`: menyimpan `jamPertama`, `jamBerikutnya`, `maksHarian`

**`KonfigurasiTarif`**: memetakan `JenisKendaraan` ke `TarifStrategy`, dan dapat diubah oleh Admin.

### 6.3 Slot dan Karcis

**`SlotParkir`**: `kode` (contoh `M-01`, `C-01`, `T-01`), `jenis`, `terisi`.

**`Karcis`**: `id`, `kendaraan`, `slot`, `waktuMasuk`, `waktuKeluar`, `biaya`, `status` (`StatusKarcis`: `AKTIF` atau `SELESAI`), `petugas` (username pencatat).

### 6.4 Service

**`ParkirService`**
- `catatMasuk(plat, jenis, petugas)` mengembalikan `Karcis`
- `catatKeluar(platAtauIdKarcis)` mengembalikan `Karcis` yang sudah selesai
- `daftarParkirAktif()`, `ringkasanSlot()`

**`LaporanService`**: `laporanHarian(LocalDate)` mengembalikan jumlah masuk, jumlah keluar, total pendapatan, dan rincian per jenis.

### 6.5 Penyimpanan

Interface: `UserRepository`, `KarcisRepository`, `SlotRepository`, `TarifRepository`. Implementasi awal: `CsvUserRepository`, `CsvKarcisRepository`, `CsvSlotRepository`, `CsvTarifRepository`. Implementasi SQLite hanya ditambahkan jika dosen mewajibkan database.

### 6.6 Exception

`LoginGagalException`, `ParkirPenuhException`, `PlatSudahParkirException`, `PlatTidakValidException`, `KarcisTidakDitemukanException`, `DataRusakException`, `PenyimpananGagalException`. Semuanya turunan dari satu class dasar `ParkirException`.

## 7. Aturan Bisnis

**Tarif**
- Durasi dibulatkan ke atas per jam, minimal 1 jam.
- Biaya = `jamPertama` + (jumlah jam − 1) × `jamBerikutnya`, dibatasi `maksHarian`.
- Untuk durasi lebih dari 24 jam: `blok = jam / 24`, `sisa = jam % 24`, biaya = `blok × maksHarian` + (jika `sisa > 0`: `min(maksHarian, jamPertama + (sisa − 1) × jamBerikutnya)`).
- Validasi tarif oleh Admin: semua nilai ≥ 0 dan `maksHarian ≥ jamPertama`.

Tarif awal (dapat diubah Admin):

| Jenis | Jam pertama | Jam berikutnya | Maks/hari |
|---|---|---|---|
| Motor | Rp2.000 | Rp1.000 | Rp10.000 |
| Mobil | Rp5.000 | Rp3.000 | Rp30.000 |
| Truk | Rp8.000 | Rp5.000 | Rp60.000 |

Contoh:
- Mobil 3 jam 20 menit → 4 jam → 5.000 + 3 × 3.000 = **Rp14.000**
- Motor 12 jam → 2.000 + 11 × 1.000 = 13.000, dibatasi → **Rp10.000**
- Motor 25 jam → 1 blok 24 jam (10.000) + 1 jam (2.000) = **Rp12.000**
- Motor tepat 24 jam → 1 blok, sisa 0 → **Rp10.000**
- Durasi 0 menit → dibulatkan menjadi 1 jam → tarif jam pertama

**Slot**
- Kapasitas awal: 20 slot Motor, 10 slot Mobil, 4 slot Truk.
- Slot dipilih berdasarkan kode terkecil yang kosong untuk jenis kendaraan tersebut.
- Jika tidak ada slot kosong, sistem melempar `ParkirPenuhException`.

**Plat nomor**
- Dinormalisasi: huruf besar, spasi berlebih dihapus.
- Format yang diterima: 1-2 huruf, 1-4 angka, 0-3 huruf (contoh `AD 1234 AB`).
- Plat yang masih berstatus `AKTIF` tidak boleh dimasukkan lagi.

**Waktu**
- Waktu masuk dan keluar memakai jam sistem (`LocalDateTime`).
- Waktu keluar tidak boleh sebelum waktu masuk; jika terjadi, sistem menolak dengan pesan yang jelas.

**Akun**
- Password disimpan sebagai hash SHA-256 (cukup untuk lingkup tugas).
- Saat pertama dijalankan tanpa `users.csv`, sistem membuat satu akun Admin bawaan (`admin` / `admin123`) dan menampilkan anjuran untuk menggantinya.
- Admin tidak dapat menghapus akunnya sendiri.

**ID karcis**: format `PKR-yyyyMMdd-NNNN`, nomor urut per hari.

## 8. Model Data (CSV)

Semua file disimpan di folder `data/` dengan baris pertama berisi nama kolom.

| File | Kolom |
|---|---|
| `users.csv` | `username, passwordHash, nama, peran` |
| `slots.csv` | `kode, jenis, terisi` |
| `tarif.csv` | `jenis, jamPertama, jamBerikutnya, maksHarian` |
| `karcis.csv` | `id, plat, jenis, slot, waktuMasuk, waktuKeluar, biaya, status, petugas` |

Waktu ditulis dalam format ISO-8601 (`2026-10-09T08:30:00`). `waktuKeluar` dan `biaya` kosong untuk karcis berstatus `AKTIF`.

## 9. Pembagian Kerja

Hari pertama: kelompok menyepakati kontrak bersama berupa enum `JenisKendaraan`, `Fitur`, `StatusKarcis`, interface `TarifStrategy` dan keempat Repository, serta signature method publik `ParkirService`, `AuthService`, dan `LaporanService`. Setelah itu semua anggota bekerja paralel, dan modul yang belum selesai diganti class dummy.

| Anggota | Modul | Isi |
|---|---|---|
| 1 | Pengguna dan Autentikasi | `User`, `Admin`, `Petugas`, `Fitur`, `AuthService`, `UserRepository` dan `CsvUserRepository`, `LoginGagalException`; layar Login dan Kelola Petugas |
| 2 | Kendaraan dan Tarif | `JenisKendaraan`, `Kendaraan`, `Motor`, `Mobil`, `Truk`, `TarifStrategy`, `TarifProgresif`, `KonfigurasiTarif`, `TarifRepository` dan `CsvTarifRepository`; layar Kelola Tarif |
| 3 | Slot dan Transaksi | `SlotParkir`, `Karcis`, `StatusKarcis`, `ParkirService`, validasi plat, `SlotRepository` dan `CsvSlotRepository`, exception transaksi |
| 4 | Laporan dan Penyimpanan Karcis | `KarcisRepository` dan `CsvKarcisRepository`, `LaporanService`, generator ID karcis, utilitas baca/tulis CSV bersama, `DataRusakException`, `PenyimpananGagalException`; layar Laporan |
| 5 | GUI Utama | `MainFrame`, navigasi antar layar, Dashboard per peran, layar Kendaraan Masuk, Kendaraan Keluar beserta dialog struk, Daftar Parkir, Kelola Slot, tema tampilan, penanganan exception di GUI |

Catatan:
- Anggota 5 bekerja dengan service dummy di awal sehingga tidak menunggu modul lain.
- Anggota 4 memegang utilitas CSV bersama; anggota 2 dan 3 memakainya setelah tersedia, atau menulis versi sementara lebih dulu.
- Jika beban GUI anggota 5 terasa berat, layar Kelola Slot dapat dipindahkan ke anggota 3.

## 10. Layar GUI

1. **Login:** username, password, tombol Masuk, pesan error inline.
2. **Dashboard:** ringkasan slot kosong dan terisi per jenis, jumlah kendaraan parkir saat ini, menu sesuai peran.
3. **Kendaraan Masuk:** isian plat, pilihan jenis, tombol Catat; hasilnya menampilkan slot dan nomor karcis.
4. **Kendaraan Keluar:** pencarian berdasarkan plat atau nomor karcis; menampilkan waktu masuk, durasi, dan biaya sebelum dikonfirmasi; dialog struk setelah selesai.
5. **Daftar Parkir:** tabel kendaraan yang sedang parkir dengan kolom plat, jenis, slot, waktu masuk, dan lama parkir.
6. **Kelola Tarif (Admin):** tabel tarif per jenis, formulir ubah.
7. **Kelola Slot (Admin):** tambah atau kurangi kapasitas per jenis; slot yang sedang terisi tidak dapat dihapus.
8. **Kelola Petugas (Admin):** tambah, ubah, dan hapus akun petugas.
9. **Laporan (Admin):** pilih tanggal; menampilkan jumlah masuk, keluar, pendapatan total, dan rincian per jenis.

Keadaan yang harus ditangani di GUI: tabel kosong menampilkan teks "Belum ada data", tombol aksi nonaktif saat isian belum valid, dan menu yang tidak diizinkan tidak ditampilkan sama sekali.

## 11. Penanganan Error

- Semua kegagalan aturan bisnis dilempar sebagai turunan `ParkirException`. GUI menangkapnya dan menampilkan pesan berbahasa Indonesia lewat dialog, tanpa menghentikan aplikasi.
- Login gagal: pesan umum "Username atau password salah" tanpa menyebut mana yang salah.
- File CSV tidak ditemukan: dibuat otomatis dengan data awal.
- File CSV rusak (kolom kurang atau format salah): `DataRusakException` menyebut nama file dan nomor baris; fitur yang bergantung pada file itu ditolak sampai file diperbaiki, aplikasi tidak crash.
- Gagal menulis file: `PenyimpananGagalException`. Perubahan di memori (status karcis dan slot) dibatalkan agar data tidak berbeda dari file.

## 12. Pengujian

**Otomatis (JUnit, tanpa GUI):**
- Tarif: contoh angka pada bagian 7, pembulatan ke atas, batas tepat 24 jam, durasi 0 menit, maksimum harian, validasi tarif tidak valid
- Masuk: pemilihan slot kode terkecil, `ParkirPenuhException`, plat ganda aktif ditolak, plat tidak valid ditolak, normalisasi plat
- Keluar: slot dibebaskan, karcis menjadi `SELESAI`, `KarcisTidakDitemukanException`, waktu keluar sebelum waktu masuk ditolak
- Login: berhasil, password salah, username tidak ada
- Hak akses: `Petugas` ditolak untuk fitur Admin, `Admin` diizinkan semua
- Repository: simpan lalu muat menghasilkan data yang sama; file rusak melempar `DataRusakException`
- Laporan: jumlah dan total pendapatan sesuai data uji

**Manual (skenario demo):**
- Login Petugas, catat masuk motor, mobil, dan truk, lalu catat keluar dan periksa struk
- Isi semua slot satu jenis hingga muncul pesan penuh
- Login Admin, ubah tarif, ulangi transaksi dan periksa biaya baru
- Tutup aplikasi lalu buka lagi; data tetap ada
- Rusakkan satu file CSV dan pastikan pesan error muncul tanpa crash

## 13. Pemetaan Konsep OOP

| Konsep | Penerapan |
|---|---|
| Encapsulation | Atribut `User`, `Kendaraan`, `Karcis`, dan `SlotParkir` private; perubahan status hanya lewat method |
| Inheritance | `User` → `Admin`/`Petugas`; `Kendaraan` → `Motor`/`Mobil`/`Truk`; `ParkirException` → exception turunan |
| Abstraction | Class abstract `User` dan `Kendaraan`; interface `TarifStrategy` dan Repository |
| Polymorphism | `getPeran()` dan `bolehMengakses()` berbeda per peran; `getJenis()` per kendaraan; `TarifStrategy` dan Repository dapat diganti implementasinya |
| Composition | `Karcis` memiliki `Kendaraan` dan `SlotParkir`; `KonfigurasiTarif` memiliki `TarifStrategy` |
| Pola desain (nilai tambah) | Strategy (tarif), Repository (penyimpanan) |

## 14. Keputusan yang Masih Terbuka

| Pertanyaan | Pemilik | Dibutuhkan sebelum |
|---|---|---|
| Apakah dosen mewajibkan database (JDBC)? Jika ya, implementasi SQLite masuk MVP | Ketua kelompok (tanyakan ke dosen) | Anggota 3 dan 4 mulai menulis penyimpanan |
| Apakah ada batas minimal jumlah class, fitur, atau format laporan tertentu? | Ketua kelompok | Finalisasi scope |
| Build tool: Maven atau project IntelliJ biasa tanpa Maven? | Seluruh anggota | Hari pertama |
| Versi JDK yang dipakai semua anggota (disarankan 21) | Seluruh anggota | Hari pertama |
| Kapasitas slot dan nilai tarif awal sudah sesuai kebutuhan demo? | Anggota 2 dan 3 | Akhir minggu pertama |
