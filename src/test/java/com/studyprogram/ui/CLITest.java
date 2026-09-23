package com.studyprogram.ui;

import com.studyprogram.coding.CodingExerciseRunner;
import com.studyprogram.core.PlacementCheck;
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

    private static Question faded(String id, Topic topic, String... missingLines) {
        return Question.builder()
                .id(id).topic(topic).type(QuestionType.FADED).difficulty(1)
                .prompt("Worked example " + id)
                .code(">>> blank 1 <<<"
                        + (missingLines.length > 1 ? "\n>>> blank 2 <<<" : ""))
                .answer(String.join("\n", missingLines))
                .explanation("Because " + id + ".")
                .build();
    }

    /**
     * A bank with one answerable probe per level, so the placement walk has somewhere to go.
     * Every question answers "a", as elsewhere, so a script does not depend on which is served.
     */
    private static QuestionBank placementBank() {
        List<Question> all = new java.util.ArrayList<>();
        Topic[] perLevel = {Topic.VARIABLES, Topic.LOOPS, Topic.ARRAYS_ARRAYLISTS,
                            Topic.INHERITANCE, Topic.STREAM_API};
        for (Topic t : perLevel) {
            for (int i = 1; i <= 3; i++) all.add(mc("p-" + t + "-" + i, t, "a"));
        }
        return QuestionBank.of(all);
    }

    @Test
    void placementOpensWhatTheStudentAlreadyKnows() throws Exception {
        String[] answers = new String[PlacementCheck.LENGTH];
        java.util.Arrays.fill(answers, "a");          // right every time: climb the ladder
        java.util.List<String> script = new java.util.ArrayList<>(
                List.of("Ada", "y"));                  // name, take the check
        script.addAll(List.of(answers));
        script.addAll(List.of("done", "9"));

        String out = run(placementBank(), script.toArray(new String[0]));

        assertTrue(out.contains("Placement Check"), out);
        assertTrue(out.contains("Placement result"), out);
        assertTrue(out.contains("opened, not ticked off"),
                "the summary must say what placement did and did not prove: " + out);

        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertFalse(saved.getPerformance().isEmpty(), "placement must seed the profile");
        assertTrue(Topic.LOOPS.isUnlocked(saved.getPerformance()),
                "a student who answered correctly should not be locked out of loops");
        for (var perf : saved.getPerformance().values()) {
            assertTrue(perf.getMasteryScore() < 0.8,
                    "placement opens topics, it does not mark them mastered");
        }
        assertFalse(AttemptLog.forProfile(storage.directory(), "Ada").readAll().isEmpty(),
                "placement answers are real answers and belong in the log");
    }

    @Test
    void decliningPlacementLeavesAnUntouchedProfile() throws Exception {
        run(placementBank(), "Ada", "n", "done", "9");

        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertTrue(saved.getPerformance().isEmpty(), "declining must seed nothing");
    }

    @Test
    void placementRefusesHintsAndCanBeTakenLaterFromTheProfileMenu() throws Exception {
        run(placementBank(), "Ada", "n", "done", "9");   // skip it at first

        java.util.List<String> script = new java.util.ArrayList<>(
                List.of("P", "1",        // profile menu: placement check on profile 1
                        "h", "a"));      // ask for a hint, then answer
        for (int i = 1; i < PlacementCheck.LENGTH; i++) script.add("a");
        script.addAll(List.of("9"));

        String out = run(placementBank(), script.toArray(new String[0]));

        assertTrue(out.contains("[P] placement check"), "the menu must offer it: " + out);
        assertTrue(out.contains("No hints during placement"), out);
        assertFalse(storage.load("Ada").orElseThrow().getPerformance().isEmpty(),
                "the later check must still seed the profile");
    }

    @Test
    void passingTheTestsStillEarnsAdviceOnTheCodeItself() throws Exception {
        // The starter already passes: isYes("yes") is true because short literals are interned,
        // which is exactly the bug that tests do not catch and a reviewer does.
        Question coding = Question.builder()
                .id("style-1").topic(Topic.VARIABLES).type(QuestionType.CODING).difficulty(2)
                .prompt("Decide whether the answer was yes.")
                .starterCode("""
                        public class Greeter {
                            public static boolean isYes(String answer) {
                                return answer == "yes";
                            }
                        }
                        """)
                .testCode("""
                        public class GreeterTest {
                            public static void main(String[] args) {
                                if (!Greeter.isYes("yes")) { System.out.println("FAIL yes"); System.exit(1); }
                                if (Greeter.isYes("no"))   { System.out.println("FAIL no");  System.exit(1); }
                                System.out.println("ALL TESTS PASSED");
                            }
                        }
                        """)
                .answer("public class Greeter { }")
                .explanation("Use .equals to compare text.")
                .build();

        String out = run(QuestionBank.of(List.of(coding)),
                "Ada", "n", "done",
                "1", "", "1",
                "",             // Enter: compile and run the tests
                "",             // Enter: next
                "9");

        assertTrue(out.contains("all tests passed") || out.contains("tests passed"),
                "the exercise should pass: " + out);
        assertTrue(out.contains("It works. Worth tightening"),
                "green tests are not the end of the story: " + out);
        assertTrue(out.contains(".equals"),
                "the advice should name the actual problem: " + out);
    }

    @Test
    void aMissedQuestionIsOfferedBackAsAMistakeToPractise() throws Exception {
        // First run: get a question wrong.
        run(tinyBank(), "Ada", "n", "done", "1", "", "1", "delta", "", "9");

        // Second run: the feed should say so, and [x] should re-serve exactly that question.
        String out = run(tinyBank(), "1", "1", "x", "a", "", "9");

        assertTrue(out.contains("waiting to be re-tried"), out);
        assertTrue(out.contains("[x] practice your mistakes"), out);
        assertTrue(out.contains("Practice — Your Mistakes"), out);
        assertTrue(out.contains("1/1 correct"), "the retry should be scored: " + out);
    }

    @Test
    void fixingAMistakeClearsItFromTheDeck() throws Exception {
        run(tinyBank(), "Ada", "n", "done", "1", "", "1", "delta", "", "9");
        run(tinyBank(), "1", "1", "x", "a", "", "9");

        String out = run(tinyBank(), "1", "1", "x", "9");
        assertTrue(out.contains("Nothing outstanding"),
                "a mistake the student has since fixed must not keep coming back: " + out);
    }

    @Test
    void reviewingWrongAnswersReachesTheAttemptLog() throws Exception {
        run(tinyBank(),
                "Ada", "n", "done",
                "1", "", "1",
                "delta",      // wrong
                "",           // next
                "y",          // yes, review the wrong answers now
                "a",          // right this time
                "",           // [Enter] next
                "9");

        var attempts = AttemptLog.forProfile(storage.directory(), "Ada").readAll();
        assertEquals(2, attempts.size(),
                "the review answer must be logged too, or the mistake never resolves: " + attempts);
        assertTrue(attempts.get(1).isCorrect());
    }

    @Test
    void aGoalShowsOnTheMenuAndSteersTheFeed() throws Exception {
        String out = run(tinyBank(),
                "Ada", "n", "done",
                "5", "g",               // exam mode -> set a goal
                "Midterm",              // title
                "14",                   // in 14 days
                "1",                    // unit 1
                "1", "", "1",           // start a session on the auto feed
                "a", "",
                "9");

        assertTrue(out.contains("Goal: Midterm in 14 days"), out);
        assertTrue(out.contains("a day to get there"), "the menu must state the pace: " + out);
        assertTrue(out.contains("Goal plan (Midterm)"),
                "the auto feed should head for the goal's topics: " + out);

        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertNotNull(saved.getGoal(), "the goal must be persisted");
        assertEquals("Midterm", saved.getGoal().getTitle());
        assertTrue(saved.getGoal().getTopics().contains(Topic.VARIABLES));
    }

    @Test
    void aGoalCanBeClearedAndAPastDateIsRefused() throws Exception {
        String out = run(tinyBank(),
                "Ada", "n", "done",
                "5", "g", "Quiz",
                "2020-01-01",           // in the past
                "3",                    // fine: three days from now
                "1",
                "5", "c",               // exam mode -> clear it
                "9");

        assertTrue(out.contains("already passed"), out);
        assertTrue(out.contains("Goal cleared"), out);
        assertNull(storage.load("Ada").orElseThrow().getGoal());
    }

    @Test
    void aWorksheetAndItsAnswerKeyAreWrittenSeparately() throws Exception {
        String out = run(tinyBank(),
                "Ada", "n", "done",
                "5", "w",         // exam mode -> worksheet
                "1",              // unit 1
                "2",              // two questions
                "Quiz One",       // title
                "9");

        assertTrue(out.contains("Worksheet:"), out);
        assertTrue(out.contains("Answer key:"), out);
        assertTrue(out.contains("no answers on it"), out);

        Path sheet = home.resolve("worksheets").resolve("Quiz_One.html");
        Path key   = home.resolve("worksheets").resolve("Quiz_One-answers.html");
        assertTrue(Files.exists(sheet) && Files.exists(key),
                "both documents must be written");
        assertFalse(Files.readString(sheet).contains("Answer:"),
                "the printable worksheet must not carry answers");
        assertTrue(Files.readString(key).contains("Answer:"));
    }

    @Test
    void examModeScoresEachSyllabusUnitSeparately() throws Exception {
        String out = run(tinyBank(),
                "Ada", "n", "done",
                "5",            // exam mode
                "",             // sit a paper (not set a goal)
                "1",            // units to examine
                "2",            // two questions
                "y",            // start the clock
                "a", "a",       // both correct
                "9");

        assertTrue(out.contains("Exam Mode"), out);
        assertTrue(out.contains("Unit 1"), "the paper must be scored against the course's units");
        assertTrue(out.contains("OVERALL"), out);
        assertTrue(out.contains("100%"), "two correct answers is a perfect paper: " + out);
        assertTrue(Files.isDirectory(home.resolve("reports")),
                "the exam result must be saved where the student can hand it in");
    }

    @Test
    void examModeRefusesHintsAndReportsTheWeakUnit() throws Exception {
        String out = run(tinyBank(),
                "Ada", "n", "done",
                "5", "", "1", "2", "y",
                "h",            // asking for help
                "delta",        // …and then answering wrongly anyway
                "delta",
                "9");

        assertTrue(out.contains("No help during an exam"), out);
        assertTrue(out.contains("needs work"), "a unit answered wrongly is reported as a gap: " + out);
        assertTrue(out.contains("Work on these before the real thing"), out);
    }

    @Test
    void examModeCanBeBackedOutOfWithoutRecordingAnything() throws Exception {
        run(tinyBank(), "Ada", "n", "done", "5", "", "1", "2", "n", "9");

        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertEquals(0, saved.getTotalQuestionsAnswered(),
                "declining to start the clock must not count as attempts");
    }

    @Test
    void aFadedExampleWithSeveralBlanksAsksForEachLineInTurn() throws Exception {
        QuestionBank bank = QuestionBank.of(List.of(
                faded("f-1", Topic.VARIABLES, "int total = 0;", "total += n;")));

        String out = run(bank,
                "Ada", "n", "done",
                "1", "", "1",
                "int total = 0;",   // blank 1
                "total += n;",      // blank 2
                "", "9");

        assertTrue(out.contains("blank 1"), out);
        assertTrue(out.contains("Line for blank 2"),
                "the second line must be collected separately, not crammed onto one line: " + out);
        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertEquals(1, saved.getTotalQuestionsAnswered());
        assertEquals(1, saved.getTotalCorrect(), "both lines were right: " + out);
    }

    @Test
    void aFadedExampleTellsYouWhichBlankYouGotWrong() throws Exception {
        QuestionBank bank = QuestionBank.of(List.of(
                faded("f-1", Topic.VARIABLES, "int total = 0;", "total += n;")));

        String out = run(bank,
                "Ada", "n", "done",
                "1", "", "1",
                "int total = 0;",
                "total = n;",       // wrong second line
                "", "9");

        assertTrue(out.contains("Blank 2"), "the feedback must name the blank that was wrong: " + out);
        assertTrue(out.contains("total += n;"), out);
    }

    @Test
    void createsAProfileAndSavesIt() throws Exception {
        String out = run(tinyBank(),
                "Ada",        // name
                "n",          // decline the placement check
                "done",       // topic selection
                "9");         // exit

        assertTrue(out.contains("Profile created for Ada"), out);
        assertTrue(storage.load("Ada").isPresent(), "the profile must be persisted on exit");
    }

    @Test
    void answeringAQuestionRecordsProgressAndLogsTheAttempt() throws Exception {
        run(tinyBank(),
                "Ada", "n", "done",
                "1", "", "1", // start a one-question session on the auto feed
                "a",          // answer (correct for every fixture)
                "",           // [Enter] next
                "9");

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
                "Ada", "n", "done", "1", "", "1",
                "d",          // wrong: every fixture answers "a"
                "",           // [Enter] next
                "n",          // decline the wrong-answer review
                "9");

        assertTrue(out.contains("Incorrect"), out);
        assertTrue(out.contains("Because t-"), "the explanation must be shown: " + out);
    }

    @Test
    void hintsAreAvailableWithoutAnsweringAndDoNotEndTheQuestion() {
        String out = run(tinyBank(),
                "Ada", "n", "done", "1", "", "1",
                "h",          // ask for a hint first
                "a",          // then answer
                "", "9");

        assertTrue(out.contains("Think about t-"), "the authored hint must be offered: " + out);
        assertTrue(out.contains("Correct") || out.contains("Incorrect"),
                "the question must still be answerable after a hint");
    }

    @Test
    void flaggingAQuestionRecordsItForTheInstructor() throws Exception {
        run(tinyBank(),
                "Ada", "n", "done", "1", "", "1",
                "f",                      // flag it
                "the wording is unclear", // the note
                "a", "", "9");

        Path flags = storage.directory().resolveSibling("flags.jsonl");
        assertTrue(Files.exists(flags), "a flag must be written for the class report");
        String line = Files.readString(flags);
        assertTrue(line.contains("the wording is unclear"), line);
        assertTrue(line.contains("Ada"), line);
    }

    @Test
    void skippingIsRecordedAsAvoidanceRatherThanAWrongAnswer() throws Exception {
        // a skip is deliberately not progress, so the session asks again — quit out of it
        run(tinyBank(), "Ada", "n", "done", "1", "", "1", "s", "q", "9");

        StudentProfile saved = storage.load("Ada").orElseThrow();
        assertEquals(0, saved.getTotalQuestionsAnswered(), "a skip is not an answer");
        var perf = saved.getPerformance().get(Topic.VARIABLES);
        assertNotNull(perf, "but it is still recorded against the topic");
        assertEquals(1, perf.getSkipped());
    }

    @Test
    void theProgressReportIsWrittenAndCarriesACard() throws Exception {
        String out = run(tinyBank(),
                "Ada", "n", "done",
                "1", "", "1", "a", "",   // do one question so there is something to report
                "6",                      // progress report
                "9");

        assertTrue(out.contains("JAVA STUDY PROGRESS CARD"), out);
        assertTrue(out.contains("Checksum") || out.contains("Signature"));
        Path report = storage.directory().resolveSibling("reports").resolve("Ada-progress.html");
        assertTrue(Files.exists(report), "the HTML report must be written");
        assertTrue(Files.readString(report).contains("Ada"));
    }

    @Test
    void aLockedBossCannotBeFought() {
        String out = run(tinyBank(), "Ada", "n", "done", "4", "1", "9");
        assertTrue(out.contains("locked"), "an unearned boss must stay locked: " + out);
    }

    @Test
    void unitReviewOffersTheInstructorsCourseUnits() {
        // run from the repo, where data/courses/sample-java2.json ships
        String out = run(tinyBank(), "Ada", "n", "done", "1", "u", "1-2", "1", "a", "", "9");
        assertTrue(out.contains("Units to review"), "the unit picker must appear: " + out);
        assertTrue(out.contains("Tools & Java I Review") || out.contains("Sample Course"),
                "the shipped course must be listed: " + out);
    }

    @Test
    void profilesCanBeRenamedCarryingTheirHistory() throws Exception {
        run(tinyBank(), "Ada", "n", "done", "1", "", "1", "a", "", "9");
        assertEquals(1, AttemptLog.forProfile(storage.directory(), "Ada").readAll().size());

        run(tinyBank(), "R", "1", "Grace", "1", "n", "done", "9");

        assertTrue(storage.load("Grace").isPresent(), "the renamed profile must exist");
        assertTrue(storage.load("Ada").isEmpty(), "the old name must be gone");
        assertEquals(1, AttemptLog.forProfile(storage.directory(), "Grace").readAll().size(),
                "the attempt log must follow the rename");
    }

    @Test
    void deletingAProfileRequiresTypingItsName() throws Exception {
        run(tinyBank(), "Ada", "n", "done", "9");
        assertTrue(storage.load("Ada").isPresent());

        run(tinyBank(), "D", "1", "not the name", "1", "n", "done", "9");
        assertTrue(storage.load("Ada").isPresent(), "a mistyped confirmation must not delete");

        run(tinyBank(), "D", "1", "Ada", "Fresh", "n", "done", "9");
        assertTrue(storage.load("Ada").isEmpty(), "typing the name deletes the profile");
    }

    @Test
    void aFinishedSessionCanBeRepeatedWithoutReansweringTheSetup() throws Exception {
        run(tinyBank(), "Ada", "n", "done", "1", "", "1", "a", "", "9");
        assertTrue(storage.load("Ada").orElseThrow().hasResumableSession(),
                "the session just finished should be repeatable");

        // a second launch starts at the profile picker: choose Ada, then repeat her last session
        String out = run(tinyBank(),
                "1",   // profile menu: pick the existing Ada
                "1",   // main menu: start a session
                "r",   // repeat the last one -- no feed or length prompt follows
                "a", "", "9");
        assertTrue(out.contains("repeat last session"), "the option must be offered: " + out);
        assertTrue(out.contains("Resuming:"), out);

        assertEquals(2, storage.load("Ada").orElseThrow().getTotalQuestionsAnswered(),
                "the repeated session must record its answer too");
    }

    @Test
    void aFreshProfileIsNotOfferedAResume() {
        String out = run(tinyBank(), "Ada", "n", "done", "1", "", "1", "a", "", "9");
        int firstPrompt = out.indexOf("Feed:");
        assertTrue(firstPrompt >= 0);
        assertFalse(out.substring(firstPrompt, out.indexOf("\n", firstPrompt) + 1)
                        .contains("repeat last session"),
                "there is nothing to repeat on a brand-new profile");
    }

    @Test
    void anInvalidMenuChoiceIsRejectedWithoutCrashing() {
        String out = run(tinyBank(), "Ada", "n", "done", "99", "9");
        assertTrue(out.contains("Invalid choice"), out);
    }

    @Test
    void theBannerReportsTheEnvironmentHonestly() {
        String out = run(tinyBank(), "Ada", "n", "done", "9");
        assertTrue(out.contains("Questions in bank: 3"), out);
        assertTrue(out.contains("AI help:"), "the student must see whether AI is on");
        assertTrue(out.contains("Exercise sandbox:"), "and how exercises are contained");
    }
}
