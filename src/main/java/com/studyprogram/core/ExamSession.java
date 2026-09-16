package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A timed exam: a fixed set of questions spanning several syllabus units, taken in one sitting
 * with no hints, no LLM help and no second attempts, then scored per learning outcome.
 *
 * <p>This is deliberately unlike everything else in the program. The study feed adapts to the
 * student, spaces repetitions and offers hints — all of which make practice effective and make it
 * a poor predictor of how an exam will go. An exam here is the other thing: fixed, mixed across
 * units, and reported as "you can do this, you cannot do that yet" against the course's own units
 * rather than against internal topic names. It answers the question the student is really asking
 * a week before the midterm.
 *
 * <p>It is also not a boss fight. A boss covers one world and gates progress on the map; an exam
 * covers a range of units the student chooses, gates nothing, and is safe to fail.
 */
public final class ExamSession {

    /** The default share of exam questions that require writing real code. */
    public static final double CODING_SHARE = 0.4;
    /** Minutes allowed per question when the exam length is left to the program. */
    public static final int MINUTES_PER_QUESTION = 4;
    /** A section is reported as "solid" at or above this score. */
    public static final double SOLID = 0.8;
    /** Below this, the section is reported as a gap to work on before the real exam. */
    public static final double SHAKY = 0.6;

    /** One reported outcome: a syllabus unit, or a world when no course overlay is in use. */
    public record Section(String title, List<Topic> topics) {}

    /** One question, remembered with the section it is scored under. */
    public record Item(Section section, Question question) {}

    /** How one section came out. {@code answered} excludes questions that ran out of time. */
    public record SectionScore(String title, int correct, int answered, int total) {
        public double ratio() { return total == 0 ? 0 : (double) correct / total; }
        public String verdict() {
            double r = ratio();
            return r >= SOLID ? "solid" : r >= SHAKY ? "shaky" : "needs work";
        }
    }

    private ExamSession() {}

    // ── Building an exam ─────────────────────────────────────────────────────

    /** Sections for units {@code from}..{@code to} of a course overlay, skipping empty units. */
    public static List<Section> sectionsFor(CourseOverlay course, int from, int to) {
        List<Section> sections = new ArrayList<>();
        for (CourseOverlay.Unit u : course.getUnits()) {
            if (u.number() < from || u.number() > to || u.topics().isEmpty()) continue;
            String title = "Unit " + u.number()
                    + (u.title().isBlank() ? "" : " · " + u.title());
            sections.add(new Section(title, u.topics()));
        }
        return sections;
    }

    /**
     * Sections for a student with no course overlay: the map's worlds, which is the closest
     * thing the program has to a syllabus of its own.
     */
    public static List<Section> worldSections(int fromLevel, int toLevel) {
        String[] names = {"Foundations", "Elementary", "Intermediate", "Advanced", "Expert"};
        List<Section> sections = new ArrayList<>();
        for (int level = fromLevel; level <= toLevel; level++) {
            final int band = level;
            List<Topic> topics = Topic.visibleValues().stream()
                    .filter(t -> t.baseLevel == band).toList();
            if (topics.isEmpty()) continue;
            String name = level >= 1 && level <= names.length ? names[level - 1] : "";
            sections.add(new Section("World " + level + " · " + name, topics));
        }
        return sections;
    }

    /**
     * Picks the paper: {@code questionCount} questions spread as evenly as the bank allows across
     * the sections, with roughly {@link #CODING_SHARE} of them real programs to write.
     *
     * <p>Unlike the study feed this ignores the student's history entirely — an exam that quietly
     * avoided a student's weak topics would be worthless as a prediction. Selection is seeded, so
     * a given seed reproduces a given paper and an instructor can hand the whole class the same
     * one.
     */
    public static List<Item> build(QuestionBank bank, List<Section> sections,
                                   int questionCount, Random rng) {
        List<Item> paper = new ArrayList<>();
        if (sections.isEmpty() || questionCount <= 0) return paper;

        int perSection = Math.max(1, questionCount / sections.size());
        int codingTarget = (int) Math.round(perSection * CODING_SHARE);
        Set<String> used = new LinkedHashSet<>();

        for (Section section : sections) {
            paper.addAll(pick(bank, section, perSection, codingTarget, used, rng));
        }
        // Remainder from integer division goes to the sections that still have unused questions,
        // so a 12-question exam over 5 units is 12 questions and not 10.
        for (Section section : sections) {
            if (paper.size() >= questionCount) break;
            paper.addAll(pick(bank, section, questionCount - paper.size(), 0, used, rng));
        }
        if (paper.size() > questionCount) paper = new ArrayList<>(paper.subList(0, questionCount));
        return paper;
    }

    private static List<Item> pick(QuestionBank bank, Section section, int want, int codingWant,
                                   Set<String> used, Random rng) {
        List<Question> pool = new ArrayList<>(bank.getQuestionsForTopics(section.topics()));
        pool.removeIf(q -> used.contains(q.getId()));
        Collections.shuffle(pool, rng);
        // Exams sit in the middle of the difficulty range: a paper of only easy questions cannot
        // distinguish students, and one of only hard questions cannot either.
        pool.sort(Comparator.comparingInt(q -> Math.abs(q.getDifficulty() - 3)));

        List<Item> chosen = new ArrayList<>();
        for (Question q : pool) {
            if (chosen.size() >= codingWant) break;
            if (q.getType() == QuestionType.CODING) {
                chosen.add(new Item(section, q));
                used.add(q.getId());
            }
        }
        for (Question q : pool) {
            if (chosen.size() >= want) break;
            if (used.contains(q.getId())) continue;
            chosen.add(new Item(section, q));
            used.add(q.getId());
        }
        return chosen;
    }

    /** Default time limit for a paper, in minutes. Coding questions are worth three normal ones. */
    public static int suggestedMinutes(List<Item> paper) {
        int weight = 0;
        for (Item item : paper) {
            weight += item.question().getType() == QuestionType.CODING ? 3 : 1;
        }
        return Math.max(5, (int) Math.round(weight * MINUTES_PER_QUESTION / 2.0));
    }

    // ── Scoring ──────────────────────────────────────────────────────────────

    /**
     * Scores a finished paper. {@code correctIds} are the questions answered correctly and
     * {@code attemptedIds} those reached at all — questions left when the clock ran out count
     * against the score but are reported separately, because "ran out of time" and "got it wrong"
     * are different problems with different fixes.
     */
    public static List<SectionScore> score(List<Item> paper, Set<String> correctIds,
                                           Set<String> attemptedIds) {
        List<SectionScore> scores = new ArrayList<>();
        List<String> order = new ArrayList<>();
        for (Item item : paper) {
            if (!order.contains(item.section().title())) order.add(item.section().title());
        }
        for (String title : order) {
            int correct = 0, answered = 0, total = 0;
            for (Item item : paper) {
                if (!item.section().title().equals(title)) continue;
                total++;
                if (attemptedIds.contains(item.question().getId())) answered++;
                if (correctIds.contains(item.question().getId())) correct++;
            }
            scores.add(new SectionScore(title, correct, answered, total));
        }
        return scores;
    }

    /** Overall score as a fraction of the whole paper. */
    public static double overall(List<SectionScore> scores) {
        int correct = 0, total = 0;
        for (SectionScore s : scores) { correct += s.correct(); total += s.total(); }
        return total == 0 ? 0 : (double) correct / total;
    }

    /** Section titles the student should work on before the real thing, weakest first. */
    public static List<String> weakestSections(List<SectionScore> scores) {
        List<SectionScore> sorted = new ArrayList<>(scores);
        sorted.sort(Comparator.comparingDouble(SectionScore::ratio));
        List<String> weak = new ArrayList<>();
        for (SectionScore s : sorted) {
            if (s.ratio() < SOLID) weak.add(s.title());
        }
        return weak;
    }
}
