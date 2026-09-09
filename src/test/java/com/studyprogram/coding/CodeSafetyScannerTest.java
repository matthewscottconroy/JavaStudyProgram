package com.studyprogram.coding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeSafetyScannerTest {

    private static List<String> categories(String source) {
        return CodeSafetyScanner.scan(List.of(source)).stream()
                .map(CodeSafetyScanner.Finding::category).toList();
    }

    @Test
    void ordinaryExerciseCodeIsClean() {
        String benign = """
                public class SumOfMultiples {
                    public static int sumOfMultiples(int limit) {
                        int sum = 0;
                        for (int i = 1; i < limit; i++) {
                            if (i % 3 == 0 || i % 5 == 0) sum += i;
                        }
                        return sum;
                    }
                }
                """;
        assertTrue(CodeSafetyScanner.scan(List.of(benign)).isEmpty());
    }

    @Test
    void flagsProcessExecution() {
        assertTrue(categories("class X { void go() throws Exception { "
                + "new ProcessBuilder(\"rm\", \"-rf\").start(); } }")
                .contains("process execution"));
    }

    @Test
    void flagsNetworkAndFilesystemWrites() {
        assertTrue(categories("class X { void go() throws Exception { "
                + "var s = new java.net.Socket(\"example.com\", 80); } }")
                .contains("network access"));
        assertTrue(categories("class X { void go() throws Exception { "
                + "java.nio.file.Files.delete(java.nio.file.Path.of(\"/etc/passwd\")); } }")
                .contains("filesystem writes outside temp"));
    }

    @Test
    void flagsReflectionOverrideAndNativeLoading() {
        assertTrue(categories("class X { void go(java.lang.reflect.Field f) { f.setAccessible(true); } }")
                .contains("reflective access override"));
        assertTrue(categories("class X { static { System.loadLibrary(\"evil\"); } }")
                .contains("native code"));
    }

    @Test
    void proseAndStringLiteralsDoNotTriggerFindings() {
        String source = """
                /**
                 * Never call System.exit or use a ProcessBuilder in an exercise.
                 * A Socket would be wrong here too.
                 */
                public class Note {
                    static final String SQL = "SELECT * FROM Socket WHERE ProcessBuilder = 1";
                    static final String HINT = "Files.delete is not what you want";
                }
                """;
        assertTrue(CodeSafetyScanner.scan(List.of(source)).isEmpty(),
                "comments and string literals must not be scanned as code");
    }

    @Test
    void detectsSocketUsageForNetworkAwareExercises() {
        assertTrue(CodeSafetyScanner.usesNetwork("var ss = new ServerSocket(0);"));
        assertFalse(CodeSafetyScanner.usesNetwork("int x = 1 + 2;"));
    }

    @Test
    void scanIsNullSafeAndSkipsBlanks() {
        List<String> sources = new java.util.ArrayList<>();
        sources.add(null);
        sources.add("   ");
        assertTrue(CodeSafetyScanner.scan(sources).isEmpty());
    }
}
