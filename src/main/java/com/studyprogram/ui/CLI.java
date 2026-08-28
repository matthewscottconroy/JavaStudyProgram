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

    private final QuestionBank   bank;
    private final ProfileStorage storage;
    private final LLMService     llm;
    private final Grader         grader;
    private final Scanner        in;
    private final CodingExerciseRunner codingRunner;

    private StudentProfile currentProfile;
    private AttemptLog     attemptLog;
    private int            lastCodingHints;   // hints used in the most recent codingFlow run

    public CLI(QuestionBank bank, ProfileStorage storage, LLMService llm) {
        this.bank         = bank;
        this.storage      = storage;
        this.llm          = llm;
        this.grader       = new CompositeGrader(llm);
        this.in           = new Scanner(System.in);
        this.codingRunner = new CodingExerciseRunner();
    }

    public void run() {
        Display.header("Java Study Program");
        System.out.printf("  Questions in bank: %d%n", bank.totalQuestions());
        System.out.printf("  LLM support:       %s%n",
                          llm.isAvailable() ? Display.GREEN + "enabled" + Display.RESET
                                            : Display.DIM   + "disabled (set ANTHROPIC_API_KEY)" + Display.RESET);
        System.out.printf("  Coding exercises:  %s%n",
                          CodingExerciseRunner.compilerAvailable()
                                  ? Display.GREEN + "enabled" + Display.RESET
                                  : Display.YELLOW + "disabled — run with a full JDK (not a JRE) to "
                                    + "compile and test real programs" + Display.RESET);

        for (String warning : bank.getWarnings()) {
            System.out.println("  " + Display.YELLOW + "⚠ " + warning + Display.RESET);
        }

        profileMenu();

        while (true) {
            System.out.println();
            System.out.println("  [1] Start Study Session");
            System.out.println("  [2] View Performance");
            System.out.println("  [3] Concept Map");
            System.out.println("  [4] Boss Challenge");
            System.out.println("  [5] Progress Report (HTML)");
            System.out.println("  [6] Select Topics");
            System.out.println("  [7] Switch Profile");
            System.out.println("  [8] Exit");
            System.out.print("\n  Choice: ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1" -> studySession();
                case "2" -> Display.performanceTable(currentProfile.getPerformance(),
                                                      currentProfile.getSelectedTopicsList());
                case "3" -> conceptMap();
                case "4" -> bossChallenge();
                case "5" -> progressReport();
                case "6" -> selectTopics();
                case "7" -> profileMenu();
                case "8" -> { saveProfile(); return; }
                default  -> System.out.println("  Invalid choice.");
            }
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    private void profileMenu() {
        Display.header("Profile");
        try {
            List<String> profiles = storage.listProfileNames();
            if (!profiles.isEmpty()) {
                System.out.println("  Existing profiles:");
                for (int i = 0; i < profiles.size(); i++) {
                    System.out.printf("    [%d] %s%n", i + 1, profiles.get(i));
                }
                System.out.println("    [N] New profile");
                System.out.print("\n  Choose or 'N': ");
                String pick = in.nextLine().trim();
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
                                System.out.printf("  Welcome back, %s!%n", currentProfile.getName());
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

    private void createProfile() {
        System.out.print("  Enter your name: ");
        String name = in.nextLine().trim();
        if (name.isBlank()) name = "Student";
        currentProfile = new StudentProfile(name);
        attemptLog = AttemptLog.forProfile(storage.directory(), name);
        System.out.printf("  Profile created for %s.%n", name);
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
                System.out.println("  " + Display.YELLOW + "⚠ " + w + Display.RESET));

        System.out.println("  " + Display.BOLD + course.getName() + Display.RESET);
        for (CourseOverlay.Unit u : course.getUnits()) {
            System.out.printf("    %2d  %s%n", u.number(), u.title());
        }
        System.out.print("  Units to review (e.g. 3 or 1-4): ");
        String range = in.nextLine().trim();
        int from, to;
        try {
            if (range.contains("-")) {
                String[] parts = range.split("-", 2);
                from = Integer.parseInt(parts[0].trim());
                to   = Integer.parseInt(parts[1].trim());
            } else {
                from = to = Integer.parseInt(range);
            }
        } catch (NumberFormatException e) {
            return List.of();
        }
        List<Topic> topics = course.topicsForUnits(from, to);
        if (topics.isEmpty()) {
            System.out.println("  Those units cover no topics.");
        } else {
            System.out.println("  Reviewing: " + Display.CYAN + Display.topicSummary(topics)
                    + Display.RESET);
        }
        return topics;
    }

    /**
     * World boss fights: a 10-question, no-hints quiz across one level band's topics.
     * Clearing one (80%+) is recorded on the profile and shown on the concept map.
     */
    private void bossChallenge() {
        Display.header("Boss Challenges");
        String[] worldNames = {"", "Foundations", "Elementary", "Intermediate", "Advanced", "Expert"};
        for (int level = 1; level <= 5; level++) {
            String status;
            if (BossChallenge.cleared(currentProfile, level)) {
                status = Display.GREEN + "CLEARED ★" + Display.RESET;
            } else if (BossChallenge.unlocked(currentProfile, level)) {
                status = Display.CYAN + "READY — face the boss!" + Display.RESET;
            } else {
                status = Display.DIM + "locked (raise the world's average mastery to "
                        + (int) (BossChallenge.UNLOCK_AVG_MASTERY * 100) + "%)" + Display.RESET;
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
            Display.question(q, asked, quiz.size());
            long qStart = System.nanoTime();
            String answer = in.nextLine().trim();
            if (answer.equalsIgnoreCase("q")) {
                System.out.println("  You fled the boss fight!");
                asked--;
                break;
            }
            if (answer.equalsIgnoreCase("h") || answer.equalsIgnoreCase("e")) {
                System.out.println("  " + Display.YELLOW + "No help during a boss fight!"
                        + Display.RESET + " Your answer counts as given:");
                answer = in.nextLine().trim();
            }
            GradingResult result = grader.grade(q, answer.equalsIgnoreCase("s") ? "" : answer);
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
            System.out.println("  " + Display.GREEN + Display.BOLD
                    + "★ WORLD " + level + " CLEARED! ★" + Display.RESET
                    + "  It now shows on your concept map.");
        } else if (asked == quiz.size()) {
            System.out.println("  The boss survives… study up and challenge it again "
                    + "(the questions change each attempt).");
        }
        saveProfile();
    }

    /** Opens the Swing overworld map (non-blocking; selections there update the profile). */
    private void conceptMap() {
        boolean opened = com.studyprogram.ui.map.OverworldFrame.open(currentProfile, this::saveProfile);
        if (opened) {
            System.out.println("  Map window opened. Click topics to add or remove them from "
                    + "your session topic list — changes save automatically.");
        } else {
            System.out.println("  No display available (headless environment) — "
                    + "use [5] Select Topics instead.");
        }
    }

    /** Generates the self-contained HTML progress report and tries to open it. */
    private void progressReport() {
        try {
            String safe = currentProfile.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
            Path out = storage.directory().resolveSibling("reports").resolve(safe + "-progress.html");
            QuestionCalibration calibration = QuestionCalibration.fromRecords(
                    AttemptLog.readAllInDirectory(storage.directory()));
            List<String> flagged = calibration.flaggedForReview(
                    bank.getQuestionsForTopics(List.of(Topic.values())));
            Path written = new HtmlReportGenerator()
                    .generate(currentProfile, attemptLog.readAll(), flagged, out);
            System.out.println("  Report written to: " + Display.CYAN
                    + written.toAbsolutePath() + Display.RESET);
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

    /** Appends one attempt to the append-only log (timings in whole seconds). */
    private void logAttempt(Question q, String outcome, long startNano, int hintsUsed) {
        if (attemptLog == null) return;
        long secs = Math.max(0, (System.nanoTime() - startNano) / 1_000_000_000L);
        attemptLog.append(new AttemptRecord(LocalDateTime.now(), q, outcome, secs, hintsUsed));
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
        Topic[] allTopics = Topic.values();
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

                String color  = locked ? Display.DIM : (sel ? Display.CYAN : "");
                String marker = sel    ? " ●" : "";
                String lock   = locked ? Display.RED + " [LOCKED]" + Display.RESET : "";
                System.out.printf("  [%2d] %s%-40s%s%s  %3d%%%n",
                        idx, color, t.displayName + marker, Display.RESET, lock, pct);
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
        System.out.print("\n  Feed: [a] auto — follows the concept map (recommended)  "
                + "[m] my selected topics  [u] course unit review  [a]: ");
        String mode = in.nextLine().trim().toLowerCase();

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
            System.out.println("  Auto plan: " + Display.CYAN
                    + active.stream().map(t -> t.displayName).collect(Collectors.joining(", "))
                    + Display.RESET);
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

        // Calibrate question difficulty from every profile's attempt history on this
        // machine — real student data gradually corrects the authored difficulty labels
        QuestionCalibration calibration = QuestionCalibration.fromRecords(
                AttemptLog.readAllInDirectory(storage.directory()));
        AdaptiveEngine engine  = new AdaptiveEngine(bank, new Random(), llm, calibration);
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
                        logAttempt(q, AttemptRecord.OUTCOME_SKIPPED, qStart, lastCodingHints);
                        System.out.println("  Skipped.");
                    }
                    case CORRECT -> {
                        GradingResult r = GradingResult.correct(q.getExplanation());
                        session.recordAnswer(q, r);
                        logAttempt(q, AttemptRecord.OUTCOME_CORRECT, qStart, lastCodingHints);
                        saveProfile();
                        Display.correct(r);
                    }
                    case GAVE_UP -> {
                        GradingResult r = GradingResult.incorrect(
                                "Recorded as incorrect — study the reference solution and it "
                                + "will come around again.", q.getExplanation());
                        session.recordAnswer(q, r);
                        logAttempt(q, AttemptRecord.OUTCOME_INCORRECT, qStart, lastCodingHints);
                        saveProfile();
                    }
                }
                if (quit) break;
                if (outcome == CodingOutcome.CORRECT || outcome == CodingOutcome.GAVE_UP) {
                    System.out.print("\n  [Enter] next  [q] quit: ");
                    if (in.nextLine().trim().equalsIgnoreCase("q")) break;
                }
                if (masteryMode && allTopicsMastered(active)) {
                    System.out.println("\n  " + Display.GREEN + Display.BOLD
                            + "All topics mastered! Session complete." + Display.RESET);
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
                    System.out.println("  Skipped.");
                    answer = null;
                    break;
                }
                if (input.equalsIgnoreCase("h")) {
                    hintsUsed++;
                    System.out.println("  Hint: " + Display.YELLOW + llm.generateHint(q) + Display.RESET);
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (input.equalsIgnoreCase("e")) {
                    System.out.println("  " + Display.DIM
                            + llm.explainConcept(q.getTopic(), q.getPrompt()) + Display.RESET);
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (!input.isBlank()) {
                    answer = input;
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
                System.out.println("  " + Display.DIM
                        + llm.explainConcept(q.getTopic(), q.getPrompt()) + Display.RESET);
            }

            // Mastery mode: stop when all active topics hit the target
            if (masteryMode && allTopicsMastered(active)) {
                System.out.println("\n  " + Display.GREEN + Display.BOLD
                        + "All topics mastered! Session complete." + Display.RESET);
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

        while (true) {
            String input = in.nextLine().trim().toLowerCase();
            switch (input) {
                case "q" -> { return CodingOutcome.QUIT; }
                case "s" -> { return CodingOutcome.SKIPPED; }
                case "g" -> {
                    Display.referenceSolution(q);
                    return CodingOutcome.GAVE_UP;
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
                    List<String> hints = q.getHints();
                    String hint = lastCodingHints < hints.size()
                            ? hints.get(lastCodingHints)
                            : llm.generateHint(q);
                    lastCodingHints++;
                    System.out.println("  Hint: " + Display.YELLOW + hint + Display.RESET);
                    Display.codingMenu();
                }
                case "e" -> {
                    System.out.println("  " + Display.DIM
                            + llm.explainConcept(q.getTopic(), q.getPrompt()) + Display.RESET);
                    Display.codingMenu();
                }
                default -> {   // Enter (or anything else) runs the tests
                    System.out.println("  Compiling and running tests…");
                    CodingResult result = codingRunner.run(q);
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
                    System.out.println("  Hint: " + Display.YELLOW + llm.generateHint(q) + Display.RESET);
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (!input.isBlank()) answer = input;
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
