package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionCalibrationTest {

    private Question question(String id, int difficulty) {
        return Question.builder()
                .id(id).topic(Topic.LOOPS).type(QuestionType.TRACING).difficulty(difficulty)
                .prompt("p").answer("a").build();
    }

    private List<AttemptRecord> attempts(Question q, int correct, int wrong) {
        List<AttemptRecord> records = new ArrayList<>();
        for (int i = 0; i < correct; i++) {
            records.add(new AttemptRecord(LocalDateTime.now(), q, AttemptRecord.OUTCOME_CORRECT, 10, 0));
        }
        for (int i = 0; i < wrong; i++) {
            records.add(new AttemptRecord(LocalDateTime.now(), q, AttemptRecord.OUTCOME_INCORRECT, 10, 0));
        }
        return records;
    }

    @Test
    void noDataKeepsAuthoredDifficulty() {
        assertEquals(3.0, QuestionCalibration.none().effectiveDifficulty(question("q", 3)), 1e-9);
    }

    @Test
    void everyonePassingPullsDifficultyDown() {
        Question q = question("easy-actually", 5);
        QuestionCalibration cal = QuestionCalibration.fromRecords(attempts(q, 30, 0));
        double eff = cal.effectiveDifficulty(q);
        assertTrue(eff < 3.0, "30/30 pass on a 'difficulty 5' should pull it far down, got " + eff);
        assertTrue(eff >= 1.0);
    }

    @Test
    void everyoneFailingPullsDifficultyUp() {
        Question q = question("hard-actually", 1);
        QuestionCalibration cal = QuestionCalibration.fromRecords(attempts(q, 0, 30));
        assertTrue(cal.effectiveDifficulty(q) > 3.0);
    }

    @Test
    void fewAttemptsStayNearThePrior() {
        Question q = question("q", 3);
        QuestionCalibration cal = QuestionCalibration.fromRecords(attempts(q, 0, 1));
        // one failure against prior weight 10 barely moves the estimate
        assertEquals(3.18, cal.effectiveDifficulty(q), 0.01);
    }

    @Test
    void skipsAreNotEvidence() {
        Question q = question("q", 3);
        List<AttemptRecord> records = List.of(
                new AttemptRecord(LocalDateTime.now(), q, AttemptRecord.OUTCOME_SKIPPED, 5, 0));
        QuestionCalibration cal = QuestionCalibration.fromRecords(records);
        assertEquals(3.0, cal.effectiveDifficulty(q), 1e-9);
        assertNull(cal.statsFor("q"));
    }

    @Test
    void driftedQuestionsAreFlagged() {
        Question drifted = question("mislabeled", 5);
        Question fine    = question("fine", 3);
        List<AttemptRecord> records = new ArrayList<>(attempts(drifted, 30, 0));
        records.addAll(attempts(fine, 15, 15));
        QuestionCalibration cal = QuestionCalibration.fromRecords(records);

        List<String> flagged = cal.flaggedForReview(List.of(drifted, fine));
        assertEquals(1, flagged.size());
        assertTrue(flagged.get(0).startsWith("mislabeled:"));
    }

    @Test
    void flaggingRequiresEnoughAttempts() {
        Question q = question("q", 5);
        QuestionCalibration cal = QuestionCalibration.fromRecords(attempts(q, 5, 0));
        assertTrue(cal.flaggedForReview(List.of(q)).isEmpty(),
                "5 attempts is not enough evidence to flag");
    }
}
