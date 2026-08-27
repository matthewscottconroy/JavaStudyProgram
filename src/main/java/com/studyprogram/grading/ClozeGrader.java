package com.studyprogram.grading;

import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;

/**
 * Grades fill-in-the-blank answers: whitespace-insensitive, case-sensitive
 * comparison against the canonical answer and any listed alternatives
 * (Java identifiers are case-sensitive, so case must match).
 */
public class ClozeGrader implements Grader {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        String given = normalize(studentAnswer);
        if (given.equals(normalize(question.getAnswer()))) {
            return GradingResult.correct(question.getExplanation());
        }
        for (String alt : question.getAlternativeAnswers()) {
            if (given.equals(normalize(alt))) {
                return GradingResult.correct(question.getExplanation());
            }
        }
        return GradingResult.incorrect(
                "The missing code is: " + question.getAnswer(),
                question.getExplanation());
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}
