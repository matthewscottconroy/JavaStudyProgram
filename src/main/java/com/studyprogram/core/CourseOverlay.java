package com.studyprogram.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyprogram.model.Topic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * A course overlay maps an instructor's syllabus units onto topics, enabling
 * "unit review" sessions ("everything from units 1-4") without touching the
 * topic graph. Overlays are JSON files dropped into {@code data/courses/}:
 *
 * <pre>{@code
 * { "name": "Java II",
 *   "units": [ { "number": 1, "title": "Tools & Review", "topics": ["variables", "loops"] },
 *              { "number": 2, "title": "Arrays & Strings", "topics": ["arrays", "strings"] } ] }
 * }</pre>
 */
public class CourseOverlay {

    public record Unit(int number, String title, List<Topic> topics) {}

    private final String name;
    private final List<Unit> units;
    private final List<String> warnings = new ArrayList<>();

    private CourseOverlay(String name, List<Unit> units) {
        this.name = name;
        this.units = units;
    }

    public String getName()        { return name; }
    public List<Unit> getUnits()   { return units; }
    public List<String> getWarnings() { return warnings; }

    /** All course overlays found in a directory, alphabetically by filename. */
    public static List<CourseOverlay> loadAll(Path dir) {
        List<CourseOverlay> courses = new ArrayList<>();
        if (!Files.isDirectory(dir)) return courses;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path p : files.filter(f -> f.toString().endsWith(".json")).sorted().toList()) {
                try {
                    courses.add(load(p));
                } catch (IOException e) {
                    System.err.println("Warning: skipping course file " + p + " — " + e.getMessage());
                }
            }
        } catch (IOException ignored) {}
        return courses;
    }

    public static CourseOverlay load(Path file) throws IOException {
        JsonNode root = new ObjectMapper().readTree(file.toFile());
        String name = root.path("name").asText(file.getFileName().toString());
        List<Unit> units = new ArrayList<>();
        CourseOverlay course = new CourseOverlay(name, units);
        for (JsonNode u : root.path("units")) {
            List<Topic> topics = new ArrayList<>();
            for (JsonNode t : u.path("topics")) {
                Topic topic = Topic.fromDirSlug(t.asText());
                if (topic == null) {
                    course.warnings.add(name + " unit " + u.path("number").asInt()
                            + ": unknown topic '" + t.asText() + "' ignored.");
                } else {
                    topics.add(topic);
                }
            }
            units.add(new Unit(u.path("number").asInt(units.size() + 1),
                               u.path("title").asText(""), topics));
        }
        return course;
    }

    /** Union of topics for units in [from, to], in unit order without duplicates. */
    public List<Topic> topicsForUnits(int from, int to) {
        LinkedHashSet<Topic> result = new LinkedHashSet<>();
        for (Unit u : units) {
            if (u.number() >= from && u.number() <= to) result.addAll(u.topics());
        }
        return new ArrayList<>(result);
    }
}
