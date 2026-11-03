package com.example.score.web;

import com.example.score.model.ClassInfo;
import com.example.score.model.Student;
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
import java.util.List;

@WebServlet("/teacher/student")
public class StudentFormServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ClassInfo> classes = gradeService.listClasses();
        if (classes.isEmpty()) {
            Flash.error(request, "请先准备班级数据");
            response.sendRedirect(request.getContextPath() + "/teacher/students");
            return;
        }

        Integer id = WebUtil.parseInteger(request.getParameter("id"));
        Student student;
        String mode;
        if (id == null) {
            student = new Student();
            student.setGender("男");
            student.setClassId(classes.get(0).getId());
            mode = "create";
        } else {
            student = gradeService.findStudent(id);
            if (student == null) {
                Flash.error(request, "未找到该学生");
                response.sendRedirect(request.getContextPath() + "/teacher/students");
                return;
            }
            mode = "edit";
        }
        request.setAttribute("student", student);
        request.setAttribute("classes", classes);
        request.setAttribute("mode", mode);
        request.getRequestDispatcher("/WEB-INF/views/teacher/student-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        List<ClassInfo> classes = gradeService.listClasses();
        Integer id = WebUtil.parseInteger(request.getParameter("id"));
        String mode = id == null ? "create" : "edit";

        Student student = new Student();
        if (id != null) {
            student.setId(id);
        }
        student.setStudentNo(WebUtil.trim(request.getParameter("studentNo")));
        student.setName(WebUtil.trim(request.getParameter("name")));
        student.setGender(WebUtil.trim(request.getParameter("gender")));
        student.setPhone(WebUtil.trim(request.getParameter("phone")));
        Integer classId = WebUtil.parseInteger(request.getParameter("classId"));
        if (classId != null) {
            student.setClassId(classId);
        }
        student.setClassName(classes.stream()
                .filter(c -> c.getId() == student.getClassId())
                .map(ClassInfo::getClassName)
                .findFirst().orElse(""));

        String password = request.getParameter("password");
        try {
            if (id == null) {
                gradeService.createStudent(student, password);
                Flash.success(request, "学生 " + student.getName() + " 已添加，登录账号为学号");
            } else {
                gradeService.updateStudent(student, password);
                Flash.success(request, "学生信息已更新");
            }
            response.sendRedirect(request.getContextPath() + "/teacher/students");
        } catch (ValidationException ex) {
            request.setAttribute("error", ex.getMessage());
            request.setAttribute("student", student);
            request.setAttribute("classes", classes);
            request.setAttribute("mode", mode);
            request.getRequestDispatcher("/WEB-INF/views/teacher/student-form.jsp").forward(request, response);
        } catch (Exception ex) {
            getServletContext().log("保存学生失败", ex);
            request.setAttribute("error", "保存失败，请稍后重试");
            request.setAttribute("student", student);
            request.setAttribute("classes", classes);
            request.setAttribute("mode", mode);
            request.getRequestDispatcher("/WEB-INF/views/teacher/student-form.jsp").forward(request, response);
        }
    }
}
