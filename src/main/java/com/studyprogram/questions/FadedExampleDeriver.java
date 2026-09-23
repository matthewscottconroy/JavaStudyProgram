package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Derives faded worked examples from a coding exercise's reference solution.
 *
 * <p>This is the rung below a Parsons problem. A beginner told to "write a method that returns the
 * largest value in an array" has to hold the algorithm, the syntax and the structure in mind at
 * once; the usual result is a blank screen. A worked example removes all three costs — here is a
 * correct solution, study it — and then fades support back out one step at a time: first one line
 * is missing, then several, and finally the student writes the whole thing from scratch as the
 * original {@link QuestionType#CODING} exercise. Completion problems of exactly this shape are
 * one of the better-supported results in the instructional-design literature, and the reason the
 * effect holds is that each step asks for one new thing rather than everything at once.
 *
 * <p>Like {@link ParsonsDeriver} this costs nothing to author: the solutions already exist, so
 * every verified coding exercise yields its own scaffolded ladder, derived deterministically at
 * load time.
 */
public final class FadedExampleDeriver {

    /** Solutions shorter than this have nothing meaningful left once a line is removed. */
    private static final int MIN_LINES = 6;
    /** Beyond this a "study the whole example" screen stops being studied and starts being skipped. */
    private static final int MAX_LINES = 30;

    /** Structure and boilerplate: nothing is learned by retyping these. */
    private static final Pattern BORING = Pattern.compile(
            "^\\s*(\\}.*|\\{|import .*|package .*|(public |final |abstract )*class .*"
            + "|(public |private |protected )?(static )?(interface|enum|record) .*)\\s*$");

    /**
     * A method or constructor declaration. These are excluded deliberately: the point of fading is
     * to practise the logic, and a student asked to reproduce a signature exactly is being tested
     * on transcription. Control-flow headers ({@code for}, {@code if}, {@code while}) look similar
     * but are kept — getting a loop header right is exactly the skill being practised.
     */
    private static final Pattern DECLARATION = Pattern.compile(
            "^\\s*(?!(if|for|while|switch|catch|try|else|do|synchronized|return)\\b)"
            + "[\\w<>\\[\\],.?\\s&]*\\w+\\s*\\([^;]*\\)\\s*(throws [\\w,.\\s]+)?\\{\\s*$");

    /**
     * Introduces the worked commentary inside a faded question's prompt. Public because the
     * printed worksheet has to find and remove it: on screen the commentary is the point of the
     * step, but on a quiz handed to a class it explains the very lines the blanks ask for.
     */
    public static final String COMMENTARY_HEADING = "How this solution works:";

    private FadedExampleDeriver() {}

    /**
     * The ladder for one coding exercise: a one-blank rung and a three-blank rung, in that order.
     * Empty when the solution is the wrong shape to fade.
     */
    public static List<Question> derive(Question coding) {
        List<Question> rungs = new ArrayList<>();
        if (coding.getType() != QuestionType.CODING || coding.isMultiFile()) return rungs;

        List<String> lines = ParsonsDeriver.solutionLines(coding.getAnswer());
        if (lines.size() < MIN_LINES || lines.size() > MAX_LINES) return rungs;

        List<Integer> candidates = fadeable(lines);
        if (candidates.size() < 2) return rungs;

        build(coding, lines, pick(candidates, 1, coding.getId()), 1).ifPresent(rungs::add);
        if (candidates.size() >= 3) {
            build(coding, lines, pick(candidates, 3, coding.getId()), 3).ifPresent(rungs::add);
        }
        return rungs;
    }

    /** Indices of lines worth blanking: real statements, not punctuation or boilerplate. */
    static List<Integer> fadeable(List<String> lines) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (BORING.matcher(line).matches()) continue;
            if (DECLARATION.matcher(line).matches()) continue;
            if (line.strip().length() < 4) continue;
            indices.add(i);
        }
        return indices;
    }

    /**
     * Chooses which lines to fade: spread evenly through the candidates rather than clustered, so
     * a student cannot reconstruct a blank purely from the line above it. Deterministic in the
     * question id, so everyone sees the same exercise.
     */
    static List<Integer> pick(List<Integer> candidates, int count, String id) {
        List<Integer> chosen = new ArrayList<>();
        int n = candidates.size();
        int offset = Math.floorMod(id.hashCode(), Math.max(1, n / Math.max(1, count)));
        for (int k = 0; k < count && chosen.size() < n; k++) {
            int index = Math.min(n - 1, offset + (int) Math.round((double) k * n / count));
            while (chosen.contains(candidates.get(index)) && index < n - 1) index++;
            while (chosen.contains(candidates.get(index)) && index > 0) index--;
            if (!chosen.contains(candidates.get(index))) chosen.add(candidates.get(index));
        }
        chosen.sort(Integer::compareTo);
        return chosen;
    }

    private static Optional<Question> build(Question coding, List<String> lines,
                                            List<Integer> blanks, int rung) {
        if (blanks.size() != rung) return Optional.empty();

        StringBuilder listing = new StringBuilder();
        List<String> missing = new ArrayList<>();
        int blankNumber = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (blanks.contains(i)) {
                blankNumber++;
                String indent = lines.get(i).substring(0,
                        lines.get(i).length() - lines.get(i).stripLeading().length());
                listing.append(indent).append(">>> blank ").append(blankNumber).append(" <<<");
                missing.add(lines.get(i).strip());
            } else {
                listing.append(lines.get(i));
            }
            listing.append('\n');
        }

        String commentary = coding.getExplanation() == null || coding.getExplanation().isBlank()
                ? "" : "\n\n" + COMMENTARY_HEADING + "\n" + coding.getExplanation();

        String prompt = (rung == 1
                ? "A worked solution to this exercise, with one line faded out. Study the rest, "
                  + "then supply the missing line.\n\n"
                : "The same worked solution with more of it faded out. Supply each missing line "
                  + "in order.\n\n")
                + coding.getPrompt() + commentary;

        return Optional.of(Question.builder()
                .id(coding.getId() + "-faded" + rung)
                .topic(coding.getTopic())
                .type(QuestionType.FADED)
                .difficulty(Math.max(1, coding.getDifficulty() - (rung == 1 ? 2 : 1)))
                .prompt(prompt)
                .code(listing.toString().stripTrailing())
                .answer(String.join("\n", missing))
                .explanation(coding.getExplanation())
                .build());
    }

    /** How many lines a faded question expects back. */
    public static int blankCount(Question faded) {
        return faded.getAnswer().split("\n").length;
    }
}
