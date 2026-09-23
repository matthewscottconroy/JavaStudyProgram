package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * A short adaptive check that finds out what a student already knows, so the program can start
 * them where they actually are.
 *
 * <p>Without this, everyone begins at zero with almost everything locked. That is right for a
 * genuine beginner and insulting to a student halfway through Java II, who would have to answer
 * their way through variables and printing to reach the material they came for. Manually
 * selecting topics does not help either, because mastery still reads 0% and the map stays shut.
 *
 * <p>The check exploits the prerequisite graph rather than fighting it: <b>a correct answer on an
 * advanced topic is evidence about everything underneath it.</b> Someone who can trace a stream
 * pipeline can use a loop. So a dozen questions aimed at well-chosen topics can speak to all 61,
 * and the walk is adaptive — do well and it climbs, struggle and it drops back — so the questions
 * land near the edge of what the student knows instead of wasting a third of them at the bottom.
 *
 * <p>Two deliberate limits keep it honest. A wrong answer propagates <b>nothing</b> downward: a
 * missed arrays question could mean weak arrays or weak loops, and guessing which would be worse
 * than not guessing. And a passed topic is seeded only to {@link #PLACEMENT_MASTERY} — over the
 * unlock line, well under the mastery target — because answering one question is evidence that a
 * topic should be open, never that it has been learned. Placement unlocks; it does not certify.
 */
public final class PlacementCheck {

    /** Questions asked, at most. Long enough to be informative, short enough to actually finish. */
    public static final int LENGTH = 12;
    /** Topics probed at each level before deciding whether to climb. */
    public static final int PROBES_PER_LEVEL = 3;
    /** Correct answers out of {@link #PROBES_PER_LEVEL} needed to move up a level. */
    public static final int PASS_MARK = 2;
    /** Seeded mastery for a topic the student demonstrated. Above the unlock line, below mastery. */
    public static final double PLACEMENT_MASTERY = 0.5;
    /** Where the walk starts: low enough not to demoralise, high enough not to waste questions. */
    public static final int START_LEVEL = 2;
    public static final int MAX_LEVEL = 5;

    /** One answered probe. */
    public record Result(Question question, boolean correct) {}

    /** What the check concluded. */
    public record Outcome(Set<Topic> unlocked, int highestLevelPassed, int asked, int correct) {
        public boolean placedAtBeginning() { return unlocked.isEmpty(); }
    }

    private PlacementCheck() {}

    /**
     * The probe topics for one level: the ones that best stand for it.
     *
     * <p>Ranked by how many other topics depend on them, because a topic the rest of the
     * curriculum is built on — loops, arrays, classes — is what "knowing this level" actually
     * means. Ranking by prerequisite depth instead looks reasonable and is not: the deepest
     * topics at a level tend to be peripheral ones like Javadoc or Maven, which sit on top of
     * everything and carry nothing, so a student fluent in Java but hazy on build tools would be
     * placed at the beginning. Prerequisite depth is kept only as a tie-break, where it does
     * measure how much a correct answer certifies.
     */
    static List<Topic> probeTopics(QuestionBank bank, int level) {
        List<Topic> candidates = new ArrayList<>();
        for (Topic t : Topic.visibleValues()) {
            if (t.baseLevel != level) continue;
            if (probeQuestions(bank, t).isEmpty()) continue;
            candidates.add(t);
        }
        candidates.sort(Comparator
                .comparingInt((Topic t) -> -dependentCount(t))
                .thenComparingInt(t -> -transitivePrerequisites(t).size())
                .thenComparing(Topic::name));
        return candidates.size() > PROBES_PER_LEVEL
                ? candidates.subList(0, PROBES_PER_LEVEL) : candidates;
    }

    /** How many visible topics depend on this one, directly or transitively. */
    static int dependentCount(Topic topic) {
        int count = 0;
        for (Topic other : Topic.visibleValues()) {
            if (other != topic && transitivePrerequisites(other).contains(topic)) count++;
        }
        return count;
    }

    /**
     * Questions usable as probes: quick to answer and gradeable without a compiler, so the check
     * is five minutes rather than an afternoon. Middling difficulty — an easy question tells you
     * nothing about a strong student and a hard one tells you nothing about a weak one.
     */
    static List<Question> probeQuestions(QuestionBank bank, Topic topic) {
        List<Question> usable = new ArrayList<>();
        for (Question q : bank.getQuestionsForTopic(topic)) {
            if (q.getType() == QuestionType.CODING || q.getType() == QuestionType.PARSONS
                    || q.getType() == QuestionType.FADED) continue;
            if (!q.isMultipleChoice()) continue;   // one keystroke, unambiguous grading
            usable.add(q);
        }
        usable.sort(Comparator.comparingInt(q -> Math.abs(q.getDifficulty() - 3)));
        return usable;
    }

    /**
     * Plans the whole check up front as a level-ordered list of probes. The caller asks them in
     * order and reports back; {@link #nextLevel} decides where the walk goes next.
     */
    public static List<Question> probesForLevel(QuestionBank bank, int level, Random rng) {
        List<Question> probes = new ArrayList<>();
        for (Topic t : probeTopics(bank, level)) {
            List<Question> pool = probeQuestions(bank, t);
            if (pool.isEmpty()) continue;
            // From the most suitable few, pick one at random so a retake is a different check.
            List<Question> best = pool.subList(0, Math.min(5, pool.size()));
            probes.add(best.get(rng.nextInt(best.size())));
        }
        Collections.shuffle(probes, rng);
        return probes;
    }

    /**
     * Where to go after a level: up when the student cleared the pass mark, down otherwise.
     * Returns 0 when the walk is finished (either end of the ladder reached, or a level already
     * visited — the walk never oscillates).
     */
    public static int nextLevel(int level, int correctAtLevel, Set<Integer> visited) {
        int next = correctAtLevel >= PASS_MARK ? level + 1 : level - 1;
        if (next < 1 || next > MAX_LEVEL || visited.contains(next)) return 0;
        return next;
    }

    /**
     * Turns answered probes into seeded mastery.
     *
     * <p>Every topic answered correctly is opened, along with everything it depends on,
     * transitively. Nothing is closed and nothing is inferred from a wrong answer.
     */
    public static Outcome apply(StudentProfile profile, List<Result> results) {
        Set<Topic> unlocked = new LinkedHashSet<>();
        int correct = 0;
        for (Result r : results) {
            if (!r.correct()) continue;
            correct++;
            unlocked.add(r.question().getTopic());
            unlocked.addAll(transitivePrerequisites(r.question().getTopic()));
        }

        int highest = 0;
        for (Topic t : unlocked) {
            TopicPerformance perf = profile.getOrCreatePerformance(t);
            // Never lower what a student has already earned by practising.
            if (perf.getMasteryScore() < PLACEMENT_MASTERY) {
                perf.setMasteryScore(PLACEMENT_MASTERY);
                perf.setMasteryUpdatedAt(java.time.LocalDateTime.now());
                // Flagged so reports never present an estimate as though it were practice.
                if (perf.getAttempts() == 0) perf.setPlacementSeeded(true);
            }
            highest = Math.max(highest, t.baseLevel);
        }
        return new Outcome(unlocked, highest, results.size(), correct);
    }

    /** A topic's prerequisites, and theirs, all the way down. */
    static Set<Topic> transitivePrerequisites(Topic topic) {
        Set<Topic> found = EnumSet.noneOf(Topic.class);
        collect(topic, found);
        found.remove(topic);
        return found;
    }

    private static void collect(Topic topic, Set<Topic> found) {
        for (Topic p : topic.getPrerequisites()) {
            if (found.add(p)) collect(p, found);
        }
    }

    /** A sentence saying where the student was placed and what it does and does not mean. */
    public static String summary(Outcome outcome) {
        if (outcome.placedAtBeginning()) {
            return "Starting from the beginning — nothing to skip, which is exactly where a "
                 + "first course starts.";
        }
        Map<Integer, Integer> byLevel = new LinkedHashMap<>();
        for (Topic t : outcome.unlocked()) byLevel.merge(t.baseLevel, 1, Integer::sum);
        StringBuilder spread = new StringBuilder();
        for (var e : byLevel.entrySet()) {
            if (spread.length() > 0) spread.append(", ");
            spread.append(e.getValue()).append(" in world ").append(e.getKey());
        }
        return outcome.correct() + "/" + outcome.asked() + " correct — opening "
             + outcome.unlocked().size() + " topics (" + spread + "). "
             + "They are opened, not ticked off: the feed will still practise them, and their "
             + "mastery only moves when you answer their questions.";
    }
}
