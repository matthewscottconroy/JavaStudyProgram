package com.studyprogram.grading;

import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;

/**
 * Grades a student's raw answer string against a question.
 * Implementations range from exact-match to LLM-powered grading.
 */
public interface Grader {
    GradingResult grade(Question question, String studentAnswer);
}
