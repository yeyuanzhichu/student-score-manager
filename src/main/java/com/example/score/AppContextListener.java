package com.example.score;

import com.example.score.config.DatabaseInitializer;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseInitializer.initialize();
        sce.getServletContext().log("班级学生成绩管理系统启动完成");
    }
}
