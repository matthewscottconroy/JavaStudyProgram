package com.studyprogram.grading;

import com.studyprogram.llm.LLMService;
import com.studyprogram.llm.NullLLMService;
import com.studyprogram.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CompositeGraderTest {

    private CompositeGrader graderNoLLM;
    private CompositeGrader graderWithLLM;

    @BeforeEach
    void setUp() {
        graderNoLLM   = new CompositeGrader(new NullLLMService());
        graderWithLLM = new CompositeGrader(new StubLLMService("AI says: review this concept"));
    }

    private static Question mcQ(String answer) {
        return Question.builder()
                .topic(Topic.VARIABLES)
                .type(QuestionType.MULTIPLE_CHOICE)
                .difficulty(1)
                .prompt("Pick one")
                .choices("Opt A", "Opt B", "Opt C", "Opt D")
                .answer(answer)
                .explanation("Explanation.")
                .build();
    }

    private static Question traceQ(String answer) {
        return Question.builder()
                .topic(Topic.VARIABLES)
                .type(QuestionType.TRACING)
                .difficulty(1)
                .prompt("Trace this")
                .answer(answer)
                .explanation("Trace explanation.")
                .build();
    }

    // ── Routing ───────────────────────────────────────────────────────────────

    @Test
    void routesToMCGraderForMCQuestion() {
        GradingResult r = graderNoLLM.grade(mcQ("a"), "A");
        assertTrue(r.correct());
    }

    @Test
    void routesToExactMatchGraderForTraceQuestion() {
        GradingResult r = graderNoLLM.grade(traceQ("42"), "42");
        assertTrue(r.correct());
    }

    // ── LLM augmentation ─────────────────────────────────────────────────────

    @Test
    void noLLMFeedbackWhenAnswerIsCorrect() {
        GradingResult r = graderWithLLM.grade(mcQ("a"), "A");
        assertFalse(r.hasLLMFeedback(),
                "LLM should not be called for correct answers");
    }

    @Test
    void noLLMFeedbackWhenLLMUnavailable() {
        GradingResult r = graderNoLLM.grade(mcQ("a"), "B");
        assertFalse(r.correct());
        assertFalse(r.hasLLMFeedback(),
                "No LLM feedback expected when LLM service is NullLLMService");
    }

    @Test
    void llmFeedbackAddedOnWrongAnswerWhenLLMAvailable() {
        GradingResult r = graderWithLLM.grade(mcQ("a"), "B");
        assertFalse(r.correct());
        assertTrue(r.hasLLMFeedback(),
                "LLM feedback should be added for wrong answers when LLM is available");
        assertEquals("AI says: review this concept", r.llmFeedback());
    }

    // ── Stub LLM for testing ──────────────────────────────────────────────────

    private static class StubLLMService implements LLMService {
        private final String feedback;

        StubLLMService(String feedback) { this.feedback = feedback; }

        @Override public boolean isAvailable()                                  { return true; }
        @Override public String explainAnswer(Question q, String answer)        { return feedback; }
        @Override public String generateHint(Question q)                        { return "hint"; }
        @Override public String explainConcept(Topic t, String concept)         { return "concept"; }
        @Override public Optional<Question> generateQuestion(Topic t, QuestionType qt, int d) {
            return Optional.empty();
        }
    }
}
