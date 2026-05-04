package com.studyprogram.grading;

import com.studyprogram.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ExactMatchGraderTest {

    private final ExactMatchGrader grader = new ExactMatchGrader();

    private static Question traceQ(String answer) {
        return Question.builder()
                .topic(Topic.VARIABLES)
                .type(QuestionType.TRACING)
                .difficulty(1)
                .prompt("Trace this code")
                .answer(answer)
                .explanation("Because X.")
                .build();
    }

    private static Question traceWithAlternative(String answer, String alt) {
        return Question.builder()
                .topic(Topic.VARIABLES)
                .type(QuestionType.TRACING)
                .difficulty(1)
                .prompt("Trace this code")
                .answer(answer)
                .alternative(alt)
                .explanation("Because X.")
                .build();
    }

    // ── Correct answers ───────────────────────────────────────────────────────

    @Test
    void exactMatch() {
        assertTrue(grader.grade(traceQ("42"), "42").correct());
    }

    @Test
    void caseInsensitiveMatch() {
        assertTrue(grader.grade(traceQ("Hello, World!"), "hello, world!").correct());
    }

    @Test
    void leadingTrailingWhitespaceTrimmed() {
        assertTrue(grader.grade(traceQ("42"), "  42  ").correct());
    }

    @Test
    void internalWhitespaceNormalised() {
        assertTrue(grader.grade(traceQ("hello world"), "hello  world").correct());
    }

    @Test
    void alternativeAnswerAccepted() {
        Question q = traceWithAlternative("eight", "8");
        assertTrue(grader.grade(q, "8").correct());
    }

    @Test
    void alternativeAnswerCaseInsensitive() {
        Question q = traceWithAlternative("TRUE", "true");
        assertTrue(grader.grade(q, "True").correct());
    }

    // ── Preserved-case bug fix: stored answer shown verbatim in feedback ──────

    @Test
    void feedbackShowsOriginalCaseOfStoredAnswer() {
        GradingResult r = grader.grade(traceQ("JAVA4"), "wrong");
        assertFalse(r.correct());
        assertTrue(r.feedback().contains("JAVA4"),
                "Feedback must show the original-case answer, not 'java4'");
    }

    @Test
    void feedbackShowsMixedCaseAnswer() {
        GradingResult r = grader.grade(traceQ("Hello, World!"), "wrong");
        assertTrue(r.feedback().contains("Hello, World!"));
    }

    // ── Wrong answers ─────────────────────────────────────────────────────────

    @Test
    void wrongAnswerIsIncorrect() {
        assertFalse(grader.grade(traceQ("42"), "43").correct());
    }

    @Test
    void emptyStudentAnswerIsIncorrect() {
        assertFalse(grader.grade(traceQ("42"), "").correct());
    }

    @Test
    void blankStudentAnswerIsIncorrect() {
        assertFalse(grader.grade(traceQ("42"), "   ").correct());
    }

    @Test
    void nullStudentAnswerIsIncorrect() {
        assertFalse(grader.grade(traceQ("42"), null).correct());
    }

    // ── Result content ────────────────────────────────────────────────────────

    @Test
    void correctResultHasExplanation() {
        GradingResult r = grader.grade(traceQ("42"), "42");
        assertEquals("Because X.", r.explanation());
    }

    @Test
    void incorrectResultHasFeedbackAndExplanation() {
        GradingResult r = grader.grade(traceQ("42"), "99");
        assertFalse(r.correct());
        assertNotNull(r.feedback());
        assertFalse(r.feedback().isBlank());
        assertEquals("Because X.", r.explanation());
    }

    // ── Parameterised: various accepted forms ─────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"5", "  5  ", "5 "})
    void variousWhitespaceForms(String answer) {
        assertTrue(grader.grade(traceQ("5"), answer).correct());
    }
}
