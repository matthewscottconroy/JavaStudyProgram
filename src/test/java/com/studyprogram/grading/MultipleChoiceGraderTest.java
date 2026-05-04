package com.studyprogram.grading;

import com.studyprogram.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MultipleChoiceGraderTest {

    private final MultipleChoiceGrader grader = new MultipleChoiceGrader();

    private static Question mcQ(String correctLetter) {
        return Question.builder()
                .topic(Topic.VARIABLES)
                .type(QuestionType.MULTIPLE_CHOICE)
                .difficulty(1)
                .prompt("Pick one")
                .choices("Choice A text", "Choice B text", "Choice C text", "Choice D text")
                .answer(correctLetter)
                .explanation("Because of X.")
                .build();
    }

    private static Question traceQ() {
        return Question.builder()
                .topic(Topic.VARIABLES)
                .type(QuestionType.TRACING)
                .difficulty(1)
                .prompt("Trace")
                .answer("42")
                .build();
    }

    // ── Correct answers ───────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({"a,a", "b,b", "c,c", "d,d"})
    void correctLetterLowercase(String answer, String correct) {
        assertTrue(grader.grade(mcQ(correct), answer).correct());
    }

    @ParameterizedTest
    @CsvSource({"A,a", "B,b", "C,c", "D,d"})
    void correctLetterUppercase(String answer, String correct) {
        assertTrue(grader.grade(mcQ(correct), answer).correct());
    }

    @ParameterizedTest
    @ValueSource(strings = {"A.", "a.", "A. "})
    void correctLetterWithTrailingPeriod(String answer) {
        assertTrue(grader.grade(mcQ("a"), answer).correct());
    }

    @Test
    void correctAnswerReturnsCorrectResult() {
        GradingResult r = grader.grade(mcQ("b"), "B");
        assertTrue(r.correct());
        assertEquals("Because of X.", r.explanation());
    }

    // ── Wrong answers ─────────────────────────────────────────────────────────

    @Test
    void wrongLetterIsIncorrect() {
        assertFalse(grader.grade(mcQ("a"), "B").correct());
    }

    @Test
    void wrongAnswerFeedbackContainsCorrectLetter() {
        GradingResult r = grader.grade(mcQ("c"), "A");
        assertFalse(r.correct());
        assertTrue(r.feedback().contains("C"),
                "Feedback should name the correct option: " + r.feedback());
    }

    @Test
    void wrongAnswerFeedbackContainsCorrectChoiceText() {
        GradingResult r = grader.grade(mcQ("b"), "A");
        assertTrue(r.feedback().contains("Choice B text"),
                "Feedback should include the choice text: " + r.feedback());
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    void nullAnswerReturnsIncorrect() {
        GradingResult r = grader.grade(mcQ("a"), null);
        assertFalse(r.correct());
        assertFalse(r.feedback().isBlank());
    }

    @Test
    void blankAnswerReturnsIncorrect() {
        GradingResult r = grader.grade(mcQ("a"), "   ");
        assertFalse(r.correct());
    }

    @Test
    void nonMCQuestionThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                grader.grade(traceQ(), "A"));
    }

    // ── Each letter position correct ──────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({"a", "b", "c", "d"})
    void eachLetterCanBeCorrect(String letter) {
        Question q = mcQ(letter);
        assertTrue(grader.grade(q, letter).correct());
        assertFalse(grader.grade(q, letter.equals("a") ? "b" : "a").correct());
    }
}
