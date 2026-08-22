package com.studyprogram.core;

import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the content pipeline: every question directory must map to a topic and the
 * bank must load without warnings. This is what catches a typo'd directory name or a
 * new directory added before its Topic exists — problems that previously caused
 * questions to be silently dropped.
 */
class QuestionDataValidationTest {

    private static final Path QUESTIONS_DIR = Path.of("data", "questions");

    @Test
    void everyQuestionDirectoryMapsToATopic() throws IOException {
        assertTrue(Files.isDirectory(QUESTIONS_DIR), "data/questions not found — run tests from the repo root");
        try (Stream<Path> dirs = Files.list(QUESTIONS_DIR)) {
            for (Path dir : dirs.filter(Files::isDirectory).toList()) {
                String slug = dir.getFileName().toString();
                assertNotNull(Topic.fromDirSlug(slug),
                        "Directory data/questions/" + slug + " matches no Topic — its questions "
                        + "would be silently dropped. Add the topic or rename the directory.");
            }
        }
    }

    @Test
    void everyTopicWithADirectoryLoadsItsQuestions() throws IOException {
        QuestionBank bank = new QuestionBank();
        try (Stream<Path> dirs = Files.list(QUESTIONS_DIR)) {
            for (Path dir : dirs.filter(Files::isDirectory).toList()) {
                Topic topic = Topic.fromDirSlug(dir.getFileName().toString());
                if (topic == null) continue;  // covered by the test above
                long jsonFiles;
                try (Stream<Path> files = Files.list(dir)) {
                    jsonFiles = files.filter(p -> p.toString().endsWith(".json")).count();
                }
                if (jsonFiles > 0) {
                    assertFalse(bank.getQuestionsForTopic(topic).isEmpty(),
                            "Topic " + topic + " has " + jsonFiles + " JSON files but loaded no questions");
                }
            }
        }
    }

    @Test
    void bankLoadsWithoutWarnings() {
        QuestionBank bank = new QuestionBank();
        List<String> warnings = bank.getWarnings();
        assertTrue(warnings.isEmpty(),
                "Question bank reported problems:\n  " + String.join("\n  ", warnings));
    }
}
