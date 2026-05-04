package com.studyprogram.core;

import com.studyprogram.llm.LLMService;
import com.studyprogram.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects the next question using a spaced-repetition, mastery-weighted algorithm:
 *
 *  1. Filter to the student's selected topics.
 *  2. Prioritise topics with lowest mastery.
 *  3. Within a topic, match difficulty to current mastery level.
 *  4. Deprioritise questions the student answered correctly recently.
 *  5. Never repeat the immediately preceding question.
 *  6. Optionally generate new questions via LLM when a topic bank is thin (< 4 questions).
 */
public class AdaptiveEngine {

    private static final int THIN_BANK_THRESHOLD = 4;

    private final QuestionBank bank;
    private final Random rng;
    private final LLMService llm;

    // LLM-generated questions cached for the lifetime of this engine instance
    private final Map<Topic, List<Question>> generated = new EnumMap<>(Topic.class);

    public AdaptiveEngine(QuestionBank bank) {
        this(bank, new Random(), null);
    }

    public AdaptiveEngine(QuestionBank bank, Random rng) {
        this(bank, rng, null);
    }

    public AdaptiveEngine(QuestionBank bank, LLMService llm) {
        this(bank, new Random(), llm);
    }

    public AdaptiveEngine(QuestionBank bank, Random rng, LLMService llm) {
        this.bank = bank;
        this.rng  = rng;
        this.llm  = llm;
    }

    /**
     * Pick the best next question for this student.
     *
     * @param profile        current student state
     * @param activeTopics   topics selected for this session
     * @param lastQuestionId the ID of the last question asked, to avoid immediate repetition
     * @return the chosen question, or empty if no questions are available
     */
    public Optional<Question> nextQuestion(StudentProfile profile,
                                           Collection<Topic> activeTopics,
                                           String lastQuestionId) {
        maybeGenerateForThinTopics(activeTopics, profile);

        List<Question> candidates = buildCandidateList(profile, activeTopics, lastQuestionId);
        if (candidates.isEmpty()) return Optional.empty();

        List<ScoredQuestion> scored = candidates.stream()
                .map(q -> new ScoredQuestion(q, score(q, profile)))
                .sorted(Comparator.comparingDouble(ScoredQuestion::score).reversed())
                .toList();

        return Optional.of(weightedPick(scored));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<Question> buildCandidateList(StudentProfile profile,
                                              Collection<Topic> activeTopics,
                                              String lastQuestionId) {
        List<Question> candidates = new ArrayList<>();
        for (Topic topic : activeTopics) {
            bank.getQuestionsForTopic(topic).stream()
                    .filter(q -> !q.getId().equals(lastQuestionId))
                    .forEach(candidates::add);
            List<Question> gen = generated.getOrDefault(topic, List.of());
            gen.stream()
                    .filter(q -> !q.getId().equals(lastQuestionId))
                    .forEach(candidates::add);
        }
        return candidates;
    }

    /**
     * Higher score → more likely to be selected.
     *
     * Components:
     *  - Low mastery topics score higher (student needs more practice)
     *  - Difficulty match: questions near the student's current level score higher
     *  - Penalty if the question was recently answered correctly
     */
    private double score(Question q, StudentProfile profile) {
        // Read-only lookup — never create phantom performance records as a scoring side-effect
        TopicPerformance perf = profile.getPerformance().get(q.getTopic());

        double masteryScore     = (perf == null) ? 0.0 : perf.getMasteryScore();
        double topicPriority    = 1.0 - masteryScore;

        int suggestedDifficulty = (perf == null) ? 1 : perf.suggestedDifficulty();
        int diffGap             = Math.abs(q.getDifficulty() - suggestedDifficulty);
        double difficultyMatch  = Math.max(0, 1.0 - (diffGap * 0.25));

        double recentPenalty    = (perf != null && perf.wasRecentlySeen(q.getId())) ? -0.5 : 0.0;

        return topicPriority + difficultyMatch + recentPenalty + rng.nextDouble() * 0.1;
    }

    /** Weighted random selection from the top-N candidates. */
    private Question weightedPick(List<ScoredQuestion> scored) {
        int n = Math.min(10, scored.size());
        List<ScoredQuestion> top = scored.subList(0, n);

        double totalWeight = top.stream().mapToDouble(s -> Math.max(0, s.score())).sum();
        if (totalWeight <= 0) return top.get(0).question();

        double dart = rng.nextDouble() * totalWeight;
        double cumulative = 0;
        for (ScoredQuestion sq : top) {
            cumulative += Math.max(0, sq.score());
            if (dart <= cumulative) return sq.question();
        }
        return top.get(top.size() - 1).question();
    }

    /** For topics with very few static questions, ask the LLM to generate one (cached per session). */
    private void maybeGenerateForThinTopics(Collection<Topic> activeTopics, StudentProfile profile) {
        if (llm == null || !llm.isAvailable()) return;
        for (Topic topic : activeTopics) {
            int staticCount = bank.getQuestionsForTopic(topic).size();
            int genCount    = generated.getOrDefault(topic, List.of()).size();
            if (staticCount + genCount < THIN_BANK_THRESHOLD) {
                TopicPerformance perf = profile.getPerformance().get(topic);
                int difficulty = (perf == null) ? 2 : perf.suggestedDifficulty();
                QuestionType type = (staticCount % 2 == 0)
                        ? QuestionType.MULTIPLE_CHOICE : QuestionType.TRACING;
                llm.generateQuestion(topic, type, difficulty).ifPresent(q ->
                        generated.computeIfAbsent(topic, k -> new ArrayList<>()).add(q));
            }
        }
    }

    private record ScoredQuestion(Question question, double score) {}
}
