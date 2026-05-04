package com.studyprogram.storage;

import com.studyprogram.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonProfileStorageTest {

    @TempDir
    Path tempDir;

    private JsonProfileStorage storage;

    @BeforeEach
    void setUp() throws IOException {
        storage = new JsonProfileStorage(tempDir);
    }

    private static StudentProfile makeProfile(String name) {
        StudentProfile p = new StudentProfile(name);
        p.getSelectedTopics().add(Topic.VARIABLES);
        p.getSelectedTopics().add(Topic.PRINTING);
        return p;
    }

    private static Question makeQuestion(Topic topic) {
        return Question.builder()
                .id("q-" + topic.name())
                .topic(topic)
                .type(QuestionType.TRACING)
                .difficulty(1)
                .prompt("test")
                .answer("42")
                .build();
    }

    // ── Save / load roundtrip ─────────────────────────────────────────────────

    @Test
    void saveAndLoadPreservesName() throws IOException {
        storage.save(makeProfile("Alice"));
        Optional<StudentProfile> loaded = storage.load("Alice");
        assertTrue(loaded.isPresent());
        assertEquals("Alice", loaded.get().getName());
    }

    @Test
    void saveAndLoadPreservesId() throws IOException {
        StudentProfile original = makeProfile("Bob");
        storage.save(original);
        StudentProfile loaded = storage.load("Bob").get();
        assertEquals(original.getId(), loaded.getId());
    }

    @Test
    void saveAndLoadPreservesSelectedTopics() throws IOException {
        StudentProfile p = makeProfile("Carol");
        storage.save(p);
        StudentProfile loaded = storage.load("Carol").get();
        assertTrue(loaded.getSelectedTopics().contains(Topic.VARIABLES));
        assertTrue(loaded.getSelectedTopics().contains(Topic.PRINTING));
    }

    @Test
    void saveAndLoadPreservesTotals() throws IOException {
        StudentProfile p = makeProfile("Dave");
        p.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        p.recordAnswer(makeQuestion(Topic.PRINTING), false);
        storage.save(p);

        StudentProfile loaded = storage.load("Dave").get();
        assertEquals(2, loaded.getTotalQuestionsAnswered());
        assertEquals(1, loaded.getTotalCorrect());
    }

    @Test
    void saveAndLoadPreservesTopicPerformance() throws IOException {
        StudentProfile p = makeProfile("Eve");
        p.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        p.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        storage.save(p);

        StudentProfile loaded = storage.load("Eve").get();
        TopicPerformance perf = loaded.getPerformance().get(Topic.VARIABLES);
        assertNotNull(perf);
        assertEquals(2, perf.getAttempts());
        assertEquals(2, perf.getCorrect());
        assertTrue(perf.getMasteryScore() > 0.0);
    }

    @Test
    void saveOverwritesPreviousData() throws IOException {
        StudentProfile p = makeProfile("Frank");
        storage.save(p);
        p.recordAnswer(makeQuestion(Topic.VARIABLES), true);
        storage.save(p);

        StudentProfile loaded = storage.load("Frank").get();
        assertEquals(1, loaded.getTotalQuestionsAnswered());
    }

    // ── Load missing profile ──────────────────────────────────────────────────

    @Test
    void loadMissingProfileReturnsEmpty() throws IOException {
        Optional<StudentProfile> result = storage.load("NonExistent");
        assertTrue(result.isEmpty());
    }

    // ── listProfileNames ──────────────────────────────────────────────────────

    @Test
    void emptyDirectoryReturnsEmptyList() throws IOException {
        assertTrue(storage.listProfileNames().isEmpty());
    }

    @Test
    void listNamesReturnsAllSavedProfiles() throws IOException {
        storage.save(makeProfile("Alice"));
        storage.save(makeProfile("Bob"));
        storage.save(makeProfile("Carol"));

        List<String> names = storage.listProfileNames();
        assertEquals(3, names.size());
        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Bob"));
        assertTrue(names.contains("Carol"));
    }

    @Test
    void listNamesIsSorted() throws IOException {
        storage.save(makeProfile("Zara"));
        storage.save(makeProfile("Alice"));
        storage.save(makeProfile("Mike"));

        List<String> names = storage.listProfileNames();
        assertEquals(List.of("Alice", "Mike", "Zara"), names);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void deleteRemovesProfile() throws IOException {
        storage.save(makeProfile("Grace"));
        storage.delete("Grace");
        assertTrue(storage.load("Grace").isEmpty());
        assertFalse(storage.listProfileNames().contains("Grace"));
    }

    @Test
    void deleteNonExistentProfileDoesNotThrow() {
        assertDoesNotThrow(() -> storage.delete("Ghost"));
    }

    // ── Name sanitisation ─────────────────────────────────────────────────────

    @Test
    void specialCharactersInNameAreSanitized() throws IOException {
        StudentProfile p = makeProfile("Alice/Bob");
        storage.save(p);
        // Should not throw and should be loadable under the same display name
        Optional<StudentProfile> loaded = storage.load("Alice/Bob");
        assertTrue(loaded.isPresent());
        assertEquals("Alice/Bob", loaded.get().getName());
    }

    @Test
    void spacesInNameAreSanitized() throws IOException {
        StudentProfile p = makeProfile("John Doe");
        storage.save(p);
        Optional<StudentProfile> loaded = storage.load("John Doe");
        assertTrue(loaded.isPresent());
        assertEquals("John Doe", loaded.get().getName());
    }

    // ── Directory creation ────────────────────────────────────────────────────

    @Test
    void constructorCreatesDirectoryIfMissing() throws IOException {
        Path subDir = tempDir.resolve("nested/profiles");
        new JsonProfileStorage(subDir);   // should not throw
        assertTrue(subDir.toFile().exists());
    }
}
