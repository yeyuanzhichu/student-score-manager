package com.example.score.model;

import java.util.List;

public class DashboardView {
    private final OverallStat overall;
    private final List<ClassStat> classStats;
    private final List<CourseStat> courseStats;
    private final List<DistributionBucket> distribution;
    private final List<RecentScore> recentScores;

    public DashboardView(OverallStat overall, List<ClassStat> classStats, List<CourseStat> courseStats,
                         List<DistributionBucket> distribution, List<RecentScore> recentScores) {
        this.overall = overall;
        this.classStats = classStats;
        this.courseStats = courseStats;
        this.distribution = distribution;
        this.recentScores = recentScores;
    }

    public OverallStat getOverall() {
        return overall;
    }

    public List<ClassStat> getClassStats() {
        return classStats;
    }

    public List<CourseStat> getCourseStats() {
        return courseStats;
    }

    public List<DistributionBucket> getDistribution() {
        return distribution;
    }

    public List<RecentScore> getRecentScores() {
        return recentScores;
    }
}
