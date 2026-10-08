package com.parkir.repository.csv;

import com.parkir.exception.DataRusakException;
import com.parkir.model.Admin;
import com.parkir.model.Peran;
import com.parkir.model.Petugas;
import com.parkir.model.User;
import com.parkir.repository.UserRepository;
import com.parkir.util.CsvUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CsvUserRepository implements UserRepository {
    private static final String HEADER = "username,passwordHash,nama,peran";
    private final Path filePath;
    private final Map<String, User> cache = new LinkedHashMap<>();

    public CsvUserRepository(Path filePath) {
        this.filePath = filePath;
        initAndLoad();
    }

    private void initAndLoad() {
        if (!Files.exists(filePath)) {
            // Default bawaan: admin / admin123
            Admin defaultAdmin = new Admin("admin", User.hashPassword("admin123"), "Administrator");
            cache.put(defaultAdmin.getUsername().toLowerCase(), defaultAdmin);
            flush();
            return;
        }

        List<String[]> rows = CsvUtil.readRows(filePath, 4, true);
        cache.clear();
        for (String[] row : rows) {
            String username = row[0];
            String passwordHash = row[1];
            String nama = row[2];
            String peranStr = row[3];

            Peran peran;
            try {
                peran = Peran.valueOf(peranStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new DataRusakException("Peran '" + peranStr + "' tidak valid di " + filePath.getFileName());
            }

            User user = (peran == Peran.ADMIN)
                    ? new Admin(username, passwordHash, nama)
                    : new Petugas(username, passwordHash, nama);
            cache.put(username.toLowerCase(), user);
        }
    }

    private synchronized void flush() {
        List<String> lines = new ArrayList<>();
        for (User u : cache.values()) {
            lines.add(String.join(",",
                    u.getUsername(),
                    u.getPasswordHash(),
                    u.getNama(),
                    u.getPeran().name()
            ));
        }
        CsvUtil.writeRowsAtomically(filePath, HEADER, lines);
    }

    @Override
    public synchronized List<User> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(cache.get(username.trim().toLowerCase()));
    }

    @Override
    public synchronized void save(User user) {
        cache.put(user.getUsername().toLowerCase(), user);
        flush();
    }

    @Override
    public synchronized void delete(String username) {
        if (username == null) return;
        cache.remove(username.trim().toLowerCase());
        flush();
    }
}
