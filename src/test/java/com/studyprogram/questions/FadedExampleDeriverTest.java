package com.studyprogram.questions;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.grading.FadedGrader;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Content gate for derived faded worked examples. Derivation is automatic, so nobody reviews these
 * by hand — the only defence against shipping thousands of unanswerable questions is to answer
 * every one of them here, through the real grader, exactly as a student who knew the solution
 * would.
 */
class FadedExampleDeriverTest {

    /** A declaration line: parameters and an opening brace, with no control keyword in front. */
    static boolean looksLikeAMethodSignature(String line) {
        return line.matches("^(?!(if|for|while|switch|catch|try|else|do|return)\\b)"
                + "[\\w<>\\[\\],.?\\s&]*\\w+\\s*\\([^;]*\\)\\s*(throws [\\w,.\\s]+)?\\{$");
    }

    private static List<Question> allFaded() {
        return new QuestionBank().getQuestionsForTopics(List.of(Topic.values())).stream()
                .filter(q -> q.getType() == QuestionType.FADED)
                .toList();
    }

    @Test
    void everyDerivedExampleIsSolvableThroughTheGrader() {
        List<Question> faded = allFaded();
        assertFalse(faded.isEmpty(), "expected derived faded examples");

        FadedGrader grader = new FadedGrader();
        List<String> broken = new ArrayList<>();
        for (Question q : faded) {
            if (!grader.grade(q, q.getAnswer()).correct()) {
                broken.add(q.getId() + " (grader rejected its own answer)");
            }
            // A student retyping with different indentation must still be right.
            String sloppy = q.getAnswer().replace("\n", "\n    ");
            if (!grader.grade(q, "   " + sloppy).correct()) {
                broken.add(q.getId() + " (indentation counted against the student)");
            }
        }
        assertTrue(broken.isEmpty(), broken.size() + " broken example(s): "
                + broken.subList(0, Math.min(5, broken.size())));
    }

    @Test
    void everyExampleShowsAsManyBlanksAsItAsksFor() {
        List<String> wrong = new ArrayList<>();
        for (Question q : allFaded()) {
            int asked = FadedExampleDeriver.blankCount(q);
            int shown = q.getCode().split(">>> blank ", -1).length - 1;
            if (asked != shown) {
                wrong.add(q.getId() + ": asks for " + asked + " line(s) but shows " + shown);
            }
            if (q.getAnswer().isBlank()) wrong.add(q.getId() + ": blanked out nothing");
            if (!q.hasCode()) wrong.add(q.getId() + ": has no listing to study");
        }
        assertTrue(wrong.isEmpty(), wrong.size() + " malformed: "
                + wrong.subList(0, Math.min(5, wrong.size())));
    }

    @Test
    void theLadderRunsEasyToHardAndEndsAtTheCodingExercise() {
        QuestionBank bank = new QuestionBank();
        int laddersChecked = 0;
        for (Question q : bank.getQuestionsForTopics(List.of(Topic.values()))) {
            if (q.getType() != QuestionType.FADED || !q.getId().endsWith("-faded1")) continue;
            String base = q.getId().substring(0, q.getId().length() - "-faded1".length());
            var three = bank.findById(base + "-faded3");
            var coding = bank.findById(base);
            assertTrue(coding.isPresent(), base + ": faded example outlived its coding exercise");
            if (three.isEmpty()) continue;

            laddersChecked++;
            assertTrue(q.getDifficulty() <= three.get().getDifficulty(),
                    base + ": one blank should not be harder than three");
            assertTrue(three.get().getDifficulty() <= coding.get().getDifficulty(),
                    base + ": filling blanks should not be harder than writing it from scratch");
            assertEquals(1, FadedExampleDeriver.blankCount(q));
            assertEquals(3, FadedExampleDeriver.blankCount(three.get()));
        }
        assertTrue(laddersChecked > 50, "expected many complete ladders, found " + laddersChecked);
    }

    @Test
    void theWrongNumberOfLinesIsRejectedWithAnExplanation() {
        Question q = allFaded().stream()
                .filter(f -> FadedExampleDeriver.blankCount(f) == 3)
                .findFirst().orElseThrow();
        var result = new FadedGrader().grade(q, "one line only");
        assertFalse(result.correct());
        assertTrue(result.feedback().contains("3"),
                "the student should be told how many lines were expected: " + result.feedback());
    }

    @Test
    void bracesAndBoilerplateAreNeverTheBlank() {
        for (Question q : allFaded()) {
            for (String line : q.getAnswer().split("\n")) {
                String stripped = line.strip();
                assertNotEquals("}", stripped, q.getId() + ": a closing brace is not a question");
                assertNotEquals("{", stripped, q.getId() + ": an opening brace is not a question");
                assertFalse(stripped.startsWith("import "),
                        q.getId() + ": an import is not worth fading out");
                assertFalse(stripped.startsWith("}"),
                        q.getId() + ": a closing line is structure, not logic");
                assertFalse(stripped.startsWith("public class ") || stripped.startsWith("class "),
                        q.getId() + ": a class declaration is boilerplate");
                assertFalse(FadedExampleDeriverTest.looksLikeAMethodSignature(stripped),
                        q.getId() + ": fading a method signature tests typing, not thinking — "
                        + stripped);
            }
        }
    }
}
