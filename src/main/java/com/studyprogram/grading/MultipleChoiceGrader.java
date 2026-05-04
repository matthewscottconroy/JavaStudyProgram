package com.studyprogram.grading;

import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;

/**
 * Grades multiple-choice answers. Accepts the letter (A/B/C/D) or the full
 * choice text. Case-insensitive.
 */
public class MultipleChoiceGrader implements Grader {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        if (!question.isMultipleChoice()) {
            throw new IllegalArgumentException("Question " + question.getId() + " is not multiple choice");
        }
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return GradingResult.incorrect("No answer provided.", question.getExplanation());
        }

        String given = studentAnswer.trim().toUpperCase();
        String correct = question.getAnswer().trim().toUpperCase();

        // Accept both the letter ("A") and the letter with dot ("A.")
        String givenLetter = given.replaceAll("\\.$", "");
        String correctLetter = correct.replaceAll("\\.$", "");

        boolean letterMatch = givenLetter.equals(correctLetter);

        // Also accept if the student typed the full choice text
        boolean textMatch = false;
        if (!letterMatch && givenLetter.length() == 1) {
            // already handled above
        } else if (!letterMatch) {
            int idx = letterToIndex(correctLetter);
            if (idx >= 0 && idx < question.getChoices().size()) {
                String choiceText = question.getChoices().get(idx);
                textMatch = given.equalsIgnoreCase(choiceText.trim());
            }
        }

        if (letterMatch || textMatch) {
            int idx = letterToIndex(correctLetter);
            String choiceText = (idx >= 0 && idx < question.getChoices().size())
                    ? question.getChoices().get(idx) : correct;
            return GradingResult.correct(question.getExplanation());
        }

        int correctIdx = letterToIndex(correctLetter);
        String correctChoiceText = (correctIdx >= 0 && correctIdx < question.getChoices().size())
                ? question.getChoices().get(correctIdx) : "";
        String feedback = String.format("Incorrect. The correct answer is %s) %s",
                                        correctLetter, correctChoiceText);
        return GradingResult.incorrect(feedback, question.getExplanation());
    }

    private int letterToIndex(String letter) {
        return switch (letter) {
            case "A" -> 0;
            case "B" -> 1;
            case "C" -> 2;
            case "D" -> 3;
            default  -> -1;
        };
    }
}
