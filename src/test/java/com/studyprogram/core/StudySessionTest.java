package com.studyprogram.core;

import com.studyprogram.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StudySessionTest {

    private QuestionBank bank;
    private AdaptiveEngine engine;
    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        bank    = new QuestionBank();
        engine  = new AdaptiveEngine(bank);
        profile = new StudentProfile("Tester");
    }

    private StudySession session(List<Topic> topics, int limit) {
        return new StudySession(profile, topics, engine, limit);
    }

    // ── nextQuestion ─────────────────────────────────────────────────────────

    @Test
    void nextQuestionReturnsPresentWhenQuestionsExist() {
        StudySession s = session(List.of(Topic.VARIABLES), 5);
        assertTrue(s.nextQuestion().isPresent());
    }

    @Test
    void nextQuestionReturnsEmptyWhenTargetReached() {
        StudySession s = session(List.of(Topic.VARIABLES), 2);
        Optional<Question> q1 = s.nextQuestion();
        s.recordAnswer(q1.get(), GradingResult.correct(""));
        Optional<Question> q2 = s.nextQuestion();
        s.recordAnswer(q2.get(), GradingResult.correct(""));

        assertTrue(s.nextQuestion().isEmpty(),
                "Session should return empty after reaching the target question count");
    }

    @Test
    void sessionMarkedFinishedAfterTargetReached() {
        StudySession s = session(List.of(Topic.VARIABLES), 1);
        Optional<Question> q = s.nextQuestion();
        s.recordAnswer(q.get(), GradingResult.correct(""));
        s.nextQuestion(); // triggers finished = true
        assertTrue(s.isFinished());
    }

    @Test
    void nextQuestionReturnsEmptyWhenNoTopicsSelected() {
        StudySession s = session(List.of(), 10);
        assertTrue(s.nextQuestion().isEmpty());
    }

    @Test
    void sessionMarkedFinishedWhenNoQuestionsAvailable() {
        StudySession s = session(List.of(), 10);
        s.nextQuestion();
        assertTrue(s.isFinished(),
                "Session must be marked finished when engine returns empty (bug fix)");
    }

    @Test
    void subsequentCallsAfterFinishedReturnEmpty() {
        StudySession s = session(List.of(Topic.VARIABLES), 1);
        s.nextQuestion();
        s.recordAnswer(bank.getQuestionsForTopic(Topic.VARIABLES).get(0), GradingResult.correct(""));
        s.nextQuestion(); // sets finished
        // All further calls must respect finished flag
        assertTrue(s.nextQuestion().isEmpty());
        assertTrue(s.nextQuestion().isEmpty());
    }

    @Test
    void unlimitedSessionDoesNotStopAtArbitraryLimit() {
        StudySession s = session(List.of(Topic.VARIABLES), 0);
        for (int i = 0; i < 5; i++) {
            Optional<Question> q = s.nextQuestion();
            if (q.isPresent()) s.recordAnswer(q.get(), GradingResult.correct(""));
        }
        // With targetQuestions == 0, the limit condition is never triggered
        assertFalse(s.isFinished(),
                "targetQuestions=0 means unlimited — session should not be finished after 5 questions");
    }

    // ── recordAnswer ─────────────────────────────────────────────────────────

    @Test
    void recordAnswerIncrementsQuestionsAnswered() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        Question q = s.nextQuestion().get();
        s.recordAnswer(q, GradingResult.correct(""));
        assertEquals(1, s.questionsAnswered());
    }

    @Test
    void recordAnswerTracksCorrectCount() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        Question q1 = s.nextQuestion().get();
        s.recordAnswer(q1, GradingResult.correct(""));

        Optional<Question> next = s.nextQuestion();
        if (next.isPresent()) {
            s.recordAnswer(next.get(), GradingResult.incorrect("no", ""));
        }

        assertEquals(1, s.correctAnswers());
    }

    @Test
    void recordAnswerUpdatesStudentProfile() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        Question q = s.nextQuestion().get();
        s.recordAnswer(q, GradingResult.correct(""));

        assertEquals(1, profile.getTotalQuestionsAnswered());
        assertEquals(1, profile.getTotalCorrect());
    }

    // ── sessionAccuracy ───────────────────────────────────────────────────────

    @Test
    void sessionAccuracyZeroWithNoAnswers() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        assertEquals(0.0, s.sessionAccuracy());
    }

    @Test
    void sessionAccuracy100PercentAllCorrect() {
        StudySession s = session(List.of(Topic.VARIABLES), 3);
        for (int i = 0; i < 3; i++) {
            Optional<Question> q = s.nextQuestion();
            if (q.isPresent()) s.recordAnswer(q.get(), GradingResult.correct(""));
        }
        assertEquals(1.0, s.sessionAccuracy(), 1e-9);
    }

    @Test
    void sessionAccuracy50PercentMixed() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        Optional<Question> q1 = s.nextQuestion();
        if (q1.isPresent()) s.recordAnswer(q1.get(), GradingResult.correct(""));
        Optional<Question> q2 = s.nextQuestion();
        if (q2.isPresent()) s.recordAnswer(q2.get(), GradingResult.incorrect("no", ""));
        assertEquals(0.5, s.sessionAccuracy(), 1e-9);
    }

    // ── topicBreakdown ────────────────────────────────────────────────────────

    @Test
    void topicBreakdownEmptyWithNoAnswers() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        assertTrue(s.topicBreakdown().isEmpty());
    }

    @Test
    void topicBreakdownCountsCorrectly() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        Optional<Question> q1 = s.nextQuestion();
        if (q1.isPresent()) s.recordAnswer(q1.get(), GradingResult.correct(""));
        Optional<Question> q2 = s.nextQuestion();
        if (q2.isPresent()) s.recordAnswer(q2.get(), GradingResult.incorrect("no", ""));

        Map<Topic, long[]> bd = s.topicBreakdown();
        if (bd.containsKey(Topic.VARIABLES)) {
            long[] counts = bd.get(Topic.VARIABLES);
            assertEquals(2, counts[0], "total count");
            assertEquals(1, counts[1], "correct count");
        }
    }

    // ── elapsed ───────────────────────────────────────────────────────────────

    @Test
    void elapsedIsNonNegative() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        assertTrue(s.elapsed().toSeconds() >= 0);
    }

    // ── getActiveTopics ───────────────────────────────────────────────────────

    @Test
    void activeTopicsReturnsConfiguredTopics() {
        StudySession s = session(List.of(Topic.VARIABLES, Topic.PRINTING), 10);
        assertEquals(List.of(Topic.VARIABLES, Topic.PRINTING), s.getActiveTopics());
    }

    @Test
    void activeTopicsListIsUnmodifiable() {
        StudySession s = session(List.of(Topic.VARIABLES), 10);
        assertThrows(UnsupportedOperationException.class, () -> s.getActiveTopics().add(Topic.LOOPS));
    }
}
