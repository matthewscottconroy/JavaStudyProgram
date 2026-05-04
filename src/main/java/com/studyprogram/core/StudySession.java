package com.studyprogram.core;

import com.studyprogram.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/** Manages one study session: tracks questions asked, answers, skips, and timing. */
public class StudySession {

    private final StudentProfile profile;
    private final List<Topic> activeTopics;
    private final AdaptiveEngine engine;
    private final int targetQuestions;      // 0 = unlimited

    private final List<SessionRecord> history     = new ArrayList<>();
    private final List<Question>      wrongAnswers = new ArrayList<>();
    private String  lastQuestionId = null;
    private int     skipped        = 0;
    private final LocalDateTime startTime = LocalDateTime.now();
    private boolean finished = false;

    public StudySession(StudentProfile profile,
                        List<Topic> activeTopics,
                        AdaptiveEngine engine,
                        int targetQuestions) {
        this.profile         = profile;
        this.activeTopics    = Collections.unmodifiableList(new ArrayList<>(activeTopics));
        this.engine          = engine;
        this.targetQuestions = targetQuestions;
    }

    /** Returns the next question, or empty if the session is complete. */
    public Optional<Question> nextQuestion() {
        if (finished) return Optional.empty();
        if (targetQuestions > 0 && history.size() >= targetQuestions) {
            finished = true;
            return Optional.empty();
        }
        Optional<Question> next = engine.nextQuestion(profile, activeTopics, lastQuestionId);
        if (next.isEmpty()) finished = true;
        return next;
    }

    /** Record the result of answering a question. Updates both session history and profile. */
    public GradingResult recordAnswer(Question question, GradingResult result) {
        history.add(new SessionRecord(question, result.correct()));
        lastQuestionId = question.getId();
        profile.recordAnswer(question, result.correct());
        if (!result.correct()) wrongAnswers.add(question);
        return result;
    }

    /** Record that the student skipped this question (not counted in history). */
    public void skip() { skipped++; }

    // ── Summary ───────────────────────────────────────────────────────────────

    public int questionsAnswered()  { return history.size(); }
    public int correctAnswers()     { return (int) history.stream().filter(SessionRecord::correct).count(); }
    public int skippedCount()       { return skipped; }
    public boolean isFinished()     { return finished; }
    public List<Topic> getActiveTopics() { return activeTopics; }

    /** Questions the student answered incorrectly this session. */
    public List<Question> getWrongAnswers() { return Collections.unmodifiableList(wrongAnswers); }

    public Duration elapsed() {
        return Duration.between(startTime, LocalDateTime.now());
    }

    public double sessionAccuracy() {
        if (history.isEmpty()) return 0.0;
        return (double) correctAnswers() / history.size();
    }

    public Map<Topic, long[]> topicBreakdown() {
        Map<Topic, long[]> result = new LinkedHashMap<>();
        for (SessionRecord r : history) {
            Topic t = r.question().getTopic();
            result.computeIfAbsent(t, k -> new long[]{0, 0});
            result.get(t)[0]++;
            if (r.correct()) result.get(t)[1]++;
        }
        return result;
    }

    // ── Inner types ────────────────────────────────────────────────────────────

    private record SessionRecord(Question question, boolean correct) {}
}
