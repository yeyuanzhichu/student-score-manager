package com.example.score.web;

import com.example.score.model.Course;
import com.example.score.service.GradeService;
import com.example.score.service.ValidationException;
import com.example.score.util.Flash;
import com.example.score.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/teacher/course")
public class CourseFormServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = WebUtil.parseInteger(request.getParameter("id"));
        Course course;
        String mode;
        if (id == null) {
            course = new Course();
            course.setCredit(3.0);
            course.setSemester("2026-2027-1");
            course.setTeacherName("王强");
            mode = "create";
        } else {
            course = gradeService.findCourse(id);
            if (course == null) {
                Flash.error(request, "未找到该课程");
                response.sendRedirect(request.getContextPath() + "/teacher/courses");
                return;
            }
            mode = "edit";
        }
        request.setAttribute("course", course);
        request.setAttribute("mode", mode);
        request.getRequestDispatcher("/WEB-INF/views/teacher/course-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Integer id = WebUtil.parseInteger(request.getParameter("id"));
        String mode = id == null ? "create" : "edit";

        Course course = new Course();
        if (id != null) {
            course.setId(id);
        }
        course.setCourseCode(WebUtil.trim(request.getParameter("courseCode")));
        course.setCourseName(WebUtil.trim(request.getParameter("courseName")));
        course.setTeacherName(WebUtil.trim(request.getParameter("teacherName")));
        course.setSemester(WebUtil.trim(request.getParameter("semester")));
        try {
            course.setCredit(Double.parseDouble(WebUtil.trim(request.getParameter("credit"))));
        } catch (NumberFormatException ex) {
            request.setAttribute("error", "请输入有效的学分");
            request.setAttribute("course", course);
            request.setAttribute("mode", mode);
            request.getRequestDispatcher("/WEB-INF/views/teacher/course-form.jsp").forward(request, response);
            return;
        }

        try {
            gradeService.saveCourse(course);
            Flash.success(request, id == null ? "课程已添加" : "课程信息已更新");
            response.sendRedirect(request.getContextPath() + "/teacher/courses");
        } catch (ValidationException ex) {
            request.setAttribute("error", ex.getMessage());
            request.setAttribute("course", course);
            request.setAttribute("mode", mode);
            request.getRequestDispatcher("/WEB-INF/views/teacher/course-form.jsp").forward(request, response);
        } catch (Exception ex) {
            getServletContext().log("保存课程失败", ex);
            request.setAttribute("error", "保存失败，请稍后重试");
            request.setAttribute("course", course);
            request.setAttribute("mode", mode);
            request.getRequestDispatcher("/WEB-INF/views/teacher/course-form.jsp").forward(request, response);
        }
    }
}
