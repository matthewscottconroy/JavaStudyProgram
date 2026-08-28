package com.studyprogram.questions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses a single question JSON document into a {@link Question}.
 * Shared by the external-directory loader and the classpath (in-jar) loader.
 */
public final class JsonQuestionParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonQuestionParser() {}

    public static Question parse(InputStream in, Topic topic) throws IOException {
        return toQuestion(MAPPER.readValue(in, JsonQuestionDto.class), topic);
    }

    public static Question parse(byte[] json, Topic topic) throws IOException {
        return toQuestion(MAPPER.readValue(json, JsonQuestionDto.class), topic);
    }

    static Question toQuestion(JsonQuestionDto dto, Topic topic) {
        QuestionType type = dto.type != null
                ? QuestionType.valueOf(dto.type.toUpperCase())
                : QuestionType.MULTIPLE_CHOICE;

        Question.Builder b = Question.builder()
                .id(dto.id)
                .topic(topic)
                .type(type)
                .difficulty(dto.difficulty)
                .prompt(dto.prompt)
                .answer(dto.answer)
                .explanation(dto.explanation);

        if (dto.code != null && !dto.code.isBlank()) b.code(dto.code);
        if (dto.choices != null) dto.choices.forEach(b::choice);
        if (dto.alternatives != null) dto.alternatives.forEach(b::alternative);
        if (dto.hints != null) dto.hints.forEach(b::hint);
        if (dto.starterCode != null) b.starterCode(dto.starterCode);
        if (dto.testCode != null) b.testCode(dto.testCode);
        if (dto.relatedTopics != null) {
            // strict: an unknown name throws, so a typo'd tag surfaces as a load warning
            dto.relatedTopics.forEach(name -> b.relatedTopic(Topic.valueOf(name)));
        }
        if (dto.starterFiles != null && !dto.starterFiles.isEmpty()) b.starterFiles(dto.starterFiles);
        if (dto.solutionFiles != null && !dto.solutionFiles.isEmpty()) b.solutionFiles(dto.solutionFiles);

        return b.build();
    }
}
