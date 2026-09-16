package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MistakeDeckTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 1, 9, 0);

    private static Question q(String id, QuestionType type) {
        Question.Builder b = Question.builder()
                .id(id).topic(Topic.LOOPS).type(type).difficulty(2).prompt("p").answer("a");
        if (type == QuestionType.CODING) b.starterCode("class X {}").testCode("class XT {}");
        return b.build();
    }

    private static final List<Question> ALL = List.of(
            q("q1", QuestionType.MULTIPLE_CHOICE), q("q2", QuestionType.MULTIPLE_CHOICE),
            q("q3", QuestionType.MULTIPLE_CHOICE), q("c1", QuestionType.CODING),
            q("c2", QuestionType.CODING));

    private static QuestionBank bank() { return QuestionBank.of(ALL); }

    private static AttemptRecord at(int minute, String id, String outcome, String... errors) {
        Question question = ALL.stream().filter(x -> x.getId().equals(id)).findFirst().orElseThrow();
        return new AttemptRecord(T0.plusMinutes(minute), question, outcome, 30, 0,
                List.of(errors));
    }

    @Test
    void aQuestionGotWrongIsWaitingToBeRetried() {
        var deck = MistakeDeck.build(bank(), List.of(at(1, "q1", AttemptRecord.OUTCOME_INCORRECT)), 10);

        assertEquals(1, deck.questions().size());
        assertEquals("q1", deck.questions().get(0).getId());
        assertEquals(1, deck.unresolvedCount());
    }

    @Test
    void gettingItRightLaterClearsIt() {
        var deck = MistakeDeck.build(bank(), List.of(
                at(1, "q1", AttemptRecord.OUTCOME_INCORRECT),
                at(2, "q1", AttemptRecord.OUTCOME_CORRECT)), 10);

        assertTrue(deck.isEmpty(), "a mistake the student has since fixed is not still a mistake");
    }

    @Test
    void gettingItWrongAgainAfterGettingItRightPutsItBack() {
        var deck = MistakeDeck.build(bank(), List.of(
                at(1, "q1", AttemptRecord.OUTCOME_CORRECT),
                at(2, "q1", AttemptRecord.OUTCOME_INCORRECT)), 10);

        assertEquals(List.of("q1"), ids(deck));
    }

    @Test
    void skippingIsNotAnAnswerInEitherDirection() {
        var afterSkip = MistakeDeck.build(bank(), List.of(
                at(1, "q1", AttemptRecord.OUTCOME_INCORRECT),
                at(2, "q1", AttemptRecord.OUTCOME_SKIPPED)), 10);
        assertEquals(List.of("q1"), ids(afterSkip), "skipping does not resolve a mistake");

        var onlySkips = MistakeDeck.build(bank(),
                List.of(at(1, "q2", AttemptRecord.OUTCOME_SKIPPED)), 10);
        assertTrue(onlySkips.isEmpty(), "and skipping alone is not a mistake either");
    }

    @Test
    void theOldestUnfixedMistakeComesFirst() {
        var deck = MistakeDeck.build(bank(), List.of(
                at(1, "q1", AttemptRecord.OUTCOME_INCORRECT),
                at(5, "q2", AttemptRecord.OUTCOME_INCORRECT),
                at(9, "q3", AttemptRecord.OUTCOME_INCORRECT)), 10);

        assertEquals(List.of("q1", "q2", "q3"), ids(deck),
                "the longest-standing mistake is the most likely to be forgotten");
    }

    @Test
    void anErrorHitRepeatedlyBringsBackTheExercisesWhereItBit() {
        var deck = MistakeDeck.build(bank(), List.of(
                at(1, "c1", AttemptRecord.OUTCOME_CORRECT, "cannot find symbol"),
                at(2, "c2", AttemptRecord.OUTCOME_CORRECT, "cannot find symbol")), 10);

        assertEquals(List.of("cannot find symbol"), deck.recurringErrors());
        assertEquals(0, deck.unresolvedCount(), "both exercises were ultimately passed");
        assertEquals(List.of("c1", "c2"), ids(deck),
                "but they are where the recurring error actually happened");
    }

    @Test
    void aOneOffErrorIsNotAHabit() {
        var deck = MistakeDeck.build(bank(), List.of(
                at(1, "c1", AttemptRecord.OUTCOME_CORRECT, "missing semicolon")), 10);

        assertTrue(deck.recurringErrors().isEmpty(),
                "one slip is not worth telling a student to watch for");
        assertTrue(deck.isEmpty());
    }

    @Test
    void unfixedMistakesOutrankErrorProneExercises() {
        var deck = MistakeDeck.build(bank(), List.of(
                at(1, "c1", AttemptRecord.OUTCOME_CORRECT, "missing return"),
                at(2, "c2", AttemptRecord.OUTCOME_CORRECT, "missing return"),
                at(3, "q1", AttemptRecord.OUTCOME_INCORRECT)), 10);

        assertEquals("q1", ids(deck).get(0), "an actual wrong answer comes before a near-miss");
        assertEquals(1, deck.unresolvedCount());
    }

    @Test
    void theDeckIsCappedSoItStaysASession() {
        List<AttemptRecord> many = new ArrayList<>();
        for (int i = 0; i < ALL.size(); i++) {
            many.add(at(i, ALL.get(i).getId(), AttemptRecord.OUTCOME_INCORRECT));
        }
        assertEquals(2, MistakeDeck.build(bank(), many, 2).questions().size());
    }

    @Test
    void aQuestionThatNoLongerExistsIsSilentlyDropped() {
        Question gone = q("removed-pack-question", QuestionType.MULTIPLE_CHOICE);
        var deck = MistakeDeck.build(bank(), List.of(
                new AttemptRecord(T0, gone, AttemptRecord.OUTCOME_INCORRECT, 10, 0),
                at(2, "q1", AttemptRecord.OUTCOME_INCORRECT)), 10);

        assertEquals(List.of("q1"), ids(deck),
                "a student cannot practise a question that is no longer installed");
    }

    @Test
    void noHistoryMeansNothingToPractise() {
        assertTrue(MistakeDeck.build(bank(), List.of(), 10).isEmpty());
    }

    private static List<String> ids(MistakeDeck.Deck deck) {
        return deck.questions().stream().map(Question::getId).toList();
    }
}
