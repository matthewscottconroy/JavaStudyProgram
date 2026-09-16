package com.studyprogram.questions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The verifier is for packs written by people who will not run the test suite, so it has to
 * catch the mistakes those people actually make — and it has to catch them by file, because
 * "something is wrong with your 200 questions" is not a report anyone can act on.
 */
class QuestionPackVerifierTest {

    @TempDir Path pack;

    private void write(String relative, String json) throws Exception {
        Path file = pack.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, json);
    }

    private static List<String> messages(QuestionPackVerifier.Report r) {
        return r.problems().stream().map(QuestionPackVerifier.Problem::toString).toList();
    }

    private static final String GOOD_MC = """
            { "id": "%s", "type": "MULTIPLE_CHOICE", "difficulty": 2, "prompt": "Pick a.",
              "choices": ["alpha", "beta"], "answer": "a", "explanation": "a." }
            """;

    @Test
    void aHealthyPackIsReportedReady() throws Exception {
        write("loops/l-1.json", GOOD_MC.formatted("l-1"));
        write("loops/l-2.json", GOOD_MC.formatted("l-2"));

        var report = QuestionPackVerifier.verify(pack);
        assertTrue(report.ok(), messages(report).toString());
        assertEquals(2, report.questions());
        assertTrue(QuestionPackVerifier.render(pack, report).contains("ready to use"));
    }

    @Test
    void aMisnamedTopicFolderIsReportedWithASuggestion() throws Exception {
        write("Strings/s-1.json", GOOD_MC.formatted("s-1"));

        var report = QuestionPackVerifier.verify(pack);
        assertFalse(report.ok());
        String problem = messages(report).get(0);
        assertTrue(problem.startsWith("Strings/"), problem);
        assertTrue(problem.contains("matches no topic"), problem);
        assertTrue(problem.contains("Did you mean 'strings'"), problem);
    }

    @Test
    void aFileThatDoesNotParseIsNamed() throws Exception {
        write("loops/broken.json", "{ this is not json");
        write("loops/fine.json", GOOD_MC.formatted("fine"));

        var report = QuestionPackVerifier.verify(pack);
        assertEquals(1, report.problems().size(), messages(report).toString());
        assertTrue(messages(report).get(0).startsWith("loops/broken.json: does not parse"));
        assertEquals(1, report.questions(), "the good file still counts");
    }

    @Test
    void duplicateIdsAreCaughtBecauseOnlyOneWouldLoad() throws Exception {
        write("loops/a.json", GOOD_MC.formatted("same-id"));
        write("loops/b.json", GOOD_MC.formatted("same-id"));

        var report = QuestionPackVerifier.verify(pack);
        assertTrue(messages(report).stream().anyMatch(m -> m.contains("duplicate id 'same-id'")),
                messages(report).toString());
    }

    @Test
    void aMultipleChoiceAnswerThatIsNotAChoiceLetterIsCaught() throws Exception {
        write("loops/mc.json", """
                { "id": "mc", "type": "MULTIPLE_CHOICE", "difficulty": 2, "prompt": "p",
                  "choices": ["x", "y"], "answer": "c", "explanation": "e" }
                """);

        var report = QuestionPackVerifier.verify(pack);
        assertTrue(messages(report).stream().anyMatch(m -> m.contains("not a choice letter")),
                messages(report).toString());
    }

    @Test
    void aCodingExerciseWhoseStarterAlreadyPassesIsCaught() throws Exception {
        write("loops/code.json", """
                { "id": "code", "type": "CODING", "difficulty": 2, "prompt": "p",
                  "starterCode": "public class A { public static int f() { return 1; } }",
                  "testCode": "public class ATest { public static void main(String[] a) { if (A.f() != 1) System.exit(1); } }",
                  "answer": "public class A { public static int f() { return 1; } }",
                  "explanation": "e" }
                """);

        var report = QuestionPackVerifier.verify(pack);
        assertEquals(1, report.coding());
        assertTrue(messages(report).stream()
                        .anyMatch(m -> m.startsWith("loops/code") && m.contains("starter should compile but fail")),
                messages(report).toString());
    }

    @Test
    void aCodingExerciseWhoseSolutionDoesNotPassIsCaught() throws Exception {
        write("loops/code.json", """
                { "id": "code", "type": "CODING", "difficulty": 2, "prompt": "p",
                  "starterCode": "public class A { public static int f() { return 0; } }",
                  "testCode": "public class ATest { public static void main(String[] a) { if (A.f() != 1) System.exit(1); } }",
                  "answer": "public class A { public static int f() { return 2; } }",
                  "explanation": "e" }
                """);

        var report = QuestionPackVerifier.verify(pack);
        assertTrue(messages(report).stream().anyMatch(m -> m.contains("reference solution should pass")),
                messages(report).toString());
    }

    @Test
    void aCodingExerciseTheSafetyScreenWouldRefuseIsExplainedUpFront() throws Exception {
        write("loops/net.json", """
                { "id": "net", "type": "CODING", "difficulty": 2, "prompt": "p",
                  "starterCode": "public class A { public static int f() { return 0; } }",
                  "testCode": "import java.net.Socket;\\npublic class ATest { public static void main(String[] a) throws Exception { if (A.f() != 1) System.exit(1); if (a.length > 5) new Socket(\\"example.com\\", 80); } }",
                  "answer": "public class A { public static int f() { return 1; } }",
                  "explanation": "e" }
                """);

        var report = QuestionPackVerifier.verify(pack);
        assertTrue(report.ok(), "a working exercise is not broken: " + messages(report));
        assertEquals(1, report.warnings().size());
        assertTrue(report.warnings().get(0).message().contains("safety screen"),
                "the author should learn this before a student sees 'refused': " + report.warnings());
        assertTrue(QuestionPackVerifier.render(pack, report).contains("warning: loops/net.json"));
    }

    @Test
    void aMissingDirectoryIsAProblemNotACrash() {
        var report = QuestionPackVerifier.verify(pack.resolve("nowhere"));
        assertFalse(report.ok());
        assertTrue(messages(report).get(0).contains("not a directory"));
    }
}
