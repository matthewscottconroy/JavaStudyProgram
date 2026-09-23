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

    /**
     * Regression: the screen once flagged System.exit, which is how every test harness in the
     * bank -- and every harness written the way the README says -- reports failure. Exercise
     * code runs in its own subprocess, so exit() there is harmless, and an instructor's pack
     * written the documented way must load.
     */
    @Test
    void anExternalExerciseWrittenTheDocumentedWayIsNotRefused() throws Exception {
        Path dir = external.resolve("loops");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("mine.json"), """
                {
                  "id": "mine",
                  "type": "CODING",
                  "difficulty": 2,
                  "prompt": "p",
                  "starterCode": "public class Mine { public static int go() { return 0; } }",
                  "testCode": "public class MineTest { public static void main(String[] a) { if (Mine.go() != 1) { System.out.println(\\"FAIL\\"); System.exit(1); } System.out.println(\\"ALL TESTS PASSED\\"); } }",
                  "answer": "public class Mine { public static int go() { return 1; } }",
                  "explanation": "e"
                }
                """);
        QuestionBank bank = new QuestionBank(external);

        assertTrue(bank.findById("mine").isPresent(),
                "a harness that exits non-zero on failure is the documented pattern: "
                + bank.getWarnings());
    }

    /**
     * With no OS containment — every Windows machine, and any Linux box without bubblewrap —
     * the lexical screen is the only defence left, so a clean scan is no longer enough on its
     * own to run a stranger's code.
     */
    @Test
    void withNoSandboxACleanExternalExerciseIsStillRefusedUnlessTrusted() throws Exception {
        String previous = System.getProperty("JAVASTUDY_SANDBOX");
        try {
            com.studyprogram.coding.Sandbox.forceBackendForTesting(
                    com.studyprogram.coding.Sandbox.Backend.NONE);
            writeQuestion("plain1", "int x = 1 + 2;");
            QuestionBank bank = new QuestionBank(external);

            assertTrue(bank.findById("plain1").isEmpty(),
                    "third-party code must not run with neither containment nor a clean bill "
                    + "of health from anything else");
            assertTrue(bank.getWarnings().stream()
                            .anyMatch(w -> w.contains("no exercise sandbox")),
                    "and the reason must be stated: " + bank.getWarnings());
        } finally {
            com.studyprogram.coding.Sandbox.forceBackendForTesting(null);
            if (previous != null) System.setProperty("JAVASTUDY_SANDBOX", previous);
        }
    }

    @Test
    void withNoSandboxAnExplicitlyTrustedPackStillLoads() throws Exception {
        try {
            com.studyprogram.coding.Sandbox.forceBackendForTesting(
                    com.studyprogram.coding.Sandbox.Backend.NONE);
            System.setProperty("javastudy.trustExternal", "true");
            writeQuestion("plain2", "int x = 1 + 2;");
            QuestionBank bank = new QuestionBank(external);

            assertTrue(bank.findById("plain2").isPresent(),
                    "an instructor who vouched for their own pack must be able to use it");
        } finally {
            System.clearProperty("javastudy.trustExternal");
            com.studyprogram.coding.Sandbox.forceBackendForTesting(null);
        }
    }

    @Test
    void bundledFirstPartyContentIsNeverRefused() {
        QuestionBank bank = new QuestionBank();   // repo's own data/questions overlay
        assertTrue(bank.getWarnings().stream().noneMatch(w -> w.contains("REFUSED")),
                "shipped questions must never be screened out: " + bank.getWarnings());
        assertTrue(bank.totalQuestions() > 1000);
    }
}
