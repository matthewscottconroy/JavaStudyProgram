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

    @Test
    void cardContainsTheHeadlineNumbers() {
        StudentProfile p = new StudentProfile("Ada");
        p.getBossesCleared().add(1);
        List<AttemptRecord> attempts = List.of(
                new AttemptRecord(LocalDateTime.now(), q(Topic.LOOPS, QuestionType.CODING),
                        AttemptRecord.OUTCOME_CORRECT, 60, 0),
                new AttemptRecord(LocalDateTime.now(), q(Topic.LOOPS, QuestionType.TRACING),
                        AttemptRecord.OUTCOME_INCORRECT, 30, 1));

        String card = ProgressCard.render(p, attempts);
        assertTrue(card.contains("Ada"));
        assertTrue(card.contains("50%"), "1/2 correct");
        assertTrue(card.contains("Bosses cleared"));
        assertTrue(card.contains("1/5"));
        assertTrue(card.contains("World 1"));
        assertTrue(card.contains("Verification"));
    }

    @Test
    void verificationCodeChangesWhenNumbersChange() {
        StudentProfile p = new StudentProfile("Ada");
        String card1 = ProgressCard.render(p, List.of());
        String card2 = ProgressCard.render(p, List.of(
                new AttemptRecord(LocalDateTime.now(), q(Topic.LOOPS, QuestionType.TRACING),
                        AttemptRecord.OUTCOME_CORRECT, 5, 0)));
        String code1 = card1.lines().filter(l -> l.contains("Verification")).findFirst().orElseThrow();
        String code2 = card2.lines().filter(l -> l.contains("Verification")).findFirst().orElseThrow();
        assertNotEquals(code1, code2, "editing the card's numbers must break the code");
    }
}
