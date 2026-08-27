package com.studyprogram.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Concept drill-down: a question can also exercise prerequisite topics. */
class RelatedTopicsTest {

    private Question arraysQuestionUsing(Topic... related) {
        Question.Builder b = Question.builder()
                .id("q1").topic(Topic.ARRAYS).type(QuestionType.TRACING).difficulty(3)
                .prompt("p").answer("a");
        for (Topic t : related) b.relatedTopic(t);
        return b.build();
    }

    @Test
    void missNudgesRelatedTopicsDown() {
        StudentProfile p = new StudentProfile("s");
        p.getOrCreatePerformance(Topic.LOOPS).setMasteryScore(0.5);
        p.getOrCreatePerformance(Topic.LOOPS).setAttempts(5);

        p.recordAnswer(arraysQuestionUsing(Topic.LOOPS), false);

        assertEquals(0.47, p.getPerformance().get(Topic.LOOPS).getMasteryScore(), 1e-9);
        assertEquals(5, p.getPerformance().get(Topic.LOOPS).getAttempts(),
                "indirect signal must not count as a LOOPS attempt");
    }

    @Test
    void correctGivesSmallReinforcement() {
        StudentProfile p = new StudentProfile("s");
        p.getOrCreatePerformance(Topic.LOOPS).setMasteryScore(0.5);
        p.getOrCreatePerformance(Topic.LOOPS).setAttempts(5);

        p.recordAnswer(arraysQuestionUsing(Topic.LOOPS), true);
        assertEquals(0.51, p.getPerformance().get(Topic.LOOPS).getMasteryScore(), 1e-9);
    }

    @Test
    void untouchedRelatedTopicsAreLeftAlone() {
        StudentProfile p = new StudentProfile("s");
        p.recordAnswer(arraysQuestionUsing(Topic.LOOPS), false);
        assertNull(p.getPerformance().get(Topic.LOOPS),
                "no phantom performance record for a topic the student never practiced");
    }
}
