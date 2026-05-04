package com.studyprogram.core;

import com.studyprogram.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveEngineTest {

    private QuestionBank bank;
    private AdaptiveEngine engine;
    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        bank    = new QuestionBank();
        engine  = new AdaptiveEngine(bank);
        profile = new StudentProfile("TestStudent");
        profile.setSelectedTopics(EnumSet.of(Topic.VARIABLES, Topic.PRINTING));
    }

    // ── Basic operation ───────────────────────────────────────────────────────

    @Test
    void returnsQuestionForSelectedTopics() {
        Optional<Question> q = engine.nextQuestion(profile, List.of(Topic.VARIABLES), null);
        assertTrue(q.isPresent());
        assertEquals(Topic.VARIABLES, q.get().getTopic());
    }

    @Test
    void returnsEmptyForEmptyTopicSelection() {
        Optional<Question> q = engine.nextQuestion(profile, List.of(), null);
        assertTrue(q.isEmpty());
    }

    @Test
    void returnsEmptyForEmptyTopicList() {
        Optional<Question> q = engine.nextQuestion(profile, List.of(), null);
        assertTrue(q.isEmpty());
    }

    // ── Last-question avoidance ───────────────────────────────────────────────

    @Test
    void avoidsRepeatingLastQuestion() {
        Optional<Question> first = engine.nextQuestion(profile, List.of(Topic.VARIABLES), null);
        assertTrue(first.isPresent());
        String firstId = first.get().getId();

        boolean sawDifferent = false;
        for (int i = 0; i < 20; i++) {
            Optional<Question> next = engine.nextQuestion(profile, List.of(Topic.VARIABLES), firstId);
            if (next.isPresent() && !next.get().getId().equals(firstId)) {
                sawDifferent = true;
                break;
            }
        }
        assertTrue(sawDifferent, "Should occasionally pick a question other than the last one");
    }

    @Test
    void nullLastQuestionIdIsHandledSafely() {
        assertDoesNotThrow(() ->
                engine.nextQuestion(profile, List.of(Topic.VARIABLES), null));
    }

    // ── No phantom performance records (bug fix) ──────────────────────────────

    @Test
    void scoringDoesNotCreatePhantomPerformanceRecords() {
        int before = profile.getPerformance().size();
        engine.nextQuestion(profile, List.of(Topic.VARIABLES), null);
        int after = profile.getPerformance().size();
        assertEquals(before, after,
                "Scoring should not create phantom performance records as a side effect " +
                "(was calling getOrCreatePerformance on every candidate question)");
    }

    // ── Mastery / performance interaction ─────────────────────────────────────

    @Test
    void masteryScoreIncreasesOnCorrectAnswer() {
        Optional<Question> q = engine.nextQuestion(profile, List.of(Topic.VARIABLES), null);
        assertTrue(q.isPresent());
        profile.recordAnswer(q.get(), true);
        double mastery = profile.getOrCreatePerformance(Topic.VARIABLES).getMasteryScore();
        assertTrue(mastery > 0.0);
    }

    @Test
    void masteryScoreDecreasesOnWrongAnswer() {
        profile.getOrCreatePerformance(Topic.VARIABLES).record("fake", true);
        double before = profile.getOrCreatePerformance(Topic.VARIABLES).getMasteryScore();

        Optional<Question> q = engine.nextQuestion(profile, List.of(Topic.VARIABLES), null);
        assertTrue(q.isPresent());
        profile.recordAnswer(q.get(), false);

        double after = profile.getOrCreatePerformance(Topic.VARIABLES).getMasteryScore();
        assertTrue(after < before);
    }

    @Test
    void masteryStaysNonNegative() {
        // Answer many questions wrong
        for (int i = 0; i < 10; i++) {
            Optional<Question> q = engine.nextQuestion(profile, List.of(Topic.VARIABLES), null);
            q.ifPresent(question -> profile.recordAnswer(question, false));
        }
        double mastery = profile.getOrCreatePerformance(Topic.VARIABLES).getMasteryScore();
        assertTrue(mastery >= 0.0);
    }

    // ── Multiple topics ───────────────────────────────────────────────────────

    @Test
    void returnsQuestionsFromAnySelectedTopic() {
        List<Topic> topics = List.of(Topic.VARIABLES, Topic.PRINTING);
        boolean sawVariables = false, sawPrinting = false;

        for (int i = 0; i < 30; i++) {
            Optional<Question> q = engine.nextQuestion(profile, topics, null);
            if (q.isEmpty()) break;
            if (q.get().getTopic() == Topic.VARIABLES) sawVariables = true;
            if (q.get().getTopic() == Topic.PRINTING)  sawPrinting  = true;
            if (sawVariables && sawPrinting) break;
        }

        assertTrue(sawVariables, "Expected at least one VARIABLES question");
        assertTrue(sawPrinting,  "Expected at least one PRINTING question");
    }

    // ── Scoring determinism (edge cases) ─────────────────────────────────────

    @Test
    void stillReturnsQuestionWhenAllScoresAreNegative() {
        // Fill the recently-answered deque and set high mastery — scores will be negative
        TopicPerformance perf = profile.getOrCreatePerformance(Topic.VARIABLES);
        for (int i = 0; i < 20; i++) perf.record("q" + i, true);

        // Should still return a question rather than empty
        Optional<Question> q = engine.nextQuestion(profile, List.of(Topic.VARIABLES), null);
        assertTrue(q.isPresent(),
                "Engine must still return a question even when all scores are low/negative");
    }
}
