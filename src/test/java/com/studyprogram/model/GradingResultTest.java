package com.studyprogram.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradingResultTest {

    @Test
    void correctFactoryCreatesCorrectResult() {
        GradingResult r = GradingResult.correct("Great explanation.");
        assertTrue(r.correct());
        assertEquals("Correct!", r.feedback());
        assertEquals("Great explanation.", r.explanation());
        assertNull(r.llmFeedback());
    }

    @Test
    void incorrectFactoryCreatesIncorrectResult() {
        GradingResult r = GradingResult.incorrect("Wrong answer.", "Here's why...");
        assertFalse(r.correct());
        assertEquals("Wrong answer.", r.feedback());
        assertEquals("Here's why...", r.explanation());
        assertNull(r.llmFeedback());
    }

    @Test
    void withLLMSetsAllFields() {
        GradingResult r = GradingResult.withLLM(true, "Correct!", "Explanation.", "LLM says...");
        assertTrue(r.correct());
        assertEquals("Correct!", r.feedback());
        assertEquals("Explanation.", r.explanation());
        assertEquals("LLM says...", r.llmFeedback());
    }

    @Test
    void hasLLMFeedbackFalseWhenNull() {
        GradingResult r = GradingResult.correct("expl");
        assertFalse(r.hasLLMFeedback());
    }

    @Test
    void hasLLMFeedbackFalseWhenBlank() {
        GradingResult r = GradingResult.withLLM(true, "ok", "expl", "   ");
        assertFalse(r.hasLLMFeedback());
    }

    @Test
    void hasLLMFeedbackTrueWhenNonEmpty() {
        GradingResult r = GradingResult.withLLM(false, "bad", "expl", "Try thinking about...");
        assertTrue(r.hasLLMFeedback());
    }
}
