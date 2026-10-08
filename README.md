# Sistem Parkir (Java Swing GUI)

Aplikasi desktop manajemen sistem parkir berbasis Java Swing dengan arsitektur OOP (Strategy Pattern, Repository Pattern, Encapsulation, Polymorphism).

---

## Prasyarat di Laptop Lain

1. **Java JDK 21** terpasang (`java -version`).
2. *Maven tidak wajib terinstall manual* karena repository sudah menyertakan **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

---

## Cara Menjalankan

### 1. Clone Repository
```bash
git clone <url-repo-anda>
cd sistem-parkir
```

### 2. Jalankan Langsung

**Di Windows (CMD / PowerShell):**
```cmd
.\mvnw.cmd compile exec:java -Dexec.mainClass="com.parkir.Main"
```

**Di Linux / macOS:**
```bash
chmod +x mvnw
./mvnw compile exec:java -Dexec.mainClass="com.parkir.Main"
```

---

## Atau Build Menjadi JAR

1. Build file JAR:
   ```cmd
   .\mvnw.cmd clean package
   ```
2. Jalankan JAR:
   ```cmd
   java -jar target/sistem-parkir-1.0.0.jar
   ```

---

## Akun Login Bawaan Pertama Kali

- **Username:** `admin`
- **Password:** `admin123`
*(Bisa menambah akun Petugas baru lewat menu Admin "Kelola Petugas".)*
