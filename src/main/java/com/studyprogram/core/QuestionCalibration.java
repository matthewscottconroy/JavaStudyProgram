package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Empirical question calibration from the attempt log(s).
 *
 * Authored difficulty is a guess; real students are the measurement. Once a
 * question has enough attempts, its observed pass rate is blended with the
 * authored difficulty (a Bayesian-style prior) to give an <em>effective
 * difficulty</em> the adaptive engine matches against instead of the label.
 * Questions whose measured difficulty drifts far from their label are flagged
 * for content review — a too-easy "hard" question or a trick "easy" one.
 */
public class QuestionCalibration {

    /** Weight of the authored difficulty, in attempt-equivalents. */
    private static final int PRIOR_WEIGHT = 10;
    /** Attempts needed before a question can be flagged for review. */
    private static final int FLAG_MIN_ATTEMPTS = 10;
    private static final double FLAG_DRIFT = 1.25;

    public record Stats(int attempts, int correct, long totalSeconds, int hintsUsed) {
        public double passRate() { return attempts == 0 ? 0.0 : (double) correct / attempts; }
    }

    private final Map<String, Stats> byQuestion;

    private QuestionCalibration(Map<String, Stats> byQuestion) {
        this.byQuestion = byQuestion;
    }

    /** Empty calibration: every question keeps its authored difficulty. */
    public static QuestionCalibration none() {
        return new QuestionCalibration(Map.of());
    }

    /** Builds calibration from attempt records (skips are not evidence of difficulty). */
    public static QuestionCalibration fromRecords(Collection<AttemptRecord> records) {
        Map<String, int[]> acc = new HashMap<>();   // {attempts, correct, seconds, hints}
        for (AttemptRecord r : records) {
            if (!r.isAnswered() || r.getQuestionId() == null) continue;
            int[] a = acc.computeIfAbsent(r.getQuestionId(), k -> new int[4]);
            a[0]++;
            if (r.isCorrect()) a[1]++;
            a[2] += (int) Math.min(Integer.MAX_VALUE, r.getSeconds());
            a[3] += r.getHintsUsed();
        }
        Map<String, Stats> stats = new HashMap<>();
        acc.forEach((id, a) -> stats.put(id, new Stats(a[0], a[1], a[2], a[3])));
        return new QuestionCalibration(stats);
    }

    public Stats statsFor(String questionId) {
        return byQuestion.get(questionId);
    }

    /**
     * The difficulty the engine should match against: the authored label pulled
     * toward the measured one as attempts accumulate. With no data it IS the label.
     */
    public double effectiveDifficulty(Question q) {
        Stats s = byQuestion.get(q.getId());
        if (s == null || s.attempts() == 0) return q.getDifficulty();
        double observed = 1.0 + 4.0 * (1.0 - s.passRate());   // all pass → 1, none pass → 5
        double blended = (q.getDifficulty() * PRIOR_WEIGHT + observed * s.attempts())
                / (PRIOR_WEIGHT + s.attempts());
        return Math.max(1.0, Math.min(5.0, blended));
    }

    /**
     * Questions whose measured difficulty has drifted far from the authored label —
     * candidates for rewording, re-labeling, or removal.
     */
    public List<String> flaggedForReview(Collection<Question> questions) {
        List<String> flagged = new ArrayList<>();
        for (Question q : questions) {
            Stats s = byQuestion.get(q.getId());
            if (s == null || s.attempts() < FLAG_MIN_ATTEMPTS) continue;
            double drift = effectiveDifficulty(q) - q.getDifficulty();
            if (Math.abs(drift) >= FLAG_DRIFT) {
                flagged.add(String.format("%s: labeled difficulty %d but measures %.1f "
                                + "(%d attempts, %.0f%% pass)",
                        q.getId(), q.getDifficulty(), effectiveDifficulty(q),
                        s.attempts(), s.passRate() * 100));
            }
        }
        return flagged;
    }
}
