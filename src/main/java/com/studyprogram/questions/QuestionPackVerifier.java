package com.studyprogram.questions;

import com.studyprogram.coding.CodeSafetyScanner;
import com.studyprogram.coding.CodingExerciseRunner;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Runs the content gate over a question pack on disk — an instructor's own {@code data/questions}
 * folder, or one a colleague sent — and says exactly what is wrong with it.
 *
 * <p>The shipped bank is guarded by the test suite, which nobody writing a question pack for
 * their own course is going to run. Without this, a pack that parses but does not work is found
 * by a student mid-session: the exercise whose starter already passes, the reference solution
 * that no longer compiles, the folder named {@code Strings} that matches no topic and was quietly
 * never loaded. Every one of those is reported here, by file, before any student sees it.
 *
 * <p>Coding exercises are verified in parallel, the same way the test suite does it, so a pack of
 * a few hundred takes seconds rather than minutes.
 */
public final class QuestionPackVerifier {

    /** One thing wrong, and which file it is in. */
    public record Problem(String file, String message) {
        @Override public String toString() { return file + ": " + message; }
    }

    /**
     * @param questions how many parsed
     * @param coding    how many of those were compiled and run
     * @param problems  what failed — empty means the pack is ready to use
     * @param warnings  exercises that work but will be refused by the safety screen unless the
     *                  pack is explicitly trusted; not failures, but the author must know
     * @param notes     things worth knowing that are neither
     */
    public record Report(int questions, int coding, List<Problem> problems,
                         List<Problem> warnings, List<String> notes) {
        public boolean ok() { return problems.isEmpty(); }
    }

    private QuestionPackVerifier() {}

    public static Report verify(Path root) {
        List<Problem> problems = new ArrayList<>();
        List<Problem> warnings = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        List<Question> parsed = new ArrayList<>();
        List<Path> files = new ArrayList<>();

        if (!Files.isDirectory(root)) {
            problems.add(new Problem(root.toString(), "is not a directory"));
            return new Report(0, 0, problems, warnings, notes);
        }

        // ── Structure: folders must be topic slugs, files must parse, ids must be unique ──
        Set<String> ids = new HashSet<>();
        try (Stream<Path> dirs = Files.list(root)) {
            List<Path> topicDirs = dirs.filter(Files::isDirectory).sorted().toList();
            if (topicDirs.isEmpty()) {
                problems.add(new Problem(root.toString(),
                        "contains no topic folders (expected e.g. " + root.resolve("loops") + ")"));
            }
            for (Path dir : topicDirs) {
                String slug = dir.getFileName().toString();
                Topic topic = Topic.fromDirSlug(slug);
                if (topic == null) {
                    problems.add(new Problem(slug + "/", "matches no topic — nothing in it will load. "
                            + nearestSlug(slug)));
                    continue;
                }
                try (Stream<Path> json = Files.list(dir)) {
                    for (Path file : json.filter(f -> f.toString().endsWith(".json")).sorted().toList()) {
                        String label = slug + "/" + file.getFileName();
                        try (InputStream in = Files.newInputStream(file)) {
                            Question q = JsonQuestionParser.parse(in, topic, false);
                            if (!ids.add(q.getId())) {
                                problems.add(new Problem(label, "duplicate id '" + q.getId()
                                        + "' — only the first will load"));
                                continue;
                            }
                            parsed.add(q);
                            files.add(file);
                            checkShape(q, label, problems, warnings);
                        } catch (IOException | RuntimeException e) {
                            problems.add(new Problem(label, "does not parse — " + e.getMessage()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            problems.add(new Problem(root.toString(), "could not be read — " + e.getMessage()));
        }

        // ── Behaviour: coding exercises must actually work ──
        List<Question> coding = new ArrayList<>();
        for (int i = 0; i < parsed.size(); i++) {
            if (parsed.get(i).getType() == QuestionType.CODING) coding.add(parsed.get(i));
        }
        if (!coding.isEmpty()) {
            if (!CodingExerciseRunner.compilerAvailable()) {
                problems.add(new Problem(root.toString(), coding.size()
                        + " coding exercise(s) could not be verified: no JDK compiler on this "
                        + "machine. Run the verifier on a machine with a JDK."));
            } else {
                verifyCoding(coding, problems);
            }
        }

        // ── Notes: what the pack will yield beyond what was written ──
        int parsons = 0, faded = 0;
        for (Question q : coding) {
            if (ParsonsDeriver.derive(q).isPresent()) parsons++;
            faded += FadedExampleDeriver.derive(q).size();
        }
        if (parsons + faded > 0) {
            notes.add(coding.size() + " coding exercise(s) will also yield " + parsons
                    + " Parsons puzzle(s) and " + faded + " faded worked example(s) at load time.");
        }
        if (!warnings.isEmpty()) {
            notes.add(warnings.size() + " coding exercise(s) use capabilities the safety screen "
                    + "refuses from an external pack (file, network, process or reflection "
                    + "access). They are listed below and load only with "
                    + "JAVASTUDY_TRUST_EXTERNAL=1 — set it for packs you wrote or reviewed yourself.");
        }

        return new Report(parsed.size(), coding.size(), problems, warnings, notes);
    }

    /** Checks that do not need a compiler: the question is answerable by the grader it will meet. */
    private static void checkShape(Question q, String label, List<Problem> problems,
                                   List<Problem> warnings) {
        if (q.getPrompt() == null || q.getPrompt().isBlank()) {
            problems.add(new Problem(label, "has no prompt"));
        }
        if (q.getAnswer() == null || q.getAnswer().isBlank()) {
            problems.add(new Problem(label, "has no answer"));
        }
        if (q.isMultipleChoice()) {
            int n = q.getChoices().size();
            if (n < 2 || n > 4) {
                problems.add(new Problem(label, "has " + n + " choices; multiple choice needs 2 to 4"));
            }
            String a = q.getAnswer() == null ? "" : q.getAnswer().trim().toUpperCase().replaceAll("\\.$", "");
            if (a.length() != 1 || a.charAt(0) < 'A' || a.charAt(0) >= 'A' + n) {
                problems.add(new Problem(label, "answer '" + q.getAnswer()
                        + "' is not a choice letter (A" + (n > 1 ? "-" + (char) ('A' + n - 1) : "") + ")"));
            }
        }
        if (q.getType() == QuestionType.CODING) {
            if (q.getTestCode() == null || q.getTestCode().isBlank()) {
                problems.add(new Problem(label, "coding exercise has no testCode"));
            }
            if (!q.isMultiFile() && (q.getStarterCode() == null || q.getStarterCode().isBlank())) {
                problems.add(new Problem(label, "coding exercise has no starterCode"));
            }
            var findings = CodeSafetyScanner.scan(q.allCode());
            if (!findings.isEmpty()) {
                warnings.add(new Problem(label, "refused by the safety screen unless trusted: " + findings));
            }
        }
        if (q.getType() == QuestionType.PARSONS) {
            int lines = q.getAnswer() == null ? 0 : q.getAnswer().split("\n").length;
            if (q.getShuffledLines().size() != lines) {
                problems.add(new Problem(label, "shuffledLines has " + q.getShuffledLines().size()
                        + " lines but the answer has " + lines));
            }
        }
        if (q.getDifficulty() < 1 || q.getDifficulty() > 5) {
            problems.add(new Problem(label, "difficulty " + q.getDifficulty() + " is outside 1-5"));
        }
    }

    private static void verifyCoding(List<Question> coding, List<Problem> problems) {
        CodingExerciseRunner runner = new CodingExerciseRunner();
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<String>> futures = new ArrayList<>();
        for (Question q : coding) {
            futures.add(pool.submit(() -> runner.verifyExercise(q).orElse(null)));
        }
        pool.shutdown();
        for (int i = 0; i < coding.size(); i++) {
            String id = coding.get(i).getId();
            try {
                String problem = futures.get(i).get();
                if (problem != null) {
                    // verifyExercise prefixes the id; the file label is more useful here
                    problems.add(new Problem(coding.get(i).getTopic().dirSlug() + "/" + id,
                            problem.startsWith(id + ": ") ? problem.substring(id.length() + 2) : problem));
                }
            } catch (Exception e) {
                problems.add(new Problem(id, "verification crashed — " + e.getMessage()));
            }
        }
    }

    /** "Did you mean …?" for a misnamed topic folder. */
    private static String nearestSlug(String slug) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Topic t : Topic.values()) {
            int d = distance(slug.toLowerCase(), t.dirSlug());
            if (d < bestDistance) { bestDistance = d; best = t.dirSlug(); }
        }
        return best != null && bestDistance <= Math.max(2, slug.length() / 3)
                ? "Did you mean '" + best + "'?" : "Folder names are topic slugs like 'loops' or 'try_catch'.";
    }

    private static int distance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int[] cur = new int[b.length() + 1];
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            prev = cur;
        }
        return prev[b.length()];
    }

    /** Human-readable summary for the terminal. */
    public static String render(Path root, Report report) {
        StringBuilder out = new StringBuilder();
        out.append("Verified ").append(root).append(": ").append(report.questions())
           .append(" question(s), ").append(report.coding()).append(" compiled and run.\n");
        for (String note : report.notes()) out.append("  note: ").append(note).append('\n');
        for (Problem w : report.warnings()) {
            out.append("  warning: ").append(w.file()).append(": ").append(w.message()).append('\n');
        }
        if (report.ok()) {
            out.append("\nNo problems found. This pack is ready to use.\n");
        } else {
            out.append('\n').append(report.problems().size()).append(" problem(s):\n");
            for (Problem p : report.problems()) {
                String[] lines = p.message().split("\n");
                out.append("  ").append(p.file()).append(": ").append(lines[0]).append('\n');
                for (int i = 1; i < lines.length && i < 12; i++) {
                    out.append("      ").append(lines[i]).append('\n');
                }
            }
        }
        return out.toString();
    }
}
