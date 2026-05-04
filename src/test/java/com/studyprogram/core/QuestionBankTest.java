package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class QuestionBankTest {

    private static QuestionBank bank;

    @BeforeAll
    static void loadOnce() {
        bank = new QuestionBank();
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    @Test
    void bankLoadsAtLeastOneQuestion() {
        assertTrue(bank.totalQuestions() > 0);
    }

    @Test
    void bankLoadsQuestionsFor85OrMore() {
        // Sanity check: the current bank has 85+ questions across populated loaders
        assertTrue(bank.totalQuestions() >= 85,
                "Expected at least 85 questions, got " + bank.totalQuestions());
    }

    @Test
    void allQuestionIdsAreUnique() {
        List<Question> all = bank.getQuestionsForTopics(List.of(Topic.values()));
        Set<String> seen = new HashSet<>();
        for (Question q : all) {
            assertTrue(seen.add(q.getId()),
                    "Duplicate question ID detected: " + q.getId());
        }
    }

    // ── getQuestionsForTopic ──────────────────────────────────────────────────

    @Test
    void variablesTopicHasQuestions() {
        assertFalse(bank.getQuestionsForTopic(Topic.VARIABLES).isEmpty());
    }

    @Test
    void allQuestionsForTopicMatchTopic() {
        for (Question q : bank.getQuestionsForTopic(Topic.PRINTING)) {
            assertEquals(Topic.PRINTING, q.getTopic(),
                    "Question " + q.getId() + " is in PRINTING bank but has topic " + q.getTopic());
        }
    }

    @Test
    void allTopicsHaveQuestions() {
        // All 54 topics now have loaders — verify every topic has at least one question
        for (Topic t : Topic.values()) {
            List<Question> questions = bank.getQuestionsForTopic(t);
            assertNotNull(questions, "getQuestionsForTopic must never return null for " + t);
            assertFalse(questions.isEmpty(), "Topic " + t + " has no questions — add a loader");
        }
    }

    @Test
    void returnedListIsUnmodifiable() {
        List<Question> qs = bank.getQuestionsForTopic(Topic.VARIABLES);
        assertThrows(UnsupportedOperationException.class, () -> qs.add(null));
    }

    // ── getQuestionsForTopics ─────────────────────────────────────────────────

    @Test
    void multiTopicQueryReturnsCombinedResults() {
        int vars    = bank.getQuestionsForTopic(Topic.VARIABLES).size();
        int print   = bank.getQuestionsForTopic(Topic.PRINTING).size();
        int combined = bank.getQuestionsForTopics(List.of(Topic.VARIABLES, Topic.PRINTING)).size();
        assertEquals(vars + print, combined,
                "Combined query should return sum of individual topic counts");
    }

    @Test
    void emptyTopicListReturnsEmpty() {
        assertTrue(bank.getQuestionsForTopics(List.of()).isEmpty());
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findByIdReturnsKnownQuestion() {
        List<Question> vars = bank.getQuestionsForTopic(Topic.VARIABLES);
        assertFalse(vars.isEmpty());
        String id = vars.get(0).getId();
        assertTrue(bank.findById(id).isPresent());
        assertEquals(id, bank.findById(id).get().getId());
    }

    @Test
    void findByUnknownIdReturnsEmpty() {
        assertTrue(bank.findById("__non_existent_id__").isEmpty());
    }

    // ── questionCountsByTopic ─────────────────────────────────────────────────

    @Test
    void countsByTopicSumToTotalQuestions() {
        int sumFromMap = bank.questionCountsByTopic().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(bank.totalQuestions(), sumFromMap,
                "Sum of per-topic counts should equal totalQuestions()");
    }

    // ── Question content integrity ────────────────────────────────────────────

    @Test
    void allQuestionsHaveNonBlankId() {
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            assertFalse(q.getId().isBlank(), "Question has blank ID");
        }
    }

    @Test
    void allQuestionsHaveNonBlankPrompt() {
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            assertFalse(q.getPrompt().isBlank(),
                    "Question " + q.getId() + " has blank prompt");
        }
    }

    @Test
    void allQuestionsHaveNonBlankAnswer() {
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            assertFalse(q.getAnswer().isBlank(),
                    "Question " + q.getId() + " has blank answer");
        }
    }

    @Test
    void mcQuestionsHaveExactlyFourChoices() {
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            if (q.isMultipleChoice()) {
                assertEquals(4, q.getChoices().size(),
                        "MC question " + q.getId() + " does not have exactly 4 choices");
            }
        }
    }

    @Test
    void mcAnswerIsValidLetter() {
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            if (q.isMultipleChoice()) {
                String ans = q.getAnswer().toUpperCase();
                assertTrue(Set.of("A", "B", "C", "D").contains(ans),
                        "MC question " + q.getId() + " has invalid answer letter: " + ans);
            }
        }
    }

    @Test
    void questionDifficultyInRange() {
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            int d = q.getDifficulty();
            assertTrue(d >= 1 && d <= 5,
                    "Question " + q.getId() + " has difficulty " + d + " outside 1–5");
        }
    }
}
