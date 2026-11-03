package com.example.score.model;

import java.sql.Timestamp;

/**
 * 成绩录入页面的学生花名册行。
 */
public class RosterRow {
    private int studentId;
    private String studentNo;
    private String name;
    private String gender;
    private Integer scoreId;
    private Double score;
    private Timestamp updatedAt;

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getScoreId() {
        return scoreId;
    }

    public void setScoreId(Integer scoreId) {
        this.scoreId = scoreId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isEntered() {
        return score != null;
    }

    public boolean isPassed() {
        return score != null && score >= 60;
    }
}
