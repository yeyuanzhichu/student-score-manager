package com.example.score.model;

import java.io.Serializable;

/**
 * 登录后保存在 Session 中的用户信息。
 */
public class AuthUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String username;
    private final String displayName;
    private final String role;
    private final Integer studentId;
    private final String studentNo;
    private final String className;

    public AuthUser(int id, String username, String displayName, String role,
                    Integer studentId, String studentNo, String className) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.studentId = studentId;
        this.studentNo = studentNo;
        this.className = className;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getClassName() {
        return className;
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role) || "ADMIN".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }
}
