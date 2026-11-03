package com.example.score.web;

import com.example.score.model.DashboardView;
import com.example.score.service.GradeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/teacher/dashboard")
public class TeacherDashboardServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DashboardView dashboard = gradeService.dashboard();
        request.setAttribute("dashboard", dashboard);
        request.getRequestDispatcher("/WEB-INF/views/teacher/dashboard.jsp").forward(request, response);
    }
}
