package com.studyprogram.ui;

import com.studyprogram.core.CourseOverlay;
import com.studyprogram.core.ExamSession;
import com.studyprogram.core.GoalPlanner;
import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.GradingResult;
import com.studyprogram.model.Question;
import com.studyprogram.model.StudyGoal;
import com.studyprogram.model.Topic;
import com.studyprogram.report.Worksheet;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Exams, printable worksheets, and the goal they are usually aimed at.
 *
 * <p>These three belong together: a goal is the exam a student intends to sit, an exam is the
 * rehearsal for it, and a worksheet is the same paper on paper. They share the section chooser
 * that turns a course overlay into a set of units, which is why splitting them apart would mean
 * duplicating it.
 *
 * <p>Second flow lifted out of {@link CLI} after {@link CodingFlow}. State lives on
 * {@link StudyContext}; coding questions inside an exam are handed to the coding screen, which is
 * where the exam's no-help rule has to reach.
 */
class ExamFlow {

    private static final int DEFAULT_EXAM_LENGTH = 12;
    private static final int DEFAULT_WORKSHEET_LENGTH = 15;

    private final StudyContext ctx;
    private final CodingFlow codingFlow;

    ExamFlow(StudyContext ctx, CodingFlow codingFlow) {
        this.ctx = ctx;
        this.codingFlow = codingFlow;
    }

    /**
     * A timed, mixed exam scored against the course's own units.
     *
     * <p>Everything else in the program is designed to help the student succeed right now: the
     * feed picks what they are ready for, hints are a keypress away, and a missed question comes
     * back later. That is good practice and a bad prediction. This is the honest rehearsal —
     * fixed paper, clock running, no help — and its value is entirely in the report at the end,
     * which says which units are solid and which are not with a week still left to fix them.
     */
    void examMode() {
        Display.header("Exam Mode");
        System.out.println("  A timed paper across several units. No hints, no explanations, "
                + "no second attempts.");
        System.out.println("  " + Display.dim()
                + "Nothing here is gated on the result — it is a rehearsal, not a gate."
                + Display.reset());

        System.out.println();
        if (ctx.profile().hasActiveGoal()) {
            showGoalStatus();
            System.out.print("  [Enter] sit a practice paper  [g] change the goal  [c] clear it"
                    + "  [w] print a worksheet: ");
        } else {
            System.out.println("  " + Display.dim() + "No goal set. A goal is an exam date and "
                    + "the units it covers: the feed then heads for it, and the menu shows "
                    + "whether you are on pace." + Display.reset());
            System.out.print("  [Enter] sit a practice paper  [g] set a goal"
                    + "  [w] print a worksheet: ");
        }
        String choice = ctx.in.nextLine().trim().toLowerCase();
        if (choice.equals("g")) { setGoal(); return; }
        if (choice.equals("w")) { printWorksheet(); return; }
        if (choice.equals("c")) {
            ctx.profile().setGoal(null);
            ctx.saveProfile();
            System.out.println("  Goal cleared.");
            return;
        }

        List<ExamSession.Section> sections = chooseSections("examine");
        if (sections == null) return;

        System.out.print("  How many questions [" + DEFAULT_EXAM_LENGTH + "]: ");
        int count = DEFAULT_EXAM_LENGTH;
        String lenInput = ctx.in.nextLine().trim();
        if (!lenInput.isBlank()) {
            try { count = Math.max(1, Integer.parseInt(lenInput)); }
            catch (NumberFormatException ignored) {}
        }

        // Seeded by profile and attempt count so a retake is a different paper, while an
        // instructor handing out a fixed seed gets the same paper for everyone.
        Random rng = new Random(ctx.profile().getId().hashCode() * 31L
                + ctx.profile().getTotalQuestionsAnswered());
        List<ExamSession.Item> paper = ExamSession.build(ctx.bank, sections, count, rng);
        if (paper.isEmpty()) {
            System.out.println("  No questions available for those units.");
            return;
        }

        int minutes = ExamSession.suggestedMinutes(paper);
        System.out.printf("%n  %d questions · %d sections · %d minutes.%n",
                paper.size(), sections.size(), minutes);
        System.out.print("  Start the clock? [y/N]: ");
        if (!ctx.in.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("  Cancelled — nothing was recorded.");
            return;
        }

        sitExam(paper, minutes);
    }

    /** One line about the goal, when there is one. */
    void showGoalStatus() {
        if (!ctx.profile().hasActiveGoal()) return;
        GoalPlanner.Plan plan = GoalPlanner.plan(ctx.profile(), LocalDateTime.now().toLocalDate());
        if (plan == null) return;
        String color = plan.isDone() ? Display.green() : plan.onTrack() ? Display.cyan() : Display.yellow();
        System.out.println("  " + color + "Goal: " + GoalPlanner.summary(plan) + Display.reset());
    }

    /**
     * Sets the goal: a name, a date, and the units (or worlds) it covers. The scope reuses the
     * exam's section chooser, so a goal is literally "the exam I will sit", and the feed and the
     * menu start working toward it immediately.
     */
    private void setGoal() {
        System.out.print("  What is it? [Exam]: ");
        String title = ctx.in.nextLine().trim();
        if (title.isBlank()) title = "Exam";

        java.time.LocalDate date = null;
        while (date == null) {
            System.out.print("  When? (YYYY-MM-DD, or a number of days from now; Enter to cancel): ");
            String when = ctx.in.nextLine().trim();
            if (when.isBlank()) { System.out.println("  Cancelled."); return; }
            try {
                date = when.matches("\\d{1,3}")
                        ? LocalDateTime.now().toLocalDate().plusDays(Integer.parseInt(when))
                        : java.time.LocalDate.parse(when);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("  That is not a date I understand.");
            }
            if (date != null && date.isBefore(LocalDateTime.now().toLocalDate())) {
                System.out.println("  That date has already passed.");
                date = null;
            }
        }

        List<ExamSession.Section> sections = chooseSections("prepare for");
        if (sections == null) return;
        List<Topic> topics = new ArrayList<>();
        for (ExamSession.Section section : sections) {
            for (Topic t : section.topics()) if (!topics.contains(t)) topics.add(t);
        }
        String scope = sections.size() == 1 ? sections.get(0).title()
                : sections.get(0).title() + " to " + sections.get(sections.size() - 1).title();

        ctx.profile().setGoal(new StudyGoal(title, scope, date, topics));
        ctx.saveProfile();
        System.out.println();
        showGoalStatus();
        System.out.println("  " + Display.dim() + "The auto feed now heads for it. Exam Mode "
                + "will rehearse exactly these units." + Display.reset());
    }

    /**
     * Asks which units (or worlds, without a course overlay) to cover, returning the sections
     * or null when the student backs out.
     */
    private List<ExamSession.Section> chooseSections(String verb) {
        List<CourseOverlay> courses = CourseOverlay.loadAll(Path.of("data", "courses"));
        List<ExamSession.Section> sections;
        if (courses.isEmpty()) {
            System.out.println();
            System.out.println("  " + Display.dim() + "No course file in data/courses/, so units "
                    + "are the map's worlds instead of syllabus units." + Display.reset());
            System.out.print("  Worlds to " + verb + " (e.g. 2 or 1-3): ");
            int[] range = parseRange(ctx.in.nextLine().trim());
            if (range == null) { System.out.println("  Cancelled."); return null; }
            sections = ExamSession.worldSections(range[0], range[1]);
        } else {
            CourseOverlay course = pickCourse(courses);
            System.out.println("  " + Display.bold() + course.getName() + Display.reset());
            for (CourseOverlay.Unit u : course.getUnits()) {
                System.out.printf("    %2d  %s%n", u.number(), u.title());
            }
            System.out.print("  Units to " + verb + " (e.g. 3 or 1-4): ");
            int[] range = parseRange(ctx.in.nextLine().trim());
            if (range == null) { System.out.println("  Cancelled."); return null; }
            sections = ExamSession.sectionsFor(course, range[0], range[1]);
        }
        if (sections.isEmpty()) {
            System.out.println("  That range covers no topics.");
            return null;
        }
        return sections;
    }

    /**
     * Writes a worksheet and its answer key to disk, for working on paper.
     *
     * <p>The same builder that draws an exam draws the worksheet, so it is balanced across the
     * units the same way — but it produces two files, and the worksheet half carries no answers,
     * so it can be handed to a class.
     */
    private void printWorksheet() {
        List<ExamSession.Section> sections = chooseSections("cover");
        if (sections == null) return;

        System.out.print("  How many questions [" + DEFAULT_WORKSHEET_LENGTH + "]: ");
        int count = DEFAULT_WORKSHEET_LENGTH;
        String lenInput = ctx.in.nextLine().trim();
        if (!lenInput.isBlank()) {
            try { count = Math.max(1, Integer.parseInt(lenInput)); }
            catch (NumberFormatException ignored) {}
        }

        System.out.print("  Title [Java Practice]: ");
        String title = ctx.in.nextLine().trim();
        if (title.isBlank()) title = "Java Practice";

        List<ExamSession.Item> paper = ExamSession.build(
                ctx.bank, sections, count, new Random(), 0.0, Worksheet.SUITS_PAPER);
        if (paper.isEmpty()) {
            System.out.println("  No questions available for those units.");
            return;
        }

        try {
            Worksheet.Pair pair = Worksheet.render(title, paper);
            String safe = title.replaceAll("[^A-Za-z0-9._-]", "_");
            Path dir = ctx.storage.directory().resolveSibling("worksheets");
            java.nio.file.Files.createDirectories(dir);
            Path sheet = dir.resolve(safe + ".html");
            Path key   = dir.resolve(safe + "-answers.html");
            java.nio.file.Files.writeString(sheet, pair.worksheet());
            java.nio.file.Files.writeString(key, pair.answerKey());

            System.out.println();
            System.out.println("  " + paper.size() + " questions across " + sections.size()
                    + " section(s).");
            System.out.println("  Worksheet:  " + Display.cyan() + sheet.toAbsolutePath()
                    + Display.reset());
            System.out.println("  Answer key: " + Display.cyan() + key.toAbsolutePath()
                    + Display.reset());
            System.out.println("  " + Display.dim()
                    + "Open either in a browser and print. The worksheet has no answers on it."
                    + Display.reset());
        } catch (IOException e) {
            System.out.println("  Could not write the worksheet: " + e.getMessage());
        }
    }

    /** Runs the paper against the clock and reports the result. */
    private void sitExam(List<ExamSession.Item> paper, int minutes) {
        long deadline = System.nanoTime() + minutes * 60L * 1_000_000_000L;
        Set<String> correctIds = new LinkedHashSet<>();
        Set<String> attemptedIds = new LinkedHashSet<>();
        String lastSection = null;
        boolean ranOut = false;
        ctx.noHelp = true;

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
                    CodingFlow.Outcome outcome = codingFlow.run(q, i + 1, paper.size());
                    if (outcome == CodingFlow.Outcome.QUIT) break;
                    attemptedIds.add(q.getId());
                    boolean ok = outcome == CodingFlow.Outcome.CORRECT;
                    if (ok) correctIds.add(q.getId());
                    ctx.profile().recordAnswer(q, ok);
                    ctx.logAttempt(q, ok ? AttemptRecord.OUTCOME_CORRECT : AttemptRecord.OUTCOME_INCORRECT,
                            qStart, 0, ctx.lastCodingErrors);
                    continue;
                }

                Display.question(q, i + 1, paper.size());
                String answer = ctx.in.nextLine().trim();
                if (answer.equalsIgnoreCase("q")) break;
                if (answer.equalsIgnoreCase("h") || answer.equalsIgnoreCase("e")) {
                    System.out.println("  " + Display.yellow() + "No help during an exam."
                            + Display.reset() + " Answer as best you can:");
                    answer = ctx.in.nextLine().trim();
                }
                attemptedIds.add(q.getId());
                GradingResult result = ctx.grader.grade(q,
                        answer.equalsIgnoreCase("s") ? "" : ctx.collectAnswer(q, answer));
                if (result.correct()) correctIds.add(q.getId());
                ctx.profile().recordAnswer(q, result.correct());
                ctx.logAttempt(q, result.correct() ? AttemptRecord.OUTCOME_CORRECT
                                               : AttemptRecord.OUTCOME_INCORRECT, qStart, 0);
                // No feedback mid-exam: the whole point is to find out what the student knows
                // unaided, and marking each answer as it lands would leak the pattern.
                System.out.println("  " + Display.dim() + "Recorded." + Display.reset());
            }
        } finally {
            ctx.noHelp = false;
        }

        ctx.saveProfile();
        reportExam(paper, correctIds, attemptedIds, ranOut);
    }

    /** Prints the outcome-by-outcome result and saves it next to the student's other reports. */
    private void reportExam(List<ExamSession.Item> paper, Set<String> correctIds,
                            Set<String> attemptedIds, boolean ranOut) {
        List<ExamSession.SectionScore> scores =
                ExamSession.score(paper, correctIds, attemptedIds);
        StringBuilder out = new StringBuilder();
        out.append("Exam result — ").append(ctx.profile().getName()).append("\n");
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
            String safe = ctx.profile().getName().replaceAll("[^A-Za-z0-9._-]", "_");
            Path file = ctx.storage.directory().resolveSibling("reports")
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
    CourseOverlay pickCourse(List<CourseOverlay> courses) {
        CourseOverlay course = courses.get(0);
        if (courses.size() > 1) {
            for (int i = 0; i < courses.size(); i++) {
                System.out.printf("  [%d] %s%n", i + 1, courses.get(i).getName());
            }
            System.out.print("  Course: ");
            try {
                int idx = Integer.parseInt(ctx.in.nextLine().trim()) - 1;
                if (idx >= 0 && idx < courses.size()) course = courses.get(idx);
            } catch (NumberFormatException ignored) {}
        }
        course.getWarnings().forEach(w ->
                System.out.println("  " + Display.yellow() + "⚠ " + w + Display.reset()));
        return course;
    }

    /** Parses "3" or "1-4" into an inclusive range, or null when it is neither. */
    static int[] parseRange(String text) {
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
}
