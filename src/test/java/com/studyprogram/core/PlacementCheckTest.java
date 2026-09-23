package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlacementCheckTest {

    private static Question mc(String id, Topic topic, int difficulty) {
        return Question.builder()
                .id(id).topic(topic).type(QuestionType.MULTIPLE_CHOICE).difficulty(difficulty)
                .prompt("p").choices("a", "b", "c", "d").answer("a").build();
    }

    private static PlacementCheck.Result result(Topic topic, boolean correct) {
        return new PlacementCheck.Result(mc("q-" + topic, topic, 3), correct);
    }

    // ── Seeding ──────────────────────────────────────────────────────────────

    @Test
    void aCorrectAnswerOpensTheTopicAndEverythingUnderneathIt() {
        StudentProfile p = new StudentProfile("Placed");
        var outcome = PlacementCheck.apply(p, List.of(result(Topic.ARRAYS, true)));

        assertTrue(outcome.unlocked().contains(Topic.ARRAYS));
        for (Topic prereq : PlacementCheck.transitivePrerequisites(Topic.ARRAYS)) {
            assertTrue(outcome.unlocked().contains(prereq),
                    prereq + " is underneath arrays and should be opened too");
            assertEquals(PlacementCheck.PLACEMENT_MASTERY,
                    p.getPerformance().get(prereq).getMasteryScore(), 1e-9);
        }
    }

    @Test
    void openedTopicsAreAboveTheUnlockLineButWellBelowMastery() {
        StudentProfile p = new StudentProfile("Placed");
        PlacementCheck.apply(p, List.of(result(Topic.LOOPS, true)));

        double seeded = p.getPerformance().get(Topic.LOOPS).getMasteryScore();
        assertTrue(seeded > Curriculum.UNLOCK_THRESHOLD,
                "placement must actually open the topic, got " + seeded);
        assertTrue(seeded < Curriculum.MASTERY_TARGET,
                "answering one question is not mastery, got " + seeded);
        assertTrue(Topic.LOOPS.isUnlocked(p.getPerformance()));
    }

    @Test
    void aWrongAnswerInfersNothingInEitherDirection() {
        StudentProfile p = new StudentProfile("Placed");
        var outcome = PlacementCheck.apply(p, List.of(result(Topic.ARRAYS, false)));

        assertTrue(outcome.unlocked().isEmpty(),
                "a missed arrays question could mean weak arrays or weak loops — guessing is worse "
                + "than not guessing");
        assertTrue(p.getPerformance().isEmpty(), "and nothing is written to the profile");
        assertTrue(outcome.placedAtBeginning());
    }

    @Test
    void seededTopicsAreFlaggedSoReportsDoNotPresentAnEstimateAsPractice() {
        StudentProfile p = new StudentProfile("Placed");
        PlacementCheck.apply(p, List.of(result(Topic.ARRAYS, true)));

        var seeded = p.getPerformance().get(Topic.VARIABLES);   // reached transitively
        assertNotNull(seeded);
        assertTrue(seeded.isPlacementSeeded(),
                "50% mastery with 0 attempts must be labelled, not left looking like a bug");
        assertEquals(0, seeded.getAttempts());

        // The first real answer makes the number the student's own.
        seeded.record("q1", true, 3);
        assertFalse(seeded.isPlacementSeeded());
    }

    @Test
    void placementNeverLowersMasteryAlreadyEarnedByPractising() {
        StudentProfile p = new StudentProfile("Veteran");
        var perf = p.getOrCreatePerformance(Topic.LOOPS);
        perf.setMasteryScore(0.95);

        PlacementCheck.apply(p, List.of(result(Topic.LOOPS, true)));
        assertEquals(0.95, p.getPerformance().get(Topic.LOOPS).getMasteryScore(), 1e-9,
                "a placement probe must not undo real practice");
    }

    @Test
    void severalPassedTopicsAccumulate() {
        StudentProfile p = new StudentProfile("Placed");
        var outcome = PlacementCheck.apply(p,
                List.of(result(Topic.LOOPS, true), result(Topic.ARRAYS, true),
                        result(Topic.CLASSES, false)));

        assertEquals(2, outcome.correct());
        assertEquals(3, outcome.asked());
        assertTrue(outcome.unlocked().contains(Topic.LOOPS));
        assertTrue(outcome.unlocked().contains(Topic.ARRAYS));
        assertFalse(outcome.unlocked().contains(Topic.CLASSES));
    }

    // ── The adaptive walk ────────────────────────────────────────────────────

    @Test
    void clearingALevelClimbsAndFailingItDropsBack() {
        Set<Integer> visited = new LinkedHashSet<>(List.of(2));
        assertEquals(3, PlacementCheck.nextLevel(2, PlacementCheck.PASS_MARK, visited));
        assertEquals(1, PlacementCheck.nextLevel(2, 0, visited));
    }

    @Test
    void theWalkNeverOscillatesBetweenTwoLevels() {
        Set<Integer> visited = new LinkedHashSet<>(List.of(2, 3));
        assertEquals(0, PlacementCheck.nextLevel(3, 0, visited),
                "dropping back to a level already answered would waste the budget");
        assertEquals(0, PlacementCheck.nextLevel(2, PlacementCheck.PASS_MARK, visited));
    }

    @Test
    void theWalkStopsAtBothEndsOfTheLadder() {
        assertEquals(0, PlacementCheck.nextLevel(1, 0, new LinkedHashSet<>(List.of(1))));
        assertEquals(0, PlacementCheck.nextLevel(PlacementCheck.MAX_LEVEL, 3,
                new LinkedHashSet<>(List.of(PlacementCheck.MAX_LEVEL))));
    }

    // ── Probe selection over the real bank ───────────────────────────────────

    @Test
    void everyLevelOfTheRealBankCanBeProbed() {
        QuestionBank bank = new QuestionBank();
        for (int level = 1; level <= PlacementCheck.MAX_LEVEL; level++) {
            List<Question> probes = PlacementCheck.probesForLevel(bank, level, new Random(level));
            assertFalse(probes.isEmpty(), "no probe questions available for level " + level);
            assertTrue(probes.size() <= PlacementCheck.PROBES_PER_LEVEL);
            for (Question q : probes) {
                assertEquals(level, q.getTopic().baseLevel);
                assertTrue(q.isMultipleChoice(),
                        "a probe must be gradeable without a compiler: " + q.getId());
                assertNotSame(QuestionType.CODING, q.getType());
            }
        }
    }

    /**
     * Regression: ranking probes by prerequisite depth put Project Organization and Javadoc at the
     * front of the check. They sit on top of everything and carry nothing, so a student fluent in
     * Java but hazy on build tooling was placed at the beginning. Probes must be topics the rest
     * of the curriculum is built on.
     */
    @Test
    void probesAreCentralTopicsNotPeripheralOnes() {
        QuestionBank bank = new QuestionBank();
        Set<Topic> peripheral = Set.of(Topic.JAVADOC, Topic.MAVEN, Topic.PROJECT_ORGANIZATION,
                                       Topic.IDE, Topic.DEBUGGING_TOOLS);
        for (int level = 2; level <= PlacementCheck.MAX_LEVEL; level++) {
            for (Topic t : PlacementCheck.probeTopics(bank, level)) {
                assertFalse(peripheral.contains(t),
                        "level " + level + " should not be judged by " + t.displayName);
                // Nothing sits above the top level, so centrality only means something below it.
                if (level < PlacementCheck.MAX_LEVEL) {
                    assertTrue(PlacementCheck.dependentCount(t) > 0,
                            t.displayName + " has nothing depending on it, so it cannot stand "
                            + "for a whole level");
                }
            }
        }
    }

    @Test
    void probesPreferTheTopicTheMostOtherTopicsDependOn() {
        QuestionBank bank = new QuestionBank();
        List<Topic> probes = PlacementCheck.probeTopics(bank, 3);
        assertFalse(probes.isEmpty());
        for (Topic other : Topic.visibleValues()) {
            if (other.baseLevel != 3 || probes.contains(other)) continue;
            if (PlacementCheck.probeQuestions(bank, other).isEmpty()) continue;
            assertTrue(PlacementCheck.dependentCount(probes.get(0))
                            >= PlacementCheck.dependentCount(other),
                    probes.get(0).displayName + " should be at least as central as "
                    + other.displayName);
        }
    }

    @Test
    void aFullPassOverTheRealBankPlacesAStrongStudentHigh() {
        QuestionBank bank = new QuestionBank();
        StudentProfile p = new StudentProfile("Strong");
        List<PlacementCheck.Result> results = new ArrayList<>();
        Set<Integer> visited = new LinkedHashSet<>();

        // Simulate a student who answers everything correctly: the walk should climb to the top.
        int level = PlacementCheck.START_LEVEL;
        int asked = 0;
        while (level > 0 && asked < PlacementCheck.LENGTH) {
            visited.add(level);
            List<Question> probes = PlacementCheck.probesForLevel(bank, level, new Random(1));
            int correct = 0;
            for (Question q : probes) {
                if (asked >= PlacementCheck.LENGTH) break;
                asked++;
                correct++;
                results.add(new PlacementCheck.Result(q, true));
            }
            level = PlacementCheck.nextLevel(level, correct, visited);
        }

        var outcome = PlacementCheck.apply(p, results);
        assertTrue(outcome.highestLevelPassed() >= 4,
                "answering everything right should reach the advanced worlds, reached "
                + outcome.highestLevelPassed());
        assertTrue(outcome.unlocked().size() > 10,
                "the prerequisite graph should spread a dozen answers across many topics, opened "
                + outcome.unlocked().size());
        assertTrue(asked <= PlacementCheck.LENGTH, "the budget must be respected");
    }

    @Test
    void theSummaryIsHonestAboutWhatPlacementProved() {
        StudentProfile p = new StudentProfile("Placed");
        String summary = PlacementCheck.summary(
                PlacementCheck.apply(p, List.of(result(Topic.LOOPS, true))));
        assertTrue(summary.contains("opened, not ticked off"), summary);

        String none = PlacementCheck.summary(
                PlacementCheck.apply(new StudentProfile("New"), List.of(result(Topic.LOOPS, false))));
        assertTrue(none.contains("beginning"), none);
    }
}
