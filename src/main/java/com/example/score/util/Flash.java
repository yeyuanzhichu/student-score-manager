package com.example.score.util;

import jakarta.servlet.http.HttpServletRequest;

public final class Flash {
    private Flash() {
    }

    public static void success(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashSuccess", message);
    }

    public static void error(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashError", message);
    }
}
