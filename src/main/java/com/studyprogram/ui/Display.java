package com.studyprogram.ui;

import com.studyprogram.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static helpers for rendering questions, results, and stats to the terminal.
 *
 * <p>Presentation is accessibility-aware in two ways. Colour is never the only signal — every
 * coloured state also carries a text or symbol marker, so the display works in monochrome and for
 * colour-blind readers. And output degrades for terminals that cannot render ANSI or box-drawing
 * characters:
 *
 * <ul>
 *   <li>{@code NO_COLOR} (any value, the de-facto standard) or {@code --no-color} disables ANSI.</li>
 *   <li>{@code JAVASTUDY_ASCII=1} (or a non-UTF-8 terminal encoding) swaps box-drawing and emoji
 *       glyphs for plain ASCII, which is what old Windows consoles need.</li>
 * </ul>
 */
public class Display {

    private static boolean colorEnabled = System.getenv("NO_COLOR") == null;
    private static boolean asciiOnly = "1".equals(System.getenv("JAVASTUDY_ASCII"))
            || !String.valueOf(System.getProperty("file.encoding")).toUpperCase().contains("UTF");

    /** Disables ANSI colour (called for {@code --no-color}). */
    public static void disableColor() { colorEnabled = false; }

    /** Forces plain-ASCII glyphs (called for {@code --ascii}). */
    public static void useAsciiGlyphs() { asciiOnly = true; }

    public static boolean isColorEnabled() { return colorEnabled; }
    public static boolean isAsciiOnly()    { return asciiOnly; }

    // ANSI colour codes, suppressed when colour is off
    private static String ansi(String code) { return colorEnabled ? code : ""; }

    public static String reset()  { return ansi("\u001b[0m"); }
    public static String bold()   { return ansi("\u001b[1m"); }
    public static String green()  { return ansi("\u001b[32m"); }
    public static String red()    { return ansi("\u001b[31m"); }
    public static String yellow() { return ansi("\u001b[33m"); }
    public static String cyan()   { return ansi("\u001b[36m"); }
    public static String dim()    { return ansi("\u001b[2m"); }

    // Glyphs: the ASCII forms keep every state distinguishable without colour or Unicode
    public static String tick()      { return asciiOnly ? "[OK]"   : "\u2713"; }
    public static String cross()     { return asciiOnly ? "[X]"    : "\u2717"; }
    public static String star()      { return asciiOnly ? "*"      : "\u2605"; }
    public static String bullet()    { return asciiOnly ? "*"      : "\u25cf"; }
    public static String warnSign()  { return asciiOnly ? "!"      : "\u26a0"; }
    public static String swords()    { return asciiOnly ? ">>"     : "\u2694"; }
    private static String hLine()    { return asciiOnly ? "-"      : "\u2500"; }
    private static String vLine()    { return asciiOnly ? "|"      : "\u2502"; }
    private static String cornerTL() { return asciiOnly ? "+"      : "\u250c"; }
    private static String cornerBL() { return asciiOnly ? "+"      : "\u2514"; }
    private static String barFull()  { return asciiOnly ? "#"      : "\u2588"; }
    private static String barEmpty() { return asciiOnly ? "."      : "\u2591"; }
    private static String filledStar()  { return asciiOnly ? "*"   : "\u2605"; }
    private static String hollowStar()  { return asciiOnly ? "."   : "\u2606"; }

    private static final int WIDTH = 72;

    public static void rule() {
        System.out.println(hLine().repeat(WIDTH));
    }

    public static void header(String text) {
        System.out.println();
        rule();
        System.out.println(bold() + center(text, WIDTH) + reset());
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
        System.out.printf(bold() + "  Question %d/%d" + reset()
                + "  " + vLine() + "  Topic: " + cyan() + "%s" + reset()
                + "  " + vLine() + "  Type: " + yellow() + "%s" + reset()
                + "  " + vLine() + "  Difficulty: %s%n",
                number, total,
                q.getTopic().displayName,
                q.getType().displayName,
                stars(q.getDifficulty()));
        rule();
        System.out.println();
        System.out.println(q.getPrompt());

        if (q.hasCode()) {
            System.out.println();
            System.out.println(dim() + "  " + cornerTL() + hLine() + " Java " + hLine().repeat(WIDTH - 10) + reset());
            for (String line : q.getCode().split("\n")) {
                System.out.println(dim() + "  " + vLine() + reset() + "  " + line);
            }
            System.out.println(dim() + "  " + cornerBL() + hLine().repeat(WIDTH - 3) + reset());
        }

        if (q.getType() == QuestionType.PARSONS) {
            System.out.println();
            List<String> lines = q.getShuffledLines();
            System.out.println(dim() + "  " + cornerTL() + hLine() + " Scrambled lines " + hLine().repeat(WIDTH - 21) + reset());
            for (int i = 0; i < lines.size(); i++) {
                System.out.printf("%s  " + vLine() + "%s %2d:  %s%n", dim(), reset(), i + 1, lines.get(i));
            }
            System.out.println(dim() + "  " + cornerBL() + hLine().repeat(WIDTH - 3) + reset());
            System.out.println();
            System.out.print("  Line numbers in order (e.g. 3 1 4 2) or [h]int [e]xplain [s]kip [f]lag [q]uit: ");
        } else if (q.getType() == QuestionType.FADED) {
            int blanks = com.studyprogram.questions.FadedExampleDeriver.blankCount(q);
            System.out.println();
            if (blanks == 1) {
                System.out.print("  Type the line that belongs at the blank, "
                        + "or [h]int [e]xplain [s]kip [f]lag [q]uit: ");
            } else {
                System.out.println("  " + dim() + "You will be asked for each of the " + blanks
                        + " blanks in turn." + reset());
                System.out.print("  Line for blank 1, or [h]int [e]xplain [s]kip [f]lag [q]uit: ");
            }
        } else if (q.getType() == QuestionType.CLOZE) {
            System.out.println();
            System.out.print("  Type the missing code (the ____ part) or [h]int [e]xplain [s]kip [f]lag [q]uit: ");
        } else if (q.isMultipleChoice()) {
            System.out.println();
            List<String> choices = q.getChoices();
            String[] letters = {"A", "B", "C", "D"};
            for (int i = 0; i < choices.size(); i++) {
                System.out.printf("  %s) %s%n", letters[i], choices.get(i));
            }
            System.out.println();
            System.out.print("  Answer (A/B/C/D) or [h]int [e]xplain [s]kip [f]lag [q]uit: ");
        } else {
            System.out.println();
            System.out.print("  Answer or [h]int [e]xplain [s]kip [f]lag [q]uit: ");
        }
    }

    /** Renders a CODING exercise: prompt plus workspace instructions and the command menu. */
    public static void codingQuestion(Question q, int number, int total, java.nio.file.Path file) {
        System.out.println();
        rule();
        System.out.printf(bold() + "  Question %d/%d" + reset()
                + "  " + vLine() + "  Topic: " + cyan() + "%s" + reset()
                + "  " + vLine() + "  Type: " + yellow() + "%s" + reset()
                + "  " + vLine() + "  Difficulty: %s%n",
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
                System.out.println("  " + bold() + cyan() + dir.resolve(name).toAbsolutePath() + reset());
            }
            System.out.println();
            System.out.println("  You may also add extra .java files of your own to that folder.");
        } else {
            System.out.println("  Edit this file in your editor or IDE:");
            System.out.println("  " + bold() + cyan() + file.toAbsolutePath() + reset());
        }
        System.out.println();
        System.out.println("  The full task description is in a comment at the top of the file.");
        codingMenu();
    }

    public static void codingMenu() {
        System.out.println();
        System.out.print("  [Enter] compile & test   [w]atch for saves   [h]int  [e]xplain  "
                + "[r]eset file  [g]ive up  [s]kip  [q]uit\n  > ");
    }

    /** Renders the outcome of one compile-and-test run. */
    public static void codingResult(com.studyprogram.coding.CodingResult result) {
        System.out.println();
        switch (result.status()) {
            case PASS -> System.out.println(green() + bold() + tick() + " Compiled — all tests passed!" + reset());
            case TEST_FAILURE -> System.out.println(red() + bold() + cross() + " Compiled, but some tests failed:" + reset());
            case COMPILE_ERROR -> System.out.println(red() + bold() + cross() + " Compile error:" + reset());
            case TIMEOUT -> System.out.println(red() + bold() + cross() + " Timed out:" + reset());
            case ENVIRONMENT_ERROR -> System.out.println(yellow() + bold() + warnSign() + " Environment problem:" + reset());
        }
        String body = result.output();
        if (body != null && !body.isBlank()) {
            // The decoder's plain-English blocks are the part a stuck student should actually read,
            // so they are the one thing here that is not dimmed.
            boolean inExplanation = false;
            for (String line : body.split("\n")) {
                if (line.startsWith("What that means")) inExplanation = true;
                else if (line.isBlank()) inExplanation = false;
                String color = line.startsWith("PASS") ? green()
                        : line.startsWith("FAIL") ? red()
                        : line.startsWith("What that means") ? cyan() + bold()
                        : inExplanation ? cyan()
                        : dim();
                System.out.println("  " + color + line + reset());
            }
        }
    }

    /** Shows the reference solution (used when the student gives up on a coding exercise). */
    public static void referenceSolution(Question q) {
        System.out.println();
        System.out.println(yellow() + bold() + "  Reference solution:" + reset());
        System.out.println(dim() + "  " + cornerTL() + hLine() + " Java " + hLine().repeat(WIDTH - 10) + reset());
        for (String line : q.getAnswer().split("\n")) {
            System.out.println(dim() + "  " + vLine() + reset() + "  " + line);
        }
        System.out.println(dim() + "  " + cornerBL() + hLine().repeat(WIDTH - 3) + reset());
        if (!q.getExplanation().isBlank()) {
            System.out.println();
            System.out.println(dim() + "  " + q.getExplanation() + reset());
        }
    }

    public static void correct(GradingResult result) {
        System.out.println();
        System.out.println(green() + bold() + tick() + " Correct!" + reset());
        if (!result.explanation().isBlank()) {
            System.out.println(dim() + "  " + result.explanation() + reset());
        }
    }

    public static void incorrect(GradingResult result) {
        System.out.println();
        System.out.println(red() + bold() + cross() + " Incorrect" + reset());
        System.out.println("  " + result.feedback());
        if (!result.explanation().isBlank()) {
            System.out.println();
            System.out.println(dim() + "  " + result.explanation() + reset());
        }
        if (result.hasLLMFeedback()) {
            System.out.println();
            System.out.println(yellow() + "  AI: " + result.llmFeedback() + reset());
        }
    }

    public static void sessionSummary(int answered, int correct, int skipped, long seconds,
                                      Map<Topic, long[]> breakdown) {
        System.out.println();
        header("Session Summary");
        System.out.printf("  Total questions : %d%n", answered);
        System.out.printf("  Correct         : %s%d%s%n",
                          correct == answered ? green() : yellow(), correct, reset());
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
                String color = pct >= 80 ? green() : pct >= 50 ? yellow() : red();
                System.out.printf("    %-40s %s%3d%%%s  (%d/%d)%n",
                        topic.displayName, color, pct, reset(), right, total);
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
            boolean placed      = p != null && p.isPlacementSeeded();
            boolean selected    = selectedSet.contains(t);
            if (!selected && !hasAttempts && !placed) continue;

            double mastery = (p == null) ? 0.0 : p.getMasteryScore();
            int pct = (int)(mastery * 100);
            String marker = selected ? cyan() + bullet() + reset() : " ";
            String color  = pct >= 80 ? green() : pct >= 40 ? yellow() : dim();
            System.out.printf("  %s %-40s %s%s%s  %s%n",
                    marker,
                    t.displayName,
                    color,
                    bar(pct),
                    reset(),
                    p == null ? ""
                            : placed ? "opened by placement — not yet practised"
                            : String.format("(%d/%d) %s",
                            p.getCorrect(), p.getAttempts(),
                            com.studyprogram.stats.Confidence.label(p.getCorrect(), p.getAttempts())));
            anyShown = true;
        }
        if (!anyShown) {
            System.out.println("  No topics selected yet. Use [3] Select Topics to get started.");
        }
        rule();
        System.out.println("  " + cyan() + bullet() + reset() + " = currently selected");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String stars(int difficulty) {
        return filledStar().repeat(difficulty) + hollowStar().repeat(5 - difficulty);
    }

    private static String bar(int pct) {
        int filled = pct / 5;   // 0–20 segments
        return "[" + barFull().repeat(filled) + barEmpty().repeat(20 - filled) + "] " + pct + "%";
    }

    private static String center(String s, int width) {
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        return " ".repeat(pad) + s;
    }
}
