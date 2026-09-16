package com.studyprogram.ui;

import com.studyprogram.coding.CodingExerciseRunner;
import com.studyprogram.core.QuestionBank;
import com.studyprogram.llm.NullLLMService;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.storage.AttemptLog;
import com.studyprogram.storage.JsonProfileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the interactive program the way a student does — scripted keystrokes in, captured
 * terminal out — so the flow everyone actually touches is covered, not just the engine beneath it.
 *
 * <p>The bank is a handful of hand-built multiple-choice questions rather than the shipped 2,700,
 * which keeps every assertion deterministic and avoids invoking the compiler.
 */
class CLITest {

    @TempDir Path home;
    private JsonProfileStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        storage = new JsonProfileStorage(home.resolve("profiles"));
        Display.disableColor();     // assert on text, not escape codes
    }

    private static Question mc(String id, Topic topic, String answer) {
        return Question.builder()
                .id(id).topic(topic).type(QuestionType.MULTIPLE_CHOICE).difficulty(1)
                .prompt("Question " + id)
                .choices("alpha", "beta", "gamma", "delta")
                .answer(answer)
                .explanation("Because " + id + ".")
                .hint("Think about " + id + ".")
                .build();
    }

    private static QuestionBank tinyBank() {
        // all three share the same correct answer, so a script's answer does not depend on
        // which question the adaptive engine happens to serve
        return QuestionBank.of(List.of(
                mc("t-1", Topic.VARIABLES, "a"),
                mc("t-2", Topic.VARIABLES, "a"),
                mc("t-3", Topic.VARIABLES, "a")));
    }

    /**
     * Keystrokes that start a one-question session on the auto feed. The auto feed is used rather
     * than "my selected topics" because a profile with nothing selected detours into the topic
     * picker, which would swallow the rest of the script.
     */
    private static final String[] ONE_QUESTION = {"1", "", "1"};

    private static String[] script(String... parts) {
        return parts;
    }

    /** Runs the CLI against scripted input and returns everything it printed. */
    private String run(QuestionBank bank, String... keystrokes) {
        String script = String.join("\n", keystrokes) + "\n";
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            new CLI(bank, storage, new NullLLMService(),
                    new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
                    new CodingExerciseRunner(home.resolve("workspace")))
                    .run();
        } catch (java.util.NoSuchElementException e) {
            // the script ran out — whatever was printed before that is still the behaviour under test
        } finally {
            System.setOut(original);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createsAProfileAndSavesIt() throws Exception {
        String out = run(tinyBank(),
                "Ada",        // name
                "done",       // topic selection
                "8");         // exit

        assertTrue(out.contains("Profile created for Ada"), out);
        assertTrue(storage.load("Ada").isPresent(), "the profile must be persisted on exit");
    }

    @Test
    void answeringAQuestionRecordsProgressAndLogsTheAttempt() throws Exception {
        run(tinyBank(),
                "Ada", "done",
                "1", "", "1", // start a one-question session on the auto feed
                "a",          // answer (correct for every fixture)
                "",           // [Enter] next
                "8");

        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertEquals(1, saved.getTotalQuestionsAnswered(), "the answer must reach the profile");
        assertTrue(saved.getPerformance().containsKey(Topic.VARIABLES));

        var attempts = AttemptLog.forProfile(storage.directory(), "Ada").readAll();
        assertEquals(1, attempts.size(), "the attempt must reach the append-only log");
        assertEquals(Topic.VARIABLES, attempts.get(0).getTopic());
    }

    @Test
    void aWrongAnswerIsMarkedWrongAndExplained() {
        String out = run(tinyBank(),
                "Ada", "done", "1", "", "1",
                "d",          // wrong: every fixture answers "a"
                "",           // [Enter] next
                "n",          // decline the wrong-answer review
                "8");

        assertTrue(out.contains("Incorrect"), out);
        assertTrue(out.contains("Because t-"), "the explanation must be shown: " + out);
    }

    @Test
    void hintsAreAvailableWithoutAnsweringAndDoNotEndTheQuestion() {
        String out = run(tinyBank(),
                "Ada", "done", "1", "", "1",
                "h",          // ask for a hint first
                "a",          // then answer
                "", "8");

        assertTrue(out.contains("Think about t-"), "the authored hint must be offered: " + out);
        assertTrue(out.contains("Correct") || out.contains("Incorrect"),
                "the question must still be answerable after a hint");
    }

    @Test
    void flaggingAQuestionRecordsItForTheInstructor() throws Exception {
        run(tinyBank(),
                "Ada", "done", "1", "", "1",
                "f",                      // flag it
                "the wording is unclear", // the note
                "a", "", "8");

        Path flags = storage.directory().resolveSibling("flags.jsonl");
        assertTrue(Files.exists(flags), "a flag must be written for the class report");
        String line = Files.readString(flags);
        assertTrue(line.contains("the wording is unclear"), line);
        assertTrue(line.contains("Ada"), line);
    }

    @Test
    void skippingIsRecordedAsAvoidanceRatherThanAWrongAnswer() throws Exception {
        // a skip is deliberately not progress, so the session asks again — quit out of it
        run(tinyBank(), "Ada", "done", "1", "", "1", "s", "q", "8");

        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertEquals(0, saved.getTotalQuestionsAnswered(), "a skip is not an answer");
        var perf = saved.getPerformance().get(Topic.VARIABLES);
        assertNotNull(perf, "but it is still recorded against the topic");
        assertEquals(1, perf.getSkipped());
    }

    @Test
    void theProgressReportIsWrittenAndCarriesACard() throws Exception {
        String out = run(tinyBank(),
                "Ada", "done",
                "1", "", "1", "a", "",   // do one question so there is something to report
                "5",                      // progress report
                "8");

        assertTrue(out.contains("JAVA STUDY PROGRESS CARD"), out);
        assertTrue(out.contains("Checksum") || out.contains("Signature"));
        Path report = storage.directory().resolveSibling("reports").resolve("Ada-progress.html");
        assertTrue(Files.exists(report), "the HTML report must be written");
        assertTrue(Files.readString(report).contains("Ada"));
    }

    @Test
    void aLockedBossCannotBeFought() {
        String out = run(tinyBank(), "Ada", "done", "4", "1", "8");
        assertTrue(out.contains("locked"), "an unearned boss must stay locked: " + out);
    }

    @Test
    void unitReviewOffersTheInstructorsCourseUnits() {
        // run from the repo, where data/courses/sample-java2.json ships
        String out = run(tinyBank(), "Ada", "done", "1", "u", "1-2", "1", "a", "", "8");
        assertTrue(out.contains("Units to review"), "the unit picker must appear: " + out);
        assertTrue(out.contains("Tools & Java I Review") || out.contains("Sample Course"),
                "the shipped course must be listed: " + out);
    }

    @Test
    void profilesCanBeRenamedCarryingTheirHistory() throws Exception {
        run(tinyBank(), "Ada", "done", "1", "", "1", "a", "", "8");
        assertEquals(1, AttemptLog.forProfile(storage.directory(), "Ada").readAll().size());

        run(tinyBank(), "R", "1", "Grace", "1", "done", "8");

        assertTrue(storage.load("Grace").isPresent(), "the renamed profile must exist");
        assertTrue(storage.load("Ada").isEmpty(), "the old name must be gone");
        assertEquals(1, AttemptLog.forProfile(storage.directory(), "Grace").readAll().size(),
                "the attempt log must follow the rename");
    }

    @Test
    void deletingAProfileRequiresTypingItsName() throws Exception {
        run(tinyBank(), "Ada", "done", "8");
        assertTrue(storage.load("Ada").isPresent());

        run(tinyBank(), "D", "1", "not the name", "1", "done", "8");
        assertTrue(storage.load("Ada").isPresent(), "a mistyped confirmation must not delete");

        run(tinyBank(), "D", "1", "Ada", "Fresh", "done", "8");
        assertTrue(storage.load("Ada").isEmpty(), "typing the name deletes the profile");
    }

    @Test
    void aFinishedSessionCanBeRepeatedWithoutReansweringTheSetup() throws Exception {
        run(tinyBank(), "Ada", "done", "1", "", "1", "a", "", "8");
        assertTrue(storage.load("Ada").orElseThrow().hasResumableSession(),
                "the session just finished should be repeatable");

        // a second launch starts at the profile picker: choose Ada, then repeat her last session
        String out = run(tinyBank(),
                "1",   // profile menu: pick the existing Ada
                "1",   // main menu: start a session
                "r",   // repeat the last one -- no feed or length prompt follows
                "a", "", "8");
        assertTrue(out.contains("repeat last session"), "the option must be offered: " + out);
        assertTrue(out.contains("Resuming:"), out);

        assertEquals(2, storage.load("Ada").orElseThrow().getTotalQuestionsAnswered(),
                "the repeated session must record its answer too");
    }

    @Test
    void aFreshProfileIsNotOfferedAResume() {
        String out = run(tinyBank(), "Ada", "done", "1", "", "1", "a", "", "8");
        int firstPrompt = out.indexOf("Feed:");
        assertTrue(firstPrompt >= 0);
        assertFalse(out.substring(firstPrompt, out.indexOf("\n", firstPrompt) + 1)
                        .contains("repeat last session"),
                "there is nothing to repeat on a brand-new profile");
    }

    @Test
    void anInvalidMenuChoiceIsRejectedWithoutCrashing() {
        String out = run(tinyBank(), "Ada", "done", "99", "8");
        assertTrue(out.contains("Invalid choice"), out);
    }

    @Test
    void theBannerReportsTheEnvironmentHonestly() {
        String out = run(tinyBank(), "Ada", "done", "8");
        assertTrue(out.contains("Questions in bank: 3"), out);
        assertTrue(out.contains("AI help:"), "the student must see whether AI is on");
        assertTrue(out.contains("Exercise sandbox:"), "and how exercises are contained");
    }
}
