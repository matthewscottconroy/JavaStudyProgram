package com.studyprogram.llm;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

import java.util.Optional;

/**
 * No-op implementation used when no API key is configured.
 * All methods return empty/placeholder values so the rest of the program
 * can run without null checks everywhere.
 */
public class NullLLMService implements LLMService {

    @Override public boolean isAvailable() { return false; }

    @Override
    public String explainAnswer(Question question, String studentAnswer) {
        return "";
    }

    @Override
    public String generateHint(Question question) {
        return question.getHints().isEmpty()
                ? "No hints available."
                : question.getHints().get(0);
    }

    @Override
    public String explainConcept(Topic topic, String concept) {
        return "LLM not configured. Set ANTHROPIC_API_KEY to enable AI explanations.";
    }

    @Override
    public Optional<Question> generateQuestion(Topic topic, QuestionType type, int difficulty) {
        return Optional.empty();
    }
}
