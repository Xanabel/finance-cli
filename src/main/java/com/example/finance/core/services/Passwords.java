package com.example.finance.core.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Passwords {
    public static String sha256(String raw) {
        if (raw == null) raw = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
