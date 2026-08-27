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
     * exercises from ever reaching students. Exercises are verified in parallel
     * (each compiles in its own temp directory) so the gate stays fast as the bank grows.
     */
    @Test
    void everyCodingExerciseStarterFailsAndSolutionPasses() throws Exception {
        List<Question> coding = new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))
                .stream().filter(q -> q.getType() == QuestionType.CODING).toList();
        assertFalse(coding.isEmpty(), "expected shipped CODING exercises");

        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
        java.util.concurrent.ExecutorService pool =
                java.util.concurrent.Executors.newFixedThreadPool(threads);
        List<java.util.concurrent.Future<String>> futures = new java.util.ArrayList<>();
        for (Question q : coding) {
            futures.add(pool.submit(() -> verifyExercise(q)));
        }
        pool.shutdown();

        List<String> failures = new java.util.ArrayList<>();
        for (java.util.concurrent.Future<String> f : futures) {
            String problem = f.get(10, java.util.concurrent.TimeUnit.MINUTES);
            if (problem != null) failures.add(problem);
        }
        assertTrue(failures.isEmpty(),
                failures.size() + " broken exercise(s):\n" + String.join("\n", failures));
    }

    /** Returns null when the exercise is healthy, else a description of the problem. */
    private static String verifyExercise(Question q) {
        CodingResult starter = runner.compileAndTest(q.getStarterCode(), q.getTestCode());
        if (starter.status() != CodingResult.Status.TEST_FAILURE) {
            return q.getId() + ": starter should compile but fail tests — got "
                    + starter.status() + "\n" + starter.output();
        }
        CodingResult solution = runner.compileAndTest(q.getAnswer(), q.getTestCode());
        if (solution.status() != CodingResult.Status.PASS) {
            return q.getId() + ": reference solution should pass — got "
                    + solution.status() + "\n" + solution.output();
        }
        return null;
    }
}
