package com.studyprogram.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentProfileTest {

    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        profile = new StudentProfile("Alice");
    }

    private static Question makeQuestion(Topic topic) {
        return Question.builder()
                .id("test-" + topic.name())
                .topic(topic)
                .type(QuestionType.TRACING)
                .difficulty(1)
                .prompt("test")
                .answer("42")
                .build();
    }

    // ── Construction ──────────────────────────────────────────────────────────

    @Test
    void nameIsSet() {
        assertEquals("Alice", profile.getName());
    }

    @Test
    void idIsGeneratedOnConstruction() {
        assertNotNull(profile.getId());
        assertFalse(profile.getId().isBlank());
    }

    @Test
    void twoProfilesHaveDifferentIds() {
        StudentProfile p2 = new StudentProfile("Bob");
        assertNotEquals(profile.getId(), p2.getId());
    }

    @Test
    void createdAtIsSetOnConstruction() {
        assertNotNull(profile.getCreatedAt());
    }

    @Test
    void initialTotalsAreZero() {
        assertEquals(0, profile.getTotalQuestionsAnswered());
        assertEquals(0, profile.getTotalCorrect());
    }

    @Test
    void initialOverallAccuracyIsZero() {
        assertEquals(0.0, profile.getOverallAccuracy());
    }

    @Test
    void initialSelectedTopicsIsEmpty() {
        assertTrue(profile.getSelectedTopics().isEmpty());
    }

    // ── recordAnswer ─────────────────────────────────────────────────────────

    @Test
    void recordCorrectIncrementsBothCounters() {
        profile.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        assertEquals(1, profile.getTotalQuestionsAnswered());
        assertEquals(1, profile.getTotalCorrect());
    }

    @Test
    void recordWrongIncrementsOnlyTotalAnswered() {
        profile.recordAnswer(makeQuestion(Topic.VARIABLES), false);
        assertEquals(1, profile.getTotalQuestionsAnswered());
        assertEquals(0, profile.getTotalCorrect());
    }

    @Test
    void recordAnswerSetsLastStudied() {
        assertNull(profile.getLastStudied());
        profile.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        assertNotNull(profile.getLastStudied());
    }

    @Test
    void recordAnswerDelegatesToTopicPerformance() {
        Question q = makeQuestion(Topic.VARIABLES);
        profile.recordAnswer(q, true);
        TopicPerformance p = profile.getPerformance().get(Topic.VARIABLES);
        assertNotNull(p);
        assertEquals(1, p.getAttempts());
        assertEquals(1, p.getCorrect());
    }

    @Test
    void multipleTopicsTrackedIndependently() {
        profile.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        profile.recordAnswer(makeQuestion(Topic.PRINTING), false);

        assertEquals(1, profile.getPerformance().get(Topic.VARIABLES).getCorrect());
        assertEquals(0, profile.getPerformance().get(Topic.PRINTING).getCorrect());
    }

    // ── getOverallAccuracy ────────────────────────────────────────────────────

    @Test
    void overallAccuracy50Percent() {
        profile.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        profile.recordAnswer(makeQuestion(Topic.PRINTING), false);
        assertEquals(0.5, profile.getOverallAccuracy(), 1e-9);
    }

    @Test
    void overallAccuracy100Percent() {
        profile.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        profile.recordAnswer(makeQuestion(Topic.PRINTING), true);
        assertEquals(1.0, profile.getOverallAccuracy(), 1e-9);
    }

    // ── getOrCreatePerformance ────────────────────────────────────────────────

    @Test
    void getOrCreateReturnsExistingRecord() {
        profile.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        TopicPerformance first  = profile.getOrCreatePerformance(Topic.VARIABLES);
        TopicPerformance second = profile.getOrCreatePerformance(Topic.VARIABLES);
        assertSame(first, second, "Should return the same object, not create a new one");
    }

    @Test
    void getOrCreateDoesNotCreateForUnseenTopicWhenUsingDirectGet() {
        // Direct performance map access must not have phantom entries
        assertNull(profile.getPerformance().get(Topic.METAPROGRAMMING));
    }

    @Test
    void getOrCreateAddsEntryForNewTopic() {
        TopicPerformance p = profile.getOrCreatePerformance(Topic.LOOPS);
        assertNotNull(p);
        assertEquals(Topic.LOOPS, p.getTopic());
    }

    // ── getSelectedTopicsList ────────────────────────────────────────────────

    @Test
    void selectedTopicsListIsCopy() {
        profile.getSelectedTopics().add(Topic.VARIABLES);
        List<Topic> list = profile.getSelectedTopicsList();
        list.add(Topic.PRINTING);   // should not affect the profile
        assertFalse(profile.getSelectedTopics().contains(Topic.PRINTING));
    }
}
