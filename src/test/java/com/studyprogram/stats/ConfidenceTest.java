package com.studyprogram.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfidenceTest {

    @Test
    void noDataGivesTheWidestPossibleInterval() {
        double[] ci = Confidence.interval(0, 0);
        assertEquals(0.0, ci[0], 1e-9);
        assertEquals(1.0, ci[1], 1e-9);
        assertEquals("no data", Confidence.label(0, 0));
    }

    @Test
    void moreEvidenceNarrowsTheInterval() {
        double[] few  = Confidence.interval(3, 3);
        double[] many = Confidence.interval(40, 40);
        assertTrue(few[0] < many[0], "3/3 must be less certain than 40/40");
        assertTrue(many[1] - many[0] < few[1] - few[0]);
        assertTrue(many[0] > 0.9);
    }

    @Test
    void intervalsStayInsideZeroToOne() {
        for (int n = 1; n <= 50; n++) {
            for (int k = 0; k <= n; k++) {
                double[] ci = Confidence.interval(k, n);
                assertTrue(ci[0] >= 0.0 && ci[1] <= 1.0, "k=" + k + " n=" + n);
                assertTrue(ci[0] <= ci[1]);
            }
        }
    }

    @Test
    void labelsDescribeEvidenceStrength() {
        assertTrue(Confidence.label(3, 3).contains("confidence"));
        assertTrue(Confidence.label(3, 3).contains("n=3"));
        assertTrue(Confidence.label(40, 40).contains("high confidence"));
    }

    @Test
    void lowerBoundIsBelowTheRawRate() {
        assertTrue(Confidence.lowerBound(8, 10) < 0.8);
        assertTrue(Confidence.upperBound(8, 10) > 0.8);
    }
}
