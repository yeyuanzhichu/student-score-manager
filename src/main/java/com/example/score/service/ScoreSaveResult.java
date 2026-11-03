package com.example.score.service;

public class ScoreSaveResult {
    private final int saved;
    private final int cleared;

    public ScoreSaveResult(int saved, int cleared) {
        this.saved = saved;
        this.cleared = cleared;
    }

    public int getSaved() {
        return saved;
    }

    public int getCleared() {
        return cleared;
    }
}
