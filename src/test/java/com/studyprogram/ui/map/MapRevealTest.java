package com.studyprogram.ui.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The map's one-time NEW! reveal tracking must persist on the profile. */
class MapRevealTest {

    @Test
    void revealedOnMapSurvivesRoundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        StudentProfile p = new StudentProfile("s");
        p.getRevealedOnMap().add(Topic.VARIABLES);
        p.getRevealedOnMap().add(Topic.LOOPS);

        StudentProfile restored = mapper.readValue(
                mapper.writeValueAsString(p), StudentProfile.class);
        assertTrue(restored.getRevealedOnMap().contains(Topic.VARIABLES));
        assertTrue(restored.getRevealedOnMap().contains(Topic.LOOPS));
        assertFalse(restored.getRevealedOnMap().contains(Topic.ARRAYS));
    }
}
