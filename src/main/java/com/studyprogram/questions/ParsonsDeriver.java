package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Derives a Parsons problem (reorder-the-lines) from a CODING exercise's reference solution.
 * Derivation is deterministic — the shuffle is seeded by the question id — so every student sees
 * the same puzzle and derived questions are stable across runs.
 *
 * <p>Research on CS learning places Parsons problems as the bridge between reading code and
 * writing it: the student reasons about program structure without also fighting syntax recall.
 *
 * <p>Two strategies, in order:
 * <ol>
 *   <li><b>Whole program</b> when the solution is short enough to shuffle as one puzzle.</li>
 *   <li><b>One method body</b> otherwise. Most solutions are longer than a good puzzle (the median
 *       is around two dozen lines), and shuffling all of them would be tedious rather than
 *       instructive — but the body of a single interesting method is exactly the right size. The
 *       method signature is shown as fixed context and only its body is scrambled.</li>
 * </ol>
 */
public final class ParsonsDeriver {

    private static final int MIN_LINES = 5;
    private static final int MAX_LINES = 16;

    /** A line that opens a method: has a parameter list, ends with '{', and is not a control form. */
    private static final Pattern METHOD_DECL = Pattern.compile(
            "^\\s*(?!\\s*(if|for|while|switch|catch|try|else|do|synchronized|return)\\b)"
            + "[\\w<>\\[\\],.?\\s&]*\\w+\\s*\\([^;]*\\)\\s*(throws [\\w,.\\s]+)?\\{\\s*$");
    private static final Pattern TYPE_DECL = Pattern.compile(
            "^\\s*(public\\s+|private\\s+|protected\\s+|static\\s+|final\\s+|abstract\\s+)*"
            + "(class|interface|enum|record)\\b.*");

    private ParsonsDeriver() {}

    /** A method's opening line plus the lines of its body. */
    record MethodBody(String signature, List<String> body) {}

    /**
     * Derives a Parsons question from a coding exercise, or empty when no part of the solution
     * makes a well-sized puzzle.
     */
    public static Optional<Question> derive(Question coding) {
        if (coding.getType() != QuestionType.CODING) return Optional.empty();
        if (coding.isMultiFile()) return Optional.empty();   // one-file puzzles only

        List<String> lines = solutionLines(coding.getAnswer());

        if (lines.size() >= MIN_LINES && lines.size() <= MAX_LINES) {
            return Optional.of(build(coding, lines,
                    "The lines of a working program are shown in scrambled order "
                    + "(indentation is preserved). " + coding.getPrompt()));
        }

        return bestMethodBody(lines).map(method -> build(coding, method.body(),
                "The body of this method is scrambled (indentation is preserved). Put it back in "
                + "order:\n\n    " + method.signature().trim() + "\n\n" + coding.getPrompt()));
    }

    private static Question build(Question coding, List<String> ordered, String prompt) {
        List<String> shuffled = new ArrayList<>(ordered);
        Random rng = new Random(coding.getId().hashCode());
        int guard = 0;
        do {
            Collections.shuffle(shuffled, rng);
        } while (shuffled.equals(ordered) && ++guard < 10);

        return Question.builder()
                .id(coding.getId() + "-parsons")
                .topic(coding.getTopic())
                .type(QuestionType.PARSONS)
                .difficulty(Math.max(1, coding.getDifficulty() - 1))
                .prompt(prompt)
                .shuffledLines(shuffled)
                .answer(String.join("\n", ordered))
                .explanation(coding.getExplanation())
                .build();
    }

    /**
     * The most substantial method body that would make a well-sized puzzle: the longest one that
     * fits, so trivial getters lose to the method that carries the exercise's actual logic.
     */
    static Optional<MethodBody> bestMethodBody(List<String> lines) {
        return methodBodies(lines).stream()
                .filter(m -> m.body().size() >= MIN_LINES && m.body().size() <= MAX_LINES)
                .max(java.util.Comparator.comparingInt(m -> m.body().size()));
    }

    /** Every method in the (comment-stripped) source, found by brace matching. */
    static List<MethodBody> methodBodies(List<String> lines) {
        List<MethodBody> found = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (TYPE_DECL.matcher(line).matches() || !METHOD_DECL.matcher(line).matches()) continue;

            int depth = 0;
            List<String> body = new ArrayList<>();
            for (int j = i; j < lines.size(); j++) {
                String current = lines.get(j);
                if (j > i) {
                    // the closing brace of the method itself ends the body
                    if (depth + netBraces(current) == 0 && current.trim().startsWith("}")) break;
                    body.add(current);
                }
                depth += netBraces(current);
                if (depth <= 0 && j > i) break;
            }
            if (!body.isEmpty()) found.add(new MethodBody(line, body));
        }
        return found;
    }

    private static int netBraces(String line) {
        int net = 0;
        boolean inString = false, inChar = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inString) {
                if (c == '\\') i++;
                else if (c == '"') inString = false;
            } else if (inChar) {
                if (c == '\\') i++;
                else if (c == '\'') inChar = false;
            } else if (c == '"') inString = true;
            else if (c == '\'') inChar = true;
            else if (c == '{') net++;
            else if (c == '}') net--;
        }
        return net;
    }

    /**
     * The solution reduced to orderable lines: comments and blank lines dropped, original
     * indentation kept (it is a legitimate hint about nesting).
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
