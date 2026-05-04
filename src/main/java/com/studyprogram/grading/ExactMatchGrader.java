package com.studyprogram.grading;

import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;

/**
 * Grades by normalising both the student's answer and the stored answers,
 * then checking for an exact match. Handles whitespace and case differences.
 */
public class ExactMatchGrader implements Grader {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        String normalised = normalise(studentAnswer);

        boolean match = normalised.equals(normalise(question.getAnswer()))
                || question.getAlternativeAnswers().stream()
                           .anyMatch(alt -> normalised.equals(normalise(alt)));

        if (match) {
            return GradingResult.correct(question.getExplanation());
        }

        String feedback = String.format("Incorrect. The correct answer is: %s",
                                        question.getAnswer());
        return GradingResult.incorrect(feedback, question.getExplanation());
    }

    protected String normalise(String s) {
        if (s == null) return "";
        return s.trim()
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }
}
