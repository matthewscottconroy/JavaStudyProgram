package com.studyprogram.questions;

import com.studyprogram.core.QuestionLoader;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

/** Convenience base class providing factory methods that reduce builder boilerplate. */
public abstract class BaseQuestionLoader implements QuestionLoader {

    /**
     * Multiple-choice question with 4 options and no code snippet.
     * Answer is the letter: "a", "b", "c", or "d".
     */
    protected Question mc(String id, Topic topic, int difficulty,
                          String prompt,
                          String choiceA, String choiceB, String choiceC, String choiceD,
                          String answer, String explanation, String... hints) {
        Question.Builder b = Question.builder()
                .id(id).topic(topic).type(QuestionType.MULTIPLE_CHOICE).difficulty(difficulty)
                .prompt(prompt)
                .choices(choiceA, choiceB, choiceC, choiceD)
                .answer(answer).explanation(explanation);
        for (String h : hints) b.hint(h);
        return b.build();
    }

    /**
     * Tracing question: student types the expected output/value (single line).
     * Pass additional acceptable phrasings as {@code alternatives}.
     */
    protected Question trace(String id, Topic topic, int difficulty,
                             String prompt, String code,
                             String answer, String explanation, String... alternatives) {
        Question.Builder b = Question.builder()
                .id(id).topic(topic).type(QuestionType.TRACING).difficulty(difficulty)
                .prompt(prompt).code(code)
                .answer(answer).explanation(explanation);
        for (String alt : alternatives) b.alternative(alt);
        return b.build();
    }

    /**
     * Debugging question shown as multiple choice (what is wrong / which line).
     * Always includes a code snippet.
     */
    protected Question debug(String id, Topic topic, int difficulty,
                             String prompt, String code,
                             String choiceA, String choiceB, String choiceC, String choiceD,
                             String answer, String explanation) {
        return Question.builder()
                .id(id).topic(topic).type(QuestionType.DEBUGGING).difficulty(difficulty)
                .prompt(prompt).code(code)
                .choices(choiceA, choiceB, choiceC, choiceD)
                .answer(answer).explanation(explanation)
                .build();
    }

    /**
     * Code-generation question shown as multiple choice (which snippet is correct).
     * No code snippet — the choices themselves contain the code options.
     */
    protected Question codegen(String id, Topic topic, int difficulty,
                               String prompt,
                               String choiceA, String choiceB, String choiceC, String choiceD,
                               String answer, String explanation, String... hints) {
        Question.Builder b = Question.builder()
                .id(id).topic(topic).type(QuestionType.CODE_GENERATION).difficulty(difficulty)
                .prompt(prompt)
                .choices(choiceA, choiceB, choiceC, choiceD)
                .answer(answer).explanation(explanation);
        for (String h : hints) b.hint(h);
        return b.build();
    }
}
