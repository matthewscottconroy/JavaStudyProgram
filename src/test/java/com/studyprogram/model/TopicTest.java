package com.studyprogram.model;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TopicTest {

    // ── Prerequisite resolution ───────────────────────────────────────────────

    @Test
    void variablesHasNoPrerequisites() {
        assertTrue(Topic.VARIABLES.getPrerequisites().isEmpty());
    }

    @Test
    void printingPrerequisiteIsVariables() {
        assertTrue(Topic.PRINTING.getPrerequisites().contains(Topic.VARIABLES));
    }

    @Test
    void loopsPrerequisitesContainVariablesAndIfCase() {
        assertTrue(Topic.LOOPS.getPrerequisites().contains(Topic.VARIABLES));
        assertTrue(Topic.LOOPS.getPrerequisites().contains(Topic.IF_CASE));
    }

    @Test
    void allPrerequisiteNamesResolveToValidTopics() {
        for (Topic t : Topic.values()) {
            assertDoesNotThrow(t::getPrerequisites,
                    "Topic " + t.name() + " has an unresolvable prerequisite name");
        }
    }

    @Test
    void baseLevelIsPositive() {
        for (Topic t : Topic.values()) {
            assertTrue(t.baseLevel >= 1 && t.baseLevel <= 5,
                    "Topic " + t.name() + " has baseLevel " + t.baseLevel + " outside 1–5");
        }
    }

    @Test
    void displayNameIsNonBlank() {
        for (Topic t : Topic.values()) {
            assertFalse(t.displayName.isBlank(),
                    "Topic " + t.name() + " has a blank displayName");
        }
    }

    // ── isUnlocked ────────────────────────────────────────────────────────────

    @Test
    void topicWithNoPrerequisitesIsAlwaysUnlocked() {
        Map<Topic, TopicPerformance> empty = new EnumMap<>(Topic.class);
        assertTrue(Topic.VARIABLES.isUnlocked(empty));
        assertTrue(Topic.IDE.isUnlocked(empty));
    }

    @Test
    void topicIsLockedWhenPrerequisiteIsMissing() {
        Map<Topic, TopicPerformance> empty = new EnumMap<>(Topic.class);
        assertFalse(Topic.PRINTING.isUnlocked(empty),
                "PRINTING requires VARIABLES to be mastered");
    }

    @Test
    void topicUnlocksWhenPrerequisiteReachesMasteryThreshold() {
        Map<Topic, TopicPerformance> perf = new EnumMap<>(Topic.class);
        TopicPerformance varPerf = new TopicPerformance(Topic.VARIABLES);
        for (int i = 0; i < 4; i++) varPerf.record("q" + i, true); // mastery = 0.4
        perf.put(Topic.VARIABLES, varPerf);

        assertTrue(Topic.PRINTING.isUnlocked(perf),
                "PRINTING should unlock when VARIABLES mastery >= 0.4");
    }

    @Test
    void topicRemainsLockedWhenPrerequisiteBelowThreshold() {
        Map<Topic, TopicPerformance> perf = new EnumMap<>(Topic.class);
        TopicPerformance varPerf = new TopicPerformance(Topic.VARIABLES);
        varPerf.record("q1", true);   // mastery = 0.1
        perf.put(Topic.VARIABLES, varPerf);

        assertFalse(Topic.PRINTING.isUnlocked(perf),
                "PRINTING should be locked when VARIABLES mastery < 0.4");
    }

    @Test
    void topicWithMultiplePrerequisitesRequiresAllToBeUnlocked() {
        Map<Topic, TopicPerformance> perf = new EnumMap<>(Topic.class);

        // Satisfy VARIABLES but not IF_CASE
        TopicPerformance varPerf = new TopicPerformance(Topic.VARIABLES);
        for (int i = 0; i < 4; i++) varPerf.record("q" + i, true);
        perf.put(Topic.VARIABLES, varPerf);

        assertFalse(Topic.LOOPS.isUnlocked(perf),
                "LOOPS requires both VARIABLES and IF_CASE");

        // Now also satisfy IF_CASE
        TopicPerformance ifPerf = new TopicPerformance(Topic.IF_CASE);
        for (int i = 0; i < 4; i++) ifPerf.record("q" + i, true);
        perf.put(Topic.IF_CASE, ifPerf);

        assertTrue(Topic.LOOPS.isUnlocked(perf));
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toStringReturnsDisplayName() {
        assertEquals(Topic.VARIABLES.displayName, Topic.VARIABLES.toString());
    }
}
