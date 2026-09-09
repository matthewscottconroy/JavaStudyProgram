package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class QuestionCalibrationTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 9, 0);

    private Question question(String id, int difficulty) {
        return Question.builder()
                .id(id).topic(Topic.LOOPS).type(QuestionType.TRACING).difficulty(difficulty)
                .prompt("p").answer("a").build();
    }

    /** Round-robins a run of outcomes across N students, one minute apart. */
    private Map<String, List<AttemptRecord>> history(Question q, boolean[] outcomes, int students) {
        Map<String, List<AttemptRecord>> byStudent = new LinkedHashMap<>();
        for (int i = 0; i < students; i++) byStudent.put("s" + i, new ArrayList<>());
        for (int n = 0; n < outcomes.length; n++) {
            byStudent.get("s" + (n % students)).add(new AttemptRecord(
                    T0.plusMinutes(n), q,
                    outcomes[n] ? AttemptRecord.OUTCOME_CORRECT : AttemptRecord.OUTCOME_INCORRECT,
                    30, 0));
        }
        return byStudent;
    }

    private static boolean[] repeat(boolean value, int times) {
        boolean[] all = new boolean[times];
        Arrays.fill(all, value);
        return all;
    }

    @Test
    void noDataKeepsAuthoredDifficultyExactly() {
        assertEquals(3.0, QuestionCalibration.none().effectiveDifficulty(question("q", 3)), 1e-9);
    }

    @Test
    void aQuestionEveryoneSolvesMeasuresEasierThanItsLabel() {
        Question q = question("easy-actually", 5);
        var cal = QuestionCalibration.fromProfiles(history(q, repeat(true, 30), 6));
        assertEquals(2.70, cal.effectiveDifficulty(q), 0.02);
        assertTrue(cal.effectiveDifficulty(q) < 5.0);
    }

    @Test
    void aQuestionEveryoneMissesMeasuresHarderThanItsLabel() {
        Question q = question("hard-actually", 1);
        var cal = QuestionCalibration.fromProfiles(history(q, repeat(false, 30), 6));
        assertEquals(3.30, cal.effectiveDifficulty(q), 0.02);
        assertTrue(cal.effectiveDifficulty(q) > 1.0);
    }

    @Test
    void oneAttemptBarelyMovesTheEstimate() {
        Question q = question("q", 3);
        var cal = QuestionCalibration.fromProfiles(history(q, new boolean[] {false}, 1));
        assertEquals(3.075, cal.effectiveDifficulty(q), 1e-3);
    }

    @Test
    void mixedResultsLeaveTheLabelAlone() {
        Question q = question("q", 3);
        boolean[] alternating = new boolean[30];
        for (int i = 0; i < 30; i++) alternating[i] = i % 2 == 0;
        var cal = QuestionCalibration.fromProfiles(history(q, alternating, 6));
        assertEquals(3.0, cal.effectiveDifficulty(q), 0.2);
    }

    /** The reason for the model: pass rate alone cannot tell "easy" from "attempted by strong students". */
    @Test
    void strongStudentsPassingDoesNotMakeAQuestionLookEasy() {
        Question easyWarmup = question("warmup", 1);
        Question target = question("target", 4);

        // One student proves they are strong on 40 easy items, then passes the target 12 times
        List<AttemptRecord> strong = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            strong.add(new AttemptRecord(T0.plusMinutes(i), easyWarmup,
                    AttemptRecord.OUTCOME_CORRECT, 10, 0));
        }
        for (int i = 0; i < 12; i++) {
            strong.add(new AttemptRecord(T0.plusMinutes(100 + i), target,
                    AttemptRecord.OUTCOME_CORRECT, 10, 0));
        }
        double afterStrong = QuestionCalibration.fromProfiles(Map.of("strong", strong))
                .effectiveDifficulty(target);

        // An unproven student passing the same 12 times is stronger evidence the question is easy
        double afterAverage = QuestionCalibration
                .fromProfiles(history(target, repeat(true, 12), 1))
                .effectiveDifficulty(target);

        assertTrue(afterStrong > afterAverage,
                "identical pass counts must not measure the same when abilities differ: "
                        + afterStrong + " vs " + afterAverage);
        assertTrue(afterStrong > 3.4,
                "a strong student's passes should barely move a hard question");
    }

    @Test
    void studentAbilityIsTracked() {
        Question q = question("q", 3);
        var cal = QuestionCalibration.fromProfiles(history(q, repeat(true, 10), 1));
        assertTrue(cal.abilityOf("s0") > 0, "answering correctly raises ability");
        assertEquals(0.0, cal.abilityOf("nobody"), 1e-9);
    }

    @Test
    void skipsAreNotEvidence() {
        Question q = question("q", 3);
        var records = List.of(new AttemptRecord(T0, q, AttemptRecord.OUTCOME_SKIPPED, 5, 0));
        var cal = QuestionCalibration.fromRecords(records);
        assertEquals(3.0, cal.effectiveDifficulty(q), 1e-9);
        assertNull(cal.statsFor("q"));
    }

    @Test
    void statsCarryEvidenceAwareConfidence() {
        Question q = question("q", 3);
        var cal = QuestionCalibration.fromProfiles(history(q, repeat(true, 12), 3));
        var stats = cal.statsFor("q");
        assertEquals(12, stats.attempts());
        assertEquals(12, stats.correct());
        assertEquals(1.0, stats.passRate(), 1e-9);
        assertTrue(stats.confidenceLabel().contains("n=12"));
    }

    @Test
    void driftedQuestionsAreFlaggedWithEnoughEvidence() {
        Question drifted = question("mislabeled", 5);
        Question fine    = question("fine", 3);
        Map<String, List<AttemptRecord>> all = new LinkedHashMap<>(history(drifted, repeat(true, 30), 6));
        history(fine, alternating(30), 6).forEach((k, v) -> all.merge(k, v, (a, b) -> {
            List<AttemptRecord> merged = new ArrayList<>(a);
            merged.addAll(b);
            return merged;
        }));

        List<String> flagged = QuestionCalibration.fromProfiles(all)
                .flaggedForReview(List.of(drifted, fine));
        assertEquals(1, flagged.size(), flagged.toString());
        assertTrue(flagged.get(0).startsWith("mislabeled:"));
        assertTrue(flagged.get(0).contains("confidence"), "flags carry an evidence label");
    }

    private static boolean[] alternating(int n) {
        boolean[] all = new boolean[n];
        for (int i = 0; i < n; i++) all[i] = i % 2 == 0;
        return all;
    }

    @Test
    void flaggingRequiresEnoughAttempts() {
        Question q = question("q", 5);
        var cal = QuestionCalibration.fromProfiles(history(q, repeat(true, 5), 5));
        assertTrue(cal.flaggedForReview(List.of(q)).isEmpty(),
                "5 attempts is not enough evidence to flag a question");
    }
}
