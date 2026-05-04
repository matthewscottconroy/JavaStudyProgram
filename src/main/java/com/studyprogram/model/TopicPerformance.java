package com.studyprogram.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Tracks a student's performance on a single topic. */
public class TopicPerformance {

    private Topic topic;
    private int attempts;
    private int correct;
    private double masteryScore;          // 0.0–1.0 (SM-2-inspired running score)
    private LocalDateTime lastAttempted;
    private LocalDateTime lastCorrect;
    @JsonIgnore
    private Deque<String> recentlyAnswered = new ArrayDeque<>(); // question IDs, capped at 20

    private static final int RECENT_CAP = 20;
    private static final double CORRECT_DELTA   = 0.10;
    private static final double INCORRECT_DELTA = 0.05;

    public TopicPerformance() {}

    public TopicPerformance(Topic topic) {
        this.topic = topic;
        this.masteryScore = 0.0;
    }

    /** Record the outcome of one question attempt. */
    public void record(String questionId, boolean wasCorrect) {
        attempts++;
        lastAttempted = LocalDateTime.now();
        if (wasCorrect) {
            correct++;
            lastCorrect = LocalDateTime.now();
            masteryScore = Math.min(1.0, masteryScore + CORRECT_DELTA);
        } else {
            masteryScore = Math.max(0.0, masteryScore - INCORRECT_DELTA);
        }
        recentlyAnswered.addFirst(questionId);
        if (recentlyAnswered.size() > RECENT_CAP) recentlyAnswered.removeLast();
    }

    /** Whether this question was answered in the current session window (spaced repetition). */
    @JsonIgnore
    public boolean wasRecentlySeen(String questionId) {
        return recentlyAnswered.contains(questionId);
    }

    /** Suggested difficulty (1–5) based on current mastery. */
    @JsonIgnore
    public int suggestedDifficulty() {
        return Math.max(1, (int) Math.ceil(masteryScore * 5));
    }

    @JsonIgnore
    public double getAccuracyRate() {
        return attempts == 0 ? 0.0 : (double) correct / attempts;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public Topic getTopic()                  { return topic; }
    public void setTopic(Topic t)            { this.topic = t; }
    public int getAttempts()                 { return attempts; }
    public void setAttempts(int a)           { this.attempts = a; }
    public int getCorrect()                  { return correct; }
    public void setCorrect(int c)            { this.correct = c; }
    public double getMasteryScore()          { return masteryScore; }
    public void setMasteryScore(double s)    { this.masteryScore = s; }
    public LocalDateTime getLastAttempted()  { return lastAttempted; }
    public void setLastAttempted(LocalDateTime t) { this.lastAttempted = t; }
    public LocalDateTime getLastCorrect()    { return lastCorrect; }
    public void setLastCorrect(LocalDateTime t)   { this.lastCorrect = t; }

    // Persist the spaced-repetition deque as a plain list for Jackson
    public List<String> getRecentlyAnswered() { return new ArrayList<>(recentlyAnswered); }
    public void setRecentlyAnswered(List<String> ids) {
        recentlyAnswered = ids == null ? new ArrayDeque<>() : new ArrayDeque<>(ids);
    }
}
