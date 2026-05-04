package com.studyprogram.model;

/** The outcome of grading a student's answer to a question. */
public record GradingResult(
        boolean correct,
        String feedback,        // shown immediately after the student answers
        String explanation,     // full explanation of the correct answer
        String llmFeedback      // null when no LLM is configured
) {
    public static GradingResult correct(String explanation) {
        return new GradingResult(true, "Correct!", explanation, null);
    }

    public static GradingResult incorrect(String feedback, String explanation) {
        return new GradingResult(false, feedback, explanation, null);
    }

    public static GradingResult withLLM(boolean correct, String feedback,
                                        String explanation, String llmFeedback) {
        return new GradingResult(correct, feedback, explanation, llmFeedback);
    }

    public boolean hasLLMFeedback() {
        return llmFeedback != null && !llmFeedback.isBlank();
    }
}
