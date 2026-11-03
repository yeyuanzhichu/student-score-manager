package com.example.score.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.example.score.model.AuthUser;

public final class WebUtil {
    private WebUtil() {
    }

    public static AuthUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("currentUser");
        return value instanceof AuthUser ? (AuthUser) value : null;
    }

    public static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static int parseInt(String value, int defaultValue) {
        Integer parsed = parseInteger(value);
        return parsed == null ? defaultValue : parsed;
    }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
