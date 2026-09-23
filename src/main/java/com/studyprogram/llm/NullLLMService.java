package com.studyprogram.llm;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

import java.util.Optional;

/**
 * The help a student gets when no AI is configured — which, for most students, is always.
 *
 * <p>This used to be a null object that returned placeholders, so "explain" printed an
 * advertisement for an API key and "hint" printed "No hints available" for two thirds of the
 * bank. Optional AI was supposed to mean the program works without it, not that the help buttons
 * stop working. Everything here now comes from {@link OfflineHelp}, assembled out of the topic
 * graph, the question itself and the misconception catalogue.
 *
 * <p>{@link #isAvailable()} still reports false: no model is being called, the banner should say
 * so, and generated questions genuinely are unavailable.
 */
public class NullLLMService implements LLMService {

    @Override public boolean isAvailable() { return false; }

    @Override
    public String explainAnswer(Question question, String studentAnswer) {
        return OfflineHelp.explainAnswer(question, studentAnswer);
    }

    @Override
    public String generateHint(Question question) {
        return OfflineHelp.hint(question, 0);
    }

    @Override
    public String generateHint(Question question, int alreadyGiven) {
        return OfflineHelp.hint(question, alreadyGiven);
    }

    @Override
    public String explainConcept(Topic topic, String concept) {
        return OfflineHelp.explainConcept(topic);
    }

    @Override
    public Optional<Question> generateQuestion(Topic topic, QuestionType type, int difficulty) {
        return Optional.empty();
    }
}
