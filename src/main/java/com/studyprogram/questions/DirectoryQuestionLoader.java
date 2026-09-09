package com.studyprogram.questions;

import com.studyprogram.core.QuestionLoader;
import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.io.IOException;
import java.io.InputStream;
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

    private final Topic topic;
    private final Path directory;
    private final boolean trusted;

    public DirectoryQuestionLoader(Topic topic, Path directory) {
        this(topic, directory, true);
    }

    public DirectoryQuestionLoader(Topic topic, Path directory, boolean trusted) {
        this.topic = topic;
        this.directory = directory;
        this.trusted = trusted;
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
                     try (InputStream in = Files.newInputStream(p)) {
                         questions.add(JsonQuestionParser.parse(in, topic, trusted));
                     } catch (IOException | RuntimeException e) {
                         System.err.println("Warning: skipping " + p + " — " + e.getMessage());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Warning: cannot scan " + directory + " — " + e.getMessage());
        }
        return questions;
    }
}
