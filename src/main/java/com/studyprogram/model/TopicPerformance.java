package com.studyprogram.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    private int skipped;
    private double masteryScore;          // 0.0–1.0 (SM-2-inspired running score)
    private LocalDateTime lastAttempted;
    private LocalDateTime lastCorrect;
    private LocalDateTime masteryUpdatedAt; // last time masteryScore changed (incl. decay)
    // NOTE: no @JsonIgnore here — the field must persist so the "recently seen"
    // spaced-repetition penalty survives across program runs. Jackson serializes it
    // through the List-typed accessor pair below.
    private Deque<String> recentlyAnswered = new ArrayDeque<>(); // question IDs, capped at 20

    private static final int RECENT_CAP = 20;
    private static final double CORRECT_DELTA   = 0.10;
    private static final double INCORRECT_DELTA = 0.05;

    public TopicPerformance() {}

    public TopicPerformance(Topic topic) {
        this.topic = topic;
        this.masteryScore = 0.0;
    }

    /** Record the outcome of one question attempt with the default (flat) weighting. */
    public void record(String questionId, boolean wasCorrect) {
        recordWeighted(questionId, wasCorrect, wasCorrect ? CORRECT_DELTA : INCORRECT_DELTA);
    }

    /**
     * Record an attempt with difficulty-weighted mastery deltas: solving a hard
     * question earns more than an easy one, while missing an easy question costs
     * more than missing a hard one (a miss on easy material is the stronger signal).
     */
    public void record(String questionId, boolean wasCorrect, int difficulty) {
        int d = Math.max(1, Math.min(5, difficulty));
        double delta = wasCorrect
                ? 0.05 + 0.02 * d          // d1 +0.07 … d5 +0.15
                : 0.09 - 0.01 * d;         // d1 −0.08 … d5 −0.04
        recordWeighted(questionId, wasCorrect, delta);
    }

    private void recordWeighted(String questionId, boolean wasCorrect, double delta) {
        attempts++;
        lastAttempted = LocalDateTime.now();
        if (wasCorrect) {
            correct++;
            lastCorrect = LocalDateTime.now();
            masteryScore = Math.min(1.0, masteryScore + delta);
        } else {
            masteryScore = Math.max(0.0, masteryScore - delta);
        }
        masteryUpdatedAt = lastAttempted;
        recentlyAnswered.addFirst(questionId);
        if (recentlyAnswered.size() > RECENT_CAP) recentlyAnswered.removeLast();
    }

    /**
     * Applies time decay: after a one-week grace period, mastery halves every
     * ~120 days of inactivity, so stale topics resurface in the auto feed.
     * Tracked via {@code masteryUpdatedAt} so repeated application is stable.
     */
    public void applyDecay(LocalDateTime now) {
        LocalDateTime base = masteryUpdatedAt != null ? masteryUpdatedAt : lastAttempted;
        if (base == null || masteryScore <= 0) return;
        double days = java.time.Duration.between(base, now).toHours() / 24.0;
        if (days <= 7) return;
        masteryScore *= Math.pow(0.5, (days - 7) / 120.0);
        masteryUpdatedAt = now;
    }

    /** Record that the student chose to skip a question on this topic (an avoidance signal). */
    public void recordSkip() {
        skipped++;
        lastAttempted = LocalDateTime.now();
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
    public int getSkipped()                  { return skipped; }
    public void setSkipped(int s)            { this.skipped = s; }
    public double getMasteryScore()          { return masteryScore; }
    public void setMasteryScore(double s)    { this.masteryScore = s; }
    public LocalDateTime getLastAttempted()  { return lastAttempted; }
    public void setLastAttempted(LocalDateTime t) { this.lastAttempted = t; }
    public LocalDateTime getLastCorrect()    { return lastCorrect; }
    public void setLastCorrect(LocalDateTime t)   { this.lastCorrect = t; }
    public LocalDateTime getMasteryUpdatedAt()    { return masteryUpdatedAt; }
    public void setMasteryUpdatedAt(LocalDateTime t) { this.masteryUpdatedAt = t; }

    // Persist the spaced-repetition deque as a plain list for Jackson
    @JsonProperty("recentlyAnswered")
    public List<String> getRecentlyAnswered() { return new ArrayList<>(recentlyAnswered); }
    public void setRecentlyAnswered(List<String> ids) {
        recentlyAnswered = ids == null ? new ArrayDeque<>() : new ArrayDeque<>(ids);
    }
}
