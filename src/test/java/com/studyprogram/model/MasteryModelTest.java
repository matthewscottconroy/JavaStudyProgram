package com.studyprogram.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Difficulty weighting and time decay of the mastery score. */
class MasteryModelTest {

    @Test
    void hardCorrectEarnsMoreThanEasyCorrect() {
        TopicPerformance easy = new TopicPerformance(Topic.LOOPS);
        TopicPerformance hard = new TopicPerformance(Topic.LOOPS);
        easy.record("q1", true, 1);
        hard.record("q1", true, 5);
        assertTrue(hard.getMasteryScore() > easy.getMasteryScore());
        assertEquals(0.07, easy.getMasteryScore(), 1e-9);
        assertEquals(0.15, hard.getMasteryScore(), 1e-9);
    }

    @Test
    void easyMissCostsMoreThanHardMiss() {
        TopicPerformance easy = new TopicPerformance(Topic.LOOPS);
        TopicPerformance hard = new TopicPerformance(Topic.LOOPS);
        easy.setMasteryScore(0.5);
        hard.setMasteryScore(0.5);
        easy.record("q1", false, 1);
        hard.record("q1", false, 5);
        assertTrue(easy.getMasteryScore() < hard.getMasteryScore());
        assertEquals(0.42, easy.getMasteryScore(), 1e-9);
        assertEquals(0.46, hard.getMasteryScore(), 1e-9);
    }

    @Test
    void flatRecordKeepsLegacyDeltas() {
        TopicPerformance perf = new TopicPerformance(Topic.LOOPS);
        perf.record("q1", true);
        assertEquals(0.10, perf.getMasteryScore(), 1e-9);
    }

    @Test
    void decayReducesStaleMastery() {
        TopicPerformance perf = new TopicPerformance(Topic.LOOPS);
        perf.setMasteryScore(0.8);
        perf.setMasteryUpdatedAt(LocalDateTime.now().minusDays(127));  // 120 past the grace week
        perf.applyDecay(LocalDateTime.now());
        assertEquals(0.4, perf.getMasteryScore(), 0.01);   // one half-life
    }

    @Test
    void recentMasteryDoesNotDecay() {
        TopicPerformance perf = new TopicPerformance(Topic.LOOPS);
        perf.setMasteryScore(0.8);
        perf.setMasteryUpdatedAt(LocalDateTime.now().minusDays(3));
        perf.applyDecay(LocalDateTime.now());
        assertEquals(0.8, perf.getMasteryScore(), 1e-9);
    }

    @Test
    void decayIsStableWhenAppliedRepeatedly() {
        TopicPerformance perf = new TopicPerformance(Topic.LOOPS);
        perf.setMasteryScore(0.8);
        perf.setMasteryUpdatedAt(LocalDateTime.now().minusDays(127));
        perf.applyDecay(LocalDateTime.now());
        double afterOnce = perf.getMasteryScore();
        perf.applyDecay(LocalDateTime.now());   // immediate re-application (fresh grace period)
        assertEquals(afterOnce, perf.getMasteryScore(), 1e-9);
    }

    @Test
    void untouchedTopicIsUnaffectedByDecay() {
        TopicPerformance perf = new TopicPerformance(Topic.LOOPS);
        perf.applyDecay(LocalDateTime.now());
        assertEquals(0.0, perf.getMasteryScore(), 1e-9);
    }
}
