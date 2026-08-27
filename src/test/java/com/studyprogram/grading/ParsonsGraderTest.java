package com.studyprogram.grading;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParsonsGraderTest {

    private final ParsonsGrader grader = new ParsonsGrader();

    // solution:  A / B / } / }   — two identical closing braces
    private final Question q = Question.builder()
            .id("p1").topic(Topic.LOOPS).type(QuestionType.PARSONS).difficulty(2)
            .prompt("order me")
            .shuffledLines(List.of("}", "line A", "}", "line B"))   // shuffled listing 1..4
            .answer("line A\nline B\n}\n}")
            .explanation("because")
            .build();

    @Test
    void correctOrderPasses() {
        assertTrue(grader.grade(q, "2 4 1 3").correct());
    }

    @Test
    void interchangeableIdenticalLinesAcceptedEitherWay() {
        assertTrue(grader.grade(q, "2 4 3 1").correct(),
                "the two '}' lines are identical, so either index order is right");
    }

    @Test
    void wrongOrderFails() {
        assertFalse(grader.grade(q, "4 2 1 3").correct());
    }

    @Test
    void commasAndExtraSpacesAreTolerated() {
        assertTrue(grader.grade(q, " 2, 4,  1 3 ").correct());
    }

    @Test
    void incompleteOrDuplicateNumbersRejectedGracefully() {
        assertFalse(grader.grade(q, "2 4 1").correct());
        assertFalse(grader.grade(q, "2 2 4 1").correct());
        assertFalse(grader.grade(q, "2 4 1 9").correct());
        assertFalse(grader.grade(q, "banana").correct());
    }
}
