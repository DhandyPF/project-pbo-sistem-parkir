package com.parkir.util;

import com.parkir.exception.DataRusakException;
import com.parkir.exception.PenyimpananGagalException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {
    private CsvUtil() {}

    public static List<String[]> readRows(Path path, int expectedColumns, boolean skipHeader) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && skipHeader) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Split with trailing empty strings preserved (-1)
                String[] parts = line.split(",", -1);
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }
                if (expectedColumns > 0 && parts.length != expectedColumns) {
                    throw new DataRusakException("File " + path.getFileName() + " rusak pada baris " + lineNumber +
                            ": diharapkan " + expectedColumns + " kolom, ditemukan " + parts.length);
                }
                rows.add(parts);
            }
        } catch (IOException e) {
            throw new DataRusakException("Gagal membaca file " + path.getFileName() + ": " + e.getMessage(), e);
        }
        return rows;
    }

    public static void writeRowsAtomically(Path path, String header, List<String> lines) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Path tempFile = Files.createTempFile(path.getParent() != null ? path.getParent() : Path.of("."),
                    "tmp-" + path.getFileName(), ".tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                if (header != null && !header.isEmpty()) {
                    writer.write(header);
                    writer.newLine();
                }
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new PenyimpananGagalException("Gagal menulis file " + path.getFileName() + ": " + e.getMessage(), e);
        }
    }
}
