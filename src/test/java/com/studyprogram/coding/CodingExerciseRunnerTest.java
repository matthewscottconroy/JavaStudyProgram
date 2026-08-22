package com.studyprogram.coding;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CodingExerciseRunnerTest {

    private static CodingExerciseRunner runner;

    @BeforeAll
    static void setUp() {
        assumeTrue(CodingExerciseRunner.compilerAvailable(),
                "JDK compiler not available — skipping coding runner tests");
        runner = new CodingExerciseRunner();
    }

    private static final String PASSING = """
            public class Adder {
                public static int add(int a, int b) { return a + b; }
            }
            """;

    private static final String FAILING = """
            public class Adder {
                public static int add(int a, int b) { return 0; }
            }
            """;

    private static final String BROKEN = """
            public class Adder {
                public static int add(int a, int b) { return a + }
            }
            """;

    private static final String TEST = """
            public class AdderTest {
                public static void main(String[] args) {
                    if (Adder.add(2, 3) != 5) { System.out.println("FAIL"); System.exit(1); }
                    System.out.println("ALL TESTS PASSED");
                }
            }
            """;

    @Test
    void correctSolutionPasses() {
        CodingResult result = runner.compileAndTest(PASSING, TEST);
        assertEquals(CodingResult.Status.PASS, result.status(), result.output());
        assertTrue(result.output().contains("ALL TESTS PASSED"));
    }

    @Test
    void wrongSolutionFailsTests() {
        CodingResult result = runner.compileAndTest(FAILING, TEST);
        assertEquals(CodingResult.Status.TEST_FAILURE, result.status());
    }

    @Test
    void syntaxErrorReportsCompileError() {
        CodingResult result = runner.compileAndTest(BROKEN, TEST);
        assertEquals(CodingResult.Status.COMPILE_ERROR, result.status());
        assertFalse(result.output().isBlank(), "compile errors should be reported");
    }

    @Test
    void infiniteLoopTimesOut() {
        String loop = """
                public class Adder {
                    public static int add(int a, int b) { while (true) {} }
                }
                """;
        CodingResult result = runner.compileAndTest(loop, TEST);
        assertEquals(CodingResult.Status.TIMEOUT, result.status());
    }

    @Test
    void primaryTypeNameFindsPublicClassAfterHelpers() {
        String source = """
                class Helper { }
                public class Main { }
                """;
        assertEquals("Main", JavaSource.primaryTypeName(source));
    }

    /**
     * Content gate for every shipped CODING exercise: the starter must compile cleanly
     * but fail its tests, and the reference solution must pass them. This keeps broken
     * exercises from ever reaching students.
     */
    @Test
    void everyCodingExerciseStarterFailsAndSolutionPasses() {
        List<Question> coding = new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))
                .stream().filter(q -> q.getType() == QuestionType.CODING).toList();
        assertFalse(coding.isEmpty(), "expected shipped CODING exercises");

        for (Question q : coding) {
            CodingResult starter = runner.compileAndTest(q.getStarterCode(), q.getTestCode());
            assertEquals(CodingResult.Status.TEST_FAILURE, starter.status(),
                    q.getId() + ": starter should compile but fail tests — got "
                    + starter.status() + "\n" + starter.output());

            CodingResult solution = runner.compileAndTest(q.getAnswer(), q.getTestCode());
            assertEquals(CodingResult.Status.PASS, solution.status(),
                    q.getId() + ": reference solution should pass — got "
                    + solution.status() + "\n" + solution.output());
        }
    }
}
