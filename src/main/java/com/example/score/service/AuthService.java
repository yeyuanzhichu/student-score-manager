package com.example.score.service;

import com.example.score.dao.StudentDao;
import com.example.score.dao.UserDao;
import com.example.score.model.AuthUser;
import com.example.score.model.Student;
import com.example.score.model.UserAccount;
import com.example.score.util.PasswordUtil;

/**
 * 登录认证业务。
 *
 * <p>系统只有一张登录表单，但有两类用户：教师（users 表）和学生（students 表）。
 * 这里先按教师账号查，查不到再按学号查学生；密码都用 BCrypt 校验。
 * 登录成功后返回 AuthUser，由 LoginServlet 放进 Session。</p>
 */
public class AuthService {
    private final UserDao userDao = new UserDao();
    private final StudentDao studentDao = new StudentDao();

    /**
     * 同一个登录框同时支持教师账号和学号，角色由账号类型决定。
     *
     * @return 登录成功返回用户信息；账号或密码错误返回 null
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
