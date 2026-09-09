package com.studyprogram.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyprogram.model.Topic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Optional instructor customization of the topic graph, loaded from
 * {@code data/topic-graph.json}:
 *
 * <pre>{@code
 * { "overrides": { "generics": { "prerequisites": ["collections"] },
 *                  "regex":    { "prerequisites": [] } } }
 * }</pre>
 *
 * Keys and prerequisite entries are topic slugs (lower-cased enum names). An
 * override replaces that topic's built-in prerequisite list everywhere: unlocking,
 * the auto feed, the concept map. Overrides that would create a cycle are rejected.
 */
public final class TopicGraphOverrides {

    private TopicGraphOverrides() {}

    /**
     * Loads and applies overrides from the given file if it exists.
     *
     * @return warnings for anything ignored (unknown slugs, cycles); empty when clean
     */
    public static List<String> loadAndApply(Path file) {
        List<String> warnings = new ArrayList<>();
        if (!Files.isRegularFile(file)) return warnings;

        Map<Topic, List<Topic>> overrides = new EnumMap<>(Topic.class);
        Set<Topic> hidden = EnumSet.noneOf(Topic.class);
        try {
            JsonNode root = new ObjectMapper().readTree(file.toFile());
            for (JsonNode h : root.path("hidden")) {
                Topic topic = Topic.fromDirSlug(h.asText());
                if (topic == null) {
                    warnings.add("topic-graph.json: unknown hidden topic '" + h.asText() + "' ignored.");
                } else {
                    hidden.add(topic);
                }
            }
            JsonNode entries = root.path("overrides");
            entries.properties().forEach(entry -> {
                Topic topic = Topic.fromDirSlug(entry.getKey());
                if (topic == null) {
                    warnings.add("topic-graph.json: unknown topic '" + entry.getKey() + "' ignored.");
                    return;
                }
                List<Topic> prereqs = new ArrayList<>();
                for (JsonNode p : entry.getValue().path("prerequisites")) {
                    Topic prereq = Topic.fromDirSlug(p.asText());
                    if (prereq == null) {
                        warnings.add("topic-graph.json: unknown prerequisite '" + p.asText()
                                + "' for " + entry.getKey() + " ignored.");
                    } else if (prereq == topic) {
                        warnings.add("topic-graph.json: " + entry.getKey()
                                + " cannot require itself — ignored.");
                    } else {
                        prereqs.add(prereq);
                    }
                }
                overrides.put(topic, prereqs);
            });
        } catch (IOException e) {
            warnings.add("Could not read " + file + ": " + e.getMessage());
            return warnings;
        }
        if (!hidden.isEmpty()) {
            warnings.add("topic-graph.json: " + hidden.size() + " topic(s) hidden from this course.");
        }

        if (createsCycle(overrides)) {
            warnings.add("topic-graph.json: overrides would create a prerequisite cycle — "
                    + "all overrides ignored.");
            return warnings;
        }
        Topic.applyPrerequisiteOverrides(overrides);
        Topic.applyHidden(hidden);
        return warnings;
    }

    /** DFS cycle check over the graph with the overrides applied on top of defaults. */
    static boolean createsCycle(Map<Topic, List<Topic>> overrides) {
        Map<Topic, Integer> state = new EnumMap<>(Topic.class);   // 1 = visiting, 2 = done
        for (Topic t : Topic.values()) {
            if (visit(t, overrides, state)) return true;
        }
        return false;
    }

    private static boolean visit(Topic t, Map<Topic, List<Topic>> overrides,
                                 Map<Topic, Integer> state) {
        Integer s = state.get(t);
        if (s != null) return s == 1;
        state.put(t, 1);
        List<Topic> prereqs = overrides.containsKey(t)
                ? overrides.get(t) : t.getPrerequisites();
        for (Topic p : prereqs) {
            if (visit(p, overrides, state)) return true;
        }
        state.put(t, 2);
        return false;
    }
}
