package com.example.score.service;

import com.example.score.config.Database;
import com.example.score.dao.ClassDao;
import com.example.score.dao.CourseDao;
import com.example.score.dao.ScoreDao;
import com.example.score.dao.StudentDao;
import com.example.score.model.ClassInfo;
import com.example.score.model.ClassStat;
import com.example.score.model.Course;
import com.example.score.model.CourseStat;
import com.example.score.model.DashboardView;
import com.example.score.model.DistributionBucket;
import com.example.score.model.OverallStat;
import com.example.score.model.RecentScore;
import com.example.score.model.ReportView;
import com.example.score.model.RosterRow;
import com.example.score.model.RosterStats;
import com.example.score.model.ScoreRecord;
import com.example.score.model.Student;
import com.example.score.model.StudentScore;
import com.example.score.model.StudentSummary;
import com.example.score.model.StudentView;
import com.example.score.util.PasswordUtil;
import com.example.score.util.WebUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩管理业务层（Service）。
 *
 * <p>这一层负责“业务规则”，例如：
 * 一次保存整班成绩、统计平均分/及格率、校验学号是否重复、删除课程前检查有没有成绩。
 * 它自己不直接写 SQL，而是调用 DAO 层去读写数据库；Servlet 只负责接收请求和跳转页面。</p>
 *
 * <p>分层关系：JSP 页面 → Servlet 控制器 → GradeService 业务层 → DAO 数据访问层 → H2 数据库。</p>
 */
public class GradeService {
    /** 班级数据访问对象 */
    private final ClassDao classDao = new ClassDao();
    /** 学生数据访问对象 */
    private final StudentDao studentDao = new StudentDao();
    /** 课程数据访问对象 */
    private final CourseDao courseDao = new CourseDao();
    /** 成绩数据访问对象 */
    private final ScoreDao scoreDao = new ScoreDao();

    public List<ClassInfo> listClasses() {
        return classDao.findAll();
    }

    public List<Course> listCourses() {
        return courseDao.findAll();
    }

    public List<Student> listStudents(String keyword, Integer classId) {
        return studentDao.findAll(keyword, classId);
    }

    public Student findStudent(int id) {
        return studentDao.findById(id);
    }

    public Course findCourse(int id) {
        return courseDao.findById(id);
    }

    /**
     * 组装教师工作台的数据：总体统计、各班级统计、各课程统计、分数段分布、最近录入记录。
     * 这里只是把多个 DAO 查询结果拼成一个 DashboardView，方便 JSP 一次性渲染。
     */
    public DashboardView dashboard() {
        OverallStat overall = scoreDao.findOverall();
        overall.setStudentCount(studentDao.countAll());
        overall.setCourseCount(courseDao.countAll());
        List<ClassStat> classStats = scoreDao.findClassStats();
        List<CourseStat> courseStats = scoreDao.findCourseStats();
        List<DistributionBucket> distribution = buildDistribution(scoreDao.distributionCounts(null));
        List<RecentScore> recent = scoreDao.findRecentScores(7);
        return new DashboardView(overall, classStats, courseStats, distribution, recent);
    }

    /**
     * 组装某个班级的统计报表。
     * 如果传入的 classId 为空或不存在，就默认使用第一个班级，保证页面永远有数据可展示。
     */
    public ReportView report(Integer classId) {
        List<ClassInfo> classes = classDao.findAll();
        if (classes.isEmpty()) {
            return new ReportView(classes, null, null, List.of(), List.of(), List.of());
        }
        ClassInfo selected = classes.stream()
                .filter(c -> classId != null && c.getId() == classId)
                .findFirst()
                .orElse(classes.get(0));
        List<ClassStat> stats = scoreDao.findClassStats();
        ClassStat classStat = stats.stream()
                .filter(s -> s.getClassId() == selected.getId())
                .findFirst()
                .orElse(null);
        List<StudentSummary> students = studentDao.findSummariesByClass(selected.getId());
        applyRanks(students);
        List<CourseStat> courseStats = scoreDao.findCourseStatsByClass(selected.getId());
        List<DistributionBucket> distribution = buildDistribution(
                scoreDao.distributionCounts(selected.getId()));
        return new ReportView(classes, selected, classStat, courseStats, students, distribution);
    }

    /**
     * 学生端成绩单：个人汇总（平均分/及格率/学分/排名）+ 每门课的成绩、班级平均分、班级排名。
     * 排名是把同班同学按平均分排序后，找到当前学生所在的位置。
     */
    public StudentView studentView(int studentId) {
        StudentSummary summary = studentDao.findSummary(studentId);
        if (summary == null) {
            return null;
        }
        List<StudentSummary> classmates = studentDao.findSummariesByClass(summary.getClassId());
        applyRanks(classmates);
        classmates.stream()
                .filter(item -> item.getStudentId() == studentId)
                .findFirst()
                .ifPresent(item -> summary.setRank(item.getRank()));
        List<StudentScore> scores = scoreDao.findStudentScores(studentId, summary.getClassId());
        return new StudentView(summary, scores);
    }

    public List<RosterRow> roster(int classId, int courseId) {
        return scoreDao.findRoster(classId, courseId);
    }

    /**
     * 根据花名册计算录入进度、平均分和及格率。
     * 注意：只统计“已录入”的成绩，空白（null）不算 0 分，也不参与平均分。
     */
    public RosterStats rosterStats(List<RosterRow> rows) {
        int total = rows.size();
        int entered = 0;
        double sum = 0;
        int passed = 0;
        for (RosterRow row : rows) {
            if (row.getScore() != null) {
                entered++;
                sum += row.getScore();
                if (row.getScore() >= 60) {
                    passed++;
                }
            }
        }
        Double average = entered == 0 ? null : round1(sum / entered);
        Double passRate = entered == 0 ? null : round1(passed * 100.0 / entered);
        return new RosterStats(total, entered, average, passRate);
    }

    /**
     * 批量保存某班某门课的成绩。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>检查班级、课程是否存在；</li>
     *   <li>取出该班花名册，只允许保存花名册里的学生（防止伪造学生 ID）；</li>
     *   <li>先把所有分数校验完（0～100），再统一写库，避免“存了一半失败”；</li>
     *   <li>在一个数据库事务里逐条 upsert（有则更新、无则新增），空白则删除该成绩；</li>
     *   <li>任何一条失败就 rollback，全部回滚。</li>
     * </ol>
     */
    public ScoreSaveResult saveScores(int classId, int courseId, Map<Integer, String> rawScores,
                                      int teacherId) throws ValidationException {
        ClassInfo classInfo = classDao.findById(classId);
        Course course = courseDao.findById(courseId);
        if (classInfo == null) {
            throw new ValidationException("请选择有效的班级");
        }
        if (course == null) {
            throw new ValidationException("请选择有效的课程");
        }
        List<RosterRow> roster = scoreDao.findRoster(classId, courseId);
        // 以花名册为准：提交上来的学生 ID 必须属于这个班，否则忽略
        Map<Integer, String> rosterIds = new LinkedHashMap<>();
        for (RosterRow row : roster) {
            rosterIds.put(row.getStudentId(), row.getName());
        }

        // 第一步：只做校验，不写数据库。全部通过后再统一保存
        Map<Integer, Double> values = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : rawScores.entrySet()) {
            if (!rosterIds.containsKey(entry.getKey())) {
                continue;
            }
            String raw = WebUtil.trim(entry.getValue());
            if (raw.isEmpty()) {
                values.put(entry.getKey(), null);
                continue;
            }
            double value;
            try {
                value = Double.parseDouble(raw);
            } catch (NumberFormatException ex) {
                throw new ValidationException(rosterIds.get(entry.getKey()) + " 的成绩不是有效数字");
            }
            if (!Double.isFinite(value) || value < 0 || value > 100) {
                throw new ValidationException(rosterIds.get(entry.getKey()) + " 的成绩必须在 0～100 之间");
            }
            values.put(entry.getKey(), round1(value));
        }

        int saved = 0;
        int cleared = 0;
        // 第二步：在一个事务里写入，任何一条失败就全部回滚
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (Map.Entry<Integer, Double> entry : values.entrySet()) {
                    if (entry.getValue() == null) {
                        scoreDao.deleteScore(connection, entry.getKey(), courseId);
                        cleared++;
                    } else {
                        scoreDao.upsert(connection, entry.getKey(), courseId, entry.getValue(), teacherId);
                        saved++;
                    }
                }
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("保存成绩失败: " + ex.getMessage(), ex);
        }
        return new ScoreSaveResult(saved, cleared);
    }

    /**
     * 新增学生：先校验，再把明文密码用 BCrypt 哈希后保存（数据库里不存明文）。
     */
    public int createStudent(Student student, String initialPassword) throws ValidationException {
        validateStudent(student, null);
        if (initialPassword == null || initialPassword.length() < 6) {
            throw new ValidationException("初始密码至少需要 6 位");
        }
        return studentDao.insert(student, PasswordUtil.hash(initialPassword));
    }

    /**
     * 修改学生：newPassword 为空表示不重置密码，只有填了才更新密码哈希。
     */
    public void updateStudent(Student student, String newPassword) throws ValidationException {
        validateStudent(student, student.getId());
        String hash = null;
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                throw new ValidationException("新密码至少需要 6 位");
            }
            hash = PasswordUtil.hash(newPassword);
        }
        studentDao.update(student, hash);
    }

    public void deleteStudent(int id) throws ValidationException {
        if (studentDao.findById(id) == null) {
            throw new ValidationException("学生不存在或已被删除");
        }
        studentDao.delete(id);
    }

    /**
     * 学生表单的服务端校验：学号格式/唯一性、姓名、性别、班级、电话。
     * 前端虽然也校验了，但服务端必须再校验一次，防止绕过页面直接提交非法数据。
     */
    private void validateStudent(Student student, Integer excludeId) throws ValidationException {
        if (WebUtil.isBlank(student.getStudentNo())) {
            throw new ValidationException("学号不能为空");
        }
        if (!student.getStudentNo().matches("[A-Za-z0-9]{4,20}")) {
            throw new ValidationException("学号只能包含字母或数字，长度为 4～20 位");
        }
        if (WebUtil.isBlank(student.getName()) || student.getName().length() > 50) {
            throw new ValidationException("姓名不能为空，且不能超过 50 个字符");
        }
        if (!"男".equals(student.getGender()) && !"女".equals(student.getGender())) {
            throw new ValidationException("请选择有效的性别");
        }
        if (classDao.findById(student.getClassId()) == null) {
            throw new ValidationException("请选择有效的班级");
        }
        String phone = student.getPhone();
        if (phone != null && !phone.isBlank() && !phone.matches("[0-9\\-+]{6,20}")) {
            throw new ValidationException("联系电话格式不正确");
        }
        if (studentDao.existsStudentNo(student.getStudentNo(), excludeId)) {
            throw new ValidationException("学号 " + student.getStudentNo() + " 已存在");
        }
    }

    public int saveCourse(Course course) throws ValidationException {
        validateCourse(course);
        if (course.getId() > 0) {
            courseDao.update(course);
            return course.getId();
        }
        courseDao.insert(course);
        return 0;
    }

    /**
     * 删除课程前先检查有没有成绩记录，有成绩就不允许删，避免误删历史数据。
     */
    public void deleteCourse(int id) throws ValidationException {
        Course course = courseDao.findById(id);
        if (course == null) {
            throw new ValidationException("课程不存在或已被删除");
        }
        if (scoreDao.hasScoresForCourse(id)) {
            throw new ValidationException("课程《" + course.getCourseName()
                    + "》已有成绩记录，不能直接删除。请先清理相关成绩。");
        }
        courseDao.delete(id);
    }

    /**
     * 课程表单的服务端校验：编号格式/唯一性、名称、学分、任课教师、学期。
     */
    private void validateCourse(Course course) throws ValidationException {
        if (WebUtil.isBlank(course.getCourseCode())) {
            throw new ValidationException("课程编号不能为空");
        }
        if (!course.getCourseCode().matches("[A-Za-z0-9\\-]{2,20}")) {
            throw new ValidationException("课程编号只能包含字母、数字或短横线，长度为 2～20 位");
        }
        if (WebUtil.isBlank(course.getCourseName()) || course.getCourseName().length() > 80) {
            throw new ValidationException("课程名称不能为空，且不能超过 80 个字符");
        }
        if (course.getCredit() <= 0 || course.getCredit() > 20) {
            throw new ValidationException("学分必须在 0～20 之间");
        }
        if (WebUtil.isBlank(course.getTeacherName()) || course.getTeacherName().length() > 50) {
            throw new ValidationException("任课教师不能为空，且不能超过 50 个字符");
        }
        if (WebUtil.isBlank(course.getSemester()) || course.getSemester().length() > 30) {
            throw new ValidationException("学期不能为空，且不能超过 30 个字符");
        }
        if (courseDao.existsCode(course.getCourseCode(), course.getId() > 0 ? course.getId() : null)) {
            throw new ValidationException("课程编号 " + course.getCourseCode() + " 已存在");
        }
    }

    /**
     * 导出成绩明细（交给 ExportServlet 写成 CSV）。
     */
    public List<ScoreRecord> exportRecords(Integer classId, Integer courseId) {
        return scoreDao.findAllForExport(classId, courseId);
    }

    /**
     * 把数据库返回的 6 个计数转换成 JSP 好渲染的“分数段”对象。
     * counts[0] 是总数，counts[1..5] 分别是不及格、60–69、70–79、80–89、90–100。
     * percentage 用于页面上的横向进度条宽度。
     */
    private List<DistributionBucket> buildDistribution(int[] counts) {
        int total = counts[0];
        String[] labels = {"不及格", "60–69", "70–79", "80–89", "90–100"};
        String[] tones = {"fail", "pass", "fair", "good", "excellent"};
        List<DistributionBucket> buckets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int count = counts[i + 1];
            double percentage = total == 0 ? 0 : round1(count * 100.0 / total);
            buckets.add(new DistributionBucket(labels[i], count, percentage, tones[i]));
        }
        return buckets;
    }

    /**
     * 按平均分从高到低给列表编号（DAO 已经按平均分降序查出来了，这里只负责写名次）。
     */
    private void applyRanks(List<StudentSummary> students) {
        for (int i = 0; i < students.size(); i++) {
            students.get(i).setRank(i + 1);
        }
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
