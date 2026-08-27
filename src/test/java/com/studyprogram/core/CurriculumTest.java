package com.studyprogram.core;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurriculumTest {

    private void setMastery(StudentProfile p, Topic t, double mastery) {
        p.getOrCreatePerformance(t).setMasteryScore(mastery);
        p.getOrCreatePerformance(t).setAttempts(10);
    }

    @Test
    void newStudentFrontierIsPrereqFreeBeginnerTopics() {
        List<Topic> frontier = Curriculum.frontier(new StudentProfile("new"));
        assertFalse(frontier.isEmpty());
        // every frontier topic for a blank profile must itself have no prerequisites
        for (Topic t : frontier) {
            assertTrue(t.getPrerequisites().isEmpty(),
                    t + " has prerequisites but appeared in a blank profile's frontier");
        }
        assertTrue(frontier.contains(Topic.VARIABLES));
        // easiest first
        assertEquals(1, frontier.get(0).baseLevel);
    }

    @Test
    void masteringPrereqsUnlocksDownstreamTopics() {
        StudentProfile p = new StudentProfile("s");
        assertFalse(Curriculum.frontier(p).contains(Topic.IF_CASE));
        setMastery(p, Topic.VARIABLES, 0.5);   // >= unlock threshold
        assertTrue(Curriculum.frontier(p).contains(Topic.IF_CASE));
    }

    @Test
    void masteredTopicsLeaveTheFrontierAndBecomeReview() {
        StudentProfile p = new StudentProfile("s");
        setMastery(p, Topic.VARIABLES, 0.9);
        assertFalse(Curriculum.frontier(p).contains(Topic.VARIABLES));
        assertTrue(Curriculum.reviewCandidates(p).contains(Topic.VARIABLES));
    }

    @Test
    void autoTopicsRespectsCapAndMixesReview() {
        StudentProfile p = new StudentProfile("s");
        setMastery(p, Topic.VARIABLES, 0.9);
        List<Topic> plan = Curriculum.autoTopics(p, 6);
        assertFalse(plan.isEmpty());
        assertTrue(plan.size() <= 6);
        assertTrue(plan.contains(Topic.VARIABLES), "mastered topic should appear as review");
    }

    @Test
    void dependsOnIsTransitive() {
        // GENERICS -> COLLECTIONS -> ARRAYS_ARRAYLISTS -> ARRAYS -> VARIABLES
        assertTrue(Curriculum.dependsOn(Topic.GENERICS, Topic.VARIABLES));
        assertFalse(Curriculum.dependsOn(Topic.VARIABLES, Topic.GENERICS));
    }

    @Test
    void downstreamBlockedCountsLockedDependents() {
        StudentProfile blank = new StudentProfile("s");
        int blocked = Curriculum.downstreamBlocked(Topic.VARIABLES, blank);
        assertTrue(blocked > 10,
                "VARIABLES should block many topics for a blank profile, got " + blocked);
    }
}
