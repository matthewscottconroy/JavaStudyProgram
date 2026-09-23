package com.studyprogram.ui;

import com.studyprogram.coding.CodingExerciseRunner;
import com.studyprogram.coding.CodingResult;
import com.studyprogram.core.*;
import com.studyprogram.grading.CompositeGrader;
import com.studyprogram.grading.Grader;
import com.studyprogram.llm.LLMService;
import com.studyprogram.model.*;
import com.studyprogram.report.HtmlReportGenerator;
import com.studyprogram.report.Worksheet;
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
    private static final int     MISTAKE_DECK_LIMIT     = 12;
    private static final int     DEFAULT_WORKSHEET_LENGTH = 15;

    private final QuestionBank   bank;
    private final ProfileStorage storage;
    private final LLMService     llm;
    private final Grader         grader;
    private final Scanner        in;
    private final CodingExerciseRunner codingRunner;

    private final StudyContext context;
    private final CodingFlow codingFlow;
    private final ExamFlow   examFlow;
    private StudentProfile currentProfile;
    private AttemptLog     attemptLog;
    /** Whether a generated report is opened in the system browser. Off under test. */
    private final boolean openReportsInBrowser;

    public CLI(QuestionBank bank, ProfileStorage storage, LLMService llm) {
        this(bank, storage, llm, System.in, new CodingExerciseRunner(), true);
    }

    /**
     * Full constructor. Taking the input stream and the exercise runner as parameters is what
     * makes the interactive flow testable: a test can script keystrokes and point the workspace
     * somewhere disposable, while output is captured from {@code System.out}.
     */
    public CLI(QuestionBank bank, ProfileStorage storage, LLMService llm,
               java.io.InputStream input, CodingExerciseRunner codingRunner) {
        // Scripted input means a test: it must not open the developer's browser.
        this(bank, storage, llm, input, codingRunner, false);
    }

    private CLI(QuestionBank bank, ProfileStorage storage, LLMService llm,
                java.io.InputStream input, CodingExerciseRunner codingRunner,
                boolean openReportsInBrowser) {
        this.bank         = bank;
        this.storage      = storage;
        this.llm          = llm;
        this.grader       = new CompositeGrader(llm);
        this.in           = new Scanner(input);
        this.codingRunner = codingRunner;
        this.openReportsInBrowser = openReportsInBrowser;
        this.context      = new StudyContext(bank, storage, llm, this.in, codingRunner,
                                             openReportsInBrowser);
        this.codingFlow   = new CodingFlow(context);
        this.examFlow     = new ExamFlow(context, codingFlow);
    }

    public void run() {
        Display.header(Messages.get("app.title"));
        System.out.println("  " + Messages.get("banner.questions", bank.totalQuestions()));
        // The label column is sized from the labels themselves so a translation with longer
        // words still lines up instead of running into its own values.
        List<String> labels = List.of(Messages.get("banner.ai"), Messages.get("banner.sandbox"),
                                      Messages.get("banner.coding"));
        int width = labels.stream().mapToInt(String::length).max().orElse(18) + 2;
        String row = "  %-" + width + "s %s%n";

        System.out.printf(row, Messages.get("banner.ai"),
                          (llm.isAvailable() ? Display.green() : Display.dim())
                                  + describeAi() + Display.reset());
        System.out.printf(row, Messages.get("banner.sandbox"),
                          Display.dim() + describeSandbox() + Display.reset());
        System.out.printf(row, Messages.get("banner.coding"),
                          CodingExerciseRunner.compilerAvailable()
                                  ? Display.green() + Messages.get("banner.codingOn") + Display.reset()
                                  : Display.yellow() + Messages.get("banner.codingOff") + Display.reset());

        for (String warning : bank.getWarnings()) {
            System.out.println("  " + Display.yellow() + "⚠ " + warning + Display.reset());
        }

        profileMenu();

        while (true) {
            System.out.println();
            examFlow.showGoalStatus();
            showReviewsDue();
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
                case "5" -> examFlow.examMode();
                case "6" -> progressReport();
                case "7" -> selectTopics();
                case "8" -> profileMenu();
                case "9" -> { saveProfile(); return; }
                default  -> System.out.println("  " + Messages.get("menu.invalidChoice"));
            }
        }
    }

    /**
     * The AI and sandbox status lines, worded here rather than in those layers.
     *
     * <p>They are the first thing a student reads, so they have to translate — and the classes
     * that know the facts have no business knowing about the interface's language. The UI asks
     * what the state is and says it in the student's own words.
     */
    private static String describeAi() {
        var config = com.studyprogram.llm.LLMConfig.load();
        if (config.apiKey() == null && !config.isLocalEndpoint()) {
            return Messages.get("banner.aiOff", config.apiKeyEnv());
        }
        return Messages.get("banner.aiOn", config.model(), config.baseUrl(),
                            config.maxCallsPerSession());
    }

    private static String describeSandbox() {
        return switch (com.studyprogram.coding.Sandbox.backend()) {
            case BWRAP -> Messages.get("banner.sandboxBwrap");
            case SANDBOX_EXEC -> Messages.get("banner.sandboxMac");
            case NONE -> System.getProperty("os.name", "").toLowerCase().contains("win")
                    ? Messages.get("banner.sandboxNoneWin")
                    : Messages.get("banner.sandboxNone");
        };
    }

    // ── Profile ───────────────────────────────────────────────────────────────    // ── Profile ───────────────────────────────────────────────────────────────

    private void profileMenu() {
        Display.header(Messages.get("profile.header"));
        try {
            List<String> profiles = storage.listProfileNames();
            reportStorageWarnings();
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
                if (pick.equalsIgnoreCase("P")) { placeExistingProfile(profiles); return; }
                if (!pick.equalsIgnoreCase("N")) {
                    try {
                        int idx = Integer.parseInt(pick) - 1;
                        if (idx >= 0 && idx < profiles.size()) {
                            String chosen = profiles.get(idx);
                            Optional<StudentProfile> loaded = storage.load(chosen);
                            reportStorageWarnings();
                            if (loaded.isEmpty()) {
                                // The file was unreadable and has been quarantined. Everything
                                // that profile ever answered is still in its attempt log.
                                loaded = offerRecovery(chosen);
                            }
                            if (loaded.isPresent()) {
                                setProfile(loaded.get());
                                currentProfile.applyDecay();
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
        setProfile(new StudentProfile(name));
        System.out.println("  " + Messages.get("profile.created", name));

        System.out.println();
        System.out.println("  " + Messages.get("placement.offer"));
        System.out.print("  " + Messages.get("placement.prompt") + " ");
        if (in.nextLine().trim().equalsIgnoreCase("y")) {
            placementCheck();
        } else {
            System.out.println("  " + Display.dim() + Messages.get("placement.declined")
                    + Display.reset());
        }
        selectTopics();
    }

    /** Surfaces anything the storage layer had to quarantine. */
    private void reportStorageWarnings() {
        if (!(storage instanceof com.studyprogram.storage.JsonProfileStorage json)) return;
        for (String warning : json.getWarnings()) {
            System.out.println("  " + Display.yellow() + Display.warnSign() + " " + warning
                    + Display.reset());
        }
    }

    /**
     * Offers to rebuild a profile whose file could not be read, by replaying its attempt log.
     *
     * <p>The log is append-only and written a line at a time, so it survives the kind of damage
     * that destroys a profile. Mastery is a function of that history, which means most of what
     * was lost can simply be recomputed.
     */
    private Optional<StudentProfile> offerRecovery(String name) {
        if (!com.studyprogram.storage.ProfileRecovery.canRecover(storage.directory(), name)) {
            System.out.println("  " + Display.dim()
                    + "There is no attempt log for that profile, so there is nothing to rebuild "
                    + "from." + Display.reset());
            return Optional.empty();
        }
        System.out.print("  Rebuild it from your attempt log? [Y/n]: ");
        if (in.nextLine().trim().equalsIgnoreCase("n")) return Optional.empty();

        var rebuilt = com.studyprogram.storage.ProfileRecovery.rebuild(storage.directory(), name);
        for (String line : wrapToWidth(
                com.studyprogram.storage.ProfileRecovery.summary(rebuilt))) {
            System.out.println("  " + line);
        }
        if (rebuilt.isEmpty()) return Optional.empty();

        try {
            storage.save(rebuilt.profile());
        } catch (IOException e) {
            System.out.println("  Could not save the rebuilt profile: " + e.getMessage());
        }
        return Optional.of(rebuilt.profile());
    }

    /** Loads a profile and re-runs the placement check on it, for a student who skipped it. */    /** Loads a profile and re-runs the placement check on it, for a student who skipped it. */
    private void placeExistingProfile(List<String> profiles) throws IOException {
        System.out.print("  Which profile? ");
        StudentProfile chosen = null;
        try {
            int idx = Integer.parseInt(in.nextLine().trim()) - 1;
            if (idx >= 0 && idx < profiles.size()) {
                chosen = storage.load(profiles.get(idx)).orElse(null);
            }
        } catch (NumberFormatException ignored) {}
        if (chosen == null) {
            System.out.println("  No such profile.");
            profileMenu();
            return;
        }
        setProfile(chosen);
        currentProfile.applyDecay();
        placementCheck();
    }

    /**
     * Runs the adaptive placement check and seeds the profile from it.
     *
     * <p>The walk climbs while the student clears each level and drops back when they do not, so
     * the questions land near the edge of what they know. Answers are logged like any others:
     * the student really did answer them, and pretending otherwise would corrupt the very history
     * the reports and the scheduler are built on.
     */
    private void placementCheck() {
        Display.header(Messages.get("placement.header"));
        System.out.println("  " + Messages.get("placement.intro", PlacementCheck.LENGTH));
        System.out.println("  " + Display.dim() + Messages.get("placement.noPenalty")
                + Display.reset());

        Random rng = new Random();
        List<PlacementCheck.Result> results = new ArrayList<>();
        Set<Integer> visited = new LinkedHashSet<>();
        int level = PlacementCheck.START_LEVEL;
        int asked = 0;
        boolean quit = false;

        while (level > 0 && asked < PlacementCheck.LENGTH && !quit) {
            visited.add(level);
            List<Question> probes = PlacementCheck.probesForLevel(bank, level, rng);
            int correctHere = 0;
            for (Question q : probes) {
                if (asked >= PlacementCheck.LENGTH) break;
                asked++;
                Display.question(q, asked, PlacementCheck.LENGTH);
                long qStart = System.nanoTime();
                String answer = in.nextLine().trim();
                if (answer.equalsIgnoreCase("q")) { quit = true; break; }
                // No hints and no "skip to the answer": the point is to find out what is known.
                if (answer.equalsIgnoreCase("h") || answer.equalsIgnoreCase("e")) {
                    System.out.println("  " + Display.yellow()
                            + Messages.get("placement.noHelp") + Display.reset());
                    answer = in.nextLine().trim();
                }
                boolean correct = grader.grade(q, answer).correct();
                if (correct) correctHere++;
                results.add(new PlacementCheck.Result(q, correct));
                logAttempt(q, correct ? AttemptRecord.OUTCOME_CORRECT
                                      : AttemptRecord.OUTCOME_INCORRECT, qStart, 0);
                System.out.println("  " + Display.dim() + Messages.get("placement.recorded")
                        + Display.reset());
            }
            if (quit) break;
            level = PlacementCheck.nextLevel(level, correctHere, visited);
        }

        PlacementCheck.Outcome outcome = PlacementCheck.apply(currentProfile, results);
        saveProfile();

        System.out.println();
        System.out.println("  " + Display.bold() + Messages.get("placement.result")
                + Display.reset());
        for (String line : wrapToWidth(PlacementCheck.summary(outcome))) {
            System.out.println("  " + line);
        }
    }

    /** Wraps a sentence to the terminal width used elsewhere. */
    private static List<String> wrapToWidth(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() > 0 && line.length() + word.length() > 70) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
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
        CourseOverlay course = examFlow.pickCourse(courses);

        System.out.println("  " + Display.bold() + course.getName() + Display.reset());
        for (CourseOverlay.Unit u : course.getUnits()) {
            System.out.printf("    %2d  %s%n", u.number(), u.title());
        }
        System.out.print("  Units to review (e.g. 3 or 1-4): ");
        int[] range = ExamFlow.parseRange(in.nextLine().trim());
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

    /**
     * "N reviews due today" on the menu.
     *
     * <p>Spaced repetition has been scheduling every question since the first session, but it
     * only ever showed up as a weighting inside the feed. Saying the number out loud is what
     * turns it into a reason to sit down.
     */
    private void showReviewsDue() {
        if (attemptLog == null) return;
        int due = ReviewScheduler.fromRecords(attemptLog.readAll())
                .dueCount(LocalDateTime.now());
        if (due == 0) return;
        System.out.println("  " + Display.cyan() + Messages.get("menu.reviewsDue", due)
                + Display.reset());
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
                CodingFlow.Outcome outcome = codingFlow.run(q, asked, quiz.size());
                if (outcome == CodingFlow.Outcome.QUIT) { asked--; break; }
                boolean beat = outcome == CodingFlow.Outcome.CORRECT;
                if (beat) correct++;
                currentProfile.recordAnswer(q, beat);
                logAttempt(q, beat ? AttemptRecord.OUTCOME_CORRECT : AttemptRecord.OUTCOME_INCORRECT,
                        codeStart, context.lastCodingHints);
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
                if (openReportsInBrowser && java.awt.Desktop.isDesktopSupported()
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

    /**
     * As above, also recording which misconception a wrong choice revealed.
     *
     * <p>Kept in the log for the same reason compile errors are: one wrong answer is noise, but
     * the same wrong idea showing up across a dozen questions is a thing worth telling a student.
     */
    private void logAttempt(Question q, String outcome, long startNano, int hintsUsed,
                            String studentAnswer) {
        if (attemptLog == null) return;
        long secs = Math.max(0, (System.nanoTime() - startNano) / 1_000_000_000L);
        AttemptRecord record = new AttemptRecord(LocalDateTime.now(), q, outcome, secs, hintsUsed);
        if (AttemptRecord.OUTCOME_INCORRECT.equals(outcome)) {
            q.misconceptionFor(studentAnswer).ifPresent(m -> record.setMisconception(m.name()));
        }
        attemptLog.append(record);
    }

    /** As above, also recording which compile errors the student hit on the way. */
    private void logAttempt(Question q, String outcome, long startNano, int hintsUsed,
                            Collection<String> compileErrors) {
        if (attemptLog == null) return;
        long secs = Math.max(0, (System.nanoTime() - startNano) / 1_000_000_000L);
        attemptLog.append(new AttemptRecord(LocalDateTime.now(), q, outcome, secs, hintsUsed,
                                            new ArrayList<>(compileErrors)));
    }

    /**
     * Makes this the profile every flow is working with.
     *
     * <p>The profile and its attempt log always change together, and they are read by screens
     * that no longer live in this class, so they are set in one place rather than at each call
     * site — which is how two of them would eventually disagree.
     */
    private void setProfile(StudentProfile profile) {
        this.currentProfile = profile;
        this.attemptLog = AttemptLog.forProfile(storage.directory(), profile.getName());
        context.setProfile(profile);
        context.setAttemptLog(this.attemptLog);
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
        MistakeDeck.Deck mistakes = mistakeDeck();
        if (!mistakes.isEmpty()) {
            System.out.println("\n  " + Display.yellow()
                    + Messages.get("session.mistakesWaiting", mistakes.questions().size())
                    + Display.reset());
        }
        System.out.print("\n  " + Messages.get("session.feedPrompt")
                + (mistakes.isEmpty() ? "" : "  " + Messages.get("session.mistakesOption"))
                + (canResume ? "  " + Messages.get("session.repeatOption",
                        Display.topicSummary(currentProfile.getLastSessionTopics())) : "")
                + "  [a]: ");
        String mode = in.nextLine().trim().toLowerCase();

        if (mode.equals("x")) {
            practiceMistakes(mistakes);
            return;
        }

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
        } else if (currentProfile.hasActiveGoal()) {
            // A goal overrides the ordinary frontier walk: the feed heads for the exam.
            active = GoalPlanner.focusTopics(currentProfile, 6);
            System.out.println("  Goal plan (" + currentProfile.getGoal().getTitle() + "): "
                    + Display.cyan()
                    + active.stream().map(t -> t.displayName).collect(Collectors.joining(", "))
                    + Display.reset());
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

    /** The questions this student got wrong and has not since got right. */
    private MistakeDeck.Deck mistakeDeck() {
        if (attemptLog == null) return new MistakeDeck.Deck(List.of(), 0, List.of());
        return MistakeDeck.build(bank, attemptLog.readAll(), MISTAKE_DECK_LIMIT);
    }

    /**
     * Practises exactly the questions the student has got wrong, plus the exercises where the
     * compile errors they keep hitting actually bit them.
     */
    private void practiceMistakes(MistakeDeck.Deck deck) {
        if (deck.isEmpty()) {
            System.out.println("  Nothing outstanding — every question you have missed, you have "
                    + "since got right.");
            return;
        }

        StringBuilder intro = new StringBuilder();
        if (deck.unresolvedCount() > 0) {
            intro.append(deck.unresolvedCount()).append(" question")
                 .append(deck.unresolvedCount() == 1 ? "" : "s")
                 .append(" you missed and have not got right since");
        }
        if (!deck.recurringErrors().isEmpty()) {
            if (intro.length() > 0) intro.append(", then ");
            intro.append("the exercises where you hit ")
                 .append(String.join(", ", deck.recurringErrors()));
        }
        intro.append('.');

        practiceQuestions("Practice — Your Mistakes", intro.toString(), deck.questions());

        if (!deck.recurringErrors().isEmpty()) {
            System.out.println();
            System.out.println("  " + Display.dim() + "Errors worth watching for as you type:"
                    + Display.reset());
            for (String kind : deck.recurringErrors()) {
                System.out.println("    " + Display.bold() + kind + Display.reset() + " — "
                        + com.studyprogram.coding.CompilerErrorDecoder.explanationFor(kind));
            }
        }
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
                CodingFlow.Outcome outcome = codingFlow.run(q, qNum, displayTotal);
                switch (outcome) {
                    case QUIT -> quit = true;
                    case SKIPPED -> {
                        session.skip(q);
                        logAttempt(q, AttemptRecord.OUTCOME_SKIPPED, qStart, context.lastCodingHints, context.lastCodingErrors);
                        System.out.println("  " + Messages.get("session.skipped"));
                    }
                    case CORRECT -> {
                        GradingResult r = GradingResult.correct(q.getExplanation());
                        session.recordAnswer(q, r);
                        logAttempt(q, AttemptRecord.OUTCOME_CORRECT, qStart, context.lastCodingHints, context.lastCodingErrors);
                        saveProfile();
                        Display.correct(r);
                    }
                    case GAVE_UP -> {
                        GradingResult r = GradingResult.incorrect(
                                "Recorded as incorrect — study the reference solution and it "
                                + "will come around again.", q.getExplanation());
                        session.recordAnswer(q, r);
                        logAttempt(q, AttemptRecord.OUTCOME_INCORRECT, qStart, context.lastCodingHints, context.lastCodingErrors);
                        saveProfile();
                    }
                }
                if (quit) break;
                if (outcome == CodingFlow.Outcome.CORRECT || outcome == CodingFlow.Outcome.GAVE_UP) {
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
                    // Pass how many have already been taken, so pressing h again moves on
                    // instead of repeating the first hint.
                    String hint = llm.generateHint(q, hintsUsed);
                    hintsUsed++;
                    System.out.println("  Hint: " + Display.yellow() + hint + Display.reset());
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
                                           : AttemptRecord.OUTCOME_INCORRECT, qStart, hintsUsed,
                       answer);
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


    /** Brief review pass over questions the student got wrong this session. */
    private void reviewWrongAnswers(List<Question> wrong) {
        practiceQuestions("Review — Wrong Answers",
                "Take another shot at the questions you missed.", wrong);
    }

    /**
     * Works through an explicit list of questions, in order.
     *
     * <p>Distinct from a study session, which asks the adaptive engine what to serve next: here the
     * caller has already decided, because these specific questions are the point. Every answer is
     * appended to the attempt log like any other — without that, re-answering a question correctly
     * here would never resolve the mistake that put it in front of the student.
     */
    private void practiceQuestions(String header, String intro, List<Question> questions) {
        Display.header(header);
        System.out.println("  " + intro);

        int correct = 0, answered = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            long qStart = System.nanoTime();

            if (q.isCoding()) {
                CodingFlow.Outcome outcome = codingFlow.run(q, i + 1, questions.size());
                if (outcome == CodingFlow.Outcome.QUIT) break;
                if (outcome == CodingFlow.Outcome.CORRECT) {
                    currentProfile.recordAnswer(q, true);
                    logAttempt(q, AttemptRecord.OUTCOME_CORRECT, qStart, context.lastCodingHints,
                               context.lastCodingErrors);
                    Display.correct(GradingResult.correct(q.getExplanation()));
                    correct++;
                    answered++;
                } else if (outcome == CodingFlow.Outcome.GAVE_UP) {
                    currentProfile.recordAnswer(q, false);
                    logAttempt(q, AttemptRecord.OUTCOME_INCORRECT, qStart, context.lastCodingHints,
                               context.lastCodingErrors);
                    answered++;
                } else {
                    logAttempt(q, AttemptRecord.OUTCOME_SKIPPED, qStart, context.lastCodingHints,
                               context.lastCodingErrors);
                }
                saveProfile();
                continue;
            }

            Display.question(q, i + 1, questions.size());

            String answer = null;
            boolean quit = false;
            int hintsTaken = 0;
            while (answer == null) {
                String input = in.nextLine().trim();
                if (input.equalsIgnoreCase("q")) { quit = true; break; }
                if (input.equalsIgnoreCase("s")) {
                    logAttempt(q, AttemptRecord.OUTCOME_SKIPPED, qStart, 0);
                    break;
                }
                if (input.equalsIgnoreCase("h")) {
                    String hint = llm.generateHint(q, hintsTaken++);
                    System.out.println("  Hint: " + Display.yellow() + hint + Display.reset());
                    System.out.print("  Your answer: ");
                    continue;
                }
                if (!input.isBlank()) answer = collectAnswer(q, input);
                else System.out.print("  Your answer: ");
            }
            if (quit) break;
            if (answer == null) continue;

            GradingResult result = grader.grade(q, answer);
            currentProfile.recordAnswer(q, result.correct());
            logAttempt(q, result.correct() ? AttemptRecord.OUTCOME_CORRECT
                                           : AttemptRecord.OUTCOME_INCORRECT, qStart, 0, answer);
            answered++;
            if (result.correct()) { Display.correct(result); correct++; }
            else                  Display.incorrect(result);
            System.out.print("\n  [Enter] next: ");
            in.nextLine();
        }

        saveProfile();
        if (answered == 0) System.out.printf("%n  Nothing answered.%n");
        else System.out.printf("%n  %d/%d correct.%n", correct, answered);
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
