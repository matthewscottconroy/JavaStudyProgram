package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

/**
 * Implemented by each topic's question bank class.
 * Add a new implementation and register it in {@link QuestionBank} to extend the question set.
 */
public interface QuestionLoader {
    /** The topic this loader provides questions for. */
    Topic getTopic();

    /** Returns all questions for this topic. */
    List<Question> load();
}
