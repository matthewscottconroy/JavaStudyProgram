package com.studyprogram.grading;

import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Grades a Parsons (reorder-the-lines) answer. The student submits line numbers
 * ("3 1 4 2 …") referring to the shuffled listing; the ordering is correct when the
 * chosen line TEXTS match the solution — so interchangeable identical lines (like
 * closing braces) are accepted in any of their positions.
 */
public class ParsonsGrader implements Grader {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        List<String> shuffled = question.getShuffledLines();
        int n = shuffled.size();

        List<Integer> order = parseOrder(studentAnswer, n);
        if (order == null) {
            return GradingResult.incorrect(
                    "Answer with all " + n + " line numbers in order, separated by spaces "
                    + "— for example: 3 1 " + (n >= 4 ? "4 2 …" : "2 …"),
                    "");
        }

        // Compare line by line with indentation normalised. The Question builder trims the
        // stored answer, which would otherwise strip the first line's indentation and make any
        // puzzle drawn from an indented method body impossible to get right.
        List<String> chosen = new ArrayList<>();
        for (Integer index : order) chosen.add(shuffled.get(index - 1).strip());
        List<String> expected = new ArrayList<>();
        for (String line : question.getAnswer().split("\n")) expected.add(line.strip());

        if (chosen.equals(expected)) {
            return GradingResult.correct(question.getExplanation());
        }
        return GradingResult.incorrect(
                "That ordering doesn't produce the working program. Correct order:\n"
                + numberedSolution(question),
                question.getExplanation());
    }

    /** Parses "3 1 4 2" into 1-based indices; null unless it is a full permutation of 1..n. */
    private static List<Integer> parseOrder(String answer, int n) {
        List<Integer> order = new ArrayList<>();
        boolean[] used = new boolean[n + 1];
        for (String token : answer.trim().split("[\\s,]+")) {
            if (token.isEmpty()) continue;
            int v;
            try {
                v = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                return null;
            }
            if (v < 1 || v > n || used[v]) return null;
            used[v] = true;
            order.add(v);
        }
        return order.size() == n ? order : null;
    }

    private static String numberedSolution(Question q) {
        List<String> shuffled = q.getShuffledLines();
        StringBuilder sb = new StringBuilder();
        for (String solutionLine : q.getAnswer().split("\n")) {
            int idx = 0;
            for (int i = 0; i < shuffled.size(); i++) {
                if (shuffled.get(i).strip().equals(solutionLine.strip())) { idx = i + 1; break; }
            }
            sb.append("  ").append(idx).append(": ").append(solutionLine.strip()).append("\n");
        }
        return sb.toString();
    }
}
