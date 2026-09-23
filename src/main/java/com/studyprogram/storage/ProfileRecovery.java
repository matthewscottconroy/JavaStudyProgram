package com.studyprogram.storage;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Rebuilds a student's profile by replaying their attempt log.
 *
 * <p>This is what the append-only log was always for. Every attempt ever made is in it — topic,
 * difficulty, outcome, timestamp — and mastery is a pure function of that sequence, so a profile
 * lost to a damaged file is not really lost. Replaying the log reconstructs per-topic mastery,
 * attempt counts, accuracy and dates exactly as the original run computed them, because it runs
 * the same {@link TopicPerformance#record} the live program runs.
 *
 * <p>What cannot be recovered is what was never in the log: which topics were selected, bosses
 * cleared, the goal, map reveals. Those are choices rather than history. The rebuilt profile says
 * so rather than inventing them.
 */
public final class ProfileRecovery {

    /** What a rebuild produced, and what it could not. */
    public record Rebuilt(StudentProfile profile, int attemptsReplayed, List<Topic> topics) {
        public boolean isEmpty() { return attemptsReplayed == 0; }
    }

    private ProfileRecovery() {}

    /** True when there is a log worth rebuilding from. */
    public static boolean canRecover(Path profileDir, String name) {
        return !AttemptLog.forProfile(profileDir, name).readAll().isEmpty();
    }

    public static Rebuilt rebuild(Path profileDir, String name) {
        return rebuild(name, AttemptLog.forProfile(profileDir, name).readAll());
    }

    /** Replays attempts in time order into a fresh profile. */
    public static Rebuilt rebuild(String name, List<AttemptRecord> attempts) {
        StudentProfile profile = new StudentProfile(name);
        List<AttemptRecord> ordered = new java.util.ArrayList<>(attempts);
        ordered.sort(Comparator.comparing(AttemptRecord::getTs,
                Comparator.nullsFirst(Comparator.naturalOrder())));

        java.util.LinkedHashSet<Topic> topics = new java.util.LinkedHashSet<>();
        int replayed = 0;
        for (AttemptRecord a : ordered) {
            if (a.getTopic() == null) continue;
            TopicPerformance perf = profile.getOrCreatePerformance(a.getTopic());
            if (a.isSkipped()) {
                perf.recordSkip();
                continue;
            }
            perf.record(a.getQuestionId(), a.isCorrect(), a.getDifficulty());
            topics.add(a.getTopic());
            replayed++;
            profile.setTotalQuestionsAnswered(profile.getTotalQuestionsAnswered() + 1);
            if (a.isCorrect()) profile.setTotalCorrect(profile.getTotalCorrect() + 1);
        }

        if (!ordered.isEmpty()) {
            AttemptRecord first = ordered.get(0);
            AttemptRecord last = ordered.get(ordered.size() - 1);
            if (first.getTs() != null) profile.setCreatedAt(first.getTs());
            if (last.getTs() != null) profile.setLastStudied(last.getTs());
        }
        // Practising a topic is the clearest statement of interest the log contains.
        profile.setSelectedTopics(new java.util.LinkedHashSet<>(topics));

        return new Rebuilt(profile, replayed, List.copyOf(topics));
    }

    /** A sentence for the student about what came back and what did not. */
    public static String summary(Rebuilt rebuilt) {
        if (rebuilt.isEmpty()) {
            return "There are no recorded attempts to rebuild from — this profile starts fresh.";
        }
        return "Replayed " + rebuilt.attemptsReplayed() + " recorded attempt"
             + (rebuilt.attemptsReplayed() == 1 ? "" : "s") + " across " + rebuilt.topics().size()
             + " topic" + (rebuilt.topics().size() == 1 ? "" : "s")
             + ", restoring your mastery, accuracy and history. Bosses cleared, map discoveries "
             + "and any goal were choices rather than answers, so they were never in the log and "
             + "have not come back.";
    }
}
