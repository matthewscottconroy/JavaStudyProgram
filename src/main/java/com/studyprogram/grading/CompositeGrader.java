package com.studyprogram.grading;

import com.studyprogram.llm.LLMService;
import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;

/**
 * Routes to the appropriate grader based on question type,
 * then optionally augments the result with LLM feedback.
 */
public class CompositeGrader implements Grader {

    private final MultipleChoiceGrader mcGrader   = new MultipleChoiceGrader();
    private final ExactMatchGrader     exactGrader = new ExactMatchGrader();
    private final LLMService           llm;

    public CompositeGrader(LLMService llm) {
        this.llm = llm;
    }

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        GradingResult base = question.isMultipleChoice()
                ? mcGrader.grade(question, studentAnswer)
                : exactGrader.grade(question, studentAnswer);

        if (llm.isAvailable() && !base.correct()) {
            // Ask LLM for a richer explanation on wrong answers
            String llmFeedback = llm.explainAnswer(question, studentAnswer);
            return GradingResult.withLLM(base.correct(), base.feedback(),
                                         base.explanation(), llmFeedback);
        }
        return base;
    }
}
