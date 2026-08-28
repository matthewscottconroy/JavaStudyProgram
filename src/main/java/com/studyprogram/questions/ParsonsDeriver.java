package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Derives a Parsons problem (reorder-the-lines) from a CODING exercise's reference
 * solution. Derivation is deterministic — the shuffle is seeded by the question id —
 * so every student sees the same puzzle and derived questions are stable across runs.
 *
 * Research on CS learning places Parsons problems as the bridge between reading
 * code and writing it: the student reasons about full-program structure without
 * also fighting syntax recall.
 */
public final class ParsonsDeriver {

    private static final int MIN_LINES = 6;
    private static final int MAX_LINES = 14;

    private ParsonsDeriver() {}

    /**
     * Derives a Parsons question from a coding exercise, or empty when the solution
     * is too short or too long to make a good puzzle.
     */
    public static Optional<Question> derive(Question coding) {
        if (coding.getType() != QuestionType.CODING) return Optional.empty();
        if (coding.isMultiFile()) return Optional.empty();  // one-file puzzles only

        List<String> lines = solutionLines(coding.getAnswer());
        if (lines.size() < MIN_LINES || lines.size() > MAX_LINES) return Optional.empty();

        List<String> shuffled = new ArrayList<>(lines);
        Random rng = new Random(coding.getId().hashCode());
        do {
            Collections.shuffle(shuffled, rng);
        } while (shuffled.equals(lines));

        return Optional.of(Question.builder()
                .id(coding.getId() + "-parsons")
                .topic(coding.getTopic())
                .type(QuestionType.PARSONS)
                .difficulty(Math.max(1, coding.getDifficulty() - 1))
                .prompt("The lines of a working program are shown in scrambled order "
                        + "(indentation is preserved). " + coding.getPrompt())
                .shuffledLines(shuffled)
                .answer(String.join("\n", lines))
                .explanation(coding.getExplanation())
                .build());
    }

    /**
     * The solution reduced to orderable lines: comments and blank lines dropped,
     * original indentation kept (it is a legitimate hint about nesting).
     */
    static List<String> solutionLines(String solution) {
        List<String> lines = new ArrayList<>();
        boolean inBlockComment = false;
        for (String raw : solution.split("\n")) {
            String line = raw.replaceAll("\\s+$", "");
            String trimmed = line.trim();
            if (inBlockComment) {
                if (trimmed.contains("*/")) inBlockComment = false;
                continue;
            }
            if (trimmed.startsWith("/*")) {
                if (!trimmed.contains("*/")) inBlockComment = true;
                continue;
            }
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("*")) continue;
            lines.add(line);
        }
        return lines;
    }
}
