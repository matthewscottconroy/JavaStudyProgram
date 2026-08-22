package com.studyprogram.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test: the recently-answered list must survive a save/load round trip,
 * otherwise the spaced-repetition "recently seen" penalty resets on every program run.
 */
class TopicPerformancePersistenceTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void recentlyAnsweredSurvivesRoundTrip() throws Exception {
        TopicPerformance perf = new TopicPerformance(Topic.LOOPS);
        perf.record("lp-code-01", true);
        perf.record("lp-db-comp-01", false);

        String json = mapper.writeValueAsString(perf);
        assertTrue(json.contains("recentlyAnswered"),
                "recentlyAnswered must be serialized: " + json);

        TopicPerformance restored = mapper.readValue(json, TopicPerformance.class);
        assertTrue(restored.wasRecentlySeen("lp-code-01"));
        assertTrue(restored.wasRecentlySeen("lp-db-comp-01"));
        assertFalse(restored.wasRecentlySeen("some-other-question"));
    }

    @Test
    void skipCountSurvivesRoundTrip() throws Exception {
        TopicPerformance perf = new TopicPerformance(Topic.ARRAYS);
        perf.recordSkip();
        perf.recordSkip();

        TopicPerformance restored = mapper.readValue(
                mapper.writeValueAsString(perf), TopicPerformance.class);
        assertEquals(2, restored.getSkipped());
    }
}
