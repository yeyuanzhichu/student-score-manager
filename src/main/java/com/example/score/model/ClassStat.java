package com.example.score.model;

public class ClassStat {
    private int classId;
    private String className;
    private int studentCount;
    private int scoreCount;
    private Double averageScore;
    private Double passRate;
    private Double excellentRate;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getScoreCount() {
        return scoreCount;
    }

    public void setScoreCount(int scoreCount) {
        this.scoreCount = scoreCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }

    public Double getExcellentRate() {
        return excellentRate;
    }

    public void setExcellentRate(Double excellentRate) {
        this.excellentRate = excellentRate;
    }

    public double getPassRateWidth() {
        return passRate == null ? 0 : Math.min(100, passRate);
    }
}
