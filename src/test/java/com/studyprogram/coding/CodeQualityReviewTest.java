package com.studyprogram.coding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeQualityReviewTest {

    private static List<String> kindsOf(String source) {
        return CodeQualityReview.review(source).stream()
                .map(CodeQualityReview.Note::summary).toList();
    }

    private static boolean mentions(String source, String fragment) {
        return CodeQualityReview.review(source).stream()
                .anyMatch(n -> n.summary().contains(fragment) || n.why().contains(fragment));
    }

    @Test
    void cleanCodeGetsNoLecture() {
        String good = """
                public class Stats {
                    public static int largest(int[] values) {
                        int best = values[0];
                        for (int i = 1; i < values.length; i++) {
                            if (values[i] > best) {
                                best = values[i];
                            }
                        }
                        return best;
                    }
                }
                """;
        assertTrue(CodeQualityReview.review(good).isEmpty(),
                "well-written code must not be nitpicked: " + kindsOf(good));
        assertEquals("", CodeQualityReview.render(good));
    }

    @Test
    void comparingStringsWithDoubleEqualsIsCalledOut() {
        String source = """
                public class Greeter {
                    public static boolean isYes(String answer) {
                        return answer == "yes";
                    }
                }
                """;
        assertTrue(mentions(source, ".equals"), kindsOf(source).toString());
    }

    @Test
    void aStringLiteralContainingAnEqualsSignIsNotAFinding() {
        String source = """
                public class Printer {
                    public static void show(int n) {
                        System.out.println("n == 3 means three");
                    }
                }
                """;
        assertTrue(CodeQualityReview.review(source).isEmpty(),
                "text inside a string is not code: " + kindsOf(source));
    }

    @Test
    void anEmptyCatchBlockIsTheMostImportantThingToSay() {
        String source = """
                public class Reader {
                    public static void read() {
                        try {
                            int x = Integer.parseInt("a");
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                """;
        var notes = CodeQualityReview.review(source);
        assertFalse(notes.isEmpty());
        assertTrue(notes.get(0).summary().contains("empty catch"),
                "a swallowed error outranks style advice: " + notes);
    }

    @Test
    void aVeryLongMethodIsFlagged() {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < 40; i++) body.append("        int v").append(i).append(" = ").append(i).append(";\n");
        String source = "public class Long {\n    public static void run() {\n" + body + "    }\n}\n";
        assertTrue(mentions(source, "statements long"), kindsOf(source).toString());
    }

    @Test
    void deeplyNestedLogicIsFlagged() {
        String source = """
                public class Nest {
                    public static int run(int[][] grid, int target) {
                        for (int[] row : grid) {
                            for (int cell : row) {
                                if (cell > 0) {
                                    if (cell == target) {
                                        return cell;
                                    }
                                }
                            }
                        }
                        return -1;
                    }
                }
                """;
        assertTrue(mentions(source, "levels deep"), kindsOf(source).toString());
    }

    @Test
    void aVagueMethodNameIsCalledOut() {
        String source = """
                public class Worker {
                    public static int doStuff(int a) {
                        return a + 1;
                    }
                }
                """;
        assertTrue(mentions(source, "does not say what"), kindsOf(source).toString());
    }

    @Test
    void loopCountersAreNotNaggedAbout() {
        String source = """
                public class Counter {
                    public static int total(int[] xs) {
                        int sum = 0;
                        for (int i = 0; i < xs.length; i++) {
                            sum += xs[i];
                        }
                        return sum;
                    }
                }
                """;
        assertTrue(CodeQualityReview.review(source).isEmpty(),
                "i is a perfectly good name for a loop counter: " + kindsOf(source));
    }

    @Test
    void adviceIsCappedSoItStaysReadable() {
        String source = """
                public class messy {
                    public static int DoStuff(int a, int b, int c, int d, int e, int f) {
                        int q = 1;
                        try {
                            if (a > 0) { if (b > 0) { if (c > 0) { if (d > 0) { q = 2; } } } }
                        } catch (RuntimeException ex) {
                        }
                        return q;
                    }
                }
                """;
        var notes = CodeQualityReview.review(source);
        assertTrue(notes.size() > 1, "this code has several problems");
        assertTrue(notes.size() <= CodeQualityReview.MAX_NOTES,
                "a wall of advice gets scrolled past: " + notes.size() + " notes");
        assertTrue(notes.get(0).severity() <= notes.get(notes.size() - 1).severity(),
                "the most important note must come first");
    }

    @Test
    void aRepeatedProblemIsMentionedOnceNotOncePerLine() {
        String source = """
                public class Repeats {
                    public static boolean check(String a, String b, String c) {
                        return a == "x" || b == "y" || c == "z";
                    }
                }
                """;
        long stringNotes = CodeQualityReview.review(source).stream()
                .filter(n -> n.summary().contains("Strings")).count();
        assertEquals(1, stringNotes);
    }

    @Test
    void unparseableCodeYieldsAdviceRatherThanACrash() {
        assertTrue(CodeQualityReview.review("public class Broken { this is not java").isEmpty());
        assertTrue(CodeQualityReview.review("").isEmpty());
    }
}
