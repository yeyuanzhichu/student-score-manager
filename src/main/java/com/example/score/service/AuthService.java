package com.example.score.service;

import com.example.score.dao.StudentDao;
import com.example.score.dao.UserDao;
import com.example.score.model.AuthUser;
import com.example.score.model.Student;
import com.example.score.model.UserAccount;
import com.example.score.util.PasswordUtil;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private final StudentDao studentDao = new StudentDao();

    /**
     * 同一个登录框同时支持教师账号和学号，角色由账号类型决定。
     */
    public AuthUser login(String account, String password) {
        if (account == null || account.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        String normalized = account.trim();

        UserAccount teacher = userDao.findByUsername(normalized);
        if (teacher != null && PasswordUtil.matches(password, teacher.getPasswordHash())) {
            return new AuthUser(teacher.getId(), teacher.getUsername(), teacher.getDisplayName(),
                    teacher.getRole(), null, null, null);
        }

        Student student = studentDao.findByStudentNo(normalized);
        if (student != null && student.getStatus() == 1
                && PasswordUtil.matches(password, student.getPasswordHash())) {
            return new AuthUser(student.getId(), student.getStudentNo(), student.getName(), "STUDENT",
                    student.getId(), student.getStudentNo(), student.getClassName());
        }
        return null;
    }
}
