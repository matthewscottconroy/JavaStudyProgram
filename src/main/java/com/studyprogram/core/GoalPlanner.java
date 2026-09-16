package com.studyprogram.core;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.StudyGoal;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Back-plans from a {@link StudyGoal}: how far the student is from it, how many questions that
 * is likely to take, and therefore how many a day between now and the date.
 *
 * <p>The estimate is deliberately simple and deliberately the student's own. Each correct answer
 * raises a topic's mastery by a known amount and each wrong one lowers it (see
 * {@link TopicPerformance#record}), so at the student's current accuracy the expected gain per
 * question is a fixed number, and the gap to the target divided by that gain is a number of
 * questions. It is a forecast, not a promise — but it is honest about the one thing a student
 * cannot judge for themselves a fortnight out, which is whether the pace they are on will get
 * them there.
 */
public final class GoalPlanner {

    /** Mastery gain for a correct answer at middling difficulty, from the mastery model. */
    static final double GAIN_IF_CORRECT = 0.11;
    /** Mastery lost on a wrong answer at middling difficulty. */
    static final double LOSS_IF_WRONG = 0.06;
    /** Assumed accuracy until the student has answered enough to have one of their own. */
    static final double DEFAULT_ACCURACY = 0.7;
    static final int MIN_ANSWERS_FOR_OWN_ACCURACY = 10;
    /** Below this, no amount of answering moves the needle usefully; the estimate is floored. */
    static final double MIN_GAIN = 0.03;
    /** A pace beyond this is reported as off track — it is more than a daily session. */
    public static final int SUSTAINABLE_PER_DAY = 12;

    /**
     * @param behind          goal topics still under target, weakest first
     * @param onTarget        goal topics already at target
     * @param questionsNeeded estimated questions to bring every topic to target
     * @param questionsPerDay that spread over the days left (at least one day)
     * @param expectedGain    mastery gained per question at the student's accuracy
     * @param onTrack         whether the required pace is within a daily session
     */
    public record Plan(StudyGoal goal, long daysLeft, List<Topic> behind, List<Topic> onTarget,
                       int questionsNeeded, int questionsPerDay, double expectedGain,
                       boolean onTrack) {
        public boolean isDone() { return behind.isEmpty(); }
    }

    private GoalPlanner() {}

    public static Plan plan(StudentProfile profile, LocalDate today) {
        StudyGoal goal = profile.getGoal();
        if (goal == null) return null;

        long daysLeft = Math.max(0, ChronoUnit.DAYS.between(today, goal.getDate()));
        Map<Topic, TopicPerformance> perf = profile.getPerformance();
        double gain = expectedGain(profile);

        List<Topic> behind = new ArrayList<>();
        List<Topic> onTarget = new ArrayList<>();
        int needed = 0;
        for (Topic t : goal.getTopics()) {
            double mastery = Curriculum.mastery(perf, t);
            if (mastery >= goal.getTargetMastery()) {
                onTarget.add(t);
            } else {
                behind.add(t);
                needed += (int) Math.ceil((goal.getTargetMastery() - mastery) / gain);
            }
        }
        behind.sort(Comparator.comparingDouble(t -> Curriculum.mastery(perf, t)));

        int perDay = (int) Math.ceil(needed / (double) Math.max(1, daysLeft));
        boolean onTrack = behind.isEmpty()
                || (daysLeft > 0 && perDay <= SUSTAINABLE_PER_DAY);
        return new Plan(goal, daysLeft, behind, onTarget, needed, perDay, gain, onTrack);
    }

    /** Expected mastery gain per question at this student's own accuracy. */
    static double expectedGain(StudentProfile profile) {
        double accuracy = profile.getTotalQuestionsAnswered() >= MIN_ANSWERS_FOR_OWN_ACCURACY
                ? profile.getOverallAccuracy() : DEFAULT_ACCURACY;
        double gain = accuracy * GAIN_IF_CORRECT - (1 - accuracy) * LOSS_IF_WRONG;
        return Math.max(MIN_GAIN, gain);
    }

    /**
     * The topic mix for an auto-feed session while a goal is active: the goal's own topics that
     * are behind, weakest first, but only the ones the student is ready for — a locked goal topic
     * contributes its unmastered prerequisites instead, because that is the actual path to it.
     * Falls back to the ordinary curriculum mix when the goal is already met.
     */
    public static List<Topic> focusTopics(StudentProfile profile, int max) {
        Plan plan = plan(profile, LocalDate.now());
        if (plan == null || plan.isDone()) return Curriculum.autoTopics(profile, max);

        Map<Topic, TopicPerformance> perf = profile.getPerformance();
        Set<Topic> focus = new LinkedHashSet<>();
        Set<Topic> prerequisitesFirst = new LinkedHashSet<>();
        for (Topic t : plan.behind()) {
            if (t.isUnlocked(perf) || t.getPrerequisites().isEmpty()) {
                focus.add(t);
            } else {
                for (Topic p : t.getPrerequisites()) {
                    if (Curriculum.mastery(perf, p) < Curriculum.MASTERY_TARGET) prerequisitesFirst.add(p);
                }
            }
        }
        List<Topic> result = new ArrayList<>();
        for (Topic t : prerequisitesFirst) if (result.size() < max) result.add(t);
        for (Topic t : focus) if (result.size() < max && !result.contains(t)) result.add(t);
        for (Topic t : Curriculum.autoTopics(profile, max)) {
            if (result.size() >= max) break;
            if (!result.contains(t)) result.add(t);
        }
        return result;
    }

    /** One line for the main menu. */
    public static String summary(Plan plan) {
        String when = plan.daysLeft() == 0 ? "today"
                : plan.daysLeft() == 1 ? "tomorrow"
                : "in " + plan.daysLeft() + " days";
        int total = plan.behind().size() + plan.onTarget().size();
        if (plan.isDone()) {
            return plan.goal().getTitle() + " " + when + " — all " + total
                    + " topics at target. Keep them warm.";
        }
        String pace = plan.daysLeft() == 0
                ? plan.questionsNeeded() + " questions still to go"
                : "~" + plan.questionsPerDay() + " question" + (plan.questionsPerDay() == 1 ? "" : "s")
                  + " a day to get there";
        return plan.goal().getTitle() + " " + when + " — " + plan.onTarget().size() + " of "
                + total + " topics at target; " + pace
                + (plan.onTrack() ? "." : " — that is a lot. Start today.");
    }
}
