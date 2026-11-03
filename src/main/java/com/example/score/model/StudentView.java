package com.example.score.model;

import java.util.List;

public class StudentView {
    private final StudentSummary summary;
    private final List<StudentScore> scores;

    public StudentView(StudentSummary summary, List<StudentScore> scores) {
        this.summary = summary;
        this.scores = scores;
    }

    public StudentSummary getSummary() {
        return summary;
    }

    public List<StudentScore> getScores() {
        return scores;
    }
}
