package com.studyprogram.llm;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;
import com.studyprogram.model.QuestionType;

import java.util.Optional;

/**
 * Abstraction over a language model backend.
 * All methods degrade gracefully when the LLM is unavailable.
 */
public interface LLMService {

    /** Returns false when no API key is configured or the service is unreachable. */
    boolean isAvailable();

    /**
     * Provides a richer explanation of why the student's answer was wrong
     * and what the correct answer means.
     */
    String explainAnswer(Question question, String studentAnswer);

    /**
     * Returns a progressive hint without revealing the full answer.
     */
    String generateHint(Question question);

    /**
     * The next hint, given how many the student has already taken.
     *
     * <p>Without this a second press of "hint" returns the first hint again, which is how a
     * student learns that asking twice is pointless. Implementations that have no notion of
     * progression fall back to the single-hint form.
     */
    default String generateHint(Question question, int alreadyGiven) {
        return generateHint(question);
    }

    /**
     * Asks the model to explain a concept in the context of a topic.
     * Useful for on-demand "teach me" mode.
     */
    String explainConcept(Topic topic, String concept);

    /**
     * Dynamically generates a new question. Returns empty if generation fails
     * or the LLM is unavailable (the app falls back to the static bank).
     */
    Optional<Question> generateQuestion(Topic topic, QuestionType type, int difficulty);
}
