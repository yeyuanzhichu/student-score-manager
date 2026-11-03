package com.example.score.model;

import java.util.List;

public class ReportView {
    private final List<ClassInfo> classes;
    private final ClassInfo selectedClass;
    private final ClassStat classStat;
    private final List<CourseStat> courseStats;
    private final List<StudentSummary> students;
    private final List<DistributionBucket> distribution;

    public ReportView(List<ClassInfo> classes, ClassInfo selectedClass, ClassStat classStat,
                      List<CourseStat> courseStats, List<StudentSummary> students,
                      List<DistributionBucket> distribution) {
        this.classes = classes;
        this.selectedClass = selectedClass;
        this.classStat = classStat;
        this.courseStats = courseStats;
        this.students = students;
        this.distribution = distribution;
    }

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public ClassInfo getSelectedClass() {
        return selectedClass;
    }

    public ClassStat getClassStat() {
        return classStat;
    }

    public List<CourseStat> getCourseStats() {
        return courseStats;
    }

    public List<StudentSummary> getStudents() {
        return students;
    }

    public List<DistributionBucket> getDistribution() {
        return distribution;
    }
}
