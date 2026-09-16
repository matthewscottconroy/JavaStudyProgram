package com.studyprogram.core;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The questions this student has got wrong and not yet got right, plus the exercises where the
 * compile errors they keep hitting actually bit them.
 *
 * <p>The program already knew all of this and did nothing with it. A student's report could say
 * "you hit <i>cannot find symbol</i> fourteen times" and offer no way to practise it, and the
 * end-of-session review only ever covered the session just finished — a question missed on Tuesday
 * came back only if spaced repetition happened to surface it. This turns the record of what went
 * wrong into the thing the student practises next.
 *
 * <p>A mistake is "unresolved" when the most recent <em>answered</em> attempt at that question was
 * wrong. Getting it right later resolves it; skipping is not an answer either way.
 */
public final class MistakeDeck {

    /** How many times an error has to appear before it counts as a habit rather than a slip. */
    public static final int RECURRING_THRESHOLD = 2;
    /** At most this many error categories are called out — a list of ten is not a plan. */
    public static final int MAX_ERRORS_REPORTED = 3;

    /**
     * @param questions        what to practise, worst first
     * @param unresolvedCount  how many of those are questions previously answered wrongly
     * @param recurringErrors  compile-error categories hit {@value #RECURRING_THRESHOLD}+ times
     */
    public record Deck(List<Question> questions, int unresolvedCount, List<String> recurringErrors) {
        public boolean isEmpty() { return questions.isEmpty(); }
    }

    private MistakeDeck() {}

    /** Builds the deck, capped at {@code limit} questions. */
    public static Deck build(QuestionBank bank, List<AttemptRecord> attempts, int limit) {
        List<AttemptRecord> ordered = new ArrayList<>(attempts);
        ordered.sort(Comparator.comparing(AttemptRecord::getTs,
                Comparator.nullsFirst(Comparator.naturalOrder())));

        // Latest answered outcome per question, and when it happened.
        Map<String, AttemptRecord> latestAnswered = new LinkedHashMap<>();
        Map<String, Integer> errorCounts = new LinkedHashMap<>();
        Map<String, Set<String>> questionsByError = new LinkedHashMap<>();

        for (AttemptRecord a : ordered) {
            if (a.isAnswered()) latestAnswered.put(a.getQuestionId(), a);
            for (String kind : a.getCompileErrors()) {
                errorCounts.merge(kind, 1, Integer::sum);
                questionsByError.computeIfAbsent(kind, k -> new LinkedHashSet<>())
                                .add(a.getQuestionId());
            }
        }

        List<String> recurring = new ArrayList<>();
        errorCounts.entrySet().stream()
                .filter(e -> e.getValue() >= RECURRING_THRESHOLD)
                .sorted((x, y) -> y.getValue() - x.getValue())
                .limit(MAX_ERRORS_REPORTED)
                .forEach(e -> recurring.add(e.getKey()));

        // Unresolved mistakes, oldest first: the longer a wrong answer has stood, the more likely
        // it has been forgotten rather than quietly learned.
        List<AttemptRecord> unresolved = latestAnswered.values().stream()
                .filter(a -> !a.isCorrect())
                .sorted(Comparator.comparing(AttemptRecord::getTs,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

        List<Question> deck = new ArrayList<>();
        Set<String> included = new LinkedHashSet<>();
        for (AttemptRecord a : unresolved) {
            if (deck.size() >= limit) break;
            addIfPresent(bank, a.getQuestionId(), deck, included);
        }
        int unresolvedCount = deck.size();

        // Then the exercises where the recurring errors actually happened. Nothing here guesses
        // which exercise "provokes" an error in general — these are the ones that provoked it for
        // this student, which is the only claim the data supports.
        for (String kind : recurring) {
            for (String questionId : questionsByError.getOrDefault(kind, Set.of())) {
                if (deck.size() >= limit) break;
                addIfPresent(bank, questionId, deck, included);
            }
        }

        return new Deck(deck, unresolvedCount, recurring);
    }

    private static void addIfPresent(QuestionBank bank, String id,
                                     List<Question> deck, Set<String> included) {
        if (!included.add(id)) return;
        Optional<Question> q = bank.findById(id);
        // A question can vanish between sessions — an external pack removed, a topic hidden.
        // Silently dropping it is right: the student cannot practise what is no longer there.
        q.ifPresent(deck::add);
    }
}
