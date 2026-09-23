package com.studyprogram.llm;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.model.Misconception;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Optional AI means the program has to be useful without it. These tests are about the moment a
 * student presses "hint" or "explain" on a machine with no API key — which used to produce
 * "No hints available" for two thirds of the bank.
 */
class OfflineHelpTest {

    private static Question question(Topic topic, QuestionType type, String... hints) {
        Question.Builder b = Question.builder()
                .id("q1").topic(topic).type(type).difficulty(2)
                .prompt("p").answer("a");
        if (type == QuestionType.CODING) b.starterCode("class X {}").testCode("class XT {}");
        for (String h : hints) b.hint(h);
        return b.build();
    }

    // ── Explaining ───────────────────────────────────────────────────────────

    @Test
    void explainingATopicSaysWhatItIsWhatItRestsOnAndWhereItLeads() {
        String text = OfflineHelp.explainConcept(Topic.ARRAYS);

        assertTrue(text.contains(Topic.ARRAYS.displayName));
        assertTrue(text.contains(Topic.ARRAYS.description), "the topic's own description is the core");
        assertTrue(text.contains("It builds on:"), text);
        for (Topic p : Topic.ARRAYS.getPrerequisites()) {
            assertTrue(text.contains(p.displayName), "prerequisite " + p + " should be named");
        }
        assertTrue(text.contains("What it leads to:"), text);
    }

    @Test
    void explainingNeverAdvertisesAnApiKey() {
        for (Topic t : Topic.visibleValues()) {
            String text = new NullLLMService().explainConcept(t, "anything");
            assertFalse(text.contains("ANTHROPIC_API_KEY") || text.toLowerCase().contains("not configured"),
                    t + ": pressing explain must teach something, not sell a subscription: " + text);
            assertTrue(text.length() > 40, t + ": explanation is too thin: " + text);
        }
    }

    @Test
    void aTopicWithNoPrerequisitesStillExplainsItself() {
        Topic root = Topic.visibleValues().stream()
                .filter(t -> t.getPrerequisites().isEmpty()).findFirst().orElseThrow();
        String text = OfflineHelp.explainConcept(root);
        assertTrue(text.contains(root.description));
        assertFalse(text.contains("It builds on:"), "there is nothing underneath it to name");
    }

    // ── Hinting ──────────────────────────────────────────────────────────────

    @Test
    void authoredHintsComeFirstAndInOrder() {
        Question q = question(Topic.LOOPS, QuestionType.TRACING, "first", "second");

        assertEquals("first", OfflineHelp.hint(q, 0));
        assertEquals("second", OfflineHelp.hint(q, 1));
        assertNotEquals("second", OfflineHelp.hint(q, 2), "after the authored ones it must go on");
    }

    @Test
    void aTopicDescriptionThatContainsTheAnswerIsNotQuoted() {
        // OO Design Patterns is described as "Singleton, Factory, Strategy, ..." and some of its
        // questions answer exactly "Singleton".
        Question q = Question.builder()
                .id("dp").topic(Topic.OO_DESIGN_PATTERNS).type(QuestionType.TRACING)
                .difficulty(3).prompt("Which pattern is this?").answer("Singleton").build();

        for (String hint : OfflineHelp.derivedHints(q)) {
            assertFalse(hint.contains("Singleton"), "a hint must not hand over the answer: " + hint);
        }
        assertFalse(OfflineHelp.hint(q, 0).isBlank(), "but there must still be a hint");
    }

    @Test
    void aQuestionWithNoAuthoredHintsStillGetsHelp() {
        Question q = question(Topic.LOOPS, QuestionType.TRACING);
        String first = OfflineHelp.hint(q, 0);

        assertFalse(first.contains("No hints available"), first);
        assertTrue(first.contains(Topic.LOOPS.displayName), first);
        assertTrue(first.length() > 40, first);
    }

    @Test
    void askingAgainGivesSomethingNew() {
        for (Topic topic : List.of(Topic.ARRAYS, rootTopic())) {
            Question q = question(topic, QuestionType.TRACING);
            Set<String> seen = new LinkedHashSet<>();
            for (int i = 0; i < 3; i++) seen.add(OfflineHelp.hint(q, i));

            assertEquals(3, seen.size(), topic
                    + ": pressing hint three times must not repeat the same sentence: " + seen);
        }
    }

    /** A topic with nothing underneath it — the case that used to run out of hints early. */
    private static Topic rootTopic() {
        return Topic.visibleValues().stream()
                .filter(t -> t.getPrerequisites().isEmpty()).findFirst().orElseThrow();
    }

    @Test
    void everyQuestionInTheBankHasThreeDistinctHints() {
        List<String> thin = new ArrayList<>();
        for (Question q : new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))) {
            Set<String> seen = new LinkedHashSet<>();
            for (int i = 0; i < 3; i++) seen.add(OfflineHelp.hint(q, i));
            if (seen.size() < 3) thin.add(q.getId() + " " + seen.size());
        }
        assertTrue(thin.isEmpty(), thin.size() + " question(s) repeat a hint within the first "
                + "three presses: " + thin.subList(0, Math.min(5, thin.size())));
    }

    @Test
    void askingPastTheEndIsStillAnAnswerRatherThanACrash() {
        Question q = question(Topic.LOOPS, QuestionType.TRACING);
        assertFalse(OfflineHelp.hint(q, 99).isBlank());
        assertFalse(OfflineHelp.hint(q, 0).isBlank());
    }

    @Test
    void theStrategyAdviceFitsTheKindOfQuestion() {
        assertTrue(OfflineHelp.derivedHints(question(Topic.LOOPS, QuestionType.TRACING))
                .stream().anyMatch(h -> h.contains("one line at a time")));
        assertTrue(OfflineHelp.derivedHints(question(Topic.LOOPS, QuestionType.DEBUGGING))
                .stream().anyMatch(h -> h.contains("The bug")));
        assertTrue(OfflineHelp.derivedHints(question(Topic.LOOPS, QuestionType.PARSONS))
                .stream().anyMatch(h -> h.contains("must come first")));
        assertTrue(OfflineHelp.derivedHints(question(Topic.LOOPS, QuestionType.CODING))
                .stream().anyMatch(h -> h.contains("failing test")));
    }

    @Test
    void aTaggedQuestionWarnsAboutItsTrapsWithoutNamingTheAnswer() {
        Question q = Question.builder()
                .id("d").topic(Topic.VARIABLES).type(QuestionType.MULTIPLE_CHOICE).difficulty(1)
                .prompt("p").choices("3.5", "3.0", "3", "err").answer("b")
                .distractor("a", Misconception.INTEGER_DIVISION)
                .build();

        List<String> hints = OfflineHelp.derivedHints(q);
        String traps = hints.stream().filter(h -> h.contains("traps")).findFirst().orElse("");
        assertTrue(traps.contains(Misconception.INTEGER_DIVISION.summary), traps);
        assertFalse(traps.contains("3.0"), "naming the trap must not name the answer: " + traps);
    }

    // ── The rule that matters most ───────────────────────────────────────────

    @Test
    void noDerivedHintAnywhereInTheBankGivesAwayItsAnswer() {
        List<String> offenders = new ArrayList<>();
        for (Question q : new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))) {
            String answer = q.getAnswer() == null ? "" : q.getAnswer().strip();
            if (answer.length() < 4) continue;   // "a"/"b" letters are not giveaways
            for (String hint : OfflineHelp.derivedHints(q)) {
                if (hint.contains(answer)) {
                    offenders.add(q.getId() + " -> " + hint);
                }
            }
        }
        assertTrue(offenders.isEmpty(),
                offenders.size() + " derived hint(s) contain their own answer: "
                        + offenders.subList(0, Math.min(3, offenders.size())));
    }

    @Test
    void everyQuestionInTheBankCanBeHelpedWith() {
        List<String> silent = new ArrayList<>();
        for (Question q : new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))) {
            for (int i = 0; i < 3; i++) {
                String hint = OfflineHelp.hint(q, i);
                if (hint == null || hint.isBlank() || hint.contains("No hints available")) {
                    silent.add(q.getId());
                    break;
                }
            }
        }
        assertTrue(silent.isEmpty(), silent.size() + " question(s) still answer a stuck student "
                + "with nothing: " + silent.subList(0, Math.min(5, silent.size())));
    }
}
