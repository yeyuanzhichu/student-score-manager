package com.example.score.web;

import com.example.score.model.ScoreRecord;
import com.example.score.service.GradeService;
import com.example.score.util.WebUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/teacher/export")
public class ExportServlet extends HttpServlet {
    private final GradeService gradeService = new GradeService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer classId = WebUtil.parseInteger(request.getParameter("classId"));
        Integer courseId = WebUtil.parseInteger(request.getParameter("courseId"));
        List<ScoreRecord> records = gradeService.exportRecords(classId, courseId);

        String fileName = "成绩汇总.csv";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"scores.csv\"; filename*=UTF-8''" + encoded);

        PrintWriter out = response.getWriter();
        out.write('\ufeff'); // UTF-8 BOM，避免 Excel 打开中文乱码
        out.println("学号,姓名,班级,课程编号,课程名称,学分,成绩,任课教师,更新时间");
        for (ScoreRecord record : records) {
            out.println(String.join(",",
                    csv(record.getStudentNo()),
                    csv(record.getStudentName()),
                    csv(record.getClassName()),
                    csv(record.getCourseCode()),
                    csv(record.getCourseName()),
                    csv(formatCredit(record.getCredit())),
                    csv(formatScore(record.getScore())),
                    csv(record.getTeacherName()),
                    csv(record.getUpdatedAt() == null ? "" : record.getUpdatedAt().format(FORMATTER))));
        }
        out.flush();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String s = value;
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String formatScore(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private String formatCredit(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
