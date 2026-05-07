package com.studyprogram.questions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyprogram.core.QuestionLoader;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads all *.json files from a directory as Question objects for a given topic.
 * Adding a new question is as simple as dropping a new .json file into the directory.
 */
public class DirectoryQuestionLoader implements QuestionLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Topic topic;
    private final Path directory;

    public DirectoryQuestionLoader(Topic topic, Path directory) {
        this.topic = topic;
        this.directory = directory;
    }

    @Override
    public Topic getTopic() { return topic; }

    @Override
    public List<Question> load() {
        if (!Files.isDirectory(directory)) return List.of();
        List<Question> questions = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.getFileName().toString().endsWith(".json"))
                 .sorted()
                 .forEach(p -> {
                     try {
                         questions.add(toQuestion(MAPPER.readValue(p.toFile(), JsonQuestionDto.class)));
                     } catch (IOException e) {
                         System.err.println("Warning: skipping " + p + " — " + e.getMessage());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Warning: cannot scan " + directory + " — " + e.getMessage());
        }
        return questions;
    }

    private Question toQuestion(JsonQuestionDto dto) {
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

        return b.build();
    }
}
