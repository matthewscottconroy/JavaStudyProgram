package com.studyprogram.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One row of the append-only attempt log: a single question attempt (or skip).
 * Everything time-based in the app — progress charts, streaks, accuracy trends —
 * derives from these records, so they are never rewritten, only appended.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttemptRecord {

    public static final String OUTCOME_CORRECT   = "correct";
    public static final String OUTCOME_INCORRECT = "incorrect";
    public static final String OUTCOME_SKIPPED   = "skipped";

    private LocalDateTime ts;
    private String questionId;
    private Topic topic;
    private int difficulty;
    private QuestionType type;
    private String outcome;      // correct | incorrect | skipped
    private long seconds;        // time from question shown to answered
    private int hintsUsed;
    /** Compile-error categories hit while working this question (coding exercises only). */
    private List<String> compileErrors = new ArrayList<>();

    public AttemptRecord() {}

    public AttemptRecord(LocalDateTime ts, Question q, String outcome, long seconds, int hintsUsed) {
        this.ts         = ts;
        this.questionId = q.getId();
        this.topic      = q.getTopic();
        this.difficulty = q.getDifficulty();
        this.type       = q.getType();
        this.outcome    = outcome;
        this.seconds    = seconds;
        this.hintsUsed  = hintsUsed;
    }

    public AttemptRecord(LocalDateTime ts, Question q, String outcome, long seconds,
                         int hintsUsed, List<String> compileErrors) {
        this(ts, q, outcome, seconds, hintsUsed);
        setCompileErrors(compileErrors);
    }

    @JsonIgnore public boolean isCorrect()  { return OUTCOME_CORRECT.equals(outcome); }
    @JsonIgnore public boolean isSkipped()  { return OUTCOME_SKIPPED.equals(outcome); }
    @JsonIgnore public boolean isAnswered() { return !isSkipped(); }

    // ── Accessors ────────────────────────────────────────────────────────────

    public LocalDateTime getTs()             { return ts; }
    public void setTs(LocalDateTime t)       { this.ts = t; }
    public String getQuestionId()            { return questionId; }
    public void setQuestionId(String id)     { this.questionId = id; }
    public Topic getTopic()                  { return topic; }
    public void setTopic(Topic t)            { this.topic = t; }
    public int getDifficulty()               { return difficulty; }
    public void setDifficulty(int d)         { this.difficulty = d; }
    public QuestionType getType()            { return type; }
    public void setType(QuestionType t)      { this.type = t; }
    public String getOutcome()               { return outcome; }
    public void setOutcome(String o)         { this.outcome = o; }
    public long getSeconds()                 { return seconds; }
    public void setSeconds(long s)           { this.seconds = s; }
    public int getHintsUsed()                { return hintsUsed; }
    public void setHintsUsed(int h)          { this.hintsUsed = h; }
    public List<String> getCompileErrors()   { return compileErrors; }
    public void setCompileErrors(List<String> e) {
        this.compileErrors = e == null ? new ArrayList<>() : new ArrayList<>(e);
    }
}
