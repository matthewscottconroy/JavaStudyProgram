package com.studyprogram.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicPerformanceTest {

    private TopicPerformance perf;

    @BeforeEach
    void setUp() {
        perf = new TopicPerformance(Topic.VARIABLES);
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    void initialMasteryIsZero() {
        assertEquals(0.0, perf.getMasteryScore());
    }

    @Test
    void initialAttemptsIsZero() {
        assertEquals(0, perf.getAttempts());
    }

    @Test
    void initialCorrectIsZero() {
        assertEquals(0, perf.getCorrect());
    }

    @Test
    void initialAccuracyIsZero() {
        assertEquals(0.0, perf.getAccuracyRate());
    }

    @Test
    void initialSuggestedDifficultyIsOne() {
        assertEquals(1, perf.suggestedDifficulty());
    }

    // ── Correct answer ────────────────────────────────────────────────────────

    @Test
    void correctAnswerIncreasesAttempts() {
        perf.record("q1", true);
        assertEquals(1, perf.getAttempts());
    }

    @Test
    void correctAnswerIncreasesCorrectCount() {
        perf.record("q1", true);
        assertEquals(1, perf.getCorrect());
    }

    @Test
    void correctAnswerIncreasesMastery() {
        perf.record("q1", true);
        assertEquals(0.10, perf.getMasteryScore(), 1e-9);
    }

    @Test
    void masteryCapedAtOne() {
        for (int i = 0; i < 20; i++) perf.record("q" + i, true);
        assertEquals(1.0, perf.getMasteryScore(), 1e-9,
                "10 correct answers should cap mastery at 1.0");
    }

    // ── Wrong answer ──────────────────────────────────────────────────────────

    @Test
    void wrongAnswerIncreasesAttempts() {
        perf.record("q1", false);
        assertEquals(1, perf.getAttempts());
    }

    @Test
    void wrongAnswerDoesNotIncrementCorrect() {
        perf.record("q1", false);
        assertEquals(0, perf.getCorrect());
    }

    @Test
    void masteryDoesNotGoBelowZero() {
        // Mastery starts at 0; a wrong answer should not push it negative
        perf.record("q1", false);
        assertEquals(0.0, perf.getMasteryScore(), 1e-9);
    }

    @Test
    void wrongAnswerDecreasesMasteryFromHighValue() {
        // Prime to 0.5 (5 correct answers)
        for (int i = 0; i < 5; i++) perf.record("q" + i, true);
        double before = perf.getMasteryScore();
        perf.record("wrong", false);
        assertTrue(perf.getMasteryScore() < before);
    }

    // ── Accuracy rate ─────────────────────────────────────────────────────────

    @Test
    void accuracyWith3Correct1Wrong() {
        perf.record("q1", true);
        perf.record("q2", true);
        perf.record("q3", true);
        perf.record("q4", false);
        assertEquals(0.75, perf.getAccuracyRate(), 1e-9);
    }

    @Test
    void accuracyAllWrong() {
        perf.record("q1", false);
        perf.record("q2", false);
        assertEquals(0.0, perf.getAccuracyRate(), 1e-9);
    }

    // ── Suggested difficulty ──────────────────────────────────────────────────

    @Test
    void suggestedDifficultyScalesWithMastery() {
        // 0 correct → difficulty 1
        assertEquals(1, perf.suggestedDifficulty());

        // 5 correct (mastery 0.5) → difficulty 3
        for (int i = 0; i < 5; i++) perf.record("q" + i, true);
        assertEquals(3, perf.suggestedDifficulty());
    }

    @Test
    void suggestedDifficultyMaxFiveAtFullMastery() {
        for (int i = 0; i < 10; i++) perf.record("q" + i, true);
        assertEquals(5, perf.suggestedDifficulty());
    }

    @Test
    void suggestedDifficultyMinOneAtZeroMastery() {
        assertTrue(perf.suggestedDifficulty() >= 1);
    }

    // ── Spaced-repetition (recently seen) ─────────────────────────────────────

    @Test
    void questionNotRecentlySeenInitially() {
        assertFalse(perf.wasRecentlySeen("q1"));
    }

    @Test
    void questionIsRecentlySeenAfterRecord() {
        perf.record("q1", true);
        assertTrue(perf.wasRecentlySeen("q1"));
    }

    @Test
    void recentlyAnsweredCapAt20() {
        for (int i = 0; i < 25; i++) perf.record("q" + i, true);
        // First question should have been evicted from the deque
        assertFalse(perf.wasRecentlySeen("q0"),
                "Oldest entries should be evicted once the deque hits the cap of 20");
        // Most recent should still be present
        assertTrue(perf.wasRecentlySeen("q24"));
    }

    @Test
    void differentQuestionsTrackedSeparately() {
        perf.record("q1", true);
        assertFalse(perf.wasRecentlySeen("q2"));
    }

    // ── Timestamp ────────────────────────────────────────────────────────────

    @Test
    void lastAttemptedSetOnRecord() {
        assertNull(perf.getLastAttempted());
        perf.record("q1", true);
        assertNotNull(perf.getLastAttempted());
    }

    @Test
    void lastCorrectSetOnlyForCorrectAnswers() {
        assertNull(perf.getLastCorrect());
        perf.record("q1", false);
        assertNull(perf.getLastCorrect());
        perf.record("q2", true);
        assertNotNull(perf.getLastCorrect());
    }
}
