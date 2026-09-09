package com.studyprogram.core;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Topic-graph prerequisite overrides and course overlay files. */
class InstructorCustomizationTest {

    @TempDir
    Path dir;

    @AfterEach
    void restoreDefaults() {
        Topic.applyPrerequisiteOverrides(Map.of());
        Topic.applyHidden(java.util.Set.of());
    }

    @Test
    void overrideReplacesPrerequisitesEverywhere() throws Exception {
        Path file = dir.resolve("topic-graph.json");
        Files.writeString(file, """
                { "overrides": { "loops": { "prerequisites": [] },
                                 "generics": { "prerequisites": ["collections"] } } }
                """);
        List<String> warnings = TopicGraphOverrides.loadAndApply(file);
        assertTrue(warnings.isEmpty(), warnings.toString());
        assertTrue(Topic.LOOPS.getPrerequisites().isEmpty());
        assertEquals(List.of(Topic.COLLECTIONS), Topic.GENERICS.getPrerequisites());
        // untouched topics keep their defaults
        assertFalse(Topic.ARRAYS.getPrerequisites().isEmpty());
    }

    @Test
    void unknownSlugsWarnButDoNotBreak() throws Exception {
        Path file = dir.resolve("topic-graph.json");
        Files.writeString(file, """
                { "overrides": { "not_a_topic": { "prerequisites": [] },
                                 "loops": { "prerequisites": ["also_bogus"] } } }
                """);
        List<String> warnings = TopicGraphOverrides.loadAndApply(file);
        assertEquals(2, warnings.size());
        assertTrue(Topic.LOOPS.getPrerequisites().isEmpty(),
                "valid part of the override still applies");
    }

    @Test
    void cyclicOverridesAreRejectedWholesale() throws Exception {
        Path file = dir.resolve("topic-graph.json");
        Files.writeString(file, """
                { "overrides": { "variables": { "prerequisites": ["loops"] } } }
                """);
        // LOOPS already requires VARIABLES by default -> cycle
        List<String> warnings = TopicGraphOverrides.loadAndApply(file);
        assertFalse(warnings.isEmpty());
        assertFalse(Topic.VARIABLES.getPrerequisites().contains(Topic.LOOPS),
                "cyclic override must not be applied");
    }

    @Test
    void hiddenTopicsLeaveTheFeedTheMapAndSelection() throws Exception {
        Path file = dir.resolve("topic-graph.json");
        Files.writeString(file, """
                { "hidden": ["metaprogramming", "machine_learning", "not_a_topic"] }
                """);
        List<String> warnings = TopicGraphOverrides.loadAndApply(file);

        assertTrue(Topic.METAPROGRAMMING.isHidden());
        assertTrue(Topic.MACHINE_LEARNING.isHidden());
        assertFalse(Topic.LOOPS.isHidden());

        List<Topic> visible = Topic.visibleValues();
        assertEquals(Topic.values().length - 2, visible.size());
        assertFalse(visible.contains(Topic.METAPROGRAMMING));

        // an out-of-scope topic never reaches the student through the auto feed
        StudentProfile p = new StudentProfile("s");
        assertFalse(Curriculum.frontier(p).contains(Topic.METAPROGRAMMING));
        assertFalse(com.studyprogram.ui.map.MapModel.build(p).stream()
                .anyMatch(n -> n.topic() == Topic.METAPROGRAMMING));

        assertTrue(warnings.stream().anyMatch(w -> w.contains("unknown hidden topic")));
        assertTrue(warnings.stream().anyMatch(w -> w.contains("2 topic(s) hidden")));
    }

    @Test
    void missingFileIsFine() {
        assertTrue(TopicGraphOverrides.loadAndApply(dir.resolve("nope.json")).isEmpty());
    }

    @Test
    void courseOverlayLoadsUnitsAndUnions() throws Exception {
        Path file = dir.resolve("course.json");
        Files.writeString(file, """
                { "name": "Test Course",
                  "units": [ { "number": 1, "title": "A", "topics": ["variables", "loops"] },
                             { "number": 2, "title": "B", "topics": ["loops", "arrays"] },
                             { "number": 3, "title": "C", "topics": ["strings"] } ] }
                """);
        CourseOverlay course = CourseOverlay.load(file);
        assertEquals("Test Course", course.getName());
        assertEquals(3, course.getUnits().size());
        assertEquals(List.of(Topic.VARIABLES, Topic.LOOPS, Topic.ARRAYS),
                course.topicsForUnits(1, 2), "union in unit order, no duplicates");
        assertEquals(List.of(Topic.STRINGS), course.topicsForUnits(3, 3));
        assertTrue(course.topicsForUnits(9, 9).isEmpty());
    }

    @Test
    void shippedSampleCourseIsValid() throws Exception {
        Path sample = Path.of("data", "courses", "sample-java2.json");
        assertTrue(Files.isRegularFile(sample), "sample course file must ship with the repo");
        CourseOverlay course = CourseOverlay.load(sample);
        assertTrue(course.getWarnings().isEmpty(),
                "sample must reference only valid topics: " + course.getWarnings());
        assertEquals(7, course.getUnits().size());
        assertFalse(course.topicsForUnits(1, 7).isEmpty());
    }
}
