package com.studyprogram.ui;

import com.studyprogram.coding.CodingResult;
import com.studyprogram.model.Question;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Drives one coding exercise: writes the starter into the workspace, then loops compile-and-test
 * runs until the tests pass or the student gives up.
 *
 * <p>Lifted out of {@link CLI}, which had grown past 1,700 lines by accumulating one more screen
 * per feature. This is the most self-contained of those screens — it needs the exercise runner,
 * the help service and the console, all of which live on {@link StudyContext} — so it is the
 * natural first thing to give its own file.
 */
class CodingFlow {

    private final StudyContext ctx;

    CodingFlow(StudyContext ctx) {
        this.ctx = ctx;
    }

    public enum Outcome { CORRECT, GAVE_UP, SKIPPED, QUIT }

    /**
     * Drives one coding exercise: writes the starter file to the workspace, then loops
     * compile-and-test runs until the tests pass or the student gives up, skips, or quits.
     */
    public Outcome run(Question q, int qNum, int total) {
        Path file;
        try {
            file = ctx.codingRunner.prepareWorkspace(q);
        } catch (IOException e) {
            System.out.println("  Could not prepare the workspace: " + e.getMessage());
            return Outcome.SKIPPED;
        }

        Display.codingQuestion(q, qNum, total, file);
        ctx.lastCodingHints = 0;
        ctx.lastCodingErrors.clear();

        while (true) {
            String input = ctx.in.nextLine().trim().toLowerCase();
            switch (input) {
                case "q" -> { return Outcome.QUIT; }
                case "s" -> { return Outcome.SKIPPED; }
                case "g" -> {
                    Display.referenceSolution(q);
                    return Outcome.GAVE_UP;
                }
                case "w" -> {
                    watchAndRetest(q);
                    Display.codingMenu();
                }
                case "r" -> {
                    try {
                        ctx.codingRunner.resetToStarter(q);
                        System.out.println("  File reset to the original starter code.");
                    } catch (IOException e) {
                        System.out.println("  Could not reset the file: " + e.getMessage());
                    }
                    Display.codingMenu();
                }
                case "h" -> {
                    if (ctx.noHelp) {
                        System.out.println("  " + Display.yellow()
                                + "No hints during an exam." + Display.reset());
                        Display.codingMenu();
                        break;
                    }
                    String hint = ctx.llm.generateHint(q, ctx.lastCodingHints);
                    ctx.lastCodingHints++;
                    System.out.println("  Hint: " + Display.yellow() + hint + Display.reset());
                    Display.codingMenu();
                }
                case "e" -> {
                    if (ctx.noHelp) {
                        System.out.println("  " + Display.yellow()
                                + "No explanations during an exam." + Display.reset());
                    } else {
                        System.out.println("  " + Display.dim()
                                + ctx.llm.explainConcept(q.getTopic(), q.getPrompt()) + Display.reset());
                    }
                    Display.codingMenu();
                }
                default -> {   // Enter (or anything else) runs the tests
                    System.out.println("  Compiling and running tests…");
                    CodingResult result = ctx.codingRunner.run(q);
                    ctx.lastCodingErrors.addAll(result.errorKinds());
                    Display.codingResult(result);
                    if (result.passed()) {
                        showQualityReview(q);
                        return Outcome.CORRECT;
                    }
                    if (result.status() == CodingResult.Status.ENVIRONMENT_ERROR) {
                        // not the student's fault — don't count it against them
                        return Outcome.SKIPPED;
                    }
                    Display.codingMenu();
                }
            }
        }
    }

    /**
     * After the tests go green, says what an instructor would circle in the margin.
     *
     * <p>Deliberately placed after the pass, never before it: correctness is what the exercise is
     * marked on, and advice offered while a student is still fighting to compile would read as one
     * more thing they had got wrong.
     */
    void showQualityReview(Question q) {
        String source = studentSource(q);
        if (source == null) return;
        String review = com.studyprogram.coding.CodeQualityReview.render(source);
        if (review.isBlank()) return;

        System.out.println();
        System.out.println("  " + Display.cyan() + "It works. Worth tightening:" + Display.reset());
        for (String line : review.split("\n")) {
            System.out.println("  " + Display.dim() + line + Display.reset());
        }
    }

    /** The student's own source, or null when it cannot be read. */
    private String studentSource(Question q) {
        try {
            java.nio.file.Path file = ctx.codingRunner.studentFile(q);
            if (!java.nio.file.Files.isRegularFile(file)) return null;
            return java.nio.file.Files.readString(file);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Watch mode: recompiles and retests whenever the student saves, so the loop is
     * "save in your editor, glance at the terminal" instead of alt-tabbing to press Enter.
     * Stops when the tests pass or the student presses Enter.
     */
    void watchAndRetest(Question q) {
        System.out.println("  Watching " + ctx.codingRunner.workspaceDir(q).toAbsolutePath()
                + " — save to re-run. Press Enter to stop.");
        long lastRun = 0;
        try {
            while (true) {
                if (System.in.available() > 0) {
                    ctx.in.nextLine();
                    System.out.println("  Stopped watching.");
                    return;
                }
                long newest = newestModification(ctx.codingRunner.workspaceDir(q));
                if (newest > lastRun) {
                    lastRun = newest;
                    if (lastRun > 0) {
                        System.out.println("  Change detected — compiling…");
                        CodingResult result = ctx.codingRunner.run(q);
                        ctx.lastCodingErrors.addAll(result.errorKinds());
                        Display.codingResult(result);
                        if (result.passed()) {
                            showQualityReview(q);
                            System.out.println("  " + Display.green()
                                    + "Tests pass — press Enter to continue." + Display.reset());
                            return;
                        }
                    }
                }
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println("  Watch mode unavailable: " + e.getMessage());
        }
    }

    /** Most recent modification time across the exercise's .java files, or 0 when unreadable. */
    private static long newestModification(java.nio.file.Path dir) {
        try (var files = java.nio.file.Files.list(dir)) {
            return files.filter(f -> f.toString().endsWith(".java"))
                    .mapToLong(f -> {
                        try {
                            return java.nio.file.Files.getLastModifiedTime(f).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .max().orElse(0L);
        } catch (IOException e) {
            return 0L;
        }
    }
}
