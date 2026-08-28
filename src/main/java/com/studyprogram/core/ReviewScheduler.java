package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-question spaced-repetition scheduling, FSRS-lite: rebuilt each session from the
 * student's own attempt log (no extra persistent state). Every correct answer doubles
 * a question's review interval; a miss resets it. The engine then prefers questions
 * that are DUE for review and strongly avoids re-asking ones whose interval hasn't
 * elapsed — expanding retrieval practice across sessions.
 *
 * Within a single session, immediate repetition is still handled by the
 * recently-answered deque; this scheduler covers the cross-session spacing.
 */
public class ReviewScheduler {

    /** Interval growth: streak 1 → 1.5 days, then doubling, capped at 60 days. */
    static double intervalDays(int streak) {
        if (streak <= 0) return 0.5;                       // just missed: retry soon
        return Math.min(60.0, 1.5 * Math.pow(2, streak - 1));
    }

    private record State(LocalDateTime last, int streak) {}

    private final Map<String, State> byQuestion;

    private ReviewScheduler(Map<String, State> byQuestion) {
        this.byQuestion = byQuestion;
    }

    public static ReviewScheduler none() {
        return new ReviewScheduler(Map.of());
    }

    /** Builds from a student's attempt records in chronological order. */
    public static ReviewScheduler fromRecords(Collection<AttemptRecord> records) {
        Map<String, State> states = new HashMap<>();
        for (AttemptRecord r : records) {
            if (!r.isAnswered() || r.getQuestionId() == null || r.getTs() == null) continue;
            State prev = states.get(r.getQuestionId());
            int streak = r.isCorrect() ? (prev == null ? 1 : prev.streak() + 1) : 0;
            states.put(r.getQuestionId(), new State(r.getTs(), streak));
        }
        return new ReviewScheduler(states);
    }

    public boolean seen(String questionId) {
        return byQuestion.containsKey(questionId);
    }

    /** True when the question's review interval has elapsed. */
    public boolean isDue(String questionId, LocalDateTime now) {
        State s = byQuestion.get(questionId);
        if (s == null) return false;
        double days = Duration.between(s.last(), now).toMinutes() / (60.0 * 24.0);
        return days >= intervalDays(s.streak());
    }

    /**
     * Scoring adjustment for the adaptive engine:
     * unseen questions are neutral, due reviews get a bonus, and questions still
     * inside their interval are strongly deprioritised (asking again teaches nothing).
     */
    public double scheduleBonus(String questionId, LocalDateTime now) {
        if (!seen(questionId)) return 0.0;
        return isDue(questionId, now) ? 0.4 : -0.6;
    }
}
