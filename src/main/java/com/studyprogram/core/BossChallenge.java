package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * World boss challenges: a no-hints test drawn from every topic in one level band.
 * Clearing a boss (>= 80% correct) is recorded on the profile and shown on the map.
 *
 * A fight is a rapid-fire round followed by a coding finale, so passing a world means
 * writing working code and not only recognising it. The mix is seeded per (world,
 * attempt count), so a retry is a different fight.
 */
public final class BossChallenge {

    public static final int QUESTION_COUNT = 10;
    /** How many of the boss's questions are hands-on coding exercises, when the world has them. */
    public static final int CODING_FINALE = 2;
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
     * Picks the boss quiz: a rapid-fire round across the world's topics, then a coding finale.
     *
     * <p>A world's final test should ask the student to write something, not just recognise it,
     * so the last {@link #CODING_FINALE} slots go to coding exercises whenever the world has any.
     * The quick round is spread at most two questions per topic and favours moderate difficulty.
     */
    public static List<Question> pickQuestions(QuestionBank bank, int level, Random rng) {
        List<Question> all = bank.getQuestionsForTopics(worldTopics(level));

        List<Question> coding = new ArrayList<>(all.stream()
                .filter(q -> q.getType() == QuestionType.CODING).toList());
        Collections.shuffle(coding, rng);
        List<Question> finale = coding.stream().limit(CODING_FINALE).collect(Collectors.toList());

        List<Question> pool = new ArrayList<>(all.stream()
                .filter(q -> q.getType() != QuestionType.CODING).toList());
        Collections.shuffle(pool, rng);
        pool.sort(Comparator.comparingInt(q -> Math.abs(q.getDifficulty() - 3)));

        int quickSlots = QUESTION_COUNT - finale.size();
        List<Question> quick = new ArrayList<>();
        Map<Topic, Integer> perTopic = new EnumMap<>(Topic.class);
        for (Question q : pool) {
            if (quick.size() >= quickSlots) break;
            if (perTopic.merge(q.getTopic(), 1, Integer::sum) <= 2) quick.add(q);
        }
        for (Question q : pool) {   // backfill if the per-topic spread came up short
            if (quick.size() >= quickSlots) break;
            if (!quick.contains(q)) quick.add(q);
        }
        Collections.shuffle(quick, rng);

        List<Question> quiz = new ArrayList<>(quick);
        quiz.addAll(finale);        // the coding finale always comes last
        return quiz;
    }

    public static boolean passed(int correct, int total) {
        return total > 0 && (double) correct / total >= PASS_RATIO;
    }
}
