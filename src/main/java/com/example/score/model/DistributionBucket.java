package com.example.score.model;

public class DistributionBucket {
    private final String label;
    private final int count;
    private final double percentage;
    private final String tone;

    public DistributionBucket(String label, int count, double percentage, String tone) {
        this.label = label;
        this.count = count;
        this.percentage = percentage;
        this.tone = tone;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getTone() {
        return tone;
    }
}
