package com.studyprogram.stats;

/**
 * Small statistics helpers for reporting honestly about small samples.
 *
 * <p>A raw percentage hides how much evidence sits behind it: 3-for-3 and 40-for-40 both read
 * "100%". The Wilson score interval gives a range that narrows as attempts accumulate, so the
 * student can see the difference between "probably solid" and "not enough data yet".
 */
public final class Confidence {

    /** z for a 95% two-sided interval. */
    private static final double Z = 1.96;

    private Confidence() {}

    /** Lower bound of the 95% Wilson interval for a success proportion. */
    public static double lowerBound(int successes, int attempts) {
        return interval(successes, attempts)[0];
    }

    /** Upper bound of the 95% Wilson interval for a success proportion. */
    public static double upperBound(int successes, int attempts) {
        return interval(successes, attempts)[1];
    }

    /** The 95% Wilson score interval as {lower, upper}; {0, 1} when there is no data. */
    public static double[] interval(int successes, int attempts) {
        if (attempts <= 0) return new double[] {0.0, 1.0};
        double n = attempts;
        double phat = successes / n;
        double z2 = Z * Z;
        double denominator = 1 + z2 / n;
        double centre = phat + z2 / (2 * n);
        double spread = Z * Math.sqrt(phat * (1 - phat) / n + z2 / (4 * n * n));
        double lower = (centre - spread) / denominator;
        double upper = (centre + spread) / denominator;
        return new double[] {Math.max(0.0, lower), Math.min(1.0, upper)};
    }

    /**
     * How much to trust a rate, as a short label: the width of the interval shrinks with evidence.
     * Used in reports next to raw percentages.
     */
    public static String label(int successes, int attempts) {
        if (attempts == 0) return "no data";
        double[] ci = interval(successes, attempts);
        double width = ci[1] - ci[0];
        String strength = width > 0.5 ? "very low confidence"
                        : width > 0.3 ? "low confidence"
                        : width > 0.15 ? "moderate confidence"
                        : "high confidence";
        return String.format("%.0f–%.0f%% (%s, n=%d)", ci[0] * 100, ci[1] * 100, strength, attempts);
    }
}
