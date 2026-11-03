package com.example.score.model;

public class StudentSummary {
    private int studentId;
    private String studentNo;
    private String name;
    private String gender;
    private int classId;
    private String className;
    private int scoreCount;
    private Double averageScore;
    private Double passRate;
    private double earnedCredits;
    private double totalCredits;
    private int rank;

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

    public double getEarnedCredits() {
        return earnedCredits;
    }

    public void setEarnedCredits(double earnedCredits) {
        this.earnedCredits = earnedCredits;
    }

    public double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getLevel() {
        if (averageScore == null) {
            return "暂无";
        }
        if (averageScore >= 90) {
            return "优秀";
        }
        if (averageScore >= 80) {
            return "良好";
        }
        if (averageScore >= 70) {
            return "中等";
        }
        if (averageScore >= 60) {
            return "及格";
        }
        return "待提升";
    }

    public String getLevelTone() {
        if (averageScore == null) {
            return "muted";
        }
        if (averageScore >= 90) {
            return "excellent";
        }
        if (averageScore >= 80) {
            return "good";
        }
        if (averageScore >= 70) {
            return "fair";
        }
        if (averageScore >= 60) {
            return "pass";
        }
        return "fail";
    }
}
