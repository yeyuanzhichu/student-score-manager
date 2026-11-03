package com.example.score.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private static final int WORK_FACTOR = 10;

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(String plainPassword, String hash) {
        if (plainPassword == null || hash == null || hash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
