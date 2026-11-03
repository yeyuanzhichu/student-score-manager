package com.example.score.web.filter;

import com.example.score.model.AuthUser;
import com.example.score.util.Flash;
import com.example.score.util.WebUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

@WebFilter(filterName = "authFilter", urlPatterns = "/*")
public class AuthFilter implements Filter {
    private static final Set<String> PUBLIC_PATHS = Set.of("/login", "/logout", "/favicon.ico");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        AuthUser user = session == null ? null : (AuthUser) session.getAttribute("currentUser");
        if (user == null) {
            String target = request.getQueryString() == null
                    ? path
                    : path + "?" + request.getQueryString();
            if (target.startsWith("/teacher/") || target.startsWith("/student/")) {
                session = request.getSession(true);
                session.setAttribute("loginRedirect", target);
            }
            response.sendRedirect(contextPath + "/login");
            return;
        }

        if (path.startsWith("/teacher/") && !user.isTeacher()) {
            Flash.error(request, "你没有访问教师后台的权限");
            response.sendRedirect(contextPath + "/student/dashboard");
            return;
        }
        if (path.startsWith("/student/") && !user.isStudent()) {
            response.sendRedirect(contextPath + "/teacher/dashboard");
            return;
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return path.isEmpty()
                || "/".equals(path)
                || PUBLIC_PATHS.contains(path)
                || path.startsWith("/assets/");
    }
}
