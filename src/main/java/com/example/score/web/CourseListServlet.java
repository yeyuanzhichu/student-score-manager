package com.example.score.web;

import com.example.score.model.Course;
import com.example.score.service.GradeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/teacher/courses")
public class CourseListServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Course> courses = gradeService.listCourses();
        request.setAttribute("courses", courses);
        request.getRequestDispatcher("/WEB-INF/views/teacher/courses.jsp").forward(request, response);
    }
}
