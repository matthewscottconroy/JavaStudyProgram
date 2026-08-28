package com.studyprogram.report;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A compact plain-text progress card — something a student can paste into a lab
 * submission or show for participation credit. Ends with a verification code
 * derived from the card's own numbers and the profile id, so a casually edited
 * card won't match its code (a deterrent, not cryptographic proof).
 */
public final class ProgressCard {

    private ProgressCard() {}

    public static String render(StudentProfile profile, List<AttemptRecord> attempts) {
        long answered = attempts.stream().filter(AttemptRecord::isAnswered).count();
        long correct  = attempts.stream().filter(AttemptRecord::isCorrect).count();
        long coding   = attempts.stream()
                .filter(a -> a.getType() == QuestionType.CODING && a.isCorrect()).count();
        TreeSet<LocalDate> days = new TreeSet<>();
        attempts.forEach(a -> { if (a.getTs() != null) days.add(a.getTs().toLocalDate()); });

        StringBuilder card = new StringBuilder();
        card.append("┌─ JAVA STUDY PROGRESS CARD ").append("─".repeat(25)).append("┐\n");
        row(card, "Student", profile.getName());
        row(card, "Date", LocalDate.now().toString());
        row(card, "Questions answered", String.valueOf(answered));
        row(card, "Accuracy", answered == 0 ? "-" : Math.round(100.0 * correct / answered) + "%");
        row(card, "Programs written & passed", String.valueOf(coding));
        row(card, "Study days", String.valueOf(days.size()));
        row(card, "Bosses cleared", profile.getBossesCleared().size() + "/5");
        card.append("├").append("─".repeat(52)).append("┤\n");

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
            row(card, "World " + level + " " + worlds[level - 1],
                    "[" + "#".repeat(filled) + ".".repeat(10 - filled) + "] " + pct + "%");
        }

        card.append("├").append("─".repeat(52)).append("┤\n");
        row(card, "Verification", verificationCode(profile, card.toString()));
        card.append("└").append("─".repeat(52)).append("┘\n");
        return card.toString();
    }

    private static void row(StringBuilder sb, String label, String value) {
        sb.append(String.format("│ %-26s %-23s │%n", label, value));
    }

    /** First 8 hex chars of SHA-256 over the card body plus the (private) profile id. */
    static String verificationCode(StudentProfile profile, String body) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((body + profile.getId()).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) hex.append(String.format("%02x", hash[i]));
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            return "N/A";
        }
    }
}
