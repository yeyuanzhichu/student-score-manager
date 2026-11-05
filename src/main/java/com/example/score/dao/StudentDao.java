package com.example.score.dao;

import com.example.score.config.Database;
import com.example.score.model.Student;
import com.example.score.model.StudentSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生数据访问层（DAO）。
 *
 * <p>前半部分是学生增删改查；后半部分用一段汇总 SQL 直接算出
 * 每个学生的成绩数、平均分、及格率、已获学分 / 总学分，供报表和学生成绩页使用。</p>
 */
public class StudentDao {
    /**
     * 汇总查询的 SELECT 部分（每个学生一行）。
     * COUNT/AVG/SUM 都是数据库直接算，比在 Java 里循环快，也更简洁。
     */
    private static final String SUMMARY_COLUMNS =
            "s.id AS student_id, s.student_no, s.name, s.gender, s.class_id, c.class_name, "
                    + "COUNT(sc.id) AS score_count, "
                    + "ROUND(AVG(sc.score), 1) AS average_score, "
                    + "ROUND(100.0 * SUM(CASE WHEN sc.score >= 60 THEN 1 ELSE 0 END) "
                    + "      / NULLIF(COUNT(sc.id), 0), 1) AS pass_rate, "
                    + "COALESCE(SUM(CASE WHEN sc.score >= 60 THEN co.credit ELSE 0 END), 0) AS earned_credits, "
                    + "COALESCE(SUM(co.credit), 0) AS total_credits ";

    private static final String SUMMARY_FROM =
            "FROM students s "
                    + "JOIN classes c ON c.id = s.class_id "
                    + "LEFT JOIN scores sc ON sc.student_id = s.id "
                    + "LEFT JOIN courses co ON co.id = sc.course_id ";

    private static final String SUMMARY_GROUP =
            "GROUP BY s.id, s.student_no, s.name, s.gender, s.class_id, c.class_name ";

    public List<Student> findAll(String keyword, Integer classId) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.status, c.class_name, "
                        + "COUNT(sc.id) AS score_count, ROUND(AVG(sc.score), 1) AS average_score "
                        + "FROM students s JOIN classes c ON c.id = s.class_id "
                        + "LEFT JOIN scores sc ON sc.student_id = s.id WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (classId != null) {
            sql.append("AND s.class_id = ? ");
            params.add(classId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (s.name LIKE ? OR s.student_no LIKE ? OR COALESCE(s.phone, '') LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append("GROUP BY s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.status, c.class_name ")
                .append("ORDER BY s.student_no");

        List<Student> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapStudent(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("查询学生列表失败", e);
        }
        return result;
    }

    public Student findById(int id) {
        String sql = "SELECT s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.status, c.class_name "
                + "FROM students s JOIN classes c ON c.id = s.class_id WHERE s.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapStudent(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("查询学生失败", e);
        }
    }

    public Student findByStudentNo(String studentNo) {
        String sql = "SELECT s.id, s.student_no, s.name, s.gender, s.class_id, s.phone, s.status, "
                + "s.password_hash, c.class_name "
                + "FROM students s JOIN classes c ON c.id = s.class_id WHERE s.student_no = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = mapStudent(rs);
                    student.setPasswordHash(rs.getString("password_hash"));
                    return student;
                }
                return null;
            }
        } catch (Exception e) {
            throw new DataAccessException("查询学生账号失败", e);
        }
    }

    public boolean existsStudentNo(String studentNo, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_no = ?"
                + (excludeId == null ? "" : " AND id <> ?");
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("检查学号失败", e);
        }
    }

    public int insert(Student student, String passwordHash) {
        String sql = "INSERT INTO students (student_no, name, gender, class_id, phone, password_hash, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getStudentNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            ps.setInt(4, student.getClassId());
            ps.setString(5, student.getPhone());
            ps.setString(6, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("新增学生失败", e);
        }
    }

    public int update(Student student, String newPasswordHash) {
        boolean resetPassword = newPasswordHash != null && !newPasswordHash.isBlank();
        String sql = "UPDATE students SET student_no = ?, name = ?, gender = ?, class_id = ?, phone = ?"
                + (resetPassword ? ", password_hash = ?" : "")
                + " WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getStudentNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            ps.setInt(4, student.getClassId());
            ps.setString(5, student.getPhone());
            int index = 6;
            if (resetPassword) {
                ps.setString(index++, newPasswordHash);
            }
            ps.setInt(index, student.getId());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("更新学生失败", e);
        }
    }

    public int delete(int id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("删除学生失败", e);
        }
    }

    public int countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM students");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("统计学生人数失败", e);
        }
    }

    public List<StudentSummary> findSummariesByClass(int classId) {
        String sql = "SELECT " + SUMMARY_COLUMNS + SUMMARY_FROM
                + "WHERE s.class_id = ? " + SUMMARY_GROUP
                + "ORDER BY average_score DESC NULLS LAST, s.student_no";
        List<StudentSummary> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSummary(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("查询班级成绩汇总失败", e);
        }
        return result;
    }

    public StudentSummary findSummary(int studentId) {
        String sql = "SELECT " + SUMMARY_COLUMNS + SUMMARY_FROM
                + "WHERE s.id = ? " + SUMMARY_GROUP;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapSummary(rs) : null;
            }
        } catch (Exception e) {
            throw new DataAccessException("查询学生成绩汇总失败", e);
        }
    }

    private Student mapStudent(ResultSet rs) throws Exception {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setStudentNo(rs.getString("student_no"));
        student.setName(rs.getString("name"));
        student.setGender(rs.getString("gender"));
        student.setClassId(rs.getInt("class_id"));
        student.setClassName(rs.getString("class_name"));
        student.setPhone(rs.getString("phone"));
        student.setStatus(rs.getInt("status"));
        if (hasColumn(rs, "score_count")) {
            student.setScoreCount(rs.getInt("score_count"));
            student.setAverageScore(rs.getObject("average_score") == null
                    ? null : rs.getBigDecimal("average_score").doubleValue());
        }
        return student;
    }

    private StudentSummary mapSummary(ResultSet rs) throws Exception {
        StudentSummary summary = new StudentSummary();
        summary.setStudentId(rs.getInt("student_id"));
        summary.setStudentNo(rs.getString("student_no"));
        summary.setName(rs.getString("name"));
        summary.setGender(rs.getString("gender"));
        summary.setClassId(rs.getInt("class_id"));
        summary.setClassName(rs.getString("class_name"));
        summary.setScoreCount(rs.getInt("score_count"));
        summary.setAverageScore(rs.getObject("average_score") == null
                ? null : rs.getBigDecimal("average_score").doubleValue());
        summary.setPassRate(rs.getObject("pass_rate") == null
                ? null : rs.getBigDecimal("pass_rate").doubleValue());
        summary.setEarnedCredits(rs.getBigDecimal("earned_credits").doubleValue());
        summary.setTotalCredits(rs.getBigDecimal("total_credits").doubleValue());
        return summary;
    }

    private static boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws Exception {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else {
                ps.setString(i + 1, String.valueOf(value));
            }
        }
    }
}
