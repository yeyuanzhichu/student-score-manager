package com.example.score.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * H2 文件数据库连接工具。
 *
 * <p>默认数据库文件位于用户目录：{@code ~/.student-score-manager/grades.mv.db}。
 * 可通过 JVM 参数 {@code -Dscore.db.path=...} 或环境变量 {@code SCORE_DB_PATH}
 * 指定其他位置，便于课程演示或备份。</p>
 */
public final class Database {
    private static final String DEFAULT_DB_PATH =
            Paths.get(System.getProperty("user.home"), ".student-score-manager", "grades").toString();

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("未找到 H2 数据库驱动: " + e.getMessage());
        }
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), "sa", "");
    }

    public static String jdbcUrl() {
        Path path = databasePath();
        String normalized = path.toString().replace('\\', '/');
        return "jdbc:h2:file:" + normalized
                + ";MODE=MySQL"
                + ";DATABASE_TO_LOWER=TRUE"
                + ";DEFAULT_NULL_ORDERING=HIGH"
                + ";DB_CLOSE_ON_EXIT=FALSE";
    }

    public static Path databasePath() {
        String configured = System.getProperty("score.db.path");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("SCORE_DB_PATH");
        }
        Path path = (configured == null || configured.isBlank())
                ? Paths.get(DEFAULT_DB_PATH)
                : Paths.get(configured);
        path = path.toAbsolutePath().normalize();
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            throw new IllegalStateException("无法创建数据库目录: " + path.getParent(), e);
        }
        return path;
    }
}
