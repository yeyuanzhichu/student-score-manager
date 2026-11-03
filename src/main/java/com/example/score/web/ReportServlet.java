package com.example.score.web;

import com.example.score.model.ReportView;
import com.example.score.service.GradeService;
import com.example.score.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/teacher/reports")
public class ReportServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer classId = WebUtil.parseInteger(request.getParameter("classId"));
        ReportView report = gradeService.report(classId);
        request.setAttribute("report", report);
        request.getRequestDispatcher("/WEB-INF/views/teacher/reports.jsp").forward(request, response);
    }
}
