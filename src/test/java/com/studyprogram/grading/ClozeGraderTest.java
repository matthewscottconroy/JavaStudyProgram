package com.studyprogram.grading;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClozeGraderTest {

    private final ClozeGrader grader = new ClozeGrader();

    private final Question q = Question.builder()
            .id("c1").topic(Topic.LOOPS).type(QuestionType.CLOZE).difficulty(2)
            .prompt("fill in").code("for (int i = 0; ____; i++) {")
            .answer("i < limit")
            .alternative("limit > i")
            .explanation("loop bound")
            .build();

    @Test
    void exactAnswerPasses() {
        assertTrue(grader.grade(q, "i < limit").correct());
    }

    @Test
    void whitespaceDifferencesAreIgnored() {
        assertTrue(grader.grade(q, "  i   <   limit ").correct());
    }

    @Test
    void alternativesPass() {
        assertTrue(grader.grade(q, "limit > i").correct());
    }

    @Test
    void caseMatters() {
        assertFalse(grader.grade(q, "i < LIMIT").correct(),
                "Java identifiers are case-sensitive");
    }

    @Test
    void wrongExpressionFailsWithAnswerInFeedback() {
        var result = grader.grade(q, "i <= limit");
        assertFalse(result.correct());
        assertTrue(result.feedback().contains("i < limit"));
    }
}
