package com.studyprogram.ui;

import com.studyprogram.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Static helpers for rendering questions, results, and stats to the terminal. */
public class Display {

    // ANSI color codes (Jansi AnsiConsole.systemInstall() ensures these work on Windows too)
    public static final String RESET  = "[0m";
    public static final String BOLD   = "[1m";
    public static final String GREEN  = "[32m";
    public static final String RED    = "[31m";
    public static final String YELLOW = "[33m";
    public static final String CYAN   = "[36m";
    public static final String DIM    = "[2m";

    private static final int WIDTH = 72;

    public static void rule() {
        System.out.println("─".repeat(WIDTH));
    }

    public static void header(String text) {
        System.out.println();
        rule();
        System.out.println(BOLD + center(text, WIDTH) + RESET);
        rule();
    }

    /** Produce a summary label for the active topic list, truncated if long. */
    public static String topicSummary(List<Topic> topics) {
        if (topics.isEmpty()) return "(none)";
        if (topics.size() <= 3) {
            return topics.stream().map(t -> t.displayName).collect(Collectors.joining(", "));
        }
        return topics.get(0).displayName + ", " + topics.get(1).displayName
                + " and " + (topics.size() - 2) + " more";
    }

    public static void question(Question q, int number, int total) {
        System.out.println();
        rule();
        System.out.printf(BOLD + "  Question %d/%d" + RESET
                + "  │  Topic: " + CYAN + "%s" + RESET
                + "  │  Type: " + YELLOW + "%s" + RESET
                + "  │  Difficulty: %s%n",
                number, total,
                q.getTopic().displayName,
                q.getType().displayName,
                stars(q.getDifficulty()));
        rule();
        System.out.println();
        System.out.println(q.getPrompt());

        if (q.hasCode()) {
            System.out.println();
            System.out.println(DIM + "  ┌─ Java " + "─".repeat(WIDTH - 10) + RESET);
            for (String line : q.getCode().split("\n")) {
                System.out.println(DIM + "  │" + RESET + "  " + line);
            }
            System.out.println(DIM + "  └" + "─".repeat(WIDTH - 3) + RESET);
        }

        if (q.getType() == QuestionType.PARSONS) {
            System.out.println();
            List<String> lines = q.getShuffledLines();
            System.out.println(DIM + "  ┌─ Scrambled lines " + "─".repeat(WIDTH - 21) + RESET);
            for (int i = 0; i < lines.size(); i++) {
                System.out.printf("%s  │%s %2d:  %s%n", DIM, RESET, i + 1, lines.get(i));
            }
            System.out.println(DIM + "  └" + "─".repeat(WIDTH - 3) + RESET);
            System.out.println();
            System.out.print("  Line numbers in order (e.g. 3 1 4 2) or [h]int [e]xplain [s]kip [q]uit: ");
        } else if (q.getType() == QuestionType.CLOZE) {
            System.out.println();
            System.out.print("  Type the missing code (the ____ part) or [h]int [e]xplain [s]kip [q]uit: ");
        } else if (q.isMultipleChoice()) {
            System.out.println();
            List<String> choices = q.getChoices();
            String[] letters = {"A", "B", "C", "D"};
            for (int i = 0; i < choices.size(); i++) {
                System.out.printf("  %s) %s%n", letters[i], choices.get(i));
            }
            System.out.println();
            System.out.print("  Answer (A/B/C/D) or [h]int [e]xplain [s]kip [q]uit: ");
        } else {
            System.out.println();
            System.out.print("  Answer or [h]int [e]xplain [s]kip [q]uit: ");
        }
    }

    /** Renders a CODING exercise: prompt plus workspace instructions and the command menu. */
    public static void codingQuestion(Question q, int number, int total, java.nio.file.Path file) {
        System.out.println();
        rule();
        System.out.printf(BOLD + "  Question %d/%d" + RESET
                + "  │  Topic: " + CYAN + "%s" + RESET
                + "  │  Type: " + YELLOW + "%s" + RESET
                + "  │  Difficulty: %s%n",
                number, total,
                q.getTopic().displayName,
                q.getType().displayName,
                stars(q.getDifficulty()));
        rule();
        System.out.println();
        System.out.println(q.getPrompt());
        System.out.println();
        if (q.isMultiFile()) {
            System.out.println("  This is a project exercise — edit these files in your editor or IDE:");
            java.nio.file.Path dir = file.getParent();
            for (String name : q.getStarterFiles().keySet()) {
                System.out.println("  " + BOLD + CYAN + dir.resolve(name).toAbsolutePath() + RESET);
            }
            System.out.println();
            System.out.println("  You may also add extra .java files of your own to that folder.");
        } else {
            System.out.println("  Edit this file in your editor or IDE:");
            System.out.println("  " + BOLD + CYAN + file.toAbsolutePath() + RESET);
        }
        System.out.println();
        System.out.println("  The full task description is in a comment at the top of the file.");
        codingMenu();
    }

    public static void codingMenu() {
        System.out.println();
        System.out.print("  [Enter] compile & test   [h]int  [e]xplain  [r]eset file  "
                + "[g]ive up  [s]kip  [q]uit\n  > ");
    }

    /** Renders the outcome of one compile-and-test run. */
    public static void codingResult(com.studyprogram.coding.CodingResult result) {
        System.out.println();
        switch (result.status()) {
            case PASS -> System.out.println(GREEN + BOLD + "✓ Compiled — all tests passed!" + RESET);
            case TEST_FAILURE -> System.out.println(RED + BOLD + "✗ Compiled, but some tests failed:" + RESET);
            case COMPILE_ERROR -> System.out.println(RED + BOLD + "✗ Compile error:" + RESET);
            case TIMEOUT -> System.out.println(RED + BOLD + "✗ Timed out:" + RESET);
            case ENVIRONMENT_ERROR -> System.out.println(YELLOW + BOLD + "! Environment problem:" + RESET);
        }
        String body = result.output();
        if (body != null && !body.isBlank()) {
            for (String line : body.split("\n")) {
                String color = line.startsWith("PASS") ? GREEN : line.startsWith("FAIL") ? RED : DIM;
                System.out.println("  " + color + line + RESET);
            }
        }
    }

    /** Shows the reference solution (used when the student gives up on a coding exercise). */
    public static void referenceSolution(Question q) {
        System.out.println();
        System.out.println(YELLOW + BOLD + "  Reference solution:" + RESET);
        System.out.println(DIM + "  ┌─ Java " + "─".repeat(WIDTH - 10) + RESET);
        for (String line : q.getAnswer().split("\n")) {
            System.out.println(DIM + "  │" + RESET + "  " + line);
        }
        System.out.println(DIM + "  └" + "─".repeat(WIDTH - 3) + RESET);
        if (!q.getExplanation().isBlank()) {
            System.out.println();
            System.out.println(DIM + "  " + q.getExplanation() + RESET);
        }
    }

    public static void correct(GradingResult result) {
        System.out.println();
        System.out.println(GREEN + BOLD + "✓ Correct!" + RESET);
        if (!result.explanation().isBlank()) {
            System.out.println(DIM + "  " + result.explanation() + RESET);
        }
    }

    public static void incorrect(GradingResult result) {
        System.out.println();
        System.out.println(RED + BOLD + "✗ Incorrect" + RESET);
        System.out.println("  " + result.feedback());
        if (!result.explanation().isBlank()) {
            System.out.println();
            System.out.println(DIM + "  " + result.explanation() + RESET);
        }
        if (result.hasLLMFeedback()) {
            System.out.println();
            System.out.println(YELLOW + "  AI: " + result.llmFeedback() + RESET);
        }
    }

    public static void sessionSummary(int answered, int correct, int skipped, long seconds,
                                      Map<Topic, long[]> breakdown) {
        System.out.println();
        header("Session Summary");
        System.out.printf("  Total questions : %d%n", answered);
        System.out.printf("  Correct         : %s%d%s%n",
                          correct == answered ? GREEN : YELLOW, correct, RESET);
        System.out.printf("  Accuracy        : %.0f%%%n",
                          answered == 0 ? 0.0 : 100.0 * correct / answered);
        if (skipped > 0) {
            System.out.printf("  Skipped         : %d%n", skipped);
        }
        System.out.printf("  Time            : %d:%02d%n", seconds / 60, seconds % 60);

        if (!breakdown.isEmpty()) {
            System.out.println();
            System.out.println("  By topic:");
            breakdown.forEach((topic, counts) -> {
                long total = counts[0], right = counts[1];
                int pct = total == 0 ? 0 : (int)(100 * right / total);
                String color = pct >= 80 ? GREEN : pct >= 50 ? YELLOW : RED;
                System.out.printf("    %-40s %s%3d%%%s  (%d/%d)%n",
                        topic.displayName, color, pct, RESET, right, total);
            });
        }
        rule();
    }

    /**
     * Show performance for selected topics and any topic the student has attempted.
     * Skips topics that are neither selected nor ever attempted, keeping the table focused.
     */
    public static void performanceTable(Map<Topic, TopicPerformance> performance,
                                        List<Topic> selectedTopics) {
        Set<Topic> selectedSet = Set.copyOf(selectedTopics);
        System.out.println();
        header("Performance Overview");
        boolean anyShown = false;
        for (Topic t : Topic.values()) {
            TopicPerformance p = performance.get(t);
            boolean hasAttempts = p != null && p.getAttempts() > 0;
            boolean selected    = selectedSet.contains(t);
            if (!selected && !hasAttempts) continue;

            double mastery = (p == null) ? 0.0 : p.getMasteryScore();
            int pct = (int)(mastery * 100);
            String marker = selected ? CYAN + "●" + RESET : " ";
            String color  = pct >= 80 ? GREEN : pct >= 40 ? YELLOW : DIM;
            System.out.printf("  %s %-44s %s%s%s  %s%n",
                    marker,
                    t.displayName,
                    color,
                    bar(pct),
                    RESET,
                    p == null ? "" : String.format("(%d/%d)", p.getCorrect(), p.getAttempts()));
            anyShown = true;
        }
        if (!anyShown) {
            System.out.println("  No topics selected yet. Use [3] Select Topics to get started.");
        }
        rule();
        System.out.println("  " + CYAN + "●" + RESET + " = currently selected");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String stars(int difficulty) {
        return "★".repeat(difficulty) + "☆".repeat(5 - difficulty);
    }

    private static String bar(int pct) {
        int filled = pct / 5;   // 0–20 segments
        return "[" + "█".repeat(filled) + "░".repeat(20 - filled) + "] " + pct + "%";
    }

    private static String center(String s, int width) {
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        return " ".repeat(pad) + s;
    }
}
