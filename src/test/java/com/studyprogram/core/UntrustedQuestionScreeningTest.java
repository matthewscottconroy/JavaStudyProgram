package com.studyprogram.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A question pack is executable content. Anything dropped into an external overlay is
 * screened before it can be compiled and run.
 */
class UntrustedQuestionScreeningTest {

    @TempDir
    Path external;

    private void writeQuestion(String id, String starterBody) throws Exception {
        Path dir = external.resolve("loops");
        Files.createDirectories(dir);
        String json = """
                {
                  "id": "%s",
                  "type": "CODING",
                  "difficulty": 2,
                  "prompt": "p",
                  "starterCode": "public class Evil%s { public static int go() { %s return 0; } }",
                  "testCode": "public class Evil%sTest { public static void main(String[] a) { System.out.println(\\"ALL TESTS PASSED\\"); } }",
                  "answer": "public class Evil%s { public static int go() { return 0; } }",
                  "explanation": "e"
                }
                """.formatted(id, id, starterBody, id, id);
        Files.writeString(dir.resolve(id + ".json"), json);
    }

    @Test
    void externalQuestionUsingProcessExecutionIsRefused() throws Exception {
        writeQuestion("evil1", "try { new ProcessBuilder(\\\"sh\\\").start(); } catch (Exception e) {}");
        QuestionBank bank = new QuestionBank(external);

        assertTrue(bank.findById("evil1").isEmpty(), "flagged external question must not load");
        assertTrue(bank.getWarnings().stream().anyMatch(w -> w.contains("REFUSED") && w.contains("evil1")),
                "refusal must be reported: " + bank.getWarnings());
    }

    @Test
    void harmlessExternalQuestionLoadsNormally() throws Exception {
        writeQuestion("fine1", "int x = 1 + 2;");
        QuestionBank bank = new QuestionBank(external);

        assertTrue(bank.findById("fine1").isPresent(), "clean external questions still load");
        assertFalse(bank.findById("fine1").get().isTrusted(), "but they are marked untrusted");
    }

    @Test
    void bundledFirstPartyContentIsNeverRefused() {
        QuestionBank bank = new QuestionBank();   // repo's own data/questions overlay
        assertTrue(bank.getWarnings().stream().noneMatch(w -> w.contains("REFUSED")),
                "shipped questions must never be screened out: " + bank.getWarnings());
        assertTrue(bank.totalQuestions() > 1000);
    }
}
