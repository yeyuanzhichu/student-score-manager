package com.example.score.web;

import com.example.score.service.GradeService;
import com.example.score.service.ValidationException;
import com.example.score.util.Flash;
import com.example.score.util.WebUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/teacher/student/delete")
public class StudentDeleteServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = WebUtil.parseInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "无效的学生编号");
            response.sendRedirect(request.getContextPath() + "/teacher/students");
            return;
        }
        try {
            gradeService.deleteStudent(id);
            Flash.success(request, "学生及其成绩记录已删除");
        } catch (ValidationException ex) {
            Flash.error(request, ex.getMessage());
        } catch (Exception ex) {
            getServletContext().log("删除学生失败", ex);
            Flash.error(request, "删除失败，请稍后重试");
        }
        response.sendRedirect(request.getContextPath() + "/teacher/students");
    }
}
