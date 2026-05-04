package com.studyprogram.model;

/** The pedagogical style of a question. */
public enum QuestionType {
    /** Spot and identify a bug in provided code. */
    DEBUGGING("Debugging"),
    /** Trace through code and predict the output or final value. */
    TRACING("Tracing"),
    /** Add to or modify existing code to meet a new requirement. */
    EXTENSION("Extension"),
    /** Write code from scratch given a specification (shown as multiple choice). */
    CODE_GENERATION("Code Generation"),
    /** Standard A/B/C/D multiple-choice question. */
    MULTIPLE_CHOICE("Multiple Choice");

    public final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() { return displayName; }
}
