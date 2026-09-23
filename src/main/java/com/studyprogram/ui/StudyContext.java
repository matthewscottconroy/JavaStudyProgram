package com.studyprogram.ui;

import com.studyprogram.coding.CodingExerciseRunner;
import com.studyprogram.core.QuestionBank;
import com.studyprogram.grading.CompositeGrader;
import com.studyprogram.grading.Grader;
import com.studyprogram.llm.LLMService;
import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.storage.AttemptLog;
import com.studyprogram.storage.ProfileStorage;
import com.studyprogram.model.StudentProfile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * The collaborators and session state every interactive flow needs.
 *
 * <p>{@link CLI} grew to 1,700 lines because each new feature — placement, exams, worksheets,
 * goals, the mistakes deck — arrived as more methods on the same class, all reaching for the same
 * half-dozen fields. Passing those fields around explicitly is what lets a flow move into its own
 * file: {@link CodingFlow} and {@link ExamFlow} hold one of these instead of being inner parts of
 * the menu.
 *
 * <p>It deliberately owns the two operations every flow performs and must perform identically —
 * appending to the attempt log and saving the profile — so those rules live in one place rather
 * than being re-implemented per screen.
 */
public class StudyContext {

    public final QuestionBank bank;
    public final ProfileStorage storage;
    public final LLMService llm;
    public final Grader grader;
    public final Scanner in;
    public final CodingExerciseRunner codingRunner;
    /** False under test, so generating a report does not open the developer's browser. */
    public final boolean openReportsInBrowser;

    private StudentProfile currentProfile;
    private AttemptLog attemptLog;

    /** Hints taken during the most recent coding exercise. */
    public int lastCodingHints;
    /** Compile-error categories hit during the most recent coding exercise. */
    public final Set<String> lastCodingErrors = new LinkedHashSet<>();
    /** True while an exam is in progress: hints and explanations are refused. */
    public boolean noHelp;

    public StudyContext(QuestionBank bank, ProfileStorage storage, LLMService llm,
                        Scanner in, CodingExerciseRunner codingRunner,
                        boolean openReportsInBrowser) {
        this.bank = bank;
        this.storage = storage;
        this.llm = llm;
        this.grader = new CompositeGrader(llm);
        this.in = in;
        this.codingRunner = codingRunner;
        this.openReportsInBrowser = openReportsInBrowser;
    }

    public StudentProfile profile()            { return currentProfile; }
    public void setProfile(StudentProfile p)   { this.currentProfile = p; }
    public AttemptLog attemptLog()             { return attemptLog; }
    public void setAttemptLog(AttemptLog log)  { this.attemptLog = log; }

    /** Reads a line of input, trimmed. */
    public String readLine() {
        return in.nextLine().trim();
    }

    public void saveProfile() {
        try {
            storage.save(currentProfile);
        } catch (IOException e) {
            System.out.println("  Warning: could not save profile — " + e.getMessage());
        }
    }

    /** Appends one attempt to the append-only log (timings in whole seconds). */
    public void logAttempt(Question q, String outcome, long startNano, int hintsUsed) {
        logAttempt(q, outcome, startNano, hintsUsed, List.of());
    }

    /** As above, also recording which compile errors the student hit on the way. */
    public void logAttempt(Question q, String outcome, long startNano, int hintsUsed,
                           Collection<String> compileErrors) {
        if (attemptLog == null) return;
        attemptLog.append(new AttemptRecord(LocalDateTime.now(), q, outcome,
                seconds(startNano), hintsUsed, new java.util.ArrayList<>(compileErrors)));
    }

    /**
     * As above, also recording which misconception a wrong choice revealed. One wrong answer is
     * noise; the same wrong idea across a dozen questions is worth telling a student about.
     */
    public void logAttempt(Question q, String outcome, long startNano, int hintsUsed,
                           String studentAnswer) {
        if (attemptLog == null) return;
        AttemptRecord record = new AttemptRecord(LocalDateTime.now(), q, outcome,
                seconds(startNano), hintsUsed);
        if (AttemptRecord.OUTCOME_INCORRECT.equals(outcome)) {
            q.misconceptionFor(studentAnswer).ifPresent(m -> record.setMisconception(m.name()));
        }
        attemptLog.append(record);
    }

    private static long seconds(long startNano) {
        return Math.max(0, (System.nanoTime() - startNano) / 1_000_000_000L);
    }

    /**
     * Completes a multi-blank answer. A faded worked example with three blanks is three separate
     * lines of Java, and asking for them on one line would test the input format rather than the
     * code.
     */
    public String collectAnswer(Question q, String first) {
        if (q.getType() != com.studyprogram.model.QuestionType.FADED) return first;
        int blanks = com.studyprogram.questions.FadedExampleDeriver.blankCount(q);
        if (blanks <= 1) return first;

        StringBuilder all = new StringBuilder(first);
        for (int b = 2; b <= blanks; b++) {
            System.out.print("  Line for blank " + b + ": ");
            all.append('\n').append(in.nextLine());
        }
        return all.toString();
    }
}
