package com.example.score.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * H2 文件数据库连接工具。
 *
 * <p>数据库位置按以下顺序自动选择，谁可写就用谁：</p>
 * <ol>
 *   <li>JVM 参数 {@code -Dscore.db.path=...} 或环境变量 {@code SCORE_DB_PATH}（显式指定优先）；</li>
 *   <li>用户主目录 {@code ~/.student-score-manager/grades}（Windows / IDEA 下通常可写）；</li>
 *   <li>Tomcat 的 {@code catalina.base/data/student-score-manager/grades}（Linux 系统 Tomcat 下可写）；</li>
 *   <li>系统临时目录 {@code java.io.tmpdir/student-score-manager/grades}（兜底）。</li>
 * </ol>
 *
 * <p>这样在 Windows 上默认还是 {@code C:\Users\你的用户名\.student-score-manager\grades.mv.db}，
 * 在 Linux 的 Tomcat 服务里（服务账号 home 可能是 /nonexistent）会自动落到 Tomcat 数据目录，不需要手动配置。</p>
 */
public final class Database {
    private static final Logger LOG = Logger.getLogger(Database.class.getName());

    /** 解析一次后缓存，保证整个应用所有连接都指向同一个数据库文件。 */
    private static volatile Path resolvedPath;

    static {
        try {
            // 加载 H2 驱动，注册到 DriverManager
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("未找到 H2 数据库驱动: " + e.getMessage());
        }
    }

    private Database() {
    }

    /**
     * 获取一条数据库连接。调用方用 try-with-resources 自动关闭。
     * H2 文件库默认用户名 sa、密码为空，只在本机使用。
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), "sa", "");
    }

    /**
     * 拼接 H2 的 JDBC URL，并带上几个关键参数：
     * MODE=MySQL 兼容 MySQL 语法；DATABASE_TO_LOWER 让表名统一小写；
     * DB_CLOSE_ON_EXIT=FALSE 避免 Tomcat 停止时数据库被强制关闭导致下次启动锁文件。
     */
    public static String jdbcUrl() {
        Path path = databasePath();
        String normalized = path.toString().replace('\\', '/');
        return "jdbc:h2:file:" + normalized
                + ";MODE=MySQL"
                + ";DATABASE_TO_LOWER=TRUE"
                + ";DEFAULT_NULL_ORDERING=HIGH"
                + ";DB_CLOSE_ON_EXIT=FALSE";
    }

    /**
     * 返回最终使用的数据库文件路径（不含 .mv.db 扩展名）。
     * 通过双重检查锁保证只解析一次，避免并发请求反复探测目录。
     */
    public static Path databasePath() {
        Path path = resolvedPath;
        if (path == null) {
            synchronized (Database.class) {
                path = resolvedPath;
                if (path == null) {
                    path = resolvePath();
                    resolvedPath = path;
                    LOG.info("H2 数据库文件位置: " + path);
                }
            }
        }
        return path;
    }

    /**
     * 按优先级选择一个可写的数据库位置。
     * 显式配置的路径会严格使用（不可写就报错，避免用户以为配置生效了其实没有）。
     */
    private static Path resolvePath() {
        String configured = firstNonBlank(System.getProperty("score.db.path"),
                System.getenv("SCORE_DB_PATH"));
        if (configured != null) {
            return prepare(Paths.get(configured));
        }

        // 1. 用户主目录（Windows、普通用户启动 Tomcat 时可用）
        Path homePath = Paths.get(System.getProperty("user.home", "."),
                ".student-score-manager", "grades");
        if (isWritable(homePath)) {
            return prepare(homePath);
        }

        // 2. Tomcat 数据目录（Linux 系统 Tomcat 的服务账号 home 往往是 /nonexistent）
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            Path tomcatPath = Paths.get(catalinaBase, "data", "student-score-manager", "grades");
            if (isWritable(tomcatPath)) {
                return prepare(tomcatPath);
            }
        }

        // 3. 临时目录兜底（不持久，但至少能启动）
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir", "."),
                "student-score-manager", "grades");
        LOG.warning("用户主目录和 Tomcat 数据目录都不可写，数据库将使用临时目录: " + tempPath);
        return prepare(tempPath);
    }

    /**
     * 判断某个数据库路径的父目录是否可创建、可写。
     * 会真实创建一个临时文件再删掉，避免“目录存在但只读”的情况被漏判。
     */
    private static boolean isWritable(Path dbPath) {
        try {
            Path parent = dbPath.toAbsolutePath().normalize().getParent();
            if (parent == null) {
                return false;
            }
            Files.createDirectories(parent);
            if (!Files.isWritable(parent)) {
                return false;
            }
            Path probe = Files.createTempFile(parent, ".write-test-", ".tmp");
            Files.deleteIfExists(probe);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 确保父目录存在并返回规范化后的绝对路径。
     * 显式配置的路径不可写时，在这里抛出带提示的异常。
     */
    private static Path prepare(Path dbPath) {
        Path path = dbPath.toAbsolutePath().normalize();
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return path;
        } catch (Exception e) {
            throw new IllegalStateException("无法创建数据库目录: " + path.getParent()
                    + "。可在 Tomcat 启动参数中加 -Dscore.db.path=可写路径 指定其他位置。", e);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
