package com.studyprogram.model;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.grading.MultipleChoiceGrader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A wrong answer should say what the student was probably thinking, not just what the right
 * answer was. These tests cover the catalogue, the tagging, and the feedback it produces.
 */
class MisconceptionTest {

    @Test
    void everyEntryHasASummaryAndAnExplanationThatStandAlone() {
        for (Misconception m : Misconception.values()) {
            assertFalse(m.summary.isBlank(), m + " has no summary");
            assertTrue(m.explanation.length() > 60,
                    m + "'s explanation is too short to teach anything: " + m.explanation);
            assertFalse(m.summary.endsWith("."), m + "'s summary reads as a label, not a sentence");
        }
    }

    @Test
    void anEntryCanBeLookedUpByNameAndUnknownNamesAreRejected() {
        assertEquals(Misconception.INTEGER_DIVISION,
                Misconception.byId("integer_division").orElseThrow());
        assertEquals(Misconception.INTEGER_DIVISION,
                Misconception.byId("INTEGER-DIVISION").orElseThrow(), "hyphens are forgiven");
        assertTrue(Misconception.byId("NOT_A_REAL_ONE").isEmpty());
        assertTrue(Misconception.byId(null).isEmpty());
    }

    @Test
    void aTaggedWrongChoiceExplainsTheIdeaBehindIt() {
        Question q = Question.builder()
                .id("d1").topic(Topic.VARIABLES).type(QuestionType.MULTIPLE_CHOICE).difficulty(1)
                .prompt("What does 7 / 2 give?")
                .choices("3.5", "3", "3.0", "error")
                .answer("b")
                .distractor("a", Misconception.INTEGER_DIVISION)
                .explanation("Integer division truncates.")
                .build();

        var wrongTagged = new MultipleChoiceGrader().grade(q, "a");
        assertFalse(wrongTagged.correct());
        assertTrue(wrongTagged.feedback().contains("The correct answer is B"),
                "the right answer still has to be stated: " + wrongTagged.feedback());
        assertTrue(wrongTagged.feedback().contains(Misconception.INTEGER_DIVISION.summary),
                "and the idea behind the wrong choice named: " + wrongTagged.feedback());

        // An untagged wrong choice behaves exactly as before — no invented diagnosis.
        var wrongUntagged = new MultipleChoiceGrader().grade(q, "d");
        assertFalse(wrongUntagged.feedback().contains("What picking"),
                "a question that does not know why D is wrong must not guess: "
                + wrongUntagged.feedback());

        assertTrue(new MultipleChoiceGrader().grade(q, "b").correct());
    }

    @Test
    void theRightAnswerIsNeverTaggedAsAMisconception() {
        QuestionBank bank = new QuestionBank();
        List<String> wrong = new java.util.ArrayList<>();
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            if (q.getDistractors().isEmpty()) continue;
            String answer = q.getAnswer().trim().toUpperCase().replaceAll("\\.$", "");
            if (q.getDistractors().containsKey(answer)) {
                wrong.add(q.getId() + " tags its own correct answer " + answer);
            }
        }
        assertTrue(wrong.isEmpty(), wrong.toString());
    }

    @Test
    void everyTagPointsAtARealChoiceOfItsQuestion() {
        QuestionBank bank = new QuestionBank();
        List<String> broken = new java.util.ArrayList<>();
        int tagged = 0;
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            if (q.getDistractors().isEmpty()) continue;
            tagged++;
            for (String letter : q.getDistractors().keySet()) {
                int index = letter.charAt(0) - 'A';
                if (letter.length() != 1 || index < 0 || index >= q.getChoices().size()) {
                    broken.add(q.getId() + " tags '" + letter + "' but has only "
                            + q.getChoices().size() + " choices");
                }
            }
        }
        assertTrue(broken.isEmpty(), broken.toString());
        assertTrue(tagged >= 20, "expected the shipped diagnostic questions, found " + tagged);
    }

    @Test
    void aMisspelledMisconceptionInAQuestionFileIsRefusedRatherThanIgnored() {
        String json = """
                { "id": "x", "type": "MULTIPLE_CHOICE", "difficulty": 1, "prompt": "p",
                  "choices": ["a", "b"], "answer": "a",
                  "distractors": { "b": "INTEGRE_DIVISION" } }
                """;
        var thrown = assertThrows(Exception.class, () ->
                com.studyprogram.questions.JsonQuestionParser.parse(
                        json.getBytes(java.nio.charset.StandardCharsets.UTF_8), Topic.VARIABLES));
        assertTrue(thrown.getMessage().contains("INTEGRE_DIVISION"),
                "a typo must name itself rather than silently losing the diagnosis: "
                + thrown.getMessage());
    }
}
