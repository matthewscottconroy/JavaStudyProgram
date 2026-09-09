package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewSchedulerTest {

    private final Question q = Question.builder()
            .id("q1").topic(Topic.LOOPS).type(QuestionType.TRACING).difficulty(2)
            .prompt("p").answer("a").build();

    private AttemptRecord attempt(String outcome, LocalDateTime ts) {
        return new AttemptRecord(ts, q, outcome, 10, 0);
    }

    @Test
    void unseenQuestionsAreNeutral() {
        ReviewScheduler s = ReviewScheduler.fromRecords(List.of());
        assertFalse(s.seen("q1"));
        assertEquals(0.0, s.scheduleBonus("q1", LocalDateTime.now()), 1e-9);
    }

    @Test
    void justAnsweredCorrectlyIsNotDueAndPenalized() {
        ReviewScheduler s = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_CORRECT, LocalDateTime.now().minusHours(2))));
        assertFalse(s.isDue("q1", LocalDateTime.now()));
        assertTrue(s.scheduleBonus("q1", LocalDateTime.now()) < 0);
    }

    @Test
    void becomesDueAfterTheInterval() {
        // one correct answer -> 1.5 day interval
        ReviewScheduler s = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_CORRECT, LocalDateTime.now().minusDays(2))));
        assertTrue(s.isDue("q1", LocalDateTime.now()));
        assertTrue(s.scheduleBonus("q1", LocalDateTime.now()) > 0);
    }

    @Test
    void eachCorrectAnswerLengthensTheInterval() {
        LocalDateTime base = LocalDateTime.now().minusDays(4);
        // three consecutive corrects -> streak 3, so the interval is several days
        ReviewScheduler s = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_CORRECT, base),
                attempt(AttemptRecord.OUTCOME_CORRECT, base.plusHours(1)),
                attempt(AttemptRecord.OUTCOME_CORRECT, base.plusHours(2))));
        assertFalse(s.isDue("q1", LocalDateTime.now()), "4 days is inside a streak-3 interval");
        assertTrue(s.isDue("q1", LocalDateTime.now().plusDays(10)),
                "but it comes due once the interval elapses");
    }

    @Test
    void missResetsTheStreakToRetrySoon() {
        LocalDateTime base = LocalDateTime.now().minusDays(1);
        ReviewScheduler s = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_CORRECT, base),
                attempt(AttemptRecord.OUTCOME_CORRECT, base.plusHours(1)),
                attempt(AttemptRecord.OUTCOME_INCORRECT, base.plusHours(2))));
        // streak reset -> 0.5 day interval; a day has passed -> due again quickly
        assertTrue(s.isDue("q1", LocalDateTime.now()));
    }

    @Test
    void skipsDoNotAffectScheduling() {
        ReviewScheduler s = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_SKIPPED, LocalDateTime.now().minusHours(1))));
        assertFalse(s.seen("q1"));
    }

    @Test
    void intervalGrowthIsCapped() {
        assertEquals(1.5, ReviewScheduler.intervalDays(1), 1e-9);
        assertEquals(3.0, ReviewScheduler.intervalDays(2), 1e-9);
        assertEquals(6.0, ReviewScheduler.intervalDays(3), 1e-9);
        assertEquals(60.0, ReviewScheduler.intervalDays(10), 1e-9);
    }

    @Test
    void repeatedlyMissedQuestionsGetALowerEaseAndComeBackSooner() {
        LocalDateTime base = LocalDateTime.now().minusDays(1);
        ReviewScheduler forgotten = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_INCORRECT, base),
                attempt(AttemptRecord.OUTCOME_INCORRECT, base.plusMinutes(1)),
                attempt(AttemptRecord.OUTCOME_CORRECT, base.plusMinutes(2))));
        ReviewScheduler solid = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_CORRECT, base.plusMinutes(2))));

        assertTrue(forgotten.easeOf("q1") < solid.easeOf("q1"),
                "material you keep forgetting must be scheduled more aggressively");
        assertTrue(ReviewScheduler.intervalDays(4, forgotten.easeOf("q1"))
                        < ReviewScheduler.intervalDays(4, solid.easeOf("q1")));
    }

    @Test
    void easeStaysWithinItsBounds() {
        LocalDateTime base = LocalDateTime.now().minusDays(30);
        List<AttemptRecord> manyMisses = new java.util.ArrayList<>();
        List<AttemptRecord> manyHits = new java.util.ArrayList<>();
        for (int i = 0; i < 40; i++) {
            manyMisses.add(attempt(AttemptRecord.OUTCOME_INCORRECT, base.plusMinutes(i)));
            manyHits.add(attempt(AttemptRecord.OUTCOME_CORRECT, base.plusMinutes(i)));
        }
        assertTrue(ReviewScheduler.fromRecords(manyMisses).easeOf("q1") >= 1.3);
        assertTrue(ReviewScheduler.fromRecords(manyHits).easeOf("q1") <= 2.6);
    }

    @Test
    void dueDatesAreJitteredSoReviewsDoNotClump() {
        LocalDateTime answeredAt = LocalDateTime.now().minusHours(1);
        // identical histories for differently-named questions must not come due at the same moment
        java.util.Set<Long> dueTimes = new java.util.HashSet<>();
        for (String id : List.of("alpha", "beta", "gamma", "delta", "epsilon")) {
            Question q = Question.builder()
                    .id(id).topic(Topic.LOOPS).type(QuestionType.TRACING).difficulty(2)
                    .prompt("p").answer("a").build();
            ReviewScheduler s = ReviewScheduler.fromRecords(List.of(
                    new AttemptRecord(answeredAt, q, AttemptRecord.OUTCOME_CORRECT, 10, 0)));
            dueTimes.add(Math.round(s.daysUntilDue(id, LocalDateTime.now()) * 1000));
        }
        assertTrue(dueTimes.size() > 1, "jitter must spread identical schedules apart");
    }
}
