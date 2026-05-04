package com.studyprogram.ui;

import com.studyprogram.core.*;
import com.studyprogram.grading.CompositeGrader;
import com.studyprogram.grading.Grader;
import com.studyprogram.llm.LLMService;
import com.studyprogram.model.*;
import com.studyprogram.storage.ProfileStorage;

import java.io.IOException;
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

    private StudentProfile currentProfile;

    public CLI(QuestionBank bank, ProfileStorage storage, LLMService llm) {
        this.bank    = bank;
        this.storage = storage;
        this.llm     = llm;
        this.grader  = new CompositeGrader(llm);
        this.in      = new Scanner(System.in);
    }

    public void run() {
        Display.header("Java Study Program");
        System.out.printf("  Questions in bank: %d%n", bank.totalQuestions());
        System.out.printf("  LLM support:       %s%n",
                          llm.isAvailable() ? Display.GREEN + "enabled" + Display.RESET
                                            : Display.DIM   + "disabled (set ANTHROPIC_API_KEY)" + Display.RESET);

        profileMenu();

        while (true) {
            System.out.println();
            System.out.println("  [1] Start Study Session");
            System.out.println("  [2] View Performance");
            System.out.println("  [3] Select Topics");
            System.out.println("  [4] Switch Profile");
            System.out.println("  [5] Exit");
            System.out.print("\n  Choice: ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1" -> studySession();
                case "2" -> Display.performanceTable(currentProfile.getPerformance(),
                                                      currentProfile.getSelectedTopicsList());
                case "3" -> selectTopics();
                case "4" -> profileMenu();
                case "5" -> { saveProfile(); return; }
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
        System.out.printf("  Profile created for %s.%n", name);
        selectTopics();
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
                boolean locked = !t.prerequisites.isEmpty() && !t.isUnlocked(perf);
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
            selected.addAll(Arrays.asList(allTopics));
        } else if (!input.equalsIgnoreCase("done")) {
            for (String part : input.split(",")) {
                try {
                    int num = Integer.parseInt(part.trim());
                    Topic t = indexMap.get(num);
                    if (t != null) {
                        if (selected.contains(t)) {
                            selected.remove(t);
                        } else {
                            if (!t.prerequisites.isEmpty() && !t.isUnlocked(perf)) {
                                String prereqs = t.prerequisites.stream()
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
        List<Topic> active = currentProfile.getSelectedTopicsList();
        if (active.isEmpty()) {
            System.out.println("  No topics selected. Please select topics first.");
            selectTopics();
            active = currentProfile.getSelectedTopicsList();
            if (active.isEmpty()) return;
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

        AdaptiveEngine engine  = new AdaptiveEngine(bank, llm);
        StudySession   session = new StudySession(currentProfile, active, engine, sessionLen);

        Display.header("Study Session — " + Display.topicSummary(active));

        int qNum = 0;
        boolean quit = false;

        while (!quit) {
            Optional<Question> next = session.nextQuestion();
            if (next.isEmpty()) break;

            Question q = next.get();
            qNum++;
            Display.question(q, qNum, sessionLen > 0 ? sessionLen : qNum);

            // Pre-answer command loop — student can request hints/explains before answering
            String answer = null;
            while (answer == null) {
                String input = in.nextLine().trim();

                if (input.equalsIgnoreCase("q")) { quit = true; break; }
                if (input.equalsIgnoreCase("s")) {
                    session.skip();
                    System.out.println("  Skipped.");
                    answer = null;
                    break;
                }
                if (input.equalsIgnoreCase("h")) {
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

    /** Brief review pass over questions the student got wrong this session. */
    private void reviewWrongAnswers(List<Question> wrong) {
        Display.header("Review — Wrong Answers");
        System.out.println("  Take another shot at the questions you missed.");

        int correct = 0;
        for (int i = 0; i < wrong.size(); i++) {
            Question q = wrong.get(i);
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
