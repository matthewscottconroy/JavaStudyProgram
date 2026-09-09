package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.stats.Confidence;

import java.util.*;

/**
 * Empirical question calibration from attempt logs, using an Elo-style latent-trait model.
 *
 * <p>Authored difficulty is a guess; students are the measurement. A raw pass rate is a biased
 * measurement though — a genuinely hard question attempted mostly by strong students looks easy.
 * So each <em>student</em> carries an ability rating and each <em>question</em> a difficulty
 * rating, both on a logit scale, and every attempt updates both:
 *
 * <pre>{@code P(correct) = 1 / (1 + exp(-(ability - difficulty)))}</pre>
 *
 * <p>A question only drifts from its authored label when students do better or worse than their
 * ability predicts, so "everyone who tried it was strong" no longer makes a question look easy.
 * Question ratings move slower than student ratings because many students share one question.
 *
 * <p>Ratings start at the authored difficulty (1–5 mapped to −2…+2 logits), so an unattempted
 * question keeps exactly its label and the model degrades gracefully to "trust the author".
 */
public class QuestionCalibration {

    /** How fast a student's ability rating moves per attempt. */
    private static final double K_STUDENT = 0.40;
    /** How fast a question's difficulty rating moves per attempt (slower: shared across students). */
    private static final double K_QUESTION = 0.15;
    /** Attempts needed before a question can be flagged for content review. */
    private static final int FLAG_MIN_ATTEMPTS = 10;
    /** How far measured difficulty must drift from the label to be worth a look. */
    private static final double FLAG_DRIFT = 1.25;

    public record Stats(int attempts, int correct, long totalSeconds, int hintsUsed,
                        double measuredDifficulty) {
        public double passRate() { return attempts == 0 ? 0.0 : (double) correct / attempts; }
        /** Evidence-aware description of the pass rate. */
        public String confidenceLabel() { return Confidence.label(correct, attempts); }
    }

    private final Map<String, Stats> byQuestion;
    private final Map<String, Double> abilityByStudent;

    private QuestionCalibration(Map<String, Stats> byQuestion, Map<String, Double> ability) {
        this.byQuestion = byQuestion;
        this.abilityByStudent = ability;
    }

    /** Empty calibration: every question keeps its authored difficulty. */
    public static QuestionCalibration none() {
        return new QuestionCalibration(Map.of(), Map.of());
    }

    /** Calibration from one student's history (the common single-machine case). */
    public static QuestionCalibration fromRecords(Collection<AttemptRecord> records) {
        return fromProfiles(Map.of("student", List.copyOf(records)));
    }

    /**
     * Calibration from several students' histories — the classroom case, and the one where the
     * ability adjustment matters. Each student's records should be in chronological order.
     */
    public static QuestionCalibration fromProfiles(Map<String, List<AttemptRecord>> byStudent) {
        Map<String, double[]> question = new HashMap<>();   // id -> {rating, attempts, correct, secs, hints}
        Map<String, Double> ability = new HashMap<>();

        // Interleave students by timestamp so shared questions see a realistic mix of abilities
        List<Map.Entry<String, AttemptRecord>> timeline = new ArrayList<>();
        byStudent.forEach((student, records) -> records.stream()
                .filter(r -> r.isAnswered() && r.getQuestionId() != null)
                .forEach(r -> timeline.add(Map.entry(student, r))));
        timeline.sort(Comparator.comparing(e -> e.getValue().getTs(),
                Comparator.nullsFirst(Comparator.naturalOrder())));

        for (var entry : timeline) {
            String student = entry.getKey();
            AttemptRecord r = entry.getValue();
            double[] q = question.computeIfAbsent(r.getQuestionId(),
                    id -> new double[] {authoredRating(r.getDifficulty()), 0, 0, 0, 0});
            double a = ability.getOrDefault(student, 0.0);

            double expected = 1.0 / (1.0 + Math.exp(-(a - q[0])));
            double actual = r.isCorrect() ? 1.0 : 0.0;
            ability.put(student, a + K_STUDENT * (actual - expected));
            q[0] -= K_QUESTION * (actual - expected);

            q[1]++;
            if (r.isCorrect()) q[2]++;
            q[3] += Math.max(0, r.getSeconds());
            q[4] += Math.max(0, r.getHintsUsed());
        }

        Map<String, Stats> stats = new HashMap<>();
        question.forEach((id, q) -> stats.put(id, new Stats(
                (int) q[1], (int) q[2], (long) q[3], (int) q[4], ratingToDifficulty(q[0]))));
        return new QuestionCalibration(stats, ability);
    }

    public Stats statsFor(String questionId) {
        return byQuestion.get(questionId);
    }

    /** Estimated ability of a student on the logit scale (0 is average); 0 when unknown. */
    public double abilityOf(String student) {
        return abilityByStudent.getOrDefault(student, 0.0);
    }

    /**
     * The difficulty the engine should match against: the measured rating once there is
     * evidence, and exactly the authored label when there is none.
     */
    public double effectiveDifficulty(Question q) {
        Stats s = byQuestion.get(q.getId());
        return (s == null || s.attempts() == 0) ? q.getDifficulty() : s.measuredDifficulty();
    }

    /**
     * Questions whose measured difficulty has drifted far from the authored label — candidates
     * for rewording, re-labelling, or removal.
     */
    public List<String> flaggedForReview(Collection<Question> questions) {
        List<String> flagged = new ArrayList<>();
        for (Question q : questions) {
            Stats s = byQuestion.get(q.getId());
            if (s == null || s.attempts() < FLAG_MIN_ATTEMPTS) continue;
            double drift = effectiveDifficulty(q) - q.getDifficulty();
            if (Math.abs(drift) >= FLAG_DRIFT) {
                flagged.add(String.format("%s: labeled difficulty %d but measures %.1f — %s",
                        q.getId(), q.getDifficulty(), effectiveDifficulty(q), s.confidenceLabel()));
            }
        }
        return flagged;
    }

    private static double authoredRating(int difficulty) {
        return Math.max(1, Math.min(5, difficulty)) - 3.0;    // 1..5 -> -2..+2 logits
    }

    private static double ratingToDifficulty(double rating) {
        return Math.max(1.0, Math.min(5.0, rating + 3.0));
    }
}
