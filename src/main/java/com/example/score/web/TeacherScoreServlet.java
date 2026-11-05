package com.example.score.web;

import com.example.score.model.AuthUser;
import com.example.score.model.ClassInfo;
import com.example.score.model.Course;
import com.example.score.model.RosterRow;
import com.example.score.model.RosterStats;
import com.example.score.service.GradeService;
import com.example.score.service.ScoreSaveResult;
import com.example.score.service.ValidationException;
import com.example.score.util.Flash;
import com.example.score.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师成绩录入控制器，对应页面 /teacher/scores。
 *
 * <p>GET：根据“班级 + 课程”查出花名册（含已有成绩），转发到 scores.jsp 渲染输入框。</p>
 * <p>POST：接收表单里所有 score_学生ID 字段，交给 GradeService 批量保存，然后重定向回本页
 * （重定向是为了避免刷新页面时重复提交）。</p>
 */
@WebServlet("/teacher/scores")
public class TeacherScoreServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();

    /**
     * 展示成绩录入页。
     * 班级/课程参数缺失或非法时，自动回退到第一个班级、第一门课程。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ClassInfo> classes = gradeService.listClasses();
        List<Course> courses = gradeService.listCourses();
        if (classes.isEmpty() || courses.isEmpty()) {
            request.setAttribute("emptyData", true);
            request.setAttribute("classes", classes);
            request.setAttribute("courses", courses);
            request.getRequestDispatcher("/WEB-INF/views/teacher/scores.jsp").forward(request, response);
            return;
        }

        int classId = resolveClassId(request.getParameter("classId"), classes);
        int courseId = resolveCourseId(request.getParameter("courseId"), courses);
        ClassInfo selectedClass = classes.stream().filter(c -> c.getId() == classId).findFirst().orElse(classes.get(0));
        Course selectedCourse = courses.stream().filter(c -> c.getId() == courseId).findFirst().orElse(courses.get(0));

        List<RosterRow> roster = gradeService.roster(classId, courseId);
        RosterStats stats = gradeService.rosterStats(roster);

        request.setAttribute("classes", classes);
        request.setAttribute("courses", courses);
        request.setAttribute("selectedClassId", classId);
        request.setAttribute("selectedCourseId", courseId);
        request.setAttribute("selectedClass", selectedClass);
        request.setAttribute("selectedCourse", selectedCourse);
        request.setAttribute("roster", roster);
        request.setAttribute("rosterStats", stats);
        request.getRequestDispatcher("/WEB-INF/views/teacher/scores.jsp").forward(request, response);
    }

    /**
     * 保存整班成绩。
     * 表单字段命名规则是 score_学生ID，例如 score_3=88.5，这里解析成 Map 后交给 Service。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        AuthUser user = WebUtil.currentUser(request);
        Integer classId = WebUtil.parseInteger(request.getParameter("classId"));
        Integer courseId = WebUtil.parseInteger(request.getParameter("courseId"));
        String back = request.getContextPath() + "/teacher/scores?classId="
                + (classId == null ? "" : classId) + "&courseId=" + (courseId == null ? "" : courseId);

        if (classId == null || courseId == null) {
            Flash.error(request, "请先选择班级和课程");
            response.sendRedirect(request.getContextPath() + "/teacher/scores");
            return;
        }

        Map<Integer, String> scores = new LinkedHashMap<>();
        // 遍历所有请求参数，把 score_3、score_8 这类字段解析成 {3: "88.5", 8: "..."}
        request.getParameterMap().forEach((name, values) -> {
            if (name.startsWith("score_")) {
                Integer studentId = WebUtil.parseInteger(name.substring("score_".length()));
                if (studentId != null) {
                    scores.put(studentId, values.length == 0 ? "" : values[0]);
                }
            }
        });

        try {
            ScoreSaveResult result = gradeService.saveScores(classId, courseId, scores, user.getId());
            String message = "成绩保存成功：更新 " + result.getSaved() + " 条";
            if (result.getCleared() > 0) {
                message += "，清除 " + result.getCleared() + " 条";
            }
            Flash.success(request, message);
        } catch (ValidationException ex) {
            Flash.error(request, ex.getMessage());
        } catch (Exception ex) {
            getServletContext().log("保存成绩失败", ex);
            Flash.error(request, "保存成绩失败，请稍后重试");
        }
        response.sendRedirect(back);
    }

    /** 把请求里的 classId 转成合法班级 ID；非法就返回第一个班级。 */
    private int resolveClassId(String value, List<ClassInfo> classes) {
        Integer parsed = WebUtil.parseInteger(value);
        return classes.stream().map(ClassInfo::getId).filter(id -> id.equals(parsed))
                .findFirst().orElse(classes.get(0).getId());
    }

    /** 把请求里的 courseId 转成合法课程 ID；非法就返回第一门课程。 */
    private int resolveCourseId(String value, List<Course> courses) {
        Integer parsed = WebUtil.parseInteger(value);
        return courses.stream().map(Course::getId).filter(id -> id.equals(parsed))
                .findFirst().orElse(courses.get(0).getId());
    }
}
