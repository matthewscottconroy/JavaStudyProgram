package com.studyprogram.report;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;
import com.studyprogram.ui.Display;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A compact plain-text progress card — something a student can paste into a lab submission or
 * hand in for participation credit.
 *
 * <p><b>What the code at the bottom does and does not prove.</b> Two modes:
 * <ul>
 *   <li><b>Checksum</b> (default): a hash of the card's own numbers. It catches a card edited
 *       after the fact, and nothing more — anyone who can run the program can produce a card with
 *       whatever numbers their profile contains.</li>
 *   <li><b>Signed</b>: when {@code STUDY_SIGNING_KEY} is set, an HMAC over the same content. This
 *       is only meaningful where the student does not hold the key — a shared lab machine the
 *       instructor configured, for example. On a student's own laptop it is still just a checksum
 *       with extra steps, and the card is evidence of self-reported practice, not proctored work.</li>
 * </ul>
 * Verify a card with {@code java -jar java-study-program.jar --verify-card <file>}.
 */
public final class ProgressCard {

    private static final String KEY_ENV = "STUDY_SIGNING_KEY";
    private static final String CHECKSUM_LABEL = "Checksum";
    private static final String SIGNED_LABEL = "Signature";

    private ProgressCard() {}

    public static String render(StudentProfile profile, List<AttemptRecord> attempts) {
        long answered = attempts.stream().filter(AttemptRecord::isAnswered).count();
        long correct  = attempts.stream().filter(AttemptRecord::isCorrect).count();
        long coding   = attempts.stream()
                .filter(a -> a.getType() == QuestionType.CODING && a.isCorrect()).count();
        TreeSet<LocalDate> days = new TreeSet<>();
        attempts.forEach(a -> { if (a.getTs() != null) days.add(a.getTs().toLocalDate()); });

        String h = Display.isAsciiOnly() ? "-" : "─";
        String v = Display.isAsciiOnly() ? "|" : "│";
        String tl = Display.isAsciiOnly() ? "+" : "┌";
        String tr = Display.isAsciiOnly() ? "+" : "┐";
        String bl = Display.isAsciiOnly() ? "+" : "└";
        String br = Display.isAsciiOnly() ? "+" : "┘";
        String ml = Display.isAsciiOnly() ? "+" : "├";
        String mr = Display.isAsciiOnly() ? "+" : "┤";

        StringBuilder card = new StringBuilder();
        card.append(tl).append(h).append(" JAVA STUDY PROGRESS CARD ").append(h.repeat(25)).append(tr).append("\n");
        row(card, v, "Student", profile.getName());
        row(card, v, "Date", LocalDate.now().toString());
        row(card, v, "Questions answered", String.valueOf(answered));
        row(card, v, "Accuracy", answered == 0 ? "-" : Math.round(100.0 * correct / answered) + "%");
        row(card, v, "Programs written & passed", String.valueOf(coding));
        row(card, v, "Study days", String.valueOf(days.size()));
        row(card, v, "Bosses cleared", profile.getBossesCleared().size() + "/5");
        if (profile.hasActiveGoal()) {
            var plan = com.studyprogram.core.GoalPlanner.plan(profile, LocalDate.now());
            int total = plan.behind().size() + plan.onTarget().size();
            // the value column is 23 characters wide; the title gets whatever the label allows
            String title = profile.getGoal().getTitle();
            if (title.length() > 20) title = title.substring(0, 19) + "…";
            row(card, v, "Goal: " + title,
                    plan.onTarget().size() + "/" + total + " ready, " + plan.daysLeft()
                    + " day" + (plan.daysLeft() == 1 ? "" : "s") + " left");
        }
        card.append(ml).append(h.repeat(52)).append(mr).append("\n");

        String[] worlds = {"Foundations", "Elementary", "Intermediate", "Advanced", "Expert"};
        Map<Topic, TopicPerformance> perf = profile.getPerformance();
        for (int level = 1; level <= 5; level++) {
            double sum = 0;
            int n = 0;
            for (Topic t : Topic.values()) {
                if (t.baseLevel != level) continue;
                n++;
                TopicPerformance p = perf.get(t);
                sum += p == null ? 0 : p.getMasteryScore();
            }
            int pct = n == 0 ? 0 : (int) Math.round(100.0 * sum / n);
            int filled = pct / 10;
            row(card, v, "World " + level + " " + worlds[level - 1],
                    "[" + "#".repeat(filled) + ".".repeat(10 - filled) + "] " + pct + "%");
        }

        card.append(ml).append(h.repeat(52)).append(mr).append("\n");
        String body = card.toString();
        row(card, v, signingKey() == null ? CHECKSUM_LABEL : SIGNED_LABEL, code(profile, body));
        card.append(bl).append(h.repeat(52)).append(br).append("\n");
        return card.toString();
    }

    /**
     * Re-derives the code for a rendered card and compares it with the printed one.
     *
     * @return a human-readable verdict
     */
    public static String verify(String cardText, String profileId) {
        String marker = signingKey() == null ? CHECKSUM_LABEL : SIGNED_LABEL;
        int markerLine = cardText.indexOf(marker);
        if (markerLine < 0) {
            return "No " + marker.toLowerCase() + " line found — is this a progress card"
                    + (signingKey() == null ? "" : " (and is STUDY_SIGNING_KEY the one used to make it)?");
        }
        int lineStart = cardText.lastIndexOf('\n', markerLine) + 1;
        String body = cardText.substring(0, lineStart);
        String printed = cardText.substring(markerLine + marker.length())
                .replaceAll("[^0-9A-Fa-f]", "").trim();
        String expected = codeFor(profileId, body);
        if (printed.isEmpty()) return "Could not read the code from the card.";
        return printed.equalsIgnoreCase(expected.substring(0, Math.min(expected.length(), printed.length())))
                ? "VALID — the numbers on this card match its " + marker.toLowerCase() + "."
                : "INVALID — this card's numbers do not match its " + marker.toLowerCase()
                  + " (it was edited, or made with a different key or profile).";
    }

    private static void row(StringBuilder sb, String v, String label, String value) {
        sb.append(String.format("%s %-26s %-23s %s%n", v, label, value, v));
    }

    /** Card code: HMAC when a signing key is configured, otherwise a plain checksum. */
    static String code(StudentProfile profile, String body) {
        return codeFor(profile.getId(), body);
    }

    static String codeFor(String profileId, String body) {
        String material = body + (profileId == null ? "" : profileId);
        try {
            String key = signingKey();
            byte[] digest;
            if (key == null) {
                digest = MessageDigest.getInstance("SHA-256")
                        .digest(material.getBytes(StandardCharsets.UTF_8));
            } else {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                digest = mac.doFinal(material.getBytes(StandardCharsets.UTF_8));
            }
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) hex.append(String.format("%02x", digest[i]));
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            return "N/A";
        }
    }

    private static String signingKey() {
        String key = System.getenv(KEY_ENV);
        return key == null || key.isBlank() ? null : key;
    }
}
