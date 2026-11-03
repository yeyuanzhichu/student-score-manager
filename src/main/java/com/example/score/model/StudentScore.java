package com.example.score.model;

import java.time.LocalDateTime;

/**
 * 学生成绩单中的一门课程。
 */
public class StudentScore {
    private int courseId;
    private String courseCode;
    private String courseName;
    private double credit;
    private String teacherName;
    private String semester;
    private Double score;
    private Double classAverage;
    private Double classPassRate;
    private Integer classRank;
    private Integer classSize;
    private LocalDateTime updatedAt;

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getClassAverage() {
        return classAverage;
    }

    public void setClassAverage(Double classAverage) {
        this.classAverage = classAverage;
    }

    public Double getClassPassRate() {
        return classPassRate;
    }

    public void setClassPassRate(Double classPassRate) {
        this.classPassRate = classPassRate;
    }

    public Integer getClassRank() {
        return classRank;
    }

    public void setClassRank(Integer classRank) {
        this.classRank = classRank;
    }

    public Integer getClassSize() {
        return classSize;
    }

    public void setClassSize(Integer classSize) {
        this.classSize = classSize;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPassed() {
        return score != null && score >= 60;
    }

    public boolean isExcellent() {
        return score != null && score >= 90;
    }

    public double getDifference() {
        if (score == null || classAverage == null) {
            return 0;
        }
        return Math.round((score - classAverage) * 10.0) / 10.0;
    }
}
