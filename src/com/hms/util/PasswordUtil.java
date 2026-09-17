package com.hms.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Educational cryptographic utility providing password hashing with salted SHA-256.
 * Demonstrates standard Java Security APIs (MessageDigest, SecureRandom).
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Generates an 8-byte hexadecimal cryptographic salt.
     */
    public static String generateSalt() {
        byte[] salt = new byte[8];
        RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Hashes password combined with salt using SHA-256.
     *
     * @param password raw plain text password
     * @param salt hex salt
     * @return hex encoded SHA-256 hash
     */
    public static String hashPassword(String password, String salt) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        String salted = (salt != null ? salt : "") + password;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in environment", e);
        }
    }

    /**
     * Verifies raw password against stored salt and hash.
     */
    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        if (password == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPassword(password, salt);
        return calculated.equalsIgnoreCase(expectedHash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
