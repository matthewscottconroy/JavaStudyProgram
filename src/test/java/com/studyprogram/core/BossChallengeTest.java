package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BossChallengeTest {

    private static QuestionBank bank;

    @BeforeAll
    static void load() {
        bank = new QuestionBank();
    }

    @Test
    void lockedUntilWorldAverageMasteryReached() {
        StudentProfile p = new StudentProfile("s");
        assertFalse(BossChallenge.unlocked(p, 1));
        for (Topic t : BossChallenge.worldTopics(1)) {
            p.getOrCreatePerformance(t).setMasteryScore(0.6);
        }
        assertTrue(BossChallenge.unlocked(p, 1));
    }

    @Test
    void quizExcludesCodingAndSpreadsTopics() {
        for (int level = 1; level <= 5; level++) {
            List<Question> quiz = BossChallenge.pickQuestions(bank, level, new Random(42));
            assertEquals(BossChallenge.QUESTION_COUNT, quiz.size(),
                    "world " + level + " should fill a full quiz");
            final int lv = level;
            assertTrue(quiz.stream().allMatch(q -> q.getTopic().baseLevel == lv));
            assertTrue(quiz.stream().noneMatch(q -> q.getType() == QuestionType.CODING));
            assertEquals(quiz.size(), quiz.stream().map(Question::getId).distinct().count());
        }
    }

    @Test
    void differentSeedsGiveDifferentQuizzes() {
        List<Question> a = BossChallenge.pickQuestions(bank, 2, new Random(1));
        List<Question> b = BossChallenge.pickQuestions(bank, 2, new Random(2));
        assertNotEquals(a, b);
    }

    @Test
    void passThresholdIs80Percent() {
        assertTrue(BossChallenge.passed(8, 10));
        assertFalse(BossChallenge.passed(7, 10));
        assertFalse(BossChallenge.passed(0, 0));
    }

    @Test
    void clearedStateRoundTripsOnProfile() {
        StudentProfile p = new StudentProfile("s");
        assertFalse(BossChallenge.cleared(p, 3));
        p.getBossesCleared().add(3);
        assertTrue(BossChallenge.cleared(p, 3));
    }
}
