package com.studyprogram.report;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.core.QuestionCalibration;
import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.model.TopicPerformance;
import com.studyprogram.storage.AttemptLog;
import com.studyprogram.storage.ProfileStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Instructor view: one self-contained HTML page aggregating every profile on this
 * machine — a per-student summary table, the class's weakest topics, and questions
 * flagged by calibration. Run with {@code java -jar java-study-program.jar --class-report}.
 */
public class ClassReportGenerator {

    public Path generate(ProfileStorage storage, QuestionBank bank, Path outFile)
            throws IOException {
        List<StudentProfile> profiles = new ArrayList<>();
        for (String name : storage.listProfileNames()) {
            storage.load(name).ifPresent(profiles::add);
        }
        java.util.Map<String, List<AttemptRecord>> attemptsByProfile =
                AttemptLog.readByProfile(storage.directory());

        StringBuilder h = new StringBuilder(32_000);
        h.append("<!DOCTYPE html><html lang='en'><head><meta charset='utf-8'>")
         .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
         .append("<title>Class Report — Java Study Program</title><style>")
         .append("""
                 body { font-family: system-ui, sans-serif; background: #f5f5f2; color: #222; }
                 .page { max-width: 900px; margin: 0 auto; padding: 24px; }
                 h2 { border-bottom: 2px solid #ddd; padding-bottom: 4px; margin-top: 32px; }
                 table { border-collapse: collapse; width: 100%; background: #fff; }
                 th, td { border: 1px solid #ddd; padding: 6px 10px; font-size: 14px; text-align: left; }
                 th { background: #eee; }
                 .dim { color: #55555f; font-size: 13px; }
                 """)
         .append("</style></head><body><main class='page'>")
         .append("<h1>Class Report</h1><p class='dim'>").append(profiles.size())
         .append(" profile(s) · generated ").append(LocalDate.now()).append("</p>");

        studentTable(h, profiles, storage);
        weakestTopics(h, profiles);
        cohortMisconceptions(h, attemptsByProfile);
        cohortCompileErrors(h, attemptsByProfile);
        flaggedByStudents(h, storage.directory().resolveSibling("flags.jsonl"));
        calibrationSection(h, attemptsByProfile, bank);

        h.append("</main></body></html>");
        Files.createDirectories(outFile.toAbsolutePath().getParent());
        Files.writeString(outFile, h.toString(), StandardCharsets.UTF_8);
        return outFile;
    }

    private void studentTable(StringBuilder h, List<StudentProfile> profiles,
                              ProfileStorage storage) {
        h.append("<h2>Students</h2><table><tr><th scope='col'>Student</th><th scope='col'>Answered</th>")
         .append("<th scope='col'>Accuracy</th><th scope='col'>Programs passed</th><th scope='col'>Study days</th>")
         .append("<th scope='col'>Bosses</th><th scope='col'>Last active</th></tr>");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (StudentProfile p : profiles) {
            List<AttemptRecord> log = AttemptLog
                    .forProfile(storage.directory(), p.getName()).readAll();
            long answered = log.stream().filter(AttemptRecord::isAnswered).count();
            long correct  = log.stream().filter(AttemptRecord::isCorrect).count();
            long coding   = log.stream().filter(a ->
                    a.getType() == com.studyprogram.model.QuestionType.CODING && a.isCorrect()).count();
            Set<LocalDate> days = new HashSet<>();
            log.forEach(a -> { if (a.getTs() != null) days.add(a.getTs().toLocalDate()); });

            h.append("<tr><th scope='row'>").append(esc(p.getName())).append("</th><td>").append(answered)
             .append("</td><td>").append(answered == 0 ? "-" : Math.round(100.0 * correct / answered) + "%")
             .append("</td><td>").append(coding)
             .append("</td><td>").append(days.size())
             .append("</td><td>").append(p.getBossesCleared().size()).append("/5")
             .append("</td><td>").append(p.getLastStudied() == null ? "-"
                     : p.getLastStudied().toLocalDate().format(fmt))
             .append("</td></tr>");
        }
        h.append("</table>");
    }

    private void weakestTopics(StringBuilder h, List<StudentProfile> profiles) {
        h.append("<h2>Class-wide weak spots</h2>");
        record Avg(Topic topic, double mastery, int students) {}
        List<Avg> averages = new ArrayList<>();
        for (Topic t : Topic.values()) {
            double sum = 0;
            int n = 0;
            for (StudentProfile p : profiles) {
                TopicPerformance perf = p.getPerformance().get(t);
                if (perf != null && perf.getAttempts() > 0) {
                    sum += perf.getMasteryScore();
                    n++;
                }
            }
            if (n > 0) averages.add(new Avg(t, sum / n, n));
        }
        if (averages.isEmpty()) {
            h.append("<p class='dim'>No attempts recorded yet.</p>");
            return;
        }
        averages.sort(Comparator.comparingDouble(Avg::mastery));
        h.append("<table><tr><th scope='col'>Topic</th><th scope='col'>Avg mastery</th><th scope='col'>Students practicing</th></tr>");
        for (Avg a : averages.subList(0, Math.min(8, averages.size()))) {
            h.append("<tr><th scope='row'>").append(esc(a.topic().displayName))
             .append("</th><td>").append(Math.round(a.mastery() * 100)).append("%")
             .append("</td><td>").append(a.students()).append("</td></tr>");
        }
        h.append("</table>");
    }

    /**
     * Which wrong ideas are shared across the class.
     *
     * <p>Ranked by how many students hold each one, because that is the difference between a
     * conversation with one student and five minutes at the whiteboard for everybody.
     */
    private void cohortMisconceptions(StringBuilder h,
                                      java.util.Map<String, List<AttemptRecord>> attemptsByProfile) {
        Map<String, Set<String>> studentsByIdea = new LinkedHashMap<>();
        Map<String, Integer> occurrences = new LinkedHashMap<>();
        for (var entry : attemptsByProfile.entrySet()) {
            for (AttemptRecord a : entry.getValue()) {
                if (a.getMisconception() == null) continue;
                studentsByIdea.computeIfAbsent(a.getMisconception(), k -> new HashSet<>())
                              .add(entry.getKey());
                occurrences.merge(a.getMisconception(), 1, Integer::sum);
            }
        }
        if (studentsByIdea.isEmpty()) return;

        List<String> ranked = new ArrayList<>(studentsByIdea.keySet());
        ranked.sort((x, y) -> {
            int byStudents = studentsByIdea.get(y).size() - studentsByIdea.get(x).size();
            return byStudents != 0 ? byStudents : occurrences.get(y) - occurrences.get(x);
        });

        h.append("<h2>Ideas the class is getting wrong</h2><p class='dim'>")
         .append("From the specific wrong answers students chose, ranked by how many students ")
         .append("chose them.</p><table><tr><th scope='col'>Idea</th>")
         .append("<th scope='col'>Students</th><th scope='col'>Times</th>")
         .append("<th scope='col'>What to say</th></tr>");
        for (String id : ranked.subList(0, Math.min(8, ranked.size()))) {
            var m = com.studyprogram.model.Misconception.byId(id);
            if (m.isEmpty()) continue;
            h.append("<tr><th scope='row'>").append(esc(m.get().summary)).append("</th><td>")
             .append(studentsByIdea.get(id).size()).append("</td><td>")
             .append(occurrences.get(id)).append("</td><td class='dim'>")
             .append(esc(m.get().explanation)).append("</td></tr>");
        }
        h.append("</table>");
    }

    /**
     * Where the class is stuck at the keyboard. Topic mastery says which concepts are weak;
     * this says which compiler errors keep stopping people, which is what a five-minute
     * demonstration at the start of class can actually fix. Ranked by how many distinct
     * students hit each error, so one struggling student does not dominate the list.
     */
    private void cohortCompileErrors(StringBuilder h,
                                     java.util.Map<String, List<AttemptRecord>> attemptsByProfile) {
        Map<String, Set<String>> studentsByError = new LinkedHashMap<>();
        Map<String, Integer> occurrences = new LinkedHashMap<>();
        for (var entry : attemptsByProfile.entrySet()) {
            for (AttemptRecord a : entry.getValue()) {
                for (String kind : a.getCompileErrors()) {
                    studentsByError.computeIfAbsent(kind, k -> new HashSet<>()).add(entry.getKey());
                    occurrences.merge(kind, 1, Integer::sum);
                }
            }
        }
        if (studentsByError.isEmpty()) return;

        List<String> ranked = new ArrayList<>(studentsByError.keySet());
        ranked.sort((x, y) -> {
            int byStudents = studentsByError.get(y).size() - studentsByError.get(x).size();
            return byStudents != 0 ? byStudents : occurrences.get(y) - occurrences.get(x);
        });

        h.append("<h2>Where the class gets stuck</h2><p class='dim'>")
         .append("Compile errors students hit while writing programs, ranked by how many ")
         .append("students ran into each one.</p><table><tr><th scope='col'>Error</th>")
         .append("<th scope='col'>Students</th><th scope='col'>Occurrences</th>")
         .append("<th scope='col'>What it means</th></tr>");
        for (String kind : ranked.subList(0, Math.min(10, ranked.size()))) {
            h.append("<tr><th scope='row'>").append(esc(kind)).append("</th><td>")
             .append(studentsByError.get(kind).size()).append("</td><td>")
             .append(occurrences.get(kind)).append("</td><td class='dim'>")
             .append(esc(com.studyprogram.coding.CompilerErrorDecoder.explanationFor(kind)))
             .append("</td></tr>");
        }
        h.append("</table>");
    }

    /** Questions students reported as wrong or unclear — the human counterpart to calibration. */
    private void flaggedByStudents(StringBuilder h, Path flagsFile) {
        if (!Files.isRegularFile(flagsFile)) return;
        try {
            List<String> lines = Files.readAllLines(flagsFile);
            if (lines.isEmpty()) return;
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            h.append("<h2>Reported by students</h2><table>")
             .append("<tr><th scope='col'>Question</th><th scope='col'>Topic</th><th scope='col'>Student</th><th scope='col'>Comment</th></tr>");
            for (String line : lines) {
                if (line.isBlank()) continue;
                var node = mapper.readTree(line);
                h.append("<tr><th scope='row'>").append(esc(node.path("questionId").asText()))
                 .append("</th><td>").append(esc(node.path("topic").asText()))
                 .append("</td><td>").append(esc(node.path("student").asText()))
                 .append("</td><td>").append(esc(node.path("note").asText()))
                 .append("</td></tr>");
            }
            h.append("</table>");
        } catch (IOException e) {
            h.append("<p class='dim'>Could not read student flags: ").append(esc(e.getMessage()))
             .append("</p>");
        }
    }

    private void calibrationSection(StringBuilder h,
                                    java.util.Map<String, List<AttemptRecord>> attemptsByProfile,
                                    QuestionBank bank) {
        List<String> flagged = QuestionCalibration.fromProfiles(attemptsByProfile)
                .flaggedForReview(bank.getQuestionsForTopics(List.of(Topic.values())));
        if (flagged.isEmpty()) return;
        h.append("<h2>Questions to review</h2><ul>");
        flagged.forEach(f -> h.append("<li class='dim'>").append(esc(f)).append("</li>"));
        h.append("</ul>");
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
