package com.parkir.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public abstract class User {
    private final String username;
    private String passwordHash;
    private String nama;

    public User(String username, String passwordHash, String nama) {
        this.username = Objects.requireNonNull(username, "username tidak boleh null").trim();
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash tidak boleh null").trim();
        this.nama = Objects.requireNonNull(nama, "nama tidak boleh null").trim();
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash).trim();
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = Objects.requireNonNull(nama).trim();
    }

    public boolean cocokPassword(String plainPassword) {
        if (plainPassword == null) return false;
        return hashPassword(plainPassword).equalsIgnoreCase(this.passwordHash);
    }

    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 tidak didukung", e);
        }
    }

    public abstract Peran getPeran();

    public abstract boolean bolehMengakses(Fitur fitur);
}
