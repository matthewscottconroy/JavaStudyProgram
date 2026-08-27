package com.studyprogram.ui.map;

import com.studyprogram.core.Curriculum;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure layout/state model for the concept map — computed from the topic graph and a
 * student's performance, with no Swing dependency so it is testable headlessly.
 *
 * Topics are laid out in columns by level band (the "worlds"); prerequisite edges
 * flow left to right. Advanced optional topics are "secret": until their
 * prerequisites are met they render as unlabeled mystery nodes.
 */
public final class MapModel {

    /** Advanced optional topics hidden behind "secret exit" style reveals. */
    public static final Set<Topic> SECRET_TOPICS = EnumSet.of(
            Topic.REFLECTION, Topic.METAPROGRAMMING,
            Topic.MACHINE_LEARNING, Topic.EVOLUTIONARY_PROGRAMMING);

    public enum NodeState { LOCKED, SECRET, AVAILABLE, IN_PROGRESS, MASTERED }

    public record Node(Topic topic, int col, int row, NodeState state,
                       double mastery, boolean selected) {

        public String label() {
            return state == NodeState.SECRET ? "? ? ?" : topic.displayName;
        }
    }

    private MapModel() {}

    /** One node per topic, positioned by (level band, order within band). */
    public static List<Node> build(StudentProfile profile) {
        List<Node> nodes = new ArrayList<>();
        int[] rowInCol = new int[6];
        for (Topic t : Topic.values()) {
            int col = t.baseLevel - 1;
            nodes.add(new Node(t, col, rowInCol[t.baseLevel]++,
                    stateOf(t, profile),
                    Curriculum.mastery(profile.getPerformance(), t),
                    profile.getSelectedTopics().contains(t)));
        }
        return nodes;
    }

    public static NodeState stateOf(Topic t, StudentProfile profile) {
        Map<Topic, TopicPerformance> perf = profile.getPerformance();
        boolean unlocked = t.getPrerequisites().isEmpty() || t.isUnlocked(perf);
        if (!unlocked) {
            return SECRET_TOPICS.contains(t) ? NodeState.SECRET : NodeState.LOCKED;
        }
        TopicPerformance p = perf.get(t);
        if (p != null && p.getMasteryScore() >= Curriculum.MASTERY_TARGET) return NodeState.MASTERED;
        if (p != null && p.getAttempts() > 0) return NodeState.IN_PROGRESS;
        return NodeState.AVAILABLE;
    }

    /** Rows in the tallest column — determines the map's height. */
    public static int maxRows(List<Node> nodes) {
        int max = 0;
        for (Node n : nodes) max = Math.max(max, n.row() + 1);
        return max;
    }
}
