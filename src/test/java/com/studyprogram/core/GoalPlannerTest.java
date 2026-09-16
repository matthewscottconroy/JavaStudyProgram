package com.studyprogram.core;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.StudyGoal;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoalPlannerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    private static StudentProfile withMastery(double... masteryByTopic) {
        // topics in order: VARIABLES, LOOPS, ARRAYS
        Topic[] topics = {Topic.VARIABLES, Topic.LOOPS, Topic.ARRAYS};
        StudentProfile p = new StudentProfile("Goal");
        for (int i = 0; i < masteryByTopic.length; i++) {
            var perf = p.getOrCreatePerformance(topics[i]);
            perf.setMasteryScore(masteryByTopic[i]);
            perf.setAttempts(5);
        }
        p.setGoal(new StudyGoal("Midterm", "Units 1-2", TODAY.plusDays(10),
                List.of(Topic.VARIABLES, Topic.LOOPS, Topic.ARRAYS)));
        return p;
    }

    @Test
    void noGoalMeansNoPlan() {
        assertNull(GoalPlanner.plan(new StudentProfile("None"), TODAY));
    }

    @Test
    void topicsAreSortedIntoBehindAndOnTargetWeakestFirst() {
        var plan = GoalPlanner.plan(withMastery(0.9, 0.2, 0.5), TODAY);

        assertEquals(List.of(Topic.VARIABLES), plan.onTarget());
        assertEquals(List.of(Topic.LOOPS, Topic.ARRAYS), plan.behind(), "weakest first");
        assertEquals(10, plan.daysLeft());
        assertFalse(plan.isDone());
    }

    @Test
    void theEstimateFollowsTheMasteryModel() {
        // One topic, 0.5 behind target, default accuracy: gain = 0.7*0.11 - 0.3*0.06 = 0.059/q
        var plan = GoalPlanner.plan(withMastery(0.8, 0.8, 0.3), TODAY);

        assertEquals(1, plan.behind().size());
        int expected = (int) Math.ceil(0.5 / GoalPlanner.expectedGain(withMastery(0.8, 0.8, 0.3)));
        assertEquals(expected, plan.questionsNeeded());
        assertEquals((int) Math.ceil(expected / 10.0), plan.questionsPerDay());
        assertTrue(plan.onTrack(), "a question a day is comfortably on track");
    }

    @Test
    void aStudentsOwnAccuracyDrivesTheirEstimate() {
        StudentProfile sharp = withMastery(0.0, 0.8, 0.8);
        sharp.setTotalQuestionsAnswered(40);
        sharp.setTotalCorrect(38);
        StudentProfile struggling = withMastery(0.0, 0.8, 0.8);
        struggling.setTotalQuestionsAnswered(40);
        struggling.setTotalCorrect(20);

        assertTrue(GoalPlanner.plan(sharp, TODAY).questionsNeeded()
                        < GoalPlanner.plan(struggling, TODAY).questionsNeeded(),
                "a student who gets more right needs fewer questions to close the same gap");
    }

    @Test
    void anImpossiblePaceIsReportedAsOffTrack() {
        StudentProfile p = withMastery(0.0, 0.0, 0.0);
        p.getGoal().setDate(TODAY.plusDays(1));

        var plan = GoalPlanner.plan(p, TODAY);
        assertTrue(plan.questionsPerDay() > GoalPlanner.SUSTAINABLE_PER_DAY);
        assertFalse(plan.onTrack());
        assertTrue(GoalPlanner.summary(plan).contains("Start today"));
    }

    @Test
    void aMetGoalIsDoneAndSaysSo() {
        var plan = GoalPlanner.plan(withMastery(0.9, 0.85, 0.8), TODAY);
        assertTrue(plan.isDone());
        assertTrue(plan.onTrack());
        assertEquals(0, plan.questionsNeeded());
        assertTrue(GoalPlanner.summary(plan).contains("all 3 topics at target"));
    }

    @Test
    void thePastDateDoesNotDivideByZero() {
        StudentProfile p = withMastery(0.0, 0.8, 0.8);
        p.getGoal().setDate(TODAY.minusDays(3));

        var plan = GoalPlanner.plan(p, TODAY);
        assertEquals(0, plan.daysLeft());
        assertTrue(plan.questionsPerDay() > 0);
        assertFalse(plan.onTrack());
    }

    @Test
    void theFeedFocusesOnGoalTopicsTheStudentIsReadyFor() {
        StudentProfile p = withMastery(0.2, 0.2, 0.2);
        p.getGoal().setDate(LocalDate.now().plusDays(14));

        List<Topic> focus = GoalPlanner.focusTopics(p, 6);
        assertTrue(focus.contains(Topic.VARIABLES), focus.toString());
        assertTrue(focus.indexOf(Topic.VARIABLES) < focus.indexOf(Topic.ARRAYS)
                        || !focus.contains(Topic.ARRAYS),
                "a locked goal topic comes after the prerequisites that unlock it: " + focus);
    }

    @Test
    void aLockedGoalTopicContributesItsPrerequisitesInstead() {
        // Goal is only ARRAYS, which needs VARIABLES/LOOPS-type foundations the student lacks.
        StudentProfile p = new StudentProfile("Locked");
        p.setGoal(new StudyGoal("Quiz", "Arrays", LocalDate.now().plusDays(7), List.of(Topic.ARRAYS)));

        List<Topic> focus = GoalPlanner.focusTopics(p, 6);
        assertFalse(focus.isEmpty());
        for (Topic prereq : Topic.ARRAYS.getPrerequisites()) {
            assertTrue(focus.contains(prereq),
                    "the path to a locked goal topic runs through " + prereq + ": " + focus);
        }
    }

    @Test
    void aMetGoalFallsBackToTheOrdinaryCurriculumMix() {
        StudentProfile p = withMastery(0.9, 0.9, 0.9);
        p.getGoal().setDate(LocalDate.now().plusDays(7));
        assertEquals(Curriculum.autoTopics(p, 6), GoalPlanner.focusTopics(p, 6));
    }
}
