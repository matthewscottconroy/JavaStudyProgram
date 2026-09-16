package com.studyprogram.report;

import com.studyprogram.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgressCardTest {

    private Question q(Topic t, QuestionType type) {
        Question.Builder b = Question.builder()
                .id("x-" + type).topic(t).type(type).difficulty(2).prompt("p").answer("a");
        if (type == QuestionType.CODING) {
            b.starterCode("public class X {}").testCode("public class XTest {}");
        }
        return b.build();
    }

    private AttemptRecord attempt(QuestionType type, String outcome) {
        return new AttemptRecord(LocalDateTime.now(), q(Topic.LOOPS, type), outcome, 60, 0);
    }

    @Test
    void cardContainsTheHeadlineNumbers() {
        StudentProfile p = new StudentProfile("Ada");
        p.getBossesCleared().add(1);
        String card = ProgressCard.render(p, List.of(
                attempt(QuestionType.CODING, AttemptRecord.OUTCOME_CORRECT),
                attempt(QuestionType.TRACING, AttemptRecord.OUTCOME_INCORRECT)));

        assertTrue(card.contains("Ada"));
        assertTrue(card.contains("50%"), "1/2 correct");
        assertTrue(card.contains("Bosses cleared"));
        assertTrue(card.contains("1/5"));
        assertTrue(card.contains("World 1"));
        assertTrue(card.contains("Checksum"), "unsigned cards carry a checksum");
    }

    @Test
    void aGoalAppearsOnTheCardAndIsCoveredByTheChecksum() {
        StudentProfile p = new StudentProfile("Ada");
        p.setGoal(new StudyGoal("Final", "Worlds 1-2", java.time.LocalDate.now().plusDays(5),
                List.of(Topic.LOOPS, Topic.VARIABLES)));
        String card = ProgressCard.render(p, List.of());

        assertTrue(card.contains("Goal: Final"), card);
        assertTrue(card.contains("0/2 topics at target, 5 days left"), card);
        assertTrue(ProgressCard.verify(card, p.getId()).startsWith("VALID"),
                "the goal line is part of what is signed");
        assertTrue(ProgressCard.verify(card.replace("0/2 topics", "2/2 topics"), p.getId())
                        .startsWith("INVALID"),
                "editing the goal line must break the code, like any other number on the card");
    }

    @Test
    void codeChangesWhenTheNumbersChange() {
        StudentProfile p = new StudentProfile("Ada");
        String empty = ProgressCard.render(p, List.of());
        String worked = ProgressCard.render(p, List.of(
                attempt(QuestionType.TRACING, AttemptRecord.OUTCOME_CORRECT)));
        assertNotEquals(codeLine(empty), codeLine(worked),
                "editing the card's numbers must break its code");
    }

    @Test
    void anUnalteredCardVerifies() {
        StudentProfile p = new StudentProfile("Ada");
        String card = ProgressCard.render(p, List.of(
                attempt(QuestionType.CODING, AttemptRecord.OUTCOME_CORRECT)));
        assertTrue(ProgressCard.verify(card, p.getId()).startsWith("VALID"));
    }

    @Test
    void anEditedCardFailsVerification() {
        StudentProfile p = new StudentProfile("Ada");
        String card = ProgressCard.render(p, List.of(
                attempt(QuestionType.CODING, AttemptRecord.OUTCOME_CORRECT)));
        String doctored = card.replace("Programs written & passed  1",
                                       "Programs written & passed  9");
        assertNotEquals(card, doctored, "the test fixture must actually change a number");
        assertTrue(ProgressCard.verify(doctored, p.getId()).startsWith("INVALID"));
    }

    @Test
    void aCardFromADifferentProfileFailsVerification() {
        StudentProfile ada = new StudentProfile("Ada");
        StudentProfile bob = new StudentProfile("Bob");
        String card = ProgressCard.render(ada, List.of());
        assertTrue(ProgressCard.verify(card, bob.getId()).startsWith("INVALID"));
    }

    @Test
    void nonCardTextIsReportedClearly() {
        assertTrue(ProgressCard.verify("just some text", "id").contains("No checksum line"));
    }

    private static String codeLine(String card) {
        return card.lines().filter(l -> l.contains("Checksum") || l.contains("Signature"))
                .findFirst().orElseThrow();
    }
}
