package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Per-question spaced-repetition scheduling, FSRS-lite: rebuilt each session from the student's own
 * attempt log, so there is no extra state to persist or migrate.
 *
 * <p>Each question carries a streak and an <em>ease factor</em>. A correct answer multiplies the
 * interval by the ease; a miss resets the streak and makes the question permanently a little harder
 * for this student (lower ease), so material they repeatedly forget comes back more often than
 * material they got right first time. Intervals are jittered by a deterministic ±15% derived from
 * the question id, which stops a big study day from turning into a big review day weeks later.
 *
 * <p>Within a single session, immediate repetition is handled by the recently-answered deque;
 * this scheduler covers the spacing across sessions.
 */
public class ReviewScheduler {

    private static final double BASE_INTERVAL_DAYS = 1.5;
    private static final double RETRY_AFTER_MISS_DAYS = 0.5;
    private static final double MAX_INTERVAL_DAYS = 60.0;
    private static final double STARTING_EASE = 2.0;
    private static final double MIN_EASE = 1.3;
    private static final double MAX_EASE = 2.6;
    private static final double EASE_PENALTY = 0.25;    // per miss
    private static final double EASE_REWARD = 0.05;     // per success
    private static final double JITTER = 0.15;          // +/- 15%

    private record State(LocalDateTime last, int streak, double ease) {}

    private final Map<String, State> byQuestion;

    private ReviewScheduler(Map<String, State> byQuestion) {
        this.byQuestion = byQuestion;
    }

    public static ReviewScheduler none() {
        return new ReviewScheduler(Map.of());
    }

    /** Builds from a student's attempt records; order within the collection is respected. */
    public static ReviewScheduler fromRecords(Collection<AttemptRecord> records) {
        Map<String, State> states = new HashMap<>();
        for (AttemptRecord r : records) {
            if (!r.isAnswered() || r.getQuestionId() == null || r.getTs() == null) continue;
            State prev = states.get(r.getQuestionId());
            double ease = prev == null ? STARTING_EASE : prev.ease();
            int streak;
            if (r.isCorrect()) {
                streak = prev == null ? 1 : prev.streak() + 1;
                ease = Math.min(MAX_EASE, ease + EASE_REWARD);
            } else {
                streak = 0;
                ease = Math.max(MIN_EASE, ease - EASE_PENALTY);
            }
            states.put(r.getQuestionId(), new State(r.getTs(), streak, ease));
        }
        return new ReviewScheduler(states);
    }

    /** Interval for a streak at a given ease, before jitter. */
    static double intervalDays(int streak, double ease) {
        if (streak <= 0) return RETRY_AFTER_MISS_DAYS;
        return Math.min(MAX_INTERVAL_DAYS, BASE_INTERVAL_DAYS * Math.pow(ease, streak - 1));
    }

    /** Convenience for the default ease (used by tests and callers without state). */
    static double intervalDays(int streak) {
        return intervalDays(streak, STARTING_EASE);
    }

    public boolean seen(String questionId) {
        return byQuestion.containsKey(questionId);
    }

    /** The current ease factor for a question, or the starting ease when unseen. */
    public double easeOf(String questionId) {
        State s = byQuestion.get(questionId);
        return s == null ? STARTING_EASE : s.ease();
    }

    /** Days until this question is due again (0 when due now or unseen). */
    public double daysUntilDue(String questionId, LocalDateTime now) {
        State s = byQuestion.get(questionId);
        if (s == null) return 0;
        double elapsed = Duration.between(s.last(), now).toMinutes() / (60.0 * 24.0);
        return Math.max(0, jitteredInterval(questionId, s) - elapsed);
    }

    /** True when the question's review interval has elapsed. */
    /**
     * How many previously-answered questions are due for review right now.
     *
     * <p>This is the number that gets a student to open the program. Spaced repetition has been
     * scheduling every question individually for a while, and until now it only ever showed up
     * as a quiet nudge inside the feed's ranking — nothing ever said "twelve things you learned
     * are ready to be revisited today".
     */
    public int dueCount(LocalDateTime now) {
        int due = 0;
        for (String id : scheduledIds()) {
            if (isDue(id, now)) due++;
        }
        return due;
    }

    /** The questions this scheduler knows about, in no particular order. */
    public java.util.Set<String> scheduledIds() {
        return java.util.Collections.unmodifiableSet(byQuestion.keySet());
    }

    public boolean isDue(String questionId, LocalDateTime now) {
        State s = byQuestion.get(questionId);
        if (s == null) return false;
        double days = Duration.between(s.last(), now).toMinutes() / (60.0 * 24.0);
        return days >= jitteredInterval(questionId, s);
    }

    /**
     * Scoring adjustment for the adaptive engine: unseen questions are neutral, due reviews get a
     * bonus, and questions still inside their interval are strongly deprioritised.
     */
    public double scheduleBonus(String questionId, LocalDateTime now) {
        if (!seen(questionId)) return 0.0;
        return isDue(questionId, now) ? 0.4 : -0.6;
    }

    /**
     * Deterministic ±15% jitter keyed by question id: two questions answered in the same session
     * come due on different days, so reviews spread out instead of arriving in a clump.
     */
    private double jitteredInterval(String questionId, State s) {
        double base = intervalDays(s.streak(), s.ease());
        double factor = 1.0 + (new Random(questionId.hashCode()).nextDouble() * 2 - 1) * JITTER;
        return base * factor;
    }
}
