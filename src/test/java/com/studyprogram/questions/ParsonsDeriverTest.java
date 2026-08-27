package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParsonsDeriverTest {

    private static final String SOLUTION = """
            /**
             * A comment that must not appear in the puzzle.
             */
            public class SumTo {

                public static int sumTo(int n) {
                    int sum = 0;
                    // accumulate
                    for (int i = 1; i <= n; i++) {
                        sum += i;
                    }
                    return sum;
                }
            }
            """;

    private Question coding(String answer) {
        return Question.builder()
                .id("xx-code-99").topic(Topic.LOOPS).type(QuestionType.CODING).difficulty(3)
                .prompt("Sum 1..n.").answer(answer)
                .starterCode("public class SumTo {}").testCode("public class SumToTest {}")
                .explanation("Loop with accumulator.")
                .build();
    }

    @Test
    void derivesShuffledButReconstructiblePuzzle() {
        Question p = ParsonsDeriver.derive(coding(SOLUTION)).orElseThrow();
        assertEquals(QuestionType.PARSONS, p.getType());
        assertEquals("xx-code-99-parsons", p.getId());
        assertEquals(2, p.getDifficulty(), "one easier than the source");

        List<String> shuffled = p.getShuffledLines();
        List<String> solutionLines = List.of(p.getAnswer().split("\n"));
        assertEquals(solutionLines.size(), shuffled.size());
        assertNotEquals(solutionLines, shuffled, "must actually be shuffled");
        // same multiset of lines
        assertEquals(solutionLines.stream().sorted().toList(),
                     shuffled.stream().sorted().toList());
        // comments and blanks are gone
        assertTrue(shuffled.stream().noneMatch(l -> l.trim().startsWith("//")));
        assertTrue(shuffled.stream().noneMatch(l -> l.trim().startsWith("*")));
        assertTrue(shuffled.stream().noneMatch(String::isBlank));
    }

    @Test
    void derivationIsDeterministic() {
        Question a = ParsonsDeriver.derive(coding(SOLUTION)).orElseThrow();
        Question b = ParsonsDeriver.derive(coding(SOLUTION)).orElseThrow();
        assertEquals(a.getShuffledLines(), b.getShuffledLines());
    }

    @Test
    void tooShortSolutionsAreSkipped() {
        Optional<Question> p = ParsonsDeriver.derive(
                coding("public class A {\n    int x;\n}\n"));
        assertTrue(p.isEmpty());
    }

    @Test
    void nonCodingQuestionsAreSkipped() {
        Question mc = Question.builder()
                .id("m").topic(Topic.LOOPS).difficulty(2).prompt("p")
                .choices("a", "b", "c", "d").answer("a").build();
        assertTrue(ParsonsDeriver.derive(mc).isEmpty());
    }
}
