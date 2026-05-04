package com.studyprogram.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Immutable description of a single study question. Use {@link Builder} to construct. */
public final class Question {

    private final String id;
    private final Topic topic;
    private final QuestionType type;
    private final int difficulty;       // 1–5
    private final String prompt;        // the question text shown to the student
    private final String code;          // optional code block (null if not applicable)
    private final List<String> choices; // A/B/C/D options for MC questions; empty otherwise
    private final String answer;        // canonical correct answer (normalised to lowercase trim)
    private final List<String> alternativeAnswers; // other accepted phrasings
    private final String explanation;   // shown after the student answers
    private final List<String> hints;   // progressive hints, requested on demand

    private Question(Builder b) {
        this.id                 = b.id;
        this.topic              = b.topic;
        this.type               = b.type;
        this.difficulty         = b.difficulty;
        this.prompt             = b.prompt;
        this.code               = b.code;
        this.choices            = Collections.unmodifiableList(new ArrayList<>(b.choices));
        this.answer             = b.answer;
        this.alternativeAnswers = Collections.unmodifiableList(new ArrayList<>(b.alternatives));
        this.explanation        = b.explanation;
        this.hints              = Collections.unmodifiableList(new ArrayList<>(b.hints));
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getId()                     { return id; }
    public Topic getTopic()                   { return topic; }
    public QuestionType getType()             { return type; }
    public int getDifficulty()                { return difficulty; }
    public String getPrompt()                 { return prompt; }
    public String getCode()                   { return code; }
    public boolean hasCode()                  { return code != null && !code.isBlank(); }
    public List<String> getChoices()          { return choices; }
    public boolean isMultipleChoice()         { return !choices.isEmpty(); }
    public String getAnswer()                 { return answer; }
    public List<String> getAlternativeAnswers() { return alternativeAnswers; }
    public String getExplanation()            { return explanation; }
    public List<String> getHints()            { return hints; }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id = UUID.randomUUID().toString();
        private Topic topic;
        private QuestionType type = QuestionType.MULTIPLE_CHOICE;
        private int difficulty = 1;
        private String prompt;
        private String code;
        private final List<String> choices   = new ArrayList<>();
        private String answer;
        private final List<String> alternatives = new ArrayList<>();
        private String explanation = "";
        private final List<String> hints    = new ArrayList<>();

        public Builder id(String id)                  { this.id = id; return this; }
        public Builder topic(Topic t)                 { this.topic = t; return this; }
        public Builder type(QuestionType t)           { this.type = t; return this; }
        public Builder difficulty(int d)              { this.difficulty = d; return this; }
        public Builder prompt(String p)               { this.prompt = p; return this; }
        public Builder code(String c)                 { this.code = c; return this; }
        public Builder choice(String c)               { this.choices.add(c); return this; }
        public Builder choices(String a, String b, String c, String d) {
            this.choices.clear();
            this.choices.add(a); this.choices.add(b);
            this.choices.add(c); this.choices.add(d);
            return this;
        }
        public Builder answer(String a)               { this.answer = a.trim(); return this; }
        public Builder alternative(String a)          { this.alternatives.add(a.trim()); return this; }
        public Builder explanation(String e)          { this.explanation = e; return this; }
        public Builder hint(String h)                 { this.hints.add(h); return this; }

        public Question build() {
            if (topic == null)  throw new IllegalStateException("topic required");
            if (prompt == null) throw new IllegalStateException("prompt required");
            if (answer == null) throw new IllegalStateException("answer required");
            return new Question(this);
        }
    }
}
