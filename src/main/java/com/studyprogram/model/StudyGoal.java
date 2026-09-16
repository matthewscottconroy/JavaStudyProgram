package com.studyprogram.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Something the student is working toward: an exam on a date, covering a set of topics, at a
 * target mastery. Stored on the profile so the program can back-plan from it — how many
 * questions a day, which topics first — and say whether the student is on track.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyGoal {

    public static final double DEFAULT_TARGET = 0.8;

    private String title;              // "Midterm"
    private String scope;              // "Units 1-4 of Java II" — for display only
    private LocalDate date;
    private List<Topic> topics = new ArrayList<>();
    private double targetMastery = DEFAULT_TARGET;
    private LocalDate setOn;           // when the goal was created, for pacing

    public StudyGoal() {}

    public StudyGoal(String title, String scope, LocalDate date, List<Topic> topics) {
        this.title = title;
        this.scope = scope;
        this.date = date;
        this.topics = new ArrayList<>(topics);
        this.setOn = LocalDate.now();
    }

    @JsonIgnore
    public boolean isPast(LocalDate today) {
        return date != null && date.isBefore(today);
    }

    public String getTitle()                     { return title; }
    public void setTitle(String t)               { this.title = t; }
    public String getScope()                     { return scope; }
    public void setScope(String s)               { this.scope = s; }
    public LocalDate getDate()                   { return date; }
    public void setDate(LocalDate d)             { this.date = d; }
    public List<Topic> getTopics()               { return topics; }
    public void setTopics(List<Topic> t)         { this.topics = t == null ? new ArrayList<>() : new ArrayList<>(t); }
    public double getTargetMastery()             { return targetMastery; }
    public void setTargetMastery(double m)       { this.targetMastery = m; }
    public LocalDate getSetOn()                  { return setOn; }
    public void setSetOn(LocalDate d)            { this.setOn = d; }
}
