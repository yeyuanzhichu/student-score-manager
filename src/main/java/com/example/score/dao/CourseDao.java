package com.example.score.dao;

import com.example.score.config.Database;
import com.example.score.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    public List<Course> findAll() {
        String sql = "SELECT co.id, co.course_code, co.course_name, co.credit, co.teacher_name, co.semester, "
                + "COUNT(sc.id) AS score_count, ROUND(AVG(sc.score), 1) AS average_score "
                + "FROM courses co LEFT JOIN scores sc ON sc.course_id = co.id "
                + "GROUP BY co.id, co.course_code, co.course_name, co.credit, co.teacher_name, co.semester "
                + "ORDER BY co.id";
        List<Course> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (Exception e) {
            throw new DataAccessException("查询课程列表失败", e);
        }
        return result;
    }

    public Course findById(int id) {
        String sql = "SELECT co.id, co.course_code, co.course_name, co.credit, co.teacher_name, co.semester, "
                + "COUNT(sc.id) AS score_count, ROUND(AVG(sc.score), 1) AS average_score "
                + "FROM courses co LEFT JOIN scores sc ON sc.course_id = co.id "
                + "WHERE co.id = ? "
                + "GROUP BY co.id, co.course_code, co.course_name, co.credit, co.teacher_name, co.semester";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("查询课程失败", e);
        }
    }

    public boolean existsCode(String courseCode, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM courses WHERE course_code = ?"
                + (excludeId == null ? "" : " AND id <> ?");
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("检查课程编号失败", e);
        }
    }

    public int insert(Course course) {
        String sql = "INSERT INTO courses (course_code, course_name, credit, teacher_name, semester) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setDouble(3, course.getCredit());
            ps.setString(4, course.getTeacherName());
            ps.setString(5, course.getSemester());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("新增课程失败", e);
        }
    }

    public int update(Course course) {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, credit = ?, teacher_name = ?, "
                + "semester = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setDouble(3, course.getCredit());
            ps.setString(4, course.getTeacherName());
            ps.setString(5, course.getSemester());
            ps.setInt(6, course.getId());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("更新课程失败", e);
        }
    }

    public int delete(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("删除课程失败", e);
        }
    }

    public int countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM courses");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("统计课程数量失败", e);
        }
    }

    private Course map(ResultSet rs) throws Exception {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setCredit(rs.getBigDecimal("credit").doubleValue());
        course.setTeacherName(rs.getString("teacher_name"));
        course.setSemester(rs.getString("semester"));
        course.setScoreCount(rs.getInt("score_count"));
        course.setAverageScore(rs.getObject("average_score") == null
                ? null : rs.getBigDecimal("average_score").doubleValue());
        return course;
    }
}
