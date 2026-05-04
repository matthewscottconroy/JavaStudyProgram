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

        if (q.isMultipleChoice()) {
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
