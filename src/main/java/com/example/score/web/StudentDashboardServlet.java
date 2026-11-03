package com.example.score.web;

import com.example.score.model.AuthUser;
import com.example.score.model.ClassInfo;
import com.example.score.model.StudentView;
import com.example.score.service.GradeService;
import com.example.score.util.Flash;
import com.example.score.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AuthUser user = WebUtil.currentUser(request);
        if (user == null || user.getStudentId() == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        StudentView view = gradeService.studentView(user.getStudentId());
        if (view == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        ClassInfo classInfo = gradeService.listClasses().stream()
                .filter(c -> c.getId() == view.getSummary().getClassId())
                .findFirst().orElse(null);
        request.setAttribute("view", view);
        request.setAttribute("classInfo", classInfo);
        request.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(request, response);
    }
}
