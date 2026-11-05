package com.example.score.dao;

import com.example.score.config.Database;
import com.example.score.model.ClassStat;
import com.example.score.model.CourseStat;
import com.example.score.model.OverallStat;
import com.example.score.model.RecentScore;
import com.example.score.model.RosterRow;
import com.example.score.model.ScoreRecord;
import com.example.score.model.StudentScore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 成绩相关的数据访问层（DAO），只负责写 SQL 和把 ResultSet 转成对象。
 *
 * <p>关键查询：</p>
 * <ul>
 *   <li>{@link #findRoster}：某班某门课的花名册（学生 LEFT JOIN 成绩，没录入也能显示学生）</li>
 *   <li>{@link #findStudentScores}：学生个人成绩单，带班级平均分和班级排名</li>
 *   <li>{@link #findClassStats} / {@link #findCourseStats}：平均分、及格率、优秀率</li>
 *   <li>{@link #upsert}：有成绩就更新、没有就新增</li>
 * </ul>
 */
public class ScoreDao {

    /**
     * 查询某班某门课的花名册。
     * 用 LEFT JOIN：即使学生还没录这门课的成绩，也会出现在列表里（成绩为 null）。
     */
    public List<RosterRow> findRoster(int classId, int courseId) {
        String sql = "SELECT s.id AS student_id, s.student_no, s.name, s.gender, sc.id AS score_id, "
                + "sc.score, sc.updated_at "
                + "FROM students s "
                + "LEFT JOIN scores sc ON sc.student_id = s.id AND sc.course_id = ? "
                + "WHERE s.class_id = ? AND s.status = 1 ORDER BY s.student_no";
        List<RosterRow> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RosterRow row = new RosterRow();
                    row.setStudentId(rs.getInt("student_id"));
                    row.setStudentNo(rs.getString("student_no"));
                    row.setName(rs.getString("name"));
                    row.setGender(rs.getString("gender"));
                    row.setScoreId(rs.getObject("score_id") == null ? null : rs.getInt("score_id"));
                    row.setScore(rs.getObject("score") == null ? null : rs.getBigDecimal("score").doubleValue());
                    row.setUpdatedAt(rs.getTimestamp("updated_at"));
                    result.add(row);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("查询成绩花名册失败", e);
        }
        return result;
    }

    public List<StudentScore> findStudentScores(int studentId, int classId) {
        String sql = "SELECT co.id AS course_id, co.course_code, co.course_name, co.credit, "
                + "co.teacher_name, co.semester, sc.score, sc.updated_at, "
                + "(SELECT ROUND(AVG(s2.score), 1) FROM scores s2 "
                + " JOIN students st2 ON st2.id = s2.student_id "
                + " WHERE s2.course_id = co.id AND st2.class_id = ?) AS class_average, "
                + "(SELECT ROUND(100.0 * SUM(CASE WHEN s2.score >= 60 THEN 1 ELSE 0 END) "
                + " / NULLIF(COUNT(*), 0), 1) FROM scores s2 "
                + " JOIN students st2 ON st2.id = s2.student_id "
                + " WHERE s2.course_id = co.id AND st2.class_id = ?) AS class_pass_rate, "
                + "(SELECT COUNT(*) + 1 FROM scores s2 "
                + " JOIN students st2 ON st2.id = s2.student_id "
                + " WHERE s2.course_id = co.id AND st2.class_id = ? AND s2.score > sc.score) AS class_rank, "
                + "(SELECT COUNT(*) FROM scores s2 "
                + " JOIN students st2 ON st2.id = s2.student_id "
                + " WHERE s2.course_id = co.id AND st2.class_id = ?) AS class_size "
                + "FROM scores sc JOIN courses co ON co.id = sc.course_id "
                + "WHERE sc.student_id = ? ORDER BY co.id";
        List<StudentScore> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, classId);
            ps.setInt(3, classId);
            ps.setInt(4, classId);
            ps.setInt(5, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentScore item = new StudentScore();
                    item.setCourseId(rs.getInt("course_id"));
                    item.setCourseCode(rs.getString("course_code"));
                    item.setCourseName(rs.getString("course_name"));
                    item.setCredit(rs.getBigDecimal("credit").doubleValue());
                    item.setTeacherName(rs.getString("teacher_name"));
                    item.setSemester(rs.getString("semester"));
                    item.setScore(rs.getBigDecimal("score").doubleValue());
                    item.setClassAverage(rs.getObject("class_average") == null
                            ? null : rs.getBigDecimal("class_average").doubleValue());
                    item.setClassPassRate(rs.getObject("class_pass_rate") == null
                            ? null : rs.getBigDecimal("class_pass_rate").doubleValue());
                    item.setClassRank(rs.getInt("class_rank"));
                    item.setClassSize(rs.getInt("class_size"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    item.setUpdatedAt(ts == null ? null : ts.toLocalDateTime());
                    result.add(item);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("查询学生成绩单失败", e);
        }
        return result;
    }

    public List<ClassStat> findClassStats() {
        String sql = "SELECT c.id AS class_id, c.class_name, COUNT(DISTINCT s.id) AS student_count, "
                + "COUNT(sc.id) AS score_count, ROUND(AVG(sc.score), 1) AS average_score, "
                + "ROUND(100.0 * SUM(CASE WHEN sc.score >= 60 THEN 1 ELSE 0 END) "
                + "      / NULLIF(COUNT(sc.id), 0), 1) AS pass_rate, "
                + "ROUND(100.0 * SUM(CASE WHEN sc.score >= 90 THEN 1 ELSE 0 END) "
                + "      / NULLIF(COUNT(sc.id), 0), 1) AS excellent_rate "
                + "FROM classes c LEFT JOIN students s ON s.class_id = c.id "
                + "LEFT JOIN scores sc ON sc.student_id = s.id "
                + "GROUP BY c.id, c.class_name ORDER BY c.id";
        List<ClassStat> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClassStat stat = new ClassStat();
                stat.setClassId(rs.getInt("class_id"));
                stat.setClassName(rs.getString("class_name"));
                stat.setStudentCount(rs.getInt("student_count"));
                stat.setScoreCount(rs.getInt("score_count"));
                stat.setAverageScore(decimalOrNull(rs, "average_score"));
                stat.setPassRate(decimalOrNull(rs, "pass_rate"));
                stat.setExcellentRate(decimalOrNull(rs, "excellent_rate"));
                result.add(stat);
            }
        } catch (Exception e) {
            throw new DataAccessException("统计班级成绩失败", e);
        }
        return result;
    }

    public List<CourseStat> findCourseStats() {
        String sql = courseStatSql("") + " ORDER BY co.id";
        List<CourseStat> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapCourseStat(rs));
            }
        } catch (Exception e) {
            throw new DataAccessException("统计课程成绩失败", e);
        }
        return result;
    }

    public List<CourseStat> findCourseStatsByClass(int classId) {
        String sql = "SELECT co.id AS course_id, co.course_code, co.course_name, co.credit, "
                + "COALESCE(x.score_count, 0) AS score_count, x.average_score, x.pass_rate, "
                + "x.highest_score, x.lowest_score "
                + "FROM courses co LEFT JOIN ("
                + "  SELECT sc.course_id, COUNT(*) AS score_count, ROUND(AVG(sc.score), 1) AS average_score, "
                + "  ROUND(100.0 * SUM(CASE WHEN sc.score >= 60 THEN 1 ELSE 0 END) / COUNT(*), 1) AS pass_rate, "
                + "  MAX(sc.score) AS highest_score, MIN(sc.score) AS lowest_score "
                + "  FROM scores sc JOIN students s ON s.id = sc.student_id "
                + "  WHERE s.class_id = ? GROUP BY sc.course_id"
                + ") x ON x.course_id = co.id ORDER BY co.id";
        List<CourseStat> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapCourseStat(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("统计班级课程成绩失败", e);
        }
        return result;
    }

    public OverallStat findOverall() {
        String sql = "SELECT COUNT(DISTINCT student_id) AS student_count, COUNT(*) AS score_count, "
                + "ROUND(AVG(score), 1) AS average_score, "
                + "ROUND(100.0 * SUM(CASE WHEN score >= 60 THEN 1 ELSE 0 END) / COUNT(*), 1) AS pass_rate, "
                + "ROUND(100.0 * SUM(CASE WHEN score >= 90 THEN 1 ELSE 0 END) / COUNT(*), 1) AS excellent_rate, "
                + "SUM(CASE WHEN score < 60 THEN 1 ELSE 0 END) AS fail_count "
                + "FROM scores";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            OverallStat stat = new OverallStat();
            if (rs.next()) {
                stat.setStudentCount(rs.getInt("student_count"));
                stat.setScoreCount(rs.getInt("score_count"));
                stat.setAverageScore(decimalOrNull(rs, "average_score"));
                stat.setPassRate(decimalOrNull(rs, "pass_rate"));
                stat.setExcellentRate(decimalOrNull(rs, "excellent_rate"));
                stat.setFailCount(rs.getInt("fail_count"));
            }
            return stat;
        } catch (Exception e) {
            throw new DataAccessException("统计总体成绩失败", e);
        }
    }

    /**
     * @return [total, fail, 60-69, 70-79, 80-89, 90-100]
     */
    public int[] distributionCounts(Integer classId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS total, "
                        + "SUM(CASE WHEN sc.score < 60 THEN 1 ELSE 0 END) AS fail_count, "
                        + "SUM(CASE WHEN sc.score >= 60 AND sc.score < 70 THEN 1 ELSE 0 END) AS p60, "
                        + "SUM(CASE WHEN sc.score >= 70 AND sc.score < 80 THEN 1 ELSE 0 END) AS p70, "
                        + "SUM(CASE WHEN sc.score >= 80 AND sc.score < 90 THEN 1 ELSE 0 END) AS p80, "
                        + "SUM(CASE WHEN sc.score >= 90 THEN 1 ELSE 0 END) AS p90 "
                        + "FROM scores sc ");
        if (classId != null) {
            sql.append("JOIN students s ON s.id = sc.student_id WHERE s.class_id = ?");
        }
        int[] result = new int[6];
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            if (classId != null) {
                ps.setInt(1, classId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getInt("total");
                    result[1] = rs.getInt("fail_count");
                    result[2] = rs.getInt("p60");
                    result[3] = rs.getInt("p70");
                    result[4] = rs.getInt("p80");
                    result[5] = rs.getInt("p90");
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("统计分数段失败", e);
        }
        return result;
    }

    public List<RecentScore> findRecentScores(int limit) {
        String sql = "SELECT s.name AS student_name, s.student_no, c.class_name, co.course_name, "
                + "sc.score, sc.updated_at "
                + "FROM scores sc JOIN students s ON s.id = sc.student_id "
                + "JOIN classes c ON c.id = s.class_id JOIN courses co ON co.id = sc.course_id "
                + "ORDER BY sc.updated_at DESC LIMIT ?";
        List<RecentScore> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecentScore item = new RecentScore();
                    item.setStudentName(rs.getString("student_name"));
                    item.setStudentNo(rs.getString("student_no"));
                    item.setClassName(rs.getString("class_name"));
                    item.setCourseName(rs.getString("course_name"));
                    item.setScore(rs.getBigDecimal("score").doubleValue());
                    item.setUpdatedAt(rs.getTimestamp("updated_at"));
                    result.add(item);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("查询最近成绩失败", e);
        }
        return result;
    }

    public List<ScoreRecord> findAllForExport(Integer classId, Integer courseId) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.student_no, s.name AS student_name, c.class_name, co.course_code, co.course_name, "
                        + "co.credit, sc.score, co.teacher_name, sc.updated_at "
                        + "FROM scores sc JOIN students s ON s.id = sc.student_id "
                        + "JOIN classes c ON c.id = s.class_id JOIN courses co ON co.id = sc.course_id "
                        + "WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (classId != null) {
            sql.append("AND s.class_id = ? ");
            params.add(classId);
        }
        if (courseId != null) {
            sql.append("AND sc.course_id = ? ");
            params.add(courseId);
        }
        sql.append("ORDER BY c.id, s.student_no, co.id");
        List<ScoreRecord> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setInt(i + 1, (Integer) params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScoreRecord record = new ScoreRecord();
                    record.setStudentNo(rs.getString("student_no"));
                    record.setStudentName(rs.getString("student_name"));
                    record.setClassName(rs.getString("class_name"));
                    record.setCourseCode(rs.getString("course_code"));
                    record.setCourseName(rs.getString("course_name"));
                    record.setCredit(rs.getBigDecimal("credit").doubleValue());
                    record.setScore(rs.getBigDecimal("score").doubleValue());
                    record.setTeacherName(rs.getString("teacher_name"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    record.setUpdatedAt(ts == null ? null : ts.toLocalDateTime());
                    result.add(record);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("导出成绩失败", e);
        }
        return result;
    }

    public void upsert(Connection connection, int studentId, int courseId, double score, int teacherId)
            throws SQLException {
        String update = "UPDATE scores SET score = ?, entered_by = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setDouble(1, score);
            ps.setInt(2, teacherId);
            ps.setInt(3, studentId);
            ps.setInt(4, courseId);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        String insert = "INSERT INTO scores (student_id, course_id, score, entered_by, updated_at) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.setDouble(3, score);
            ps.setInt(4, teacherId);
            ps.executeUpdate();
        }
    }

    public void deleteScore(Connection connection, int studentId, int courseId) throws SQLException {
        String sql = "DELETE FROM scores WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    public int countAll() {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM scores");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new DataAccessException("统计成绩数量失败", e);
        }
    }

    public boolean hasScoresForCourse(int courseId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM scores WHERE course_id = ?")) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            throw new DataAccessException("检查课程成绩失败", e);
        }
    }

    private String courseStatSql(String where) {
        return "SELECT co.id AS course_id, co.course_code, co.course_name, co.credit, "
                + "COUNT(sc.id) AS score_count, ROUND(AVG(sc.score), 1) AS average_score, "
                + "ROUND(100.0 * SUM(CASE WHEN sc.score >= 60 THEN 1 ELSE 0 END) "
                + "      / NULLIF(COUNT(sc.id), 0), 1) AS pass_rate, "
                + "MAX(sc.score) AS highest_score, MIN(sc.score) AS lowest_score "
                + "FROM courses co LEFT JOIN scores sc ON sc.course_id = co.id "
                + where
                + "GROUP BY co.id, co.course_code, co.course_name, co.credit";
    }

    private CourseStat mapCourseStat(ResultSet rs) throws Exception {
        CourseStat stat = new CourseStat();
        stat.setCourseId(rs.getInt("course_id"));
        stat.setCourseCode(rs.getString("course_code"));
        stat.setCourseName(rs.getString("course_name"));
        stat.setCredit(rs.getBigDecimal("credit").doubleValue());
        stat.setScoreCount(rs.getInt("score_count"));
        stat.setAverageScore(decimalOrNull(rs, "average_score"));
        stat.setPassRate(decimalOrNull(rs, "pass_rate"));
        stat.setHighestScore(decimalOrNull(rs, "highest_score"));
        stat.setLowestScore(decimalOrNull(rs, "lowest_score"));
        return stat;
    }

    private Double decimalOrNull(ResultSet rs, String column) throws Exception {
        return rs.getObject(column) == null ? null : rs.getBigDecimal(column).doubleValue();
    }
}
