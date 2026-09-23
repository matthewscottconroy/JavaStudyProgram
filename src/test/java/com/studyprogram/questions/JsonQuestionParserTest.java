package com.studyprogram.questions;

import com.studyprogram.model.Misconception;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The parser reads files a user can edit by hand, so what it does with a malformed one matters
 * as much as what it does with a good one. The rule throughout: a mistake that would quietly
 * change a question's meaning must fail loudly rather than be ignored.
 */
class JsonQuestionParserTest {

    private static Question parse(String json) throws Exception {
        return JsonQuestionParser.parse(json.getBytes(StandardCharsets.UTF_8), Topic.LOOPS);
    }

    @Test
    void aMinimalQuestionParses() throws Exception {
        Question q = parse("""
                { "id": "q1", "type": "MULTIPLE_CHOICE", "difficulty": 2, "prompt": "p",
                  "choices": ["a", "b"], "answer": "a", "explanation": "e" }
                """);

        assertEquals("q1", q.getId());
        assertEquals(Topic.LOOPS, q.getTopic(), "the topic comes from the folder, not the file");
        assertEquals(QuestionType.MULTIPLE_CHOICE, q.getType());
        assertEquals(2, q.getChoices().size());
        assertTrue(q.isTrusted(), "classpath questions are first-party by default");
    }

    @Test
    void aTypeIsOptionalAndDefaultsToMultipleChoice() throws Exception {
        assertEquals(QuestionType.MULTIPLE_CHOICE, parse("""
                { "id": "q1", "difficulty": 1, "prompt": "p", "choices": ["a"], "answer": "a" }
                """).getType());
    }

    @Test
    void anUnknownTypeIsRefusedRatherThanGuessedAt() {
        assertThrows(Exception.class, () -> parse("""
                { "id": "q1", "type": "QUIZ", "difficulty": 1, "prompt": "p", "answer": "a" }
                """));
    }

    @Test
    void aMisspelledRelatedTopicFailsRatherThanVanishing() {
        var thrown = assertThrows(Exception.class, () -> parse("""
                { "id": "q1", "difficulty": 1, "prompt": "p", "answer": "a",
                  "relatedTopics": ["ARAYS"] }
                """));
        assertTrue(thrown.getMessage().contains("ARAYS"), thrown.getMessage());
    }

    @Test
    void distractorsBecomeMisconceptions() throws Exception {
        Question q = parse("""
                { "id": "q1", "difficulty": 1, "prompt": "p", "choices": ["x", "y"],
                  "answer": "a", "distractors": { "b": "INTEGER_DIVISION" } }
                """);
        assertEquals(Misconception.INTEGER_DIVISION, q.misconceptionFor("b").orElseThrow());
        assertTrue(q.misconceptionFor("a").isEmpty());
        assertEquals(Misconception.INTEGER_DIVISION, q.misconceptionFor("B.").orElseThrow(),
                "a letter typed with a dot is the same letter");
    }

    @Test
    void anExternalQuestionIsMarkedUntrusted() throws Exception {
        Question q = JsonQuestionParser.parse(
                new java.io.ByteArrayInputStream("""
                        { "id": "q1", "difficulty": 1, "prompt": "p", "answer": "a" }
                        """.getBytes(StandardCharsets.UTF_8)), Topic.LOOPS, false);
        assertFalse(q.isTrusted());
    }

    @Test
    void aQuestionWithNoAnswerIsRefused() {
        assertThrows(Exception.class, () -> parse("""
                { "id": "q1", "difficulty": 1, "prompt": "p" }
                """));
    }

    @Test
    void unknownFieldsAreIgnoredSoOlderFilesKeepWorking() throws Exception {
        Question q = parse("""
                { "id": "q1", "difficulty": 1, "prompt": "p", "answer": "a",
                  "somethingFromTheFuture": 42 }
                """);
        assertEquals("q1", q.getId());
    }
}
