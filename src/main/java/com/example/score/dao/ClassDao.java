package com.example.score.dao;

import com.example.score.config.Database;
import com.example.score.model.ClassInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassDao {
    public List<ClassInfo> findAll() {
        String sql = "SELECT c.id, c.class_name, c.grade_year, c.major, c.advisor, COUNT(s.id) AS student_count "
                + "FROM classes c LEFT JOIN students s ON s.class_id = c.id "
                + "GROUP BY c.id, c.class_name, c.grade_year, c.major, c.advisor "
                + "ORDER BY c.id";
        List<ClassInfo> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (Exception e) {
            throw new DataAccessException("查询班级列表失败", e);
        }
        return result;
    }

    public ClassInfo findById(int id) {
        String sql = "SELECT c.id, c.class_name, c.grade_year, c.major, c.advisor, COUNT(s.id) AS student_count "
                + "FROM classes c LEFT JOIN students s ON s.class_id = c.id "
                + "WHERE c.id = ? "
                + "GROUP BY c.id, c.class_name, c.grade_year, c.major, c.advisor";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("查询班级失败", e);
        }
    }

    private ClassInfo map(ResultSet rs) throws Exception {
        ClassInfo info = new ClassInfo();
        info.setId(rs.getInt("id"));
        info.setClassName(rs.getString("class_name"));
        info.setGradeYear(rs.getInt("grade_year"));
        info.setMajor(rs.getString("major"));
        info.setAdvisor(rs.getString("advisor"));
        info.setStudentCount(rs.getInt("student_count"));
        return info;
    }
}
