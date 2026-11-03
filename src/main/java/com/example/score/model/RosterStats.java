package com.example.score.model;

public class RosterStats {
    private final int total;
    private final int entered;
    private final Double averageScore;
    private final Double passRate;

    public RosterStats(int total, int entered, Double averageScore, Double passRate) {
        this.total = total;
        this.entered = entered;
        this.averageScore = averageScore;
        this.passRate = passRate;
    }

    public int getTotal() {
        return total;
    }

    public int getEntered() {
        return entered;
    }

    public int getPending() {
        return Math.max(0, total - entered);
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public Double getPassRate() {
        return passRate;
    }
}
