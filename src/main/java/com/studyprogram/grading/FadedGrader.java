package com.studyprogram.grading;

import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Grades a faded worked example: the student supplies the lines that were removed, in order.
 *
 * <p>Comparison ignores indentation and collapses runs of spaces, because the surrounding program
 * already shows where the line belongs and nobody learns anything from being marked wrong over a
 * double space. Everything else — spelling, capitalisation, punctuation, the semicolon — has to be
 * right, since that is the part being practised.
 */
public class FadedGrader implements Grader {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        List<String> expected = split(question.getAnswer());
        List<String> given = split(studentAnswer);

        if (given.size() != expected.size()) {
            return GradingResult.incorrect(
                    "That is " + given.size() + " line" + (given.size() == 1 ? "" : "s")
                    + " but " + expected.size() + " " + (expected.size() == 1 ? "was" : "were")
                    + " faded out. The missing code is:\n" + indent(question.getAnswer()),
                    question.getExplanation());
        }

        for (int i = 0; i < expected.size(); i++) {
            if (!normalize(given.get(i)).equals(normalize(expected.get(i)))) {
                return GradingResult.incorrect(
                        "Blank " + (i + 1) + " should be:\n    " + expected.get(i).strip()
                        + "\nyou wrote:\n    " + given.get(i).strip(),
                        question.getExplanation());
            }
        }
        return GradingResult.correct(question.getExplanation());
    }

    private static List<String> split(String text) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;
        for (String line : text.split("\n")) {
            if (!line.isBlank()) lines.add(line);
        }
        return lines;
    }

    /** Whitespace-insensitive but otherwise exact: Java identifiers are case-sensitive. */
    private static String normalize(String line) {
        return line.strip().replaceAll("\\s+", " ");
    }

    private static String indent(String text) {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n")) sb.append("    ").append(line.strip()).append("\n");
        return sb.toString();
    }
}
