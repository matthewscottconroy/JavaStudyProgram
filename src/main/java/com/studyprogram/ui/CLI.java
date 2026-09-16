package com.studyprogram.ui;

import com.studyprogram.coding.CodingExerciseRunner;
import com.studyprogram.coding.CodingResult;
import com.studyprogram.core.*;
import com.studyprogram.grading.CompositeGrader;
import com.studyprogram.grading.Grader;
import com.studyprogram.llm.LLMService;
import com.studyprogram.model.*;
import com.studyprogram.report.HtmlReportGenerator;
import com.studyprogram.storage.AttemptLog;
import com.studyprogram.storage.ProfileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Text-based CLI entry point. Drives the full student experience. */
public class CLI {

    private static final int     DEFAULT_SESSION_LENGTH = 10;
    private static final double  MASTERY_TARGET         = 0.80;
    private static final int     DEFAULT_EXAM_LENGTH    = 12;

    private final QuestionBank   bank;
    private final ProfileStorage storage;
    private final LLMService     llm;
    private final Grader         grader;
    private final Scanner        in;
    private final CodingExerciseRunner codingRunner;

    private StudentProfile currentProfile;
    private AttemptLog     attemptLog;
    private int            lastCodingHints;   // hints used in the most recent codingFlow run
    /** Compile-error categories the student hit during the most recent codingFlow run. */
    private final Set<String> lastCodingErrors = new LinkedHashSet<>();
    /** True while an exam is in progress: hints and explanations are refused. */
    private boolean noHelp;

    public CLI(QuestionBank bank, ProfileStorage storage, LLMService llm) {
        this(bank, storage, llm, System.in, new CodingExerciseRunner());
    }

    /**
     * Full constructor. Taking the input stream and the exercise runner as parameters is what
     * makes the interactive flow testable: a test can script keystrokes and point the workspace
     * somewhere disposable, while output is captured from {@code System.out}.
     */
    public CLI(QuestionBank bank, ProfileStorage storage, LLMService llm,
               java.io.InputStream input, CodingExerciseRunner codingRunner) {
        this.bank         = bank;
        this.storage      = storage;
        this.llm          = llm;
        this.grader       = new CompositeGrader(llm);
        this.in           = new Scanner(input);
        this.codingRunner = codingRunner;
    }

    public void run() {
        Display.header(Messages.get("app.title"));
        System.out.println("  " + Messages.get("banner.questions", bank.totalQuestions()));
        System.out.printf("  %-18s %s%n", Messages.get("banner.ai"),
                          llm.isAvailable()
                                  ? Display.green() + com.studyprogram.llm.LLMServiceFactory
                                        .describe(com.studyprogram.llm.LLMConfig.load()) + Display.reset()
                                  : Display.dim() + com.studyprogram.llm.LLMServiceFactory
                                        .describe(com.studyprogram.llm.LLMConfig.load()) + Display.reset());
        System.out.printf("  %-18s %s%n", Messages.get("banner.sandbox"),
                          Display.dim() + com.studyprogram.coding.Sandbox.describe() + Display.reset());
        System.out.printf("  Coding exercises:  %s%n",
                          CodingExerciseRunner.compilerAvailable()
                                  ? Display.green() + "enabled" + Display.reset()
                                  : Display.yellow() + "disabled — run with a full JDK (not a JRE) to "
                                    + "compile and test real programs" + Display.reset());

        for (String warning : bank.getWarnings()) {
            System.out.println("  " + Display.yellow() + "⚠ " + warning + Display.reset());
        }

        profileMenu();

        while (true) {
            System.out.println();
            System.out.println("  [1] " + Messages.get("menu.startSession"));
            System.out.println("  [2] " + Messages.get("menu.viewPerformance"));
            System.out.println("  [3] " + Messages.get("menu.conceptMap"));
            System.out.println("  [4] " + Messages.get("menu.bossChallenge"));
            System.out.println("  [5] " + Messages.get("menu.examMode"));
            System.out.println("  [6] " + Messages.get("menu.progressReport"));
            System.out.println("  [7] " + Messages.get("menu.selectTopics"));
            System.out.println("  [8] " + Messages.get("menu.switchProfile"));
            System.out.println("  [9] " + Messages.get("menu.exit"));
            System.out.print("\n  " + Messages.get("menu.choice") + " ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1" -> studySession();
                case "2" -> Display.performanceTable(currentProfile.getPerformance(),
                                                      currentProfile.getSelectedTopicsList());
                case "3" -> conceptMap();
                case "4" -> bossChallenge();
                case "5" -> examMode();
                case "6" -> progressReport();
                case "7" -> selectTopics();
                case "8" -> profileMenu();
                case "9" -> { saveProfile(); return; }
                default  -> System.out.println("  " + Messages.get("menu.invalidChoice"));
            }
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    private void profileMenu() {
        Display.header(Messages.get("profile.header"));
        try {
            List<String> profiles = storage.listProfileNames();
            if (!profiles.isEmpty()) {
                System.out.println("  " + Messages.get("profile.existing"));
                for (int i = 0; i < profiles.size(); i++) {
                    System.out.printf("    [%d] %s%n", i + 1, profiles.get(i));
                }
                System.out.println("    " + Messages.get("profile.options"));
                System.out.print("\n  " + Messages.get("profile.choosePrompt") + " ");
                String pick = in.nextLine().trim();
                if (pick.equalsIgnoreCase("D")) { deleteProfile(profiles); profileMenu(); return; }
                if (pick.equalsIgnoreCase("R")) { renameProfile(profiles); profileMenu(); return; }
                if (!pick.equalsIgnoreCase("N")) {
                    try {
                        int idx = Integer.parseInt(pick) - 1;
                        if (idx >= 0 && idx < profiles.size()) {
                            Optional<StudentProfile> loaded = storage.load(profiles.get(idx));
                            if (loaded.isPresent()) {
                                currentProfile = loaded.get();
                                currentProfile.applyDecay();
                                attemptLog = AttemptLog.forProfile(storage.directory(),
                                                                   currentProfile.getName());
                                System.out.println("  " + Messages.get("profile.welcomeBack", currentProfile.getName()));
                                return;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            createProfile();
        } catch (IOException e) {
            System.out.println("  Error loading profiles: " + e.getMessage());
            createProfile();
        }
    }

    /** Deletes a profile and its attempt log, with an explicit confirmation. */
    private void deleteProfile(List<String> profiles) {
        System.out.print("  Number to delete (Enter to cancel): ");
        String pick = in.nextLine().trim();
        int idx;
        try {
            idx = Integer.parseInt(pick) - 1;
        } catch (NumberFormatException e) {
            return;
        }
        if (idx < 0 || idx >= profiles.size()) return;
        String name = profiles.get(idx);
        System.out.printf("  Delete '%s' and its whole attempt history? Type the name to confirm: ", name);
        if (!in.nextLine().trim().equals(name)) {
            System.out.println("  " + Messages.get("profile.notDeleted"));
            return;
        }
        try {
            storage.delete(name);
            java.nio.file.Files.deleteIfExists(
                    AttemptLog.forProfile(storage.directory(), name).getFile());
            System.out.println("  " + Messages.get("profile.deleted", name));
        } catch (IOException e) {
            System.out.println("  Could not delete: " + e.getMessage());
        }
    }

    /** Renames a profile, carrying its attempt log across. */
    private void renameProfile(List<String> profiles) {
        System.out.print("  Number to rename (Enter to cancel): ");
        String pick = in.nextLine().trim();
        int idx;
        try {
            idx = Integer.parseInt(pick) - 1;
        } catch (NumberFormatException e) {
            return;
        }
        if (idx < 0 || idx >= profiles.size()) return;
        String oldName = profiles.get(idx);
        System.out.print("  New name: ");
        String newName = in.nextLine().trim();
        if (newName.isBlank() || newName.equals(oldName)) return;
        try {
            Optional<StudentProfile> loaded = storage.load(oldName);
            if (loaded.isEmpty()) return;
            StudentProfile profile = loaded.get();
            java.nio.file.Path oldLog = AttemptLog.forProfile(storage.directory(), oldName).getFile();
            profile.setName(newName);
            storage.save(profile);
            java.nio.file.Path newLog = AttemptLog.forProfile(storage.directory(), newName).getFile();
            if (java.nio.file.Files.exists(oldLog)) java.nio.file.Files.move(oldLog, newLog);
            storage.delete(oldName);
            System.out.println("  " + Messages.get("profile.renamed", newName));
        } catch (IOException e) {
            System.out.println("  Could not rename: " + e.getMessage());
        }
    }

    /** Records a student's report that a question is wrong or unclear. */
    private void flagQuestion(Question q) {
        System.out.print("  " + Messages.get("answer.flagPrompt") + " ");
        String note = in.nextLine().trim();
        try {
            java.nio.file.Path file = storage.directory().resolveSibling("flags.jsonl");
            String line = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                    java.util.Map.of(
                            "ts", LocalDateTime.now().toString(),
                            "questionId", q.getId(),
                            "topic", q.getTopic().name(),
                            "student", currentProfile.getName(),
                            "note", note));
            java.nio.file.Files.writeString(file, line + System.lineSeparator(),
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
            System.out.println("  " + Messages.get("answer.flagThanks"));
        } catch (Exception e) {
            System.out.println("  Could not record the flag: " + e.getMessage());
        }
    }

    private void createProfile() {
        System.out.print("  " + Messages.get("profile.enterName") + " ");
        String name = in.nextLine().trim();
        if (name.isBlank()) name = "Student";
        currentProfile = new StudentProfile(name);
        attemptLog = AttemptLog.forProfile(storage.directory(), name);
        System.out.println("  " + Messages.get("profile.created", name));
        selectTopics();
    }

    /**
     * Unit-review sessions: pick a course overlay file from data/courses/ and a unit
     * range; the session covers exactly those units' topics — ideal quiz prep.
     */
    private List<Topic> unitReviewTopics() {
        List<CourseOverlay> courses = CourseOverlay.loadAll(Path.of("data", "courses"));
        if (courses.isEmpty()) {
            System.out.println("  No course files found in data/courses/ — see the README "
                    + "for the overlay format (a sample ships with the repo).");
            return List.of();
        }
        CourseOverlay course = pickCourse(courses);

        System.out.println("  " + Display.bold() + course.getName() + Display.reset());
        for (CourseOverlay.Unit u : course.getUnits()) {
            System.out.printf("    %2d  %s%n", u.number(), u.title());
        }
        System.out.print("  Units to review (e.g. 3 or 1-4): ");
        int[] range = parseRange(in.nextLine().trim());
        if (range == null) return List.of();
        List<Topic> topics = course.topicsForUnits(range[0], range[1]);
        if (topics.isEmpty()) {
            System.out.println("  Those units cover no topics.");
        } else {
            System.out.println("  Reviewing: " + Display.cyan() + Display.topicSummary(topics)
                    + Display.reset());
        }
        return topics;
    }

    // ── Exam mode ─────────────────────────────────────────────────────────────

    /**
     * A timed, mixed exam scored against the course's own units.
     *
     * <p>Everything else in the program is designed to help the student succeed right now: the
     * feed picks what they are ready for, hints are a keypress away, and a missed question comes
     * back later. That is good practice and a bad prediction. This is the honest rehearsal —
     * fixed paper, clock running, no help — and its value is entirely in the report at the end,
     * which says which units are solid and which are not with a week still left to fix them.
     */
    private void examMode() {
        Display.header("Exam Mode");
        System.out.println("  A timed paper across several units. No hints, no explanations, "
                + "no second attempts.");
        System.out.println("  " + Display.dim()
                + "Nothing here is gated on the result — it is a rehearsal, not a gate."
                + Display.reset());

        List<CourseOverlay> courses = CourseOverlay.loadAll(Path.of("data", "courses"));
        List<ExamSession.Section> sections;
        if (courses.isEmpty()) {
            System.out.println();
            System.out.println("  " + Display.dim() + "No course file in data/courses/, so the "
                    + "exam is scored by world instead of by syllabus unit." + Display.reset());
            System.out.print("  Worlds to cover (e.g. 2 or 1-3): ");
            int[] range = parseRange(in.nextLine().trim());
            if (range == null) { System.out.println("  Cancelled."); return; }
            sections = ExamSession.worldSections(range[0], range[1]);
        } else {
            CourseOverlay course = pickCourse(courses);
            System.out.println("  " + Display.bold() + course.getName() + Display.reset());
            for (CourseOverlay.Unit u : course.getUnits()) {
                System.out.printf("    %2d  %s%n", u.number(), u.title());
            }
            System.out.print("  Units to examine (e.g. 3 or 1-4): ");
            int[] range = parseRange(in.nextLine().trim());
            if (range == null) { System.out.println("  Cancelled."); return; }
            sections = ExamSession.sectionsFor(course, range[0], range[1]);
        }

        if (sections.isEmpty()) {
            System.out.println("  That range covers no topics — nothing to examine.");
            return;
        }

        System.out.print("  How many questions [" + DEFAULT_EXAM_LENGTH + "]: ");
        int count = DEFAULT_EXAM_LENGTH;
        String lenInput = in.nextLine().trim();
        if (!lenInput.isBlank()) {
            try { count = Math.max(1, Integer.parseInt(lenInput)); }
            catch (NumberFormatException ignored) {}
        }

        // Seeded by profile and attempt count so a retake is a different paper, while an
        // instructor handing out a fixed seed gets the same paper for everyone.
        Random rng = new Random(currentProfile.getId().hashCode() * 31L
                + currentProfile.getTotalQuestionsAnswered());
        List<ExamSession.Item> paper = ExamSession.build(bank, sections, count, rng);
        if (paper.isEmpty()) {
            System.out.println("  No questions available for those units.");
            return;
        }

        int minutes = ExamSession.suggestedMinutes(paper);
        System.out.printf("%n  %d questions · %d sections · %d minutes.%n",
                paper.size(), sections.size(), minutes);
        System.out.print("  Start the clock? [y/N]: ");
        if (!in.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("  Cancelled — nothing was recorded.");
            return;
        }

        sitExam(paper, minutes);
    }

    /** Runs the paper against the clock and reports the result. */
    private void sitExam(List<ExamSession.Item> paper, int minutes) {
        long deadline = System.nanoTime() + minutes * 60L * 1_000_000_000L;
        Set<String> correctIds = new LinkedHashSet<>();
        Set<String> attemptedIds = new LinkedHashSet<>();
        String lastSection = null;
        boolean ranOut = false;
        noHelp = true;

        try {
            for (int i = 0; i < paper.size(); i++) {
                ExamSession.Item item = paper.get(i);
                Question q = item.question();

                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) { ranOut = true; break; }

                if (!item.section().title().equals(lastSection)) {
                    lastSection = item.section().title();
                    System.out.println();
                    System.out.println("  " + Display.bold() + lastSection + Display.reset());
                }
                System.out.printf("  %s%d min remaining%s%n", Display.dim(),
                        Math.max(1, remaining / 60_000_000_000L), Display.reset());

                long qStart = System.nanoTime();
                if (q.isCoding()) {
                    CodingOutcome outcome = codingFlow(q, i + 1, paper.size());
                    if (outcome == CodingOutcome.QUIT) break;
                    attemptedIds.add(q.getId());
                    boolean ok = outcome == CodingOutcome.CORRECT;
                    if (ok) correctIds.add(q.getId());
                    currentProfile.recordAnswer(q, ok);
                    logAttempt(q, ok ? AttemptRecord.OUTCOME_CORRECT : AttemptRecord.OUTCOME_INCORRECT,
                            qStart, 0, lastCodingErrors);
                    continue;
                }

                Display.question(q, i + 1, paper.size());
                String answer = in.nextLine().trim();
                if (answer.equalsIgnoreCase("q")) break;
                if (answer.equalsIgnoreCase("h") || answer.equalsIgnoreCase("e")) {
                    System.out.println("  " + Display.yellow() + "No help during an exam."
                            + Display.reset() + " Answer as best you can:");
                    answer = in.nextLine().trim();
                }
                attemptedIds.add(q.getId());
                GradingResult result = grader.grade(q,
                        answer.equalsIgnoreCase("s") ? "" : collectAnswer(q, answer));
                if (result.correct()) correctIds.add(q.getId());
                currentProfile.recordAnswer(q, result.correct());
                logAttempt(q, result.correct() ? AttemptRecord.OUTCOME_CORRECT
                                               : AttemptRecord.OUTCOME_INCORRECT, qStart, 0);
                // No feedback mid-exam: the whole point is to find out what the student knows
                // unaided, and marking each answer as it lands would leak the pattern.
                System.out.println("  " + Display.dim() + "Recorded." + Display.reset());
            }
        } finally {
            noHelp = false;
        }

        saveProfile();
        reportExam(paper, correctIds, attemptedIds, ranOut);
    }

    /** Prints the outcome-by-outcome result and saves it next to the student's other reports. */
    private void reportExam(List<ExamSession.Item> paper, Set<String> correctIds,
                            Set<String> attemptedIds, boolean ranOut) {
        List<ExamSession.SectionScore> scores =
                ExamSession.score(paper, correctIds, attemptedIds);
        StringBuilder out = new StringBuilder();
        out.append("Exam result — ").append(currentProfile.getName()).append("\n");
        out.append(LocalDateTime.now().toLocalDate()).append("\n\n");

        for (ExamSession.SectionScore s : scores) {
            out.append(String.format("  %-34s %2d/%-2d  %3d%%  %s%n", s.title(), s.correct(),
                    s.total(), Math.round(s.ratio() * 100), s.verdict()));
            if (s.answered() < s.total()) {
                out.append(String.format("  %-34s %s%n", "",
                        (s.total() - s.answered()) + " not reached before time ran out"));
            }
        }
        int correct = correctIds.size();
        out.append(String.format("%n  %-34s %2d/%-2d  %3d%%%n", "OVERALL", correct, paper.size(),
                Math.round(ExamSession.overall(scores) * 100)));
        if (ranOut) out.append("\n  The clock ran out before the end of the paper.\n");

        List<String> weak = ExamSession.weakestSections(scores);
        if (weak.isEmpty()) {
            out.append("\n  Every section is solid. This is what ready looks like.\n");
        } else {
            out.append("\n  Work on these before the real thing, weakest first:\n");
            for (String w : weak) out.append("    - ").append(w).append("\n");
        }

        Display.header("Exam Result");
        for (String line : out.toString().split("\n")) System.out.println("  " + line);

        try {
            String safe = currentProfile.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            Path file = storage.directory().resolveSibling("reports")
                    .resolve(safe + "-exam-" + LocalDateTime.now().toLocalDate() + ".txt");
            java.nio.file.Files.createDirectories(file.getParent());
            java.nio.file.Files.writeString(file, out.toString());
            System.out.println("  " + Display.dim() + "Saved to " + file.toAbsolutePath()
                    + Display.reset());
        } catch (IOException e) {
            System.out.println("  Could not save the exam report: " + e.getMessage());
        }
    }

    /** Lets the student choose among course overlays, returning the only one without asking. */
    private CourseOverlay pickCourse(List<CourseOverlay> courses) {
        CourseOverlay course = courses.get(0);
        if (courses.size() > 1) {
            for (int i = 0; i < courses.size(); i++) {
                System.out.printf("  [%d] %s%n", i + 1, courses.get(i).getName());
            }
            System.out.print("  Course: ");
            try {
                int idx = Integer.parseInt(in.nextLine().trim()) - 1;
                if (idx >= 0 && idx < courses.size()) course = courses.get(idx);
            } catch (NumberFormatException ignored) {}
        }
        course.getWarnings().forEach(w ->
                System.out.println("  " + Display.yellow() + "⚠ " + w + Display.reset()));
        return course;
    }

    /** Parses "3" or "1-4" into an inclusive range, or null when it is neither. */
    private static int[] parseRange(String text) {
        try {
            if (text.contains("-")) {
                String[] parts = text.split("-", 2);
                return new int[] { Integer.parseInt(parts[0].trim()),
                                   Integer.parseInt(parts[1].trim()) };
            }
            int one = Integer.parseInt(text.trim());
            return new int[] { one, one };
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * World boss fights: a 10-question, no-hints quiz across one level band's topics.
     * Clearing one (80%+) is recorded on the profile and shown on the concept map.
     */
    private void bossChallenge() {
        Display.header(Messages.get("boss.header"));
        String[] worldNames = {"", "Foundations", "Elementary", "Intermediate", "Advanced", "Expert"};
        for (int level = 1; level <= 5; level++) {
            String status;
            if (BossChallenge.cleared(currentProfile, level)) {
                status = Display.green() + "CLEARED ★" + Display.reset();
            } else if (BossChallenge.unlocked(currentProfile, level)) {
                status = Display.cyan() + "READY — face the boss!" + Display.reset();
            } else {
                status = Display.dim() + "locked (raise the world's average mastery to "
                        + (int) (BossChallenge.UNLOCK_AVG_MASTERY * 100) + "%)" + Display.reset();
            }
            System.out.printf("  [%d] World %d · %-14s %s%n", level, level, worldNames[level], status);
        }
        System.out.print("\n  World number (Enter to cancel): ");
        String input = in.nextLine().trim();
        if (input.isBlank()) return;

        int level;
        try {
            level = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return;
        }
        if (level < 1 || level > 5) return;
        fightBoss(level);
    }

    /**
     * Runs one world's boss quiz. Reachable from the Boss Challenge menu and from the
     * concept map, which is why the unlock check lives here rather than at the menu.
     */
    private void fightBoss(int level) {
        String[] worldNames = {"", "Foundations", "Elementary", "Intermediate", "Advanced", "Expert"};
        if (!BossChallenge.unlocked(currentProfile, level)) {
            System.out.println("  That boss is still locked — keep practicing its world first.");
            return;
        }

        Random rng = new Random(level * 1000L + currentProfile.getBossAttempts());
        currentProfile.setBossAttempts(currentProfile.getBossAttempts() + 1);
        List<Question> quiz = BossChallenge.pickQuestions(bank, level, rng);

        Display.header("BOSS FIGHT — World " + level + " · " + worldNames[level]);
        System.out.println("  " + quiz.size() + " questions. No hints. Skipping counts as a miss. "
                + "Score " + (int) (BossChallenge.PASS_RATIO * 100) + "%+ to clear the world!");

        int correct = 0, asked = 0;
        for (Question q : quiz) {
            asked++;

            // The finale is a real program: run it through the compile-and-test flow
            if (q.isCoding()) {
                System.out.println("\n  " + Display.bold() + "FINAL BLOW — write the program."
                        + Display.reset());
                long codeStart = System.nanoTime();
                CodingOutcome outcome = codingFlow(q, asked, quiz.size());
                if (outcome == CodingOutcome.QUIT) { asked--; break; }
                boolean beat = outcome == CodingOutcome.CORRECT;
                if (beat) correct++;
                currentProfile.recordAnswer(q, beat);
                logAttempt(q, beat ? AttemptRecord.OUTCOME_CORRECT : AttemptRecord.OUTCOME_INCORRECT,
                        codeStart, lastCodingHints);
                continue;
            }

            Display.question(q, asked, quiz.size());
            long qStart = System.nanoTime();
            String answer = in.nextLine().trim();
            if (answer.equalsIgnoreCase("q")) {
                System.out.println("  You fled the boss fight!");
                asked--;
                break;
            }
            if (answer.equalsIgnoreCase("h") || answer.equalsIgnoreCase("e")) {
                System.out.println("  " + Display.yellow() + "No help during a boss fight!"
                        + Display.reset() + " Your answer counts as given:");
                answer = in.nextLine().trim();
            }
            GradingResult result = grader.grade(q,
                    answer.equalsIgnoreCase("s") ? "" : collectAnswer(q, answer));
            currentProfile.recordAnswer(q, result.correct());
            logAttempt(q, result.correct() ? AttemptRecord.OUTCOME_CORRECT
                                           : AttemptRecord.OUTCOME_INCORRECT, qStart, 0);
            if (result.correct()) {
                correct++;
                Display.correct(result);
            } else {
                Display.incorrect(result);
            }
        }

        System.out.printf("%n  Boss result: %d/%d%n", correct, asked);
        if (BossChallenge.passed(correct, quiz.size())) {
            currentProfile.getBossesCleared().add(level);
            System.out.println("  " + Display.green() + Display.bold()
                    + "★ WORLD " + level + " CLEARED! ★" + Display.reset()
                    + "  It now shows on your concept map.");
        } else if (asked == quiz.size()) {
            System.out.println("  The boss survives… study up and challenge it again "
                    + "(the questions change each attempt).");
        }
        saveProfile();
    }

    /**
     * Opens the Swing overworld map and acts on whatever the student asked for there —
     * studying a topic or challenging a world boss — so the map is a way to navigate the
     * program, not just a picture of it.
     */
    private void conceptMap() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("  No display available (headless environment) — "
                    + "use [6] Select Topics instead.");
            return;
        }
        System.out.println("  Map window opened. Double-click a topic to study it, click a boss to "
                + "fight it, or just close the window.");
        var action = com.studyprogram.ui.map.OverworldFrame.showModal(currentProfile, this::saveProfile);
        action.ifPresent(a -> {
            switch (a.kind()) {
                case STUDY_TOPIC -> {
                    currentProfile.getSelectedTopics().add(a.topic());
                    saveProfile();
                    System.out.println("  Starting a session on " + a.topic().displayName + ".");
                    runSession(List.of(a.topic()), DEFAULT_SESSION_LENGTH, false);
                }
                case FIGHT_BOSS -> fightBoss(a.world());
                case NONE -> { }
            }
        });
    }

    /** Generates the self-contained HTML progress report and tries to open it. */
    private void progressReport() {
        try {
            String safe = currentProfile.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
            Path out = storage.directory().resolveSibling("reports").resolve(safe + "-progress.html");
            QuestionCalibration calibration = QuestionCalibration.fromProfiles(
                    AttemptLog.readByProfile(storage.directory()));
            List<String> flagged = calibration.flaggedForReview(
                    bank.getQuestionsForTopics(List.of(Topic.values())));
            List<AttemptRecord> myAttempts = attemptLog.readAll();
            Path written = new HtmlReportGenerator()
                    .generate(currentProfile, myAttempts, flagged, out);
            System.out.println("  Report written to: " + Display.cyan()
                    + written.toAbsolutePath() + Display.reset());

            // Compact text card for lab submissions / participation credit
            String card = com.studyprogram.report.ProgressCard.render(currentProfile, myAttempts);
            Path cardFile = out.resolveSibling(safe + "-card.txt");
            java.nio.file.Files.writeString(cardFile, card);
            System.out.println();
            for (String line : card.split("\n")) System.out.println("  " + line);
            System.out.println("  Card saved to: " + Display.dim() + cardFile.toAbsolutePath()
                    + Display.reset());
            try {
                if (java.awt.Desktop.isDesktopSupported()
                        && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(written.toAbsolutePath().toUri());
                    System.out.println("  Opened in your browser.");
                }
            } catch (Exception ignored) {
                // headless or no browser — the path above is enough
            }
        } catch (IOException e) {
            System.out.println("  Could not generate report: " + e.getMessage());
        }
    }

    /**
     * Completes a multi-blank answer. A faded worked example with three blanks is three separate
     * lines of Java, and asking for them on one line (separated by some punctuation the student
     * has to remember) would test the input format rather than the code. The remaining blanks are
     * collected one prompt at a time and joined for the grader.
     */
    private String collectAnswer(Question q, String first) {
        if (q.getType() != QuestionType.FADED) return first;
        int blanks = com.studyprogram.questions.FadedExampleDeriver.blankCount(q);
        if (blanks <= 1) return first;

        StringBuilder all = new StringBuilder(first);
        for (int b = 2; b <= blanks; b++) {
            System.out.print("  Line for blank " + b + ": ");
            all.append('\n').append(in.nextLine());
        }
        return all.toString();
    }

    /** Appends one attempt to the append-only log (timings in whole seconds). */
    private void logAttempt(Question q, String outcome, long startNano, int hintsUsed) {
        logAttempt(q, outcome, startNano, hintsUsed, List.of());
    }

    /** As above, also recording which compile errors the student hit on the way. */
    private void logAttempt(Question q, String outcome, long startNano, int hintsUsed,
                            Collection<String> compileErrors) {
        if (attemptLog == null) return;
        long secs = Math.max(0, (System.nanoTime() - startNano) / 1_000_000_000L);
        attemptLog.append(new AttemptRecord(LocalDateTime.now(), q, outcome, secs, hintsUsed,
                                            new ArrayList<>(compileErrors)));
    }

    private void saveProfile() {
        try {
            storage.save(currentProfile);
        } catch (IOException e) {
            System.out.println("  Warning: could not save profile — " + e.getMessage());
        }
    }

    // ── Topic selection ───────────────────────────────────────────────────────

    private void selectTopics() {
        Display.header("Topic Selection");
        Topic[] allTopics = Topic.visibleValues().toArray(new Topic[0]);
        Set<Topic> selected = new HashSet<>(currentProfile.getSelectedTopics());
        Map<Topic, TopicPerformance> perf = currentProfile.getPerformance();

        String[] levelNames = {"", "Beginner", "Elementary", "Intermediate", "Advanced", "Expert"};
        int idx = 1;
        Map<Integer, Topic> indexMap = new LinkedHashMap<>();

        Map<Integer, List<Topic>> byLevel = Arrays.stream(allTopics)
                .collect(Collectors.groupingBy(t -> t.baseLevel));

        for (int level = 1; level <= 5; level++) {
            List<Topic> group = byLevel.getOrDefault(level, List.of());
            if (group.isEmpty()) continue;
            System.out.println();
            System.out.printf("  ── %s ─────────────────────────────────%n", levelNames[level]);
            for (Topic t : group) {
                boolean sel    = selected.contains(t);
                boolean locked = !t.getPrerequisites().isEmpty() && !t.isUnlocked(perf);
                TopicPerformance tp = perf.get(t);
                int pct = tp == null ? 0 : (int)(tp.getMasteryScore() * 100);

                String color  = locked ? Display.dim() : (sel ? Display.cyan() : "");
                String marker = sel    ? " ●" : "";
                String lock   = locked ? Display.red() + " [LOCKED]" + Display.reset() : "";
                System.out.printf("  [%2d] %s%-40s%s%s  %3d%%%n",
                        idx, color, t.displayName + marker, Display.reset(), lock, pct);
                indexMap.put(idx, t);
                idx++;
            }
        }

        System.out.println();
        System.out.println("  Enter numbers to toggle (comma-separated), 'all', or 'done':");
        System.out.print("  > ");
        String input = in.nextLine().trim();

        if (input.equalsIgnoreCase("all")) {
            int lockedSkipped = 0;
            for (Topic t : allTopics) {
                if (t.getPrerequisites().isEmpty() || t.isUnlocked(perf)) {
                    selected.add(t);
                } else {
                    lockedSkipped++;
                }
            }
            if (lockedSkipped > 0) {
                System.out.printf("  %d locked topic(s) not added — select them individually to override.%n",
                        lockedSkipped);
            }
        } else if (!input.equalsIgnoreCase("done")) {
            for (String part : input.split(",")) {
                try {
                    int num = Integer.parseInt(part.trim());
                    Topic t = indexMap.get(num);
                    if (t != null) {
                        if (selected.contains(t)) {
                            selected.remove(t);
                        } else {
                            if (!t.getPrerequisites().isEmpty() && !t.isUnlocked(perf)) {
                                String prereqs = t.getPrerequisites().stream()
                                        .map(p -> p.displayName)
                                        .collect(Collectors.joining(", "));
                                System.out.printf("  Note: %s recommends: %s (adding anyway)%n",
                                        t.displayName, prereqs);
                            }
                            selected.add(t);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        currentProfile.setSelectedTopics(selected);
        System.out.printf("  %d topic(s) selected.%n", selected.size());
        saveProfile();
    }

    // ── Study session ─────────────────────────────────────────────────────────

    private void studySession() {
        boolean canResume = currentProfile.hasResumableSession();
        System.out.print("\n  " + Messages.get("session.feedPrompt")
                + (canResume ? "  " + Messages.get("session.repeatOption",
                        Display.topicSummary(currentProfile.getLastSessionTopics())) : "")
                + "  [a]: ");
        String mode = in.nextLine().trim().toLowerCase();

        // Picking up where you left off should not mean re-answering the setup questions
        if (canResume && mode.equals("r")) {
            List<Topic> previous = currentProfile.getLastSessionTopics();
            System.out.println("  " + Display.cyan()
                    + Messages.get("session.resuming", Display.topicSummary(previous))
                    + Display.reset());
            runSession(previous, currentProfile.getLastSessionLength(), false);
            return;
        }

        List<Topic> active;
        if (mode.equals("m")) {
            active = currentProfile.getSelectedTopicsList();
            if (active.isEmpty()) {
                System.out.println("  No topics selected. Please select topics first.");
                selectTopics();
                active = currentProfile.getSelectedTopicsList();
                if (active.isEmpty()) return;
            }
        } else if (mode.equals("u")) {
            active = unitReviewTopics();
            if (active.isEmpty()) return;
        } else {
            active = Curriculum.autoTopics(currentProfile, 6);
            System.out.println("  Auto plan: " + Display.cyan()
                    + active.stream().map(t -> t.displayName).collect(Collectors.joining(", "))
                    + Display.reset());
        }

        System.out.println();
        System.out.printf("  Questions per session [%d] (0 = unlimited, m = until %.0f%% mastery): ",
                DEFAULT_SESSION_LENGTH, MASTERY_TARGET * 100);
        String lenInput   = in.nextLine().trim();
        boolean masteryMode = lenInput.equalsIgnoreCase("m");
        int sessionLen    = DEFAULT_SESSION_LENGTH;
        if (masteryMode) {
            sessionLen = 0;
        } else if (!lenInput.isBlank()) {
            try { sessionLen = Integer.parseInt(lenInput); }
            catch (NumberFormatException ignored) {}
        }

        runSession(active, sessionLen, masteryMode);
    }

    /**
     * Runs a study session over an explicit topic list. Separate from the feed-selection
     * prompt so the concept map can start a session on one topic directly.
     */
    private void runSession(List<Topic> active, int sessionLen, boolean masteryMode) {
        currentProfile.setLastSessionTopics(active);
        currentProfile.setLastSessionLength(sessionLen);
        saveProfile();
        // Calibrate question difficulty from every profile's attempt history on this
        // machine — real student data gradually corrects the authored difficulty labels.
        // The review scheduler is per-student: it spaces THIS student's repetitions.
        QuestionCalibration calibration = QuestionCalibration.fromProfiles(
                AttemptLog.readByProfile(storage.directory()));
        ReviewScheduler scheduler = ReviewScheduler.fromRecords(attemptLog.readAll());
        AdaptiveEngine engine  = new AdaptiveEngine(bank, new Random(), llm, calibration, scheduler);
        StudySession   session = new StudySession(currentProfile, active, engine, sessionLen);

        Display.header("Study Session — " + Display.topicSummary(active));

        int qNum = 0;
        boolean quit = false;

        while (!quit) {
            Optional<Question> next = session.nextQuestion();
            if (next.isEmpty()) break;

            Question q = next.get();
            qNum++;
            int displayTotal = sessionLen > 0 ? sessionLen : qNum;

            // Coding exercises have their own compile-and-test flow
            if (q.isCoding()) {
                long qStart = System.nanoTime();
                CodingOutcome outcome = codingFlow(q, qNum, displayTotal);
                switch (outcome) {
                    case QUIT -> quit = true;
                    case SKIPPED -> {
                        session.skip(q);
                        logAttempt(q, AttemptRecord.OUTCOME_SKIPPED, qStart, lastCodingHints, lastCodingErrors);
                        System.out.println("  " + Messages.get("session.skipped"));
                    }
                    case CORRECT -> {
                        GradingResult r = GradingResult.correct(q.getExplanation());
                        session.recordAnswer(q, r);
                        logAttempt(q, AttemptRecord.OUTCOME_CORRECT, qStart, lastCodingHints, lastCodingErrors);
                        saveProfile();
                        Display.correct(r);
                    }
                    case GAVE_UP -> {
                        GradingResult r = GradingResult.incorrect(
                                "Recorded as incorrect — study the reference solution and it "
                                + "will come around again.", q.getExplanation());
                        session.recordAnswer(q, r);
                        logAttempt(q, AttemptRecord.OUTCOME_INCORRECT, qStart, lastCodingHints, lastCodingErrors);
                        saveProfile();
                    }
                }
                if (quit) break;
                if (outcome == CodingOutcome.CORRECT || outcome == CodingOutcome.GAVE_UP) {
                    System.out.print("\n  [Enter] next  [q] quit: ");
                    if (in.nextLine().trim().equalsIgnoreCase("q")) break;
                }
                if (masteryMode && allTopicsMastered(active)) {
                    System.out.println("\n  " + Display.green() + Display.bold()
                            + "All topics mastered! Session complete." + Display.reset());
                    break;
                }
                continue;
            }

            Display.question(q, qNum, displayTotal);
            long qStart = System.nanoTime();
            int hintsUsed = 0;

            // Pre-answer command loop — student can request hints/explains before answering
            String answer = null;
            while (answer == null) {
                String input = in.nextLine().trim();

                if (input.equalsIgnoreCase("q")) { quit = true; break; }
                if (input.equalsIgnoreCase("s")) {
                    session.skip(q);
                    logAttempt(q, AttemptRecord.OUTCOME_SKIPPED, qStart, hintsUsed);
                    System.out.println("  " + Messages.get("session.skipped"));
                    answer = null;
                    break;
                }
                if (input.equalsIgnoreCase("f")) {
                    flagQuestion(q);
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (input.equalsIgnoreCase("h")) {
                    hintsUsed++;
                    System.out.println("  Hint: " + Display.yellow() + llm.generateHint(q) + Display.reset());
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (input.equalsIgnoreCase("e")) {
                    System.out.println("  " + Display.dim()
                            + llm.explainConcept(q.getTopic(), q.getPrompt()) + Display.reset());
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (!input.isBlank()) {
                    answer = collectAnswer(q, input);
                } else {
                    System.out.print("  Your answer: ");
                }
            }

            if (quit) break;
            if (answer == null) continue;  // skipped

            GradingResult result = grader.grade(q, answer);
            session.recordAnswer(q, result);
            logAttempt(q, result.correct() ? AttemptRecord.OUTCOME_CORRECT
                                           : AttemptRecord.OUTCOME_INCORRECT, qStart, hintsUsed);
            saveProfile();   // auto-save after every answered question

            if (result.correct()) Display.correct(result);
            else                  Display.incorrect(result);

            // Post-answer navigation
            System.out.print("\n  [Enter] next  [e] explain more  [q] quit: ");
            String nav = in.nextLine().trim();
            if (nav.equalsIgnoreCase("q")) break;
            if (nav.equalsIgnoreCase("e")) {
                System.out.println("  " + Display.dim()
                        + llm.explainConcept(q.getTopic(), q.getPrompt()) + Display.reset());
            }

            // Mastery mode: stop when all active topics hit the target
            if (masteryMode && allTopicsMastered(active)) {
                System.out.println("\n  " + Display.green() + Display.bold()
                        + "All topics mastered! Session complete." + Display.reset());
                break;
            }
        }

        long secs = session.elapsed().getSeconds();
        Display.sessionSummary(session.questionsAnswered(), session.correctAnswers(),
                               session.skippedCount(), secs, session.topicBreakdown());
        saveProfile();

        // Offer wrong-answer review
        List<Question> wrong = session.getWrongAnswers();
        if (!wrong.isEmpty()) {
            System.out.printf("  You got %d question(s) wrong. Review them now? [y/n]: ",
                    wrong.size());
            if (in.nextLine().trim().equalsIgnoreCase("y")) {
                reviewWrongAnswers(wrong);
            }
        }
    }

    // ── Coding exercises ──────────────────────────────────────────────────────

    private enum CodingOutcome { CORRECT, GAVE_UP, SKIPPED, QUIT }

    /**
     * Drives one coding exercise: writes the starter file to the workspace, then loops
     * compile-and-test runs until the tests pass or the student gives up, skips, or quits.
     */
    private CodingOutcome codingFlow(Question q, int qNum, int total) {
        Path file;
        try {
            file = codingRunner.prepareWorkspace(q);
        } catch (IOException e) {
            System.out.println("  Could not prepare the workspace: " + e.getMessage());
            return CodingOutcome.SKIPPED;
        }

        Display.codingQuestion(q, qNum, total, file);
        lastCodingHints = 0;
        lastCodingErrors.clear();

        while (true) {
            String input = in.nextLine().trim().toLowerCase();
            switch (input) {
                case "q" -> { return CodingOutcome.QUIT; }
                case "s" -> { return CodingOutcome.SKIPPED; }
                case "g" -> {
                    Display.referenceSolution(q);
                    return CodingOutcome.GAVE_UP;
                }
                case "w" -> {
                    watchAndRetest(q);
                    Display.codingMenu();
                }
                case "r" -> {
                    try {
                        codingRunner.resetToStarter(q);
                        System.out.println("  File reset to the original starter code.");
                    } catch (IOException e) {
                        System.out.println("  Could not reset the file: " + e.getMessage());
                    }
                    Display.codingMenu();
                }
                case "h" -> {
                    if (noHelp) {
                        System.out.println("  " + Display.yellow()
                                + "No hints during an exam." + Display.reset());
                        Display.codingMenu();
                        break;
                    }
                    List<String> hints = q.getHints();
                    String hint = lastCodingHints < hints.size()
                            ? hints.get(lastCodingHints)
                            : llm.generateHint(q);
                    lastCodingHints++;
                    System.out.println("  Hint: " + Display.yellow() + hint + Display.reset());
                    Display.codingMenu();
                }
                case "e" -> {
                    if (noHelp) {
                        System.out.println("  " + Display.yellow()
                                + "No explanations during an exam." + Display.reset());
                    } else {
                        System.out.println("  " + Display.dim()
                                + llm.explainConcept(q.getTopic(), q.getPrompt()) + Display.reset());
                    }
                    Display.codingMenu();
                }
                default -> {   // Enter (or anything else) runs the tests
                    System.out.println("  Compiling and running tests…");
                    CodingResult result = codingRunner.run(q);
                    lastCodingErrors.addAll(result.errorKinds());
                    Display.codingResult(result);
                    if (result.passed()) return CodingOutcome.CORRECT;
                    if (result.status() == CodingResult.Status.ENVIRONMENT_ERROR) {
                        // not the student's fault — don't count it against them
                        return CodingOutcome.SKIPPED;
                    }
                    Display.codingMenu();
                }
            }
        }
    }

    /**
     * Watch mode: recompiles and retests whenever the student saves, so the loop is
     * "save in your editor, glance at the terminal" instead of alt-tabbing to press Enter.
     * Stops when the tests pass or the student presses Enter.
     */
    private void watchAndRetest(Question q) {
        System.out.println("  Watching " + codingRunner.workspaceDir(q).toAbsolutePath()
                + " — save to re-run. Press Enter to stop.");
        long lastRun = 0;
        try {
            while (true) {
                if (System.in.available() > 0) {
                    in.nextLine();
                    System.out.println("  Stopped watching.");
                    return;
                }
                long newest = newestModification(codingRunner.workspaceDir(q));
                if (newest > lastRun) {
                    lastRun = newest;
                    if (lastRun > 0) {
                        System.out.println("  Change detected — compiling…");
                        CodingResult result = codingRunner.run(q);
                        lastCodingErrors.addAll(result.errorKinds());
                        Display.codingResult(result);
                        if (result.passed()) {
                            System.out.println("  " + Display.green()
                                    + "Tests pass — press Enter to continue." + Display.reset());
                            return;
                        }
                    }
                }
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println("  Watch mode unavailable: " + e.getMessage());
        }
    }

    /** Most recent modification time across the exercise's .java files, or 0 when unreadable. */
    private static long newestModification(java.nio.file.Path dir) {
        try (var files = java.nio.file.Files.list(dir)) {
            return files.filter(f -> f.toString().endsWith(".java"))
                    .mapToLong(f -> {
                        try {
                            return java.nio.file.Files.getLastModifiedTime(f).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .max().orElse(0L);
        } catch (IOException e) {
            return 0L;
        }
    }

    /** Brief review pass over questions the student got wrong this session. */
    private void reviewWrongAnswers(List<Question> wrong) {
        Display.header("Review — Wrong Answers");
        System.out.println("  Take another shot at the questions you missed.");

        int correct = 0;
        for (int i = 0; i < wrong.size(); i++) {
            Question q = wrong.get(i);

            if (q.isCoding()) {
                CodingOutcome outcome = codingFlow(q, i + 1, wrong.size());
                if (outcome == CodingOutcome.QUIT) return;
                if (outcome == CodingOutcome.CORRECT) {
                    currentProfile.recordAnswer(q, true);
                    Display.correct(GradingResult.correct(q.getExplanation()));
                    correct++;
                } else if (outcome == CodingOutcome.GAVE_UP) {
                    currentProfile.recordAnswer(q, false);
                }
                saveProfile();
                continue;
            }

            Display.question(q, i + 1, wrong.size());

            String answer = null;
            while (answer == null) {
                String input = in.nextLine().trim();
                if (input.equalsIgnoreCase("q")) return;
                if (input.equalsIgnoreCase("s")) break;
                if (input.equalsIgnoreCase("h")) {
                    System.out.println("  Hint: " + Display.yellow() + llm.generateHint(q) + Display.reset());
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (!input.isBlank()) answer = collectAnswer(q, input);
                else System.out.print("  Your answer: ");
            }
            if (answer == null) continue;

            GradingResult result = grader.grade(q, answer);
            currentProfile.recordAnswer(q, result.correct());
            if (result.correct()) { Display.correct(result); correct++; }
            else                  Display.incorrect(result);
            System.out.print("\n  [Enter] next: ");
            in.nextLine();
        }

        saveProfile();
        System.out.printf("%n  Review complete: %d/%d correct.%n", correct, wrong.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean allTopicsMastered(List<Topic> topics) {
        for (Topic t : topics) {
            TopicPerformance p = currentProfile.getPerformance().get(t);
            if (p == null || p.getMasteryScore() < MASTERY_TARGET) return false;
        }
        return true;
    }
}
