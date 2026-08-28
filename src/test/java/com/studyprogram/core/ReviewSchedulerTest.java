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
    void streakDoublesTheInterval() {
        LocalDateTime base = LocalDateTime.now().minusDays(4);
        // three consecutive corrects -> streak 3 -> 6 day interval
        ReviewScheduler s = ReviewScheduler.fromRecords(List.of(
                attempt(AttemptRecord.OUTCOME_CORRECT, base),
                attempt(AttemptRecord.OUTCOME_CORRECT, base.plusHours(1)),
                attempt(AttemptRecord.OUTCOME_CORRECT, base.plusHours(2))));
        assertFalse(s.isDue("q1", LocalDateTime.now()), "4 days < 6 day interval");
        assertTrue(s.isDue("q1", LocalDateTime.now().plusDays(3)));
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
}
