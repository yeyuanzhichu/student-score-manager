package com.example.score.web;

import com.example.score.model.ClassInfo;
import com.example.score.model.Student;
import com.example.score.service.GradeService;
import com.example.score.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/teacher/students")
public class StudentListServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = WebUtil.trim(request.getParameter("keyword"));
        Integer classId = WebUtil.parseInteger(request.getParameter("classId"));
        List<ClassInfo> classes = gradeService.listClasses();

        Integer filterClassId = classId;
        boolean classExists = filterClassId != null
                && classes.stream().anyMatch(c -> c.getId() == filterClassId);
        if (!classExists) {
            classId = null;
        }
        List<Student> students = gradeService.listStudents(keyword, classId);

        request.setAttribute("classes", classes);
        request.setAttribute("students", students);
        request.setAttribute("keyword", keyword);
        request.setAttribute("classId", classId);
        request.getRequestDispatcher("/WEB-INF/views/teacher/students.jsp").forward(request, response);
    }
}
