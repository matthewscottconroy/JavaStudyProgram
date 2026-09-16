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
     * Content gate for every shipped CODING exercise: the starter must compile cleanly but fail
     * its tests, and the reference solution must pass them. This keeps broken exercises from
     * ever reaching students. The gate is {@link com.studyprogram.questions.QuestionPackVerifier},
     * the same code an instructor runs as {@code --verify-questions} over their own pack — one
     * definition of "healthy", so a pack that verifies here also loads there.
     */
    @Test
    void everyCodingExerciseStarterFailsAndSolutionPasses() {
        var report = com.studyprogram.questions.QuestionPackVerifier.verify(
                QuestionBank.DEFAULT_EXTERNAL_DIR);
        assertTrue(report.coding() > 700, "expected shipped CODING exercises, saw " + report.coding());
        assertTrue(report.ok(), report.problems().size() + " broken exercise(s):\n"
                + String.join("\n", report.problems().stream().map(Object::toString).toList()));
        assertFalse(report.notes().isEmpty(), "derivation yield should be reported");
    }

}
