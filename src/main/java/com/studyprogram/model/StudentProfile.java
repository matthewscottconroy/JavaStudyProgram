package com.studyprogram.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.*;

/** A student's persistent profile: selected topics and per-topic performance. */
public class StudentProfile {

    private String id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime lastStudied;
    private int totalQuestionsAnswered;
    private int totalCorrect;
    private Set<Topic> selectedTopics;
    private Map<Topic, TopicPerformance> performance;

    public StudentProfile() {
        this.id              = UUID.randomUUID().toString();
        this.createdAt       = LocalDateTime.now();
        this.selectedTopics  = new LinkedHashSet<>();
        this.performance     = new EnumMap<>(Topic.class);
    }

    public StudentProfile(String name) {
        this();
        this.name = name;
    }

    /** Get or create per-topic performance record. */
    public TopicPerformance getOrCreatePerformance(Topic topic) {
        return performance.computeIfAbsent(topic, TopicPerformance::new);
    }

    /** Record the result of answering one question. */
    public void recordAnswer(Question question, boolean correct) {
        totalQuestionsAnswered++;
        if (correct) totalCorrect++;
        lastStudied = LocalDateTime.now();
        getOrCreatePerformance(question.getTopic()).record(question.getId(), correct);
    }

    @JsonIgnore
    public double getOverallAccuracy() {
        return totalQuestionsAnswered == 0 ? 0.0
                : (double) totalCorrect / totalQuestionsAnswered;
    }

    @JsonIgnore
    public List<Topic> getSelectedTopicsList() {
        return new ArrayList<>(selectedTopics);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getId()                    { return id; }
    public void setId(String id)             { this.id = id; }
    public String getName()                  { return name; }
    public void setName(String n)            { this.name = n; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public void setCreatedAt(LocalDateTime t){ this.createdAt = t; }
    public LocalDateTime getLastStudied()    { return lastStudied; }
    public void setLastStudied(LocalDateTime t){ this.lastStudied = t; }
    public int getTotalQuestionsAnswered()   { return totalQuestionsAnswered; }
    public void setTotalQuestionsAnswered(int n){ this.totalQuestionsAnswered = n; }
    public int getTotalCorrect()             { return totalCorrect; }
    public void setTotalCorrect(int n)       { this.totalCorrect = n; }
    public Set<Topic> getSelectedTopics()    { return selectedTopics; }
    public void setSelectedTopics(Set<Topic> t){ this.selectedTopics = t; }
    public Map<Topic, TopicPerformance> getPerformance() { return performance; }
    public void setPerformance(Map<Topic, TopicPerformance> p){ this.performance = p; }
}
