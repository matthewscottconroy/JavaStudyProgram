package com.studyprogram.core;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Turns the topic prerequisite graph plus a student's performance into a study plan.
 *
 * The auto feed mixes three ingredients:
 *  - the "frontier": unlocked topics not yet mastered (the main course),
 *  - up to two mastered topics that haven't been touched recently (spaced review),
 *  - and nothing that is still locked behind unmet prerequisites.
 */
public final class Curriculum {

    /** A prerequisite counts as met at this mastery (matches Topic.isUnlocked). */
    public static final double UNLOCK_THRESHOLD = 0.4;
    /** A topic counts as mastered at this level. */
    public static final double MASTERY_TARGET = 0.8;

    private static final int REVIEW_SLOTS = 2;

    private Curriculum() {}

    /** Unlocked topics below the mastery target, easiest level and weakest mastery first. */
    public static List<Topic> frontier(StudentProfile profile) {
        Map<Topic, TopicPerformance> perf = profile.getPerformance();
        List<Topic> result = new ArrayList<>();
        for (Topic t : Topic.values()) {
            if (!t.isUnlocked(perf) && !t.getPrerequisites().isEmpty()) continue;
            if (mastery(perf, t) < MASTERY_TARGET) result.add(t);
        }
        result.sort(Comparator
                .comparingInt((Topic t) -> t.baseLevel)
                .thenComparingDouble(t -> mastery(perf, t)));
        return result;
    }

    /** Mastered topics, least-recently practiced first — candidates for spaced review. */
    public static List<Topic> reviewCandidates(StudentProfile profile) {
        Map<Topic, TopicPerformance> perf = profile.getPerformance();
        List<Topic> result = new ArrayList<>();
        for (Topic t : Topic.values()) {
            if (mastery(perf, t) >= MASTERY_TARGET) result.add(t);
        }
        result.sort(Comparator.comparing(
                t -> lastAttempted(perf, t),
                Comparator.nullsFirst(Comparator.naturalOrder())));
        return result;
    }

    /**
     * The topic mix for an auto-feed session: mostly frontier, with a couple of
     * review topics so mastered material stays fresh.
     */
    public static List<Topic> autoTopics(StudentProfile profile, int maxTopics) {
        List<Topic> plan = new ArrayList<>();
        for (Topic t : frontier(profile)) {
            if (plan.size() >= Math.max(1, maxTopics - REVIEW_SLOTS)) break;
            plan.add(t);
        }
        for (Topic t : reviewCandidates(profile)) {
            if (plan.size() >= maxTopics) break;
            if (!plan.contains(t)) plan.add(t);
        }
        return plan;
    }

    /**
     * How many still-locked topics this topic (directly or transitively) helps unlock.
     * Used to rank which weak prerequisite is blocking the most downstream material.
     */
    public static int downstreamBlocked(Topic topic, StudentProfile profile) {
        Map<Topic, TopicPerformance> perf = profile.getPerformance();
        int blocked = 0;
        for (Topic t : Topic.values()) {
            if (t == topic) continue;
            if (t.isUnlocked(perf) || t.getPrerequisites().isEmpty()) continue;
            if (dependsOn(t, topic)) blocked++;
        }
        return blocked;
    }

    /** True when {@code t} lists {@code prereq} among its prerequisites, transitively. */
    static boolean dependsOn(Topic t, Topic prereq) {
        Deque<Topic> stack = new ArrayDeque<>(t.getPrerequisites());
        Set<Topic> seen = EnumSet.noneOf(Topic.class);
        while (!stack.isEmpty()) {
            Topic p = stack.pop();
            if (p == prereq) return true;
            if (seen.add(p)) stack.addAll(p.getPrerequisites());
        }
        return false;
    }

    public static double mastery(Map<Topic, TopicPerformance> perf, Topic t) {
        TopicPerformance p = perf.get(t);
        return p == null ? 0.0 : p.getMasteryScore();
    }

    private static LocalDateTime lastAttempted(Map<Topic, TopicPerformance> perf, Topic t) {
        TopicPerformance p = perf.get(t);
        return p == null ? null : p.getLastAttempted();
    }
}
