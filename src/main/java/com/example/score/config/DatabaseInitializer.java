package com.example.score.config;

import com.example.score.util.PasswordUtil;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.h2.tools.RunScript;

/**
 * 首次启动时创建表结构，并生成一套可直接演示的班级、学生、课程与成绩数据。
 */
public final class DatabaseInitializer {
    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class.getName());

    private static final String[][] CLASSES = {
            {"软件工程2401班", "2024", "软件工程", "王强"},
            {"计算机科学2401班", "2024", "计算机科学与技术", "刘敏"},
            {"数据科学2401班", "2024", "数据科学与大数据技术", "陈立"}
    };

    private static final String[][] STUDENTS = {
            {"张子轩", "李梦琪", "王浩然", "刘雨桐", "陈嘉豪", "杨若曦", "赵宇辰", "黄思涵", "周俊杰", "吴静怡"},
            {"徐一鸣", "孙晓彤", "朱天宇", "胡佳宁", "高文博", "林可欣", "何嘉诚", "郭诗雅", "马泽宇", "罗雨欣"},
            {"梁子涵", "宋依诺", "唐俊熙", "许若彤", "韩明轩", "邓雅琪", "曹宇航", "彭思妍", "曾皓然", "谢欣悦"}
    };

    private static final String[][] COURSES = {
            {"MATH101", "高等数学", "4.0", "李建国", "2026-2027-1"},
            {"ENG101", "大学英语", "3.0", "周敏", "2026-2027-1"},
            {"CS201", "Java 程序设计", "4.0", "王强", "2026-2027-1"},
            {"CS202", "数据结构", "4.0", "陈立", "2026-2027-1"},
            {"CS301", "数据库原理", "3.5", "赵芳", "2026-2027-1"}
    };

    private DatabaseInitializer() {
    }

    /**
     * 应用启动时调用：
     * 1）执行 schema.sql 建表（IF NOT EXISTS，可重复执行）；
     * 2）如果 students 表为空，说明是第一次启动，生成演示数据；
     * 3）已有数据就跳过，不会覆盖用户录入的成绩。
     */
    public static synchronized void initialize() {
        try (Connection connection = Database.getConnection()) {
            // 从 classpath 读取建表脚本并执行
            try (Reader schema = new InputStreamReader(
                    DatabaseInitializer.class.getResourceAsStream("/db/schema.sql"),
                    StandardCharsets.UTF_8)) {
                if (schema == null) {
                    throw new IllegalStateException("找不到数据库结构文件 /db/schema.sql");
                }
                RunScript.execute(connection, schema);
            }

            // 只有空库才灌演示数据；事务保证要么全成功、要么全失败
            if (isEmpty(connection)) {
                connection.setAutoCommit(false);
                try {
                    seed(connection);
                    connection.commit();
                    LOG.info("演示数据初始化完成");
                } catch (Exception e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
            LOG.info("成绩数据库已就绪: " + Database.databasePath());
        } catch (Exception e) {
            throw new IllegalStateException("数据库初始化失败: " + e.getMessage(), e);
        }
    }

    private static boolean isEmpty(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM students")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static void seed(Connection connection) throws Exception {
        String passwordHash = PasswordUtil.hash("123456");
        int teacherId = insertTeacher(connection, passwordHash);

        List<Integer> classIds = new ArrayList<>();
        for (String[] cls : CLASSES) {
            classIds.add(insertClass(connection, cls));
        }

        List<Integer> courseIds = new ArrayList<>();
        for (String[] course : COURSES) {
            courseIds.add(insertCourse(connection, course));
        }

        Random random = new Random(20261103L);
        int scoreCount = 0;

        for (int classIndex = 0; classIndex < CLASSES.length; classIndex++) {
            int classId = classIds.get(classIndex);
            int availableCourses = COURSES.length - classIndex;

            for (int studentIndex = 0; studentIndex < STUDENTS[classIndex].length; studentIndex++) {
                String studentNo = String.format("202401%02d%02d", classIndex + 1, studentIndex + 1);
                String name = STUDENTS[classIndex][studentIndex];
                String gender = studentIndex % 2 == 0 ? "男" : "女";
                String phone = String.format("138%08d", classIndex * 100 + studentIndex + 1);
                int studentId = insertStudent(connection, studentNo, name, gender, classId, phone, passwordHash);

                double ability = 68 + random.nextDouble() * 24;
                if ((studentIndex + classIndex) % 9 == 0) {
                    ability -= 17;
                }

                for (int courseIndex = 0; courseIndex < availableCourses; courseIndex++) {
                    // 留出少量未录入记录，方便演示“待录入”和补录功能
                    if (classIndex == 0 && courseIndex == 4 && (studentIndex == 2 || studentIndex == 7)) {
                        continue;
                    }
                    if (classIndex == 1 && courseIndex == 3 && studentIndex == 9) {
                        continue;
                    }

                    double[] difficulty = {2.5, 1.0, -1.5, -3.0, -4.5};
                    double noise = (random.nextDouble() - 0.48) * 20;
                    double raw = ability + difficulty[courseIndex] + noise;
                    double score = Math.round(Math.max(35, Math.min(100, raw)) * 10.0) / 10.0;
                    LocalDateTime updatedAt = LocalDateTime.now()
                            .minusDays(random.nextInt(18))
                            .minusMinutes(random.nextInt(600));
                    insertScore(connection, studentId, courseIds.get(courseIndex), score, teacherId, updatedAt);
                    scoreCount++;
                }
            }
        }
        LOG.info("已生成 " + (CLASSES.length * 10) + " 名学生、" + COURSES.length
                + " 门课程、" + scoreCount + " 条成绩记录");
    }

    private static int insertTeacher(Connection connection, String passwordHash) throws Exception {
        String sql = "INSERT INTO users (username, password_hash, display_name, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "teacher");
            ps.setString(2, passwordHash);
            ps.setString(3, "王强老师");
            ps.setString(4, "TEACHER");
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private static int insertClass(Connection connection, String[] cls) throws Exception {
        String sql = "INSERT INTO classes (class_name, grade_year, major, advisor) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cls[0]);
            ps.setInt(2, Integer.parseInt(cls[1]));
            ps.setString(3, cls[2]);
            ps.setString(4, cls[3]);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private static int insertCourse(Connection connection, String[] course) throws Exception {
        String sql = "INSERT INTO courses (course_code, course_name, credit, teacher_name, semester) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course[0]);
            ps.setString(2, course[1]);
            ps.setDouble(3, Double.parseDouble(course[2]));
            ps.setString(4, course[3]);
            ps.setString(5, course[4]);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private static int insertStudent(Connection connection, String studentNo, String name, String gender,
                                     int classId, String phone, String passwordHash) throws Exception {
        String sql = "INSERT INTO students (student_no, name, gender, class_id, phone, password_hash, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, studentNo);
            ps.setString(2, name);
            ps.setString(3, gender);
            ps.setInt(4, classId);
            ps.setString(5, phone);
            ps.setString(6, passwordHash);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private static void insertScore(Connection connection, int studentId, int courseId, double score,
                                    int teacherId, LocalDateTime updatedAt) throws Exception {
        String sql = "INSERT INTO scores (student_id, course_id, score, entered_by, updated_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.setDouble(3, score);
            ps.setInt(4, teacherId);
            ps.setObject(5, updatedAt);
            ps.executeUpdate();
        }
    }

    private static int generatedId(PreparedStatement ps) throws Exception {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new IllegalStateException("无法获取新增记录的主键");
    }
}
