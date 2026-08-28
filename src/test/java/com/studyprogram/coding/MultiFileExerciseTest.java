package com.studyprogram.coding;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class MultiFileExerciseTest {

    @BeforeAll
    static void requireCompiler() {
        assumeTrue(CodingExerciseRunner.compilerAvailable());
    }

    // LinkedHashMap: the FIRST entry is the primary file shown to the student
    private static final Map<String, String> STARTER = ordered(
            "Greeter.java", "public class Greeter { public String greet() { return \"\"; } }",
            "App.java", "public class App { public static String run() { return new Greeter().greet(); } }");

    private static final Map<String, String> SOLUTION = ordered(
            "Greeter.java", "public class Greeter { public String greet() { return \"hi\"; } }",
            "App.java", "public class App { public static String run() { return new Greeter().greet(); } }");

    private static Map<String, String> ordered(String k1, String v1, String k2, String v2) {
        Map<String, String> m = new java.util.LinkedHashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }

    private static final String TEST = """
            public class AppTest {
                public static void main(String[] args) {
                    if (!"hi".equals(App.run())) { System.out.println("FAIL"); System.exit(1); }
                    System.out.println("ALL TESTS PASSED");
                }
            }
            """;

    private Question multiFileQuestion() {
        return Question.builder()
                .id("mf-test-01").topic(Topic.CLASSES).type(QuestionType.CODING).difficulty(3)
                .prompt("p").answer("combined solution text")
                .starterFiles(STARTER).solutionFiles(SOLUTION).testCode(TEST)
                .build();
    }

    @Test
    void starterFailsAndSolutionPassesAcrossFiles() {
        CodingExerciseRunner runner = new CodingExerciseRunner();
        assertEquals(CodingResult.Status.TEST_FAILURE,
                runner.compileAndTest(STARTER, TEST).status());
        assertEquals(CodingResult.Status.PASS,
                runner.compileAndTest(SOLUTION, TEST).status());
    }

    @Test
    void workspaceGetsAllStarterFilesAndExtrasAreCompiled(@TempDir Path ws) throws Exception {
        CodingExerciseRunner runner = new CodingExerciseRunner(ws);
        Question q = multiFileQuestion();

        Path first = runner.prepareWorkspace(q);
        assertEquals("Greeter.java", first.getFileName().toString());
        assertTrue(Files.exists(ws.resolve("mf-test-01").resolve("App.java")));

        // student edits Greeter to delegate to a NEW helper file they added themselves
        Files.writeString(ws.resolve("mf-test-01").resolve("Greeter.java"),
                "public class Greeter { public String greet() { return Helper.WORD; } }");
        Files.writeString(ws.resolve("mf-test-01").resolve("Helper.java"),
                "public class Helper { public static final String WORD = \"hi\"; }");

        assertEquals(CodingResult.Status.PASS, runner.run(q).status(),
                "extra student-added files must be compiled too");
    }

    @Test
    void resetRestoresEveryStarterFile(@TempDir Path ws) throws Exception {
        CodingExerciseRunner runner = new CodingExerciseRunner(ws);
        Question q = multiFileQuestion();
        runner.prepareWorkspace(q);
        Files.writeString(ws.resolve("mf-test-01").resolve("App.java"), "garbage");
        runner.resetToStarter(q);
        assertEquals(STARTER.get("App.java"),
                Files.readString(ws.resolve("mf-test-01").resolve("App.java")));
    }

    @Test
    void multiFileQuestionsRequireSolutionFiles() {
        assertThrows(IllegalStateException.class, () -> Question.builder()
                .id("bad").topic(Topic.CLASSES).type(QuestionType.CODING).difficulty(3)
                .prompt("p").answer("a")
                .starterFiles(STARTER).testCode(TEST)
                .build());
    }
}
