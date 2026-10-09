# Dokumentasi Arsitektur & Penjelasan Kode Sistem Parkir

Dokumen ini berisi dokumentasi teknis sistem parkir: struktur direktori, alur aplikasi, serta rincian tiap class (import, fungsi, dan bedah kodenya).

---

## 1. Struktur Direktori Proyek

```
sistem-parkir/
├── pom.xml                                      # Konfigurasi Maven (Java 21, Swing, JUnit 5)
├── mvnw / mvnw.cmd                              # Maven Wrapper (eksekusi tanpa install maven manual)
├── README.md                                    # Petunjuk menjalankan program
├── data/                                        # Database berbasis flat-file CSV
│   ├── users.csv                                # Akun pengguna (Admin / Petugas)
│   ├── slots.csv                                # Data slot parkir (M-xx, C-xx, T-xx)
│   ├── tarif.csv                                # Konfigurasi tarif progresif
│   └── karcis.csv                               # Rekam jejak transaksi karcis parkir
└── src/
    ├── main/java/com/parkir/
    │   ├── Main.java                            # Entry point aplikasi & DI assembler
    │   ├── exception/                           # Custom runtime exceptions
    │   │   ├── ParkirException.java             # Base exception untuk domain parkir
    │   │   ├── DataRusakException.java          # Error integritas data berkas CSV
    │   │   ├── KarcisTidakDitemukanException.java # Pencarian tiket/plat tidak ada
    │   │   ├── LoginGagalException.java         # Autentikasi user gagal
    │   │   ├── ParkirPenuhException.java        # Kuota slot habis
    │   │   ├── PenyimpananGagalException.java   # Gagal I/O penyimpanan atomic
    │   │   ├── PlatSudahParkirException.java    # Plat ganda masih aktif parkir
    │   │   └── PlatTidakValidException.java     # Format plat tidak sesuai regex
    │   ├── model/                               # Entitas OOP domain bisnis
    │   │   ├── Fitur.java                       # Enum permission hak akses sistem
    │   │   ├── Peran.java                       # Enum peran (ADMIN, PETUGAS)
    │   │   ├── JenisKendaraan.java              # Enum varian kendaraan
    │   │   ├── StatusKarcis.java                # Enum siklus hidup karcis (AKTIF, SELESAI)
    │   │   ├── User.java                        # Abstract class dasar pengguna & hashing
    │   │   ├── Admin.java                       # Subclass User dengan hak akses penuh
    │   │   ├── Petugas.java                     # Subclass User dengan hak operasional
    │   │   ├── Kendaraan.java                   # Abstract class kendaraan bermotor
    │   │   ├── Motor.java                       # Subclass Kendaraan (Jenis: MOTOR)
    │   │   ├── Mobil.java                       # Subclass Kendaraan (Jenis: MOBIL)
    │   │   ├── Truk.java                        # Subclass Kendaraan (Jenis: TRUK)
    │   │   ├── SlotParkir.java                  # Model slot area parkir fisik
    │   │   ├── Karcis.java                      # Model transaksi tiket/struk parkir
    │   │   ├── TarifStrategy.java               # Interface Strategy penghitungan biaya
    │   │   ├── TarifProgresif.java              # Implementasi Strategy tarif progresif harian
    │   │   └── KonfigurasiTarif.java            # Registry pemetaan tarif per jenis kendaraan
    │   ├── repository/                          # Data Access Object (DAO) Contracts
    │   │   ├── UserRepository.java              # Kontrak operasi penyimpanan User
    │   │   ├── SlotRepository.java              # Kontrak operasi penyimpanan SlotParkir
    │   │   ├── TarifRepository.java             # Kontrak operasi konfigurasi Tarif
    │   │   ├── KarcisRepository.java            # Kontrak operasi data Karcis
    │   │   └── csv/                             # Implementasi DAO berbasis berkas CSV
    │   │       ├── CsvUserRepository.java       # Implementasi CRUD User ke users.csv
    │   │       ├── CsvSlotRepository.java       # Implementasi CRUD Slot ke slots.csv
    │   │       ├── CsvTarifRepository.java       # Implementasi CRUD Tarif ke tarif.csv
    │   │       └── CsvKarcisRepository.java      # Implementasi CRUD Karcis ke karcis.csv
    │   ├── service/                             # Lapisan Logika Bisnis (Business Logic Layer)
    │   │   ├── AuthService.java                 # Validasi dan manajemen autentikasi sesi
    │   │   ├── ParkirService.java               # Orkestrasi alur masuk, keluar, dan slot
    │   │   └── LaporanService.java              # Kalkulasi agregasi metrik omzet & rekap
    │   ├── ui/                                  # Tampilan Antarmuka Grafis (Java Swing Desktop)
    │   │   ├── UITheme.java                     # Desain sistem palet warna, tipografi, card
    │   │   ├── MainFrame.java                   # Jendela utama & navigasi CardLayout
    │   │   ├── LoginPanel.java                  # Antarmuka form masuk akun
    │   │   ├── DashboardPanel.java              # Monitor visual sisa kapasitas slot
    │   │   ├── KendaraanMasukPanel.java         # Antarmuka input check-in plat kendaraan
    │   │   ├── KendaraanKeluarPanel.java        # Antarmuka check-out & cetak struk dialog
    │   │   ├── DaftarParkirPanel.java           # Tabel monitoring kendaraan aktif parkir
    │   │   ├── KelolaTarifPanel.java            # Form Admin pengubahan rumus tarif
    │   │   ├── KelolaSlotPanel.java             # Form Admin penambahan/pengurangan slot
    │   │   ├── KelolaPetugasPanel.java          # Form Admin manajemen CRUD akun
    │   │   └── LaporanPanel.java                # Form Admin visualisasi rekap keuangan harian
    │   └── util/                                # Helper Utilities
    │       └── CsvUtil.java                     # Engine atomic write & parser baris CSV aman
    └── test/java/com/parkir/                    # Unit testing (JUnit 5)
        ├── DomainTest.java                      # Pengujian logika tarif, model, & user
        └── ParkirServiceTest.java               # Pengujian fungsional service parkir
```

---

## 2. Penjelasan Tiap Direktori

1. **`com.parkir` (Root Package):**
   - Berisi kelas `Main.java`. Berfungsi merakit seluruh objek (*Dependency Injection* manual) dan menyalakan GUI di *Event Dispatch Thread* (EDT).

2. **`com.parkir.model`:**
   - Memodelkan objek inti domain (*Domain Entities*). Murni prinsip OOP (enkapsulasi, polimorfisme, inheritance). Bebas dari library GUI (Swing/AWT) sehingga mudah diuji secara independen.

3. **`com.parkir.exception`:**
   - Menyimpan seluruh kelas *Checked/Unchecked Exception* khusus domain sistem parkir untuk mempermudah deteksi validasi bisnis (seperti plat ganda, kapasitas habis, dll).

4. **`com.parkir.repository` & `com.parkir.repository.csv`:**
   - Menerapkan *Repository Pattern*. Direktori `repository` mendefinisikan *interface*, sedangkan `csv` mengimplementasikan manipulasi data nyata menggunakan file `.csv`.

5. **`com.parkir.service`:**
   - Memusatkan seluruh logika bisnis (validasi durasi, perhitungan biaya parkir, pengecekan ketersediaan slot). Tidak memiliki ketergantungan pada komponen Swing.

6. **`com.parkir.util`:**
   - Berisi fungsi pembantu statis. Mengisolasi I/O file CSV agar penulisan bersifat atomic dan pembacaan tahan galat.

7. **`com.parkir.ui`:**
   - Menangani presentasi visual antarmuka pengguna berbasis Java Swing. Bertugas menerima input pengguna, meneruskannya ke Service/Repository, dan menampilkan umpan balik.

---

## 3. Alur Aplikasi (Application Flow)

### 3.1 Alur Inisialisasi Aplikasi (Bootstrap)
1. `Main.main()` dijalankan.
2. Mengeset Look & Feel sistem operasi via `UIManager.setLookAndFeel`.
3. Inisialisasi direktori folder `data/` dan instansiasi concrete repository (`CsvUserRepository`, `CsvSlotRepository`, `CsvTarifRepository`, `CsvKarcisRepository`).
4. File CSV dicek: jika belum ada, repository otomatis membuat file default awal (admin default, 34 slot, tarif standar).
5. Inisialisasi Service (`AuthService`, `ParkirService`, `LaporanService`) dengan menyuntikkan repository terkait (*Dependency Injection*).
6. Membuka `MainFrame` di atas thread `SwingUtilities.invokeLater()`. Layar awal menampilkan `LoginPanel`.

### 3.2 Alur Autentikasi (Login & Hak Akses)
1. Pengguna memasukkan `username` dan `password` di `LoginPanel`.
2. `AuthService.login()` mencari data akun via `UserRepository` dan mencocokkan hash SHA-256 password.
3. Jika gagal, muncul pesan kesalahan di panel. Jika sukses, instance `User` (`Admin` atau `Petugas`) dikirim ke `MainFrame.onLoginSuccess()`.
4. `MainFrame` memeriksa izin fitur via metode polimorfik `user.bolehMengakses(Fitur)`.
   - Tombol sidebar yang tidak diizinkan otomatis disembunyikan.
   - Layar dialihkan dari `"LOGIN"` ke `"APP"` dengan kartu awal `"DASHBOARD"`.

### 3.3 Alur Kendaraan Masuk (Check-In)
1. Petugas membuka menu **Kendaraan Masuk**, mengisi plat nomor dan memilih jenis kendaraan (Motor / Mobil / Truk).
2. Sistem menjalankan `ParkirService.catatMasuk()`:
   - Validasi format plat nomor menggunakan Regular Expression (contoh: `AD 1234 AB`).
   - Cek apakah kendaraan dengan plat tersebut sudah parkir dan masih berstatus `AKTIF` (`PlatSudahParkirException`).
   - Mencari slot kosong terkecil yang sesuai jenis kendaraan (`ParkirPenuhException` jika kuota habis).
   - Membuat ID Karcis harian berformat `PKR-yyyyMMdd-NNNN`.
   - Membuat objek `Karcis` baru, mengubah status slot menjadi terisi (`terisi = true`), dan menyimpannya secara atomik ke CSV.
3. Panel menampilkan nomor karcis, slot yang ditetapkan, dan jam masuk.

### 3.4 Alur Kendaraan Keluar & Pembayaran (Check-Out)
1. Petugas membuka menu **Kendaraan Keluar**, memasukkan ID karcis atau plat nomor.
2. `ParkirService.catatKeluar()` berjalan:
   - Mencari karcis berstatus `AKTIF`.
   - Mengambil waktu keluar (`LocalDateTime.now()`) dan memastikan tidak mendahului waktu masuk.
   - Menghitung durasi parkir (pembulatan ke atas per jam).
   - Memanggil `kendaraan.hitungBiaya(durasi, konfigurasiTarif)` via Strategy Pattern (`TarifProgresif`).
   - Menyimpan status karcis menjadi `SELESAI`, mengisi kolom `waktuKeluar` dan `biaya`.
   - Mengubah status `SlotParkir` kembali menjadi `false` (kosong).
3. GUI menampilkan dialog pop-up berisi struk rincian durasi dan nominal pembayaran tunai.

### 3.5 Alur Administrasi (Admin Only)
- **Kelola Tarif:** Mengubah tarif jam pertama, jam berikutnya, atau batas maksimum harian per jenis kendaraan.
- **Kelola Slot:** Menambah kuota slot baru atau menghapus slot kosong.
- **Kelola Petugas:** Membuat akun baru (Petugas/Admin) atau menghapus akun selain akun yang sedang login.
- **Laporan Harian:** Memilih tanggal untuk melihat rekap kuantitas kendaraan masuk, kendaraan keluar, serta total nominal pendapatan tunai harian.

---

## 4. Penjelasan Tiap Class, Import, dan Fungsinya

---

### Paket: `com.parkir`

#### `Main.java`
- **Fungsi Utama:** Titik masuk utama (*entry point*) program Java desktop. Berfungsi merakit dependensi (*manual dependency injection*) dan bootstrap tampilan GUI.
- **Import:**
  - `com.parkir.repository.*`: Antarmuka repositori data.
  - `com.parkir.repository.csv.*`: Implementasi DAO berbasis CSV.
  - `com.parkir.service.*`: Layanan domain bisnis (`AuthService`, `ParkirService`, `LaporanService`).
  - `com.parkir.ui.MainFrame`: Jendela induk antarmuka Swing.
  - `javax.swing.*`: Komponen GUI (`UIManager`, `SwingUtilities`).
  - `java.nio.file.*`: Navigasi direktori penyimpanan berkas `data/`.
- **Rincian Kode:**
  - Mengatur tema OS dengan `UIManager.setLookAndFeel`.
  - Menginisialisasi `UserRepository`, `SlotRepository`, `TarifRepository`, dan `KarcisRepository`.
  - Menginstansiasi `AuthService`, `ParkirService`, dan `LaporanService`.
  - Memanggil `SwingUtilities.invokeLater()` untuk menampilkan jendela `MainFrame` secara aman di thread UI.

---

### Paket: `com.parkir.model`

#### `Fitur.java`
- **Fungsi Utama:** Enum penanda hak akses (*permission*) setiap fitur di aplikasi.
- **Import:** Tidak memerlukan import.
- **Rincian Nilai:**
  - `CATAT_MASUK`, `CATAT_KELUAR`, `LIHAT_PARKIR`, `KELOLA_TARIF`, `KELOLA_SLOT`, `KELOLA_PETUGAS`, `LIHAT_LAPORAN`.

#### `Peran.java`
- **Fungsi Utama:** Enum tingkatan pengguna sistem.
- **Import:** Tidak memerlukan import.
- **Rincian Nilai:**
  - `ADMIN`, `PETUGAS`.

#### `JenisKendaraan.java`
- **Fungsi Utama:** Enum kategori fisik kendaraan yang didukung.
- **Import:** Tidak memerlukan import.
- **Rincian Nilai:**
  - `MOTOR`, `MOBIL`, `TRUK`.

#### `StatusKarcis.java`
- **Fungsi Utama:** Enum status daur hidup tiket parkir.
- **Import:** Tidak memerlukan import.
- **Rincian Nilai:**
  - `AKTIF` (masih di dalam area parkir), `SELESAI` (sudah bayar dan keluar).

#### `User.java`
- **Fungsi Utama:** Abstract class dasar pengguna aplikasi. Menangani enkapsulasi identitas akun dan hashing password SHA-256.
- **Import:**
  - `java.nio.charset.StandardCharsets`: Standard UTF-8 decoding string.
  - `java.security.MessageDigest`: Hashing enkripsi bawaan Java standar.
  - `java.security.NoSuchAlgorithmException`: Penanganan ketersediaan algoritma hash.
  - `java.util.Objects`: Validasi parameter null (`requireNonNull`).
- **Rincian Kode:**
  - Atribut: `username`, `passwordHash`, `nama`.
  - Metode `hashPassword(String)`: Menghasilkan representasi heksadesimal dari hash SHA-256 kata sandi mentah.
  - Metode `cocokPassword(String)`: Membandingkan password mentah terhadap hash yang tersimpan.
  - Metode abstrak: `getPeran()` dan `bolehMengakses(Fitur)`.

#### `Admin.java`
- **Fungsi Utama:** Subclass dari `User` yang merepresentasikan Administrator sistem dengan akses penuh.
- **Import:** Tidak ada (berada dalam paket yang sama).
- **Rincian Kode:**
  - `getPeran()` mengembalikan `Peran.ADMIN`.
  - `bolehMengakses(Fitur fitur)` mengembalikan nilai `true` untuk semua fitur.

#### `Petugas.java`
- **Fungsi Utama:** Subclass dari `User` untuk petugas lapangan operasional.
- **Import:** Tidak ada.
- **Rincian Kode:**
  - `getPeran()` mengembalikan `Peran.PETUGAS`.
  - `bolehMengakses(Fitur fitur)` menggunakan pola `switch expression`: hanya mengizinkan `CATAT_MASUK`, `CATAT_KELUAR`, dan `LIHAT_PARKIR`. Menu administratif ditolak (`false`).

#### `Kendaraan.java`
- **Fungsi Utama:** Abstract class model kendaraan bermotor.
- **Import:**
  - `java.time.Duration`: Representasi durasi waktu parkir.
  - `java.util.Objects`: Pemeriksaan null.
- **Rincian Kode:**
  - Atribut: `platNomor`.
  - Metode abstrak `getJenis()`: Diimplementasikan oleh turunan untuk menentukan jenis spesifik.
  - Metode `hitungBiaya(Duration, KonfigurasiTarif)`: Mengambil strategi tarif yang sesuai dari konfigurasi dan mendelegasikan perhitungan biaya berdasarkan durasi menit.

#### `Motor.java`, `Mobil.java`, `Truk.java`
- **Fungsi Utama:** Subclass konkret dari `Kendaraan`.
- **Import:** Tidak ada.
- **Rincian Kode:**
  - Masing-masing mengoverride `getJenis()` dengan mengembalikan `JenisKendaraan.MOTOR`, `JenisKendaraan.MOBIL`, dan `JenisKendaraan.TRUK`.

#### `SlotParkir.java`
- **Fungsi Utama:** Merepresentasikan unit tempat parkir fisik (misal slot `M-01`, `C-05`).
- **Import:**
  - `java.util.Objects`: Validasi null dan identitas objek.
- **Rincian Kode:**
  - Atribut: `kode` (String), `jenis` (`JenisKendaraan`), `terisi` (boolean).
  - Mengimplementasikan `equals` dan `hashCode` berdasarkan kode slot case-insensitive.

#### `Karcis.java`
- **Fungsi Utama:** Entitas bukti parkir aktif atau arsip transaksi.
- **Import:**
  - `java.time.LocalDateTime`: Pencatatan tanggal dan waktu presisi tinggi.
  - `java.util.Objects`: Validasi parameter null.
- **Rincian Kode:**
  - Atribut: `id`, `kendaraan`, `slot`, `waktuMasuk`, `waktuKeluar`, `biaya`, `status`, `petugas`.
  - Factory method `Karcis.baru(...)`: Mempermudah pembentukan karcis baru berstatus `AKTIF`.
  - Metode `selesaikan(waktuKeluar, biaya)`: Mengubah status karcis menjadi `SELESAI`.

#### `TarifStrategy.java`
- **Fungsi Utama:** Interface strategi (*Strategy Design Pattern*) untuk rumus tarif parkir.
- **Import:** Tidak ada.
- **Rincian Kode:**
  - Kontrak metode: `hitungBiaya(long menit)`, `getJamPertama()`, `getJamBerikutnya()`, `getMaksHarian()`.

#### `TarifProgresif.java`
- **Fungsi Utama:** Implementasi nyata strategi tarif progresif harian dengan batas maksimum harian.
- **Import:**
  - `com.parkir.exception.ParkirException`: Validasi batas tarif tidak logis.
- **Rincian Kode:**
  - Validasi konstruktor: Memastikan parameter bernilai non-negatif dan batas harian tidak lebih kecil dari tarif jam pertama.
  - Metode `hitungBiaya(long menit)`:
    - Menghitung durasi dalam jam dengan pembulatan ke atas (minimal 1 jam).
    - Menghitung blok 24 jam (`blok * maksHarian`) ditambah sisa jam parsial yang dibatasi batas maksimum harian.

#### `KonfigurasiTarif.java`
- **Fungsi Utama:** Registry mapping yang memetakan `JenisKendaraan` ke implementasi `TarifStrategy` yang aktif.
- **Import:**
  - `java.util.EnumMap`, `java.util.Map`, `java.util.Objects`.
- **Rincian Kode:**
  - Menyimpan koleksi konfigurasi dalam `EnumMap<JenisKendaraan, TarifStrategy>`.
  - Dilengkapi nilai default awal (Motor: Rp2.000/1.000/10.000, Mobil: Rp5.000/3.000/30.000, Truk: Rp8.000/5.000/60.000).

---

### Paket: `com.parkir.exception`

Semua kelas exception merupakan turunan dari `RuntimeException` (Unchecked Exception):
1. **`ParkirException.java`**: Kelas induk seluruh exception domain parkir.
2. **`DataRusakException.java`**: Dilempar saat pembacaan file CSV mendapati format kolom tidak sesuai atau nilai korup.
3. **`KarcisTidakDitemukanException.java`**: Dilempar saat pencarian plat/ID karcis aktif tidak ditemukan pada check-out.
4. **`LoginGagalException.java`**: Dilempar ketika username atau password tidak cocok.
5. **`ParkirPenuhException.java`**: Dilempar ketika seluruh slot untuk jenis kendaraan terkait sudah terisi.
6. **`PenyimpananGagalException.java`**: Dilempar saat terjadi kegagalan I/O pada penulisan berkas CSV.
7. **`PlatSudahParkirException.java`**: Dilempar saat kendaraan dengan plat yang sama mencoba masuk padahal masih tercatat parkir aktif.
8. **`PlatTidakValidException.java`**: Dilempar ketika format plat nomor tidak memenuhi standar pola regex Indonesia.

---

### Paket: `com.parkir.repository`

#### `UserRepository.java`
- **Fungsi Utama:** Interface abstraksi penyimpanan data `User`.
- **Import:** `com.parkir.model.User`, `java.util.List`, `java.util.Optional`.
- **Metode:** `findAll()`, `findByUsername(username)`, `save(user)`, `delete(username)`.

#### `SlotRepository.java`
- **Fungsi Utama:** Interface abstraksi tempat parkir `SlotParkir`.
- **Import:** `com.parkir.model.SlotParkir`, `java.util.List`, `java.util.Optional`.
- **Metode:** `findAll()`, `findByKode(kode)`, `save(slot)`, `saveAll(slots)`, `delete(kode)`.

#### `TarifRepository.java`
- **Fungsi Utama:** Interface abstraksi penyimpanan tarif sistem.
- **Import:** `com.parkir.model.KonfigurasiTarif`.
- **Metode:** `load()`, `save(konfigurasi)`.

#### `KarcisRepository.java`
- **Fungsi Utama:** Interface abstraksi penyimpanan data transaksi karcis parkir.
- **Import:** `com.parkir.model.Karcis`, `java.time.LocalDate`, `java.util.List`, `java.util.Optional`.
- **Metode:** `findAll()`, `findById(id)`, `findAktifByPlat(plat)`, `findAktifAll()`, `findByTanggalMasuk(tanggal)`, `save(karcis)`, `generateNextId(tanggal)`.

---

### Paket: `com.parkir.repository.csv`

#### `CsvUserRepository.java`
- **Fungsi Utama:** Implementasi penyimpanan user ke dalam berkas `data/users.csv`.
- **Import:**
  - `com.parkir.exception.DataRusakException`, `com.parkir.model.*`, `com.parkir.repository.UserRepository`, `com.parkir.util.CsvUtil`.
  - `java.nio.file.Files`, `java.nio.file.Path`, `java.util.*`.
- **Rincian Kode:**
  - Mengelola in-memory cache menggunakan `LinkedHashMap`.
  - Otomatis membuat akun bawaan `admin` / `admin123` jika berkas belum ada.
  - Setiap perubahan data (`save`, `delete`) memicu pemanggilan `flush()` untuk menyimpan ulang file secara atomik via `CsvUtil`.

#### `CsvSlotRepository.java`
- **Fungsi Utama:** Implementasi penyimpanan slot parkir ke dalam berkas `data/slots.csv`.
- **Import:** Mirip dengan `CsvUserRepository` untuk kelas model `SlotParkir`.
- **Rincian Kode:**
  - Jika file tidak ada, otomatis menginisialisasi 34 slot bawaan (20 Motor: `M-01` s/d `M-20`, 10 Mobil: `C-01` s/d `C-10`, 4 Truk: `T-01` s/d `T-04`).

#### `CsvTarifRepository.java`
- **Fungsi Utama:** Implementasi penyimpanan tarif ke dalam berkas `data/tarif.csv`.
- **Import:** Entitas tarif, jenis kendaraan, dan utilitas CSV.
- **Rincian Kode:**
  - Membaca 4 kolom CSV: `jenis`, `jamPertama`, `jamBerikutnya`, `maksHarian`.
  - Mengonversi data berkas menjadi objek `KonfigurasiTarif`.

#### `CsvKarcisRepository.java`
- **Fungsi Utama:** Implementasi penyimpanan transaksi karcis ke dalam berkas `data/karcis.csv`.
- **Import:** Model karcis, waktu Java (`LocalDate`, `LocalDateTime`, `DateTimeFormatter`), serta utilitas CSV.
- **Rincian Kode:**
  - Membaca dan menulis riwayat transaksi karcis.
  - Metode `generateNextId(LocalDate)`: Menghitung jumlah karcis yang diterbitkan pada hari bersangkutan dan menghasilkan ID dengan urutan inkremental: `PKR-yyyyMMdd-NNNN`.

---

### Paket: `com.parkir.service`

#### `AuthService.java`
- **Fungsi Utama:** Menangani logika proses login dan autentikasi kredensial pengguna.
- **Import:**
  - `com.parkir.exception.LoginGagalException`, `com.parkir.model.User`, `com.parkir.repository.UserRepository`, `java.util.Objects`, `java.util.Optional`.
- **Rincian Kode:**
  - Memeriksa kelengkapan input username dan password.
  - Memanggil `userRepository.findByUsername()`.
  - Memvalidasi password dengan metode `user.cocokPassword()`. Jika tidak valid, melempar `LoginGagalException`.

#### `ParkirService.java`
- **Fungsi Utama:** Pusat orkestrasi operasional parkir (masuk, keluar, validasi slot, ringkasan kapasitas).
- **Import:**
  - Seluruh exception domain parkir (`ParkirException`, `ParkirPenuhException`, dll).
  - Entitas model (`JenisKendaraan`, `Karcis`, `Kendaraan`, `SlotParkir`, dll).
  - Kontrak repositori (`SlotRepository`, `KarcisRepository`, `TarifRepository`).
  - Java time (`Duration`, `LocalDateTime`) dan regex (`java.util.regex.Pattern`).
- **Rincian Kode:**
  - Pola regex `PLAT_PATTERN`: Memvalidasi susunan plat Indonesia (`^[A-Z]{1,2}\s+[0-9]{1,4}(\s+[A-Z]{1,3})?$`).
  - `catatMasuk(...)`: Memvalidasi plat, memastikan tidak ada duplikasi kendaraan aktif, mencari slot kosong terendah, mencetak karcis, menandai slot terisi, dan menyimpan data secara thread-safe (`synchronized`).
  - `catatKeluar(...)`: Mencari karcis aktif berdasarkan ID atau nomor plat, menghitung selisih waktu (`Duration`), menghitung biaya berdasarkan konfigurasi tarif, membebaskan slot, dan mencatat transaksi selesai.
  - `ringkasanSlot()`: Menghitung rasio keterisian kapasitas per jenis kendaraan untuk kebutuhan tampilan dashboard.

#### `LaporanService.java`
- **Fungsi Utama:** Menghasilkan rekapitulasi data agregat omzet dan transaksi harian.
- **Import:**
  - `com.parkir.model.*`, `com.parkir.repository.KarcisRepository`, `java.time.LocalDate`, `java.util.*`.
- **Rincian Kode:**
  - Class pembantu `LaporanHarian` dan `RincianJenis` sebagai DTO (Data Transfer Object) ringkasan.
  - `laporanHarian(LocalDate)`: Menyaring data karcis berdasarkan tanggal, menghitung total kendaraan masuk, kendaraan keluar, serta total nominal pendapatan per kategori kendaraan.

---

### Paket: `com.parkir.util`

#### `CsvUtil.java`
- **Fungsi Utama:** Engine I/O file CSV untuk membaca baris dan menulis data secara atomik.
- **Import:**
  - `com.parkir.exception.DataRusakException`, `com.parkir.exception.PenyimpananGagalException`.
  - `java.io.BufferedReader`, `java.io.BufferedWriter`, `java.io.IOException`.
  - `java.nio.charset.StandardCharsets`.
  - `java.nio.file.Files`, `java.nio.file.Path`, `java.nio.file.StandardCopyOption`.
  - `java.util.ArrayList`, `java.util.List`.
- **Rincian Kode:**
  - `readRows(...)`: Membaca file baris demi baris, memisah kolom dengan delimiter koma, memotong spasi liar (*trim*), dan memvalidasi jumlah kolom agar sesuai kontrak.
  - `writeRowsAtomically(...)`: Menulis data terlebih dahulu ke file temporer (`.tmp`), lalu memindahkannya menggunakan operasi `StandardCopyOption.ATOMIC_MOVE`. Pendekatan ini mencegah data korup jika program berhenti mendadak saat proses penulisan file berlangsung.

---

### Paket: `com.parkir.ui`

#### `UITheme.java`
- **Fungsi Utama:** Menyediakan konstanta desain (warna palet Slate & Indigo, font Segoe UI/Consolas) serta factory method untuk styling komponen visual.
- **Import:** Komponen `javax.swing.*`, `javax.swing.table.*`, dan `java.awt.*`.
- **Rincian Kode:**
  - Mendefinisikan konstanta warna: `BG_DARK`, `BG_APP`, `PRIMARY`, `SUCCESS`, `DANGER`, `TEXT_MAIN`.
  - Menyediakan helper: `createPrimaryButton()`, `createSecondaryButton()`, `createDangerButton()`, `createCard()`.
  - `formatTable(JTable)`: Menyeragamkan tampilan tabel Swing (warna selang-seling baris genap/ganjil, tinggi baris 32px, styling header).

#### `MainFrame.java`
- **Fungsi Utama:** Frame utama aplikasi yang mengorkestrasi navigasi antar panel menggunakan `CardLayout`.
- **Import:**
  - Model (`Fitur`, `User`), repositori, service, komponen Swing dan AWT layout.
- **Rincian Kode:**
  - Membagi area aplikasi menjadi dua lapisan utama: layar `"LOGIN"` dan layar induk `"APP"` (Sidebar kiri + Content kanan).
  - Menghubungkan seluruh sub-panel tampilan (`DashboardPanel`, `KendaraanMasukPanel`, `KendaraanKeluarPanel`, dll).
  - Memiliki callback `onLoginSuccess(User)`: Memperbarui kartu user badge dan menyaring visibilitas tombol navigasi sesuai hak akses peran pengguna (`user.bolehMengakses(fitur)`).

#### `LoginPanel.java`
- **Fungsi Utama:** Tampilan form login (username & password).
- **Import:** Komponen Swing, AWT, `AuthService`, event listener tombol Enter (`KeyAdapter`, `KeyEvent`), dan `Consumer<User>` callback.
- **Rincian Kode:**
  - Menyediakan input text username dan password field.
  - Menangani aksi tombol login atau penekanan tombol Enter pada keyboard.
  - Menampilkan pesan kesalahan inline berwarna merah jika autentikasi gagal.

#### `DashboardPanel.java`
- **Fungsi Utama:** Menampilkan kartu metrik kapasitas sisa slot parkir secara real-time.
- **Import:** Model, service, komponen Swing/AWT.
- **Rincian Kode:**
  - Mengambil data dari `parkirService.ringkasanSlot()`.
  - Menampilkan 3 kartu ringkasan (Motor, Mobil, Truk) yang berisi jumlah slot kosong, slot terisi, dan kapasitas maksimal. Label slot kosong diberi warna hijau jika tersedia dan merah jika penuh.

#### `KendaraanMasukPanel.java`
- **Fungsi Utama:** Form input untuk mencatat kendaraan yang baru masuk.
- **Import:** Model, exception, service, Swing/AWT, dan `DateTimeFormatter`.
- **Rincian Kode:**
  - Form input plat nomor dan pilihan combo box jenis kendaraan.
  - Memanggil `parkirService.catatMasuk()`.
  - Menampilkan informasi karcis yang baru dibuat pada panel sisi kanan (ID karcis, slot yang diperoleh, dan waktu masuk).

#### `KendaraanKeluarPanel.java`
- **Fungsi Utama:** Form pencarian karcis keluar, penghitungan tarif, dan dialog struk pembayaran.
- **Import:** Model, exception, service, Swing/AWT, `Duration`, `NumberFormat`, `Locale`.
- **Rincian Kode:**
  - Input field untuk nomor karcis atau plat nomor kendaraan.
  - Memanggil `parkirService.catatKeluar()`.
  - Menampilkan pop-up dialog (`JDialog`) berisi rincian struk (waktu masuk, waktu keluar, durasi per jam & menit, serta nominal rupiah yang harus dibayar).

#### `DaftarParkirPanel.java`
- **Fungsi Utama:** Menampilkan tabel seluruh kendaraan yang saat ini sedang parkir (status aktif).
- **Import:** Model, service, Swing, `DefaultTableModel`, waktu Java.
- **Rincian Kode:**
  - Mengambil data dari `parkirService.daftarParkirAktif()`.
  - Memuat data ke tabel dengan kolom: ID Karcis, Plat Nomor, Jenis, Slot, Waktu Masuk, Lama Parkir, dan Petugas.
  - Menyediakan tombol "Segarkan" untuk memperbarui data tabel secara manual.

#### `KelolaTarifPanel.java`
- **Fungsi Utama:** Panel pengaturan tarif parkir untuk Administrator.
- **Import:** Model tarif, repositori tarif, Swing/AWT, `NumberFormat`.
- **Rincian Kode:**
  - Menampilkan tabel tarif yang sedang berlaku.
  - Menyediakan form untuk mengubah nominal jam pertama, jam berikutnya, dan batas maksimum harian per jenis kendaraan.
  - Menyimpan perubahan tarif kembali ke file `tarif.csv` via `TarifRepository`.

#### `KelolaSlotPanel.java`
- **Fungsi Utama:** Panel pengaturan kapasitas slot parkir (tambah/kurangi) untuk Administrator.
- **Import:** Model slot, repositori slot, Swing/AWT, `Comparator`.
- **Rincian Kode:**
  - Menampilkan daftar seluruh slot beserta status keterisiannya.
  - Tombol "+ Tambah 1 Slot": Menambah slot baru dengan penomoran otomatis berurutan (misal `M-21`).
  - Tombol "- Kurangi 1 Slot Kosong": Menghapus slot kosong dengan nomor urut tertinggi. Slot yang sedang terisi tidak dapat dihapus.

#### `KelolaPetugasPanel.java`
- **Fungsi Utama:** Panel CRUD data akun petugas dan admin.
- **Import:** Model user/peran, repositori user, Swing/AWT.
- **Rincian Kode:**
  - Menampilkan daftar akun terdaftar pada tabel.
  - Form untuk membuat akun baru atau memperbarui password akun yang sudah ada.
  - Tombol hapus akun dengan proteksi: akun yang sedang digunakan untuk login tidak dapat dihapus.

#### `LaporanPanel.java`
- **Fungsi Utama:** Panel visualisasi ringkasan transaksi dan pendapatan harian.
- **Import:** Service laporan, Swing/AWT, format mata uang, `LocalDate`.
- **Rincian Kode:**
  - Filter input tanggal (default: tanggal hari ini).
  - Menampilkan 3 kartu ringkasan: Total Kendaraan Masuk, Total Kendaraan Keluar, dan Total Pendapatan Kotor.
  - Menampilkan tabel rincian pendapatan dan volume kendaraan yang dikelompokkan berdasarkan jenis kendaraan (Motor, Mobil, Truk).

---

## 5. Ringkasan Penerapan Konsep OOP

1. **Encapsulation:**
   - Semua atribut pada kelas model (`User`, `Kendaraan`, `Karcis`, `SlotParkir`, dll) dideklarasikan sebagai `private` atau `final`.
   - Akses data dikontrol melalui metode getter dan setter yang divalidasi dengan `Objects.requireNonNull`.

2. **Inheritance & Abstraction:**
   - `User` sebagai abstract class diturunkan ke `Admin` dan `Petugas`.
   - `Kendaraan` sebagai abstract class diturunkan ke `Motor`, `Mobil`, dan `Truk`.
   - Menggunakan interface untuk kontrak abstraksi: `TarifStrategy`, `UserRepository`, `SlotRepository`, `TarifRepository`, dan `KarcisRepository`.

3. **Polymorphism:**
   - Pemeriksaan hak akses menu memanggil metode polimorfik `user.bolehMengakses(Fitur)`, sehingga perilaku otomatis menyesuaikan objek nyata pengguna tanpa perlu percabangan `if (user instanceof Admin)` yang berulang di lapisan GUI.
   - Perhitungan biaya kendaraan mendelegasikan pemanggilan ke `kendaraan.hitungBiaya(...)` yang mengeksekusi strategi tarif sesuai jenis kendaraan terkait (*Strategy Pattern*).

4. **Separation of Concerns:**
   - Pembagian arsitektur yang terpisah antara Lapisan Data (`repository`), Lapisan Bisnis (`service`), Lapisan Entitas (`model`), dan Lapisan Antarmuka (`ui`). Lapisan domain dan service tidak memiliki ketergantungan pada pustaka GUI (`javax.swing`), sehingga dapat diuji menggunakan *unit test* tanpa memerlukan tampilan visual.
