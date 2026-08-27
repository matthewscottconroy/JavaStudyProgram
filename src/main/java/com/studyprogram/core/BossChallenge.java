package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;

import java.util.*;

/**
 * World boss challenges: a no-hints quiz drawn from every topic in one level band.
 * Clearing a boss (>= 80% correct) is recorded on the profile and shown on the map.
 *
 * Coding exercises are excluded — a boss fight is a rapid-fire check of the whole
 * world, not a workshop session — and the question mix is seeded per (world, attempt
 * count) so retries see a different set.
 */
public final class BossChallenge {

    public static final int QUESTION_COUNT = 10;
    public static final double PASS_RATIO = 0.8;
    /** Average mastery across the world's topics required before the boss appears. */
    public static final double UNLOCK_AVG_MASTERY = 0.5;

    private BossChallenge() {}

    public static List<Topic> worldTopics(int level) {
        return Arrays.stream(Topic.values()).filter(t -> t.baseLevel == level).toList();
    }

    /** The boss is challengeable once the world's average mastery reaches the bar. */
    public static boolean unlocked(StudentProfile profile, int level) {
        List<Topic> topics = worldTopics(level);
        double sum = 0;
        for (Topic t : topics) sum += Curriculum.mastery(profile.getPerformance(), t);
        return !topics.isEmpty() && sum / topics.size() >= UNLOCK_AVG_MASTERY;
    }

    public static boolean cleared(StudentProfile profile, int level) {
        return profile.getBossesCleared().contains(level);
    }

    /**
     * Picks the boss quiz: non-coding questions across the world's topics, at most
     * two per topic for spread, moderate difficulties first.
     */
    public static List<Question> pickQuestions(QuestionBank bank, int level, Random rng) {
        List<Question> pool = new ArrayList<>(
                bank.getQuestionsForTopics(worldTopics(level)).stream()
                    .filter(q -> q.getType() != QuestionType.CODING)
                    .toList());
        Collections.shuffle(pool, rng);
        pool.sort(Comparator.comparingInt(q -> Math.abs(q.getDifficulty() - 3)));

        List<Question> quiz = new ArrayList<>();
        Map<Topic, Integer> perTopic = new EnumMap<>(Topic.class);
        for (Question q : pool) {
            if (quiz.size() >= QUESTION_COUNT) break;
            if (perTopic.merge(q.getTopic(), 1, Integer::sum) <= 2) quiz.add(q);
        }
        // backfill if the two-per-topic spread came up short
        for (Question q : pool) {
            if (quiz.size() >= QUESTION_COUNT) break;
            if (!quiz.contains(q)) quiz.add(q);
        }
        Collections.shuffle(quiz, rng);
        return quiz;
    }

    public static boolean passed(int correct, int total) {
        return total > 0 && (double) correct / total >= PASS_RATIO;
    }
}
