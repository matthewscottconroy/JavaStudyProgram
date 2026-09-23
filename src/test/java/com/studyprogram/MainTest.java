package com.studyprogram;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.report.ProgressCard;
import com.studyprogram.storage.AttemptLog;
import com.studyprogram.storage.JsonProfileStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the command-line surface. These are the modes an instructor runs in front of a class —
 * collecting work, generating a report, checking a card — and until now none of them had a test,
 * so a broken flag would have been discovered by the instructor rather than by the build.
 */
class MainTest {

    @TempDir Path home;
    private ByteArrayOutputStream captured;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        System.setProperty("javastudy.profileDir", home.resolve("profiles").toString());
        captured = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.clearProperty("javastudy.profileDir");
    }

    /** Runs a command and returns everything it printed. */
    private String out() {
        return captured.toString(StandardCharsets.UTF_8);
    }

    private StudentProfile saveProfile(String name) throws Exception {
        JsonProfileStorage storage = new JsonProfileStorage(home.resolve("profiles"));
        StudentProfile p = new StudentProfile(name);
        p.recordAnswer(question(), true);
        storage.save(p);
        AttemptLog.forProfile(storage.directory(), name).append(
                new AttemptRecord(LocalDateTime.now(), question(),
                        AttemptRecord.OUTCOME_CORRECT, 30, 0));
        return p;
    }

    private static Question question() {
        return Question.builder().id("q1").topic(Topic.LOOPS)
                .type(QuestionType.MULTIPLE_CHOICE).difficulty(2).prompt("p").answer("a").build();
    }

    // ── Dispatch ─────────────────────────────────────────────────────────────

    @Test
    void helpListsEveryModeAndSucceeds() {
        assertEquals(0, Main.run("--help"));
        String text = out();
        for (String flag : List.of("--class-report", "--export-profile", "--import-profile",
                                   "--verify-card", "--verify-questions")) {
            assertTrue(text.contains(flag), flag + " is missing from --help");
        }
    }

    @Test
    void anUnknownOptionExplainsItselfAndFailsLoudly() {
        assertEquals(2, Main.run("--frobnicate"),
                "a typo'd flag must not look like success to a script");
        assertTrue(out().contains("Unknown option: --frobnicate"));
        assertTrue(out().contains("Usage:"), "and should show what the options are");
    }

    // ── Collecting and moving work ───────────────────────────────────────────

    @Test
    void aProfileRoundTripsThroughExportAndImport() throws Exception {
        saveProfile("Ada");
        Path bundle = home.resolve("ada-bundle.json");

        assertEquals(0, Main.run("--export-profile", "Ada", bundle.toString()));
        assertTrue(Files.exists(bundle), out());
        assertTrue(out().contains("Exported to:"));

        // Import it into a different directory, as a collecting instructor would.
        Path collected = home.resolve("collected");
        System.setProperty("javastudy.profileDir", collected.toString());
        assertEquals(0, Main.run("--import-profile", bundle.toString()));

        assertTrue(new JsonProfileStorage(collected).load("Ada").isPresent(),
                "the imported profile must be loadable: " + out());
    }

    @Test
    void aWholeFolderOfBundlesImportsAtOnce() throws Exception {
        saveProfile("Ada");
        saveProfile("Grace");
        Path drop = home.resolve("handins");
        Files.createDirectories(drop);
        Main.run("--export-profile", "Ada", drop.resolve("ada.json").toString());
        Main.run("--export-profile", "Grace", drop.resolve("grace.json").toString());

        Path collected = home.resolve("collected");
        System.setProperty("javastudy.profileDir", collected.toString());
        assertEquals(0, Main.run("--import-profile", drop.toString()));

        assertTrue(out().contains("Imported 2 profile(s)"), out());
        assertEquals(List.of("Ada", "Grace"), new JsonProfileStorage(collected).listProfileNames());
    }

    @Test
    void exportAndImportExplainThemselvesWithoutArguments() {
        assertEquals(0, Main.run("--export-profile"));
        assertTrue(out().contains("Usage: --export-profile"));

        assertEquals(0, Main.run("--import-profile"));
        assertTrue(out().contains("Usage: --import-profile"));
    }

    @Test
    void aMissingBundleIsReportedAsAnErrorNotAStackTrace() {
        assertEquals(1, Main.run("--import-profile", home.resolve("nope.json").toString()),
                "a missing file must set a non-zero exit code");
    }

    // ── Reporting ────────────────────────────────────────────────────────────

    @Test
    void theClassReportIsWrittenForADirectoryOfProfiles() throws Exception {
        saveProfile("Ada");
        assertEquals(0, Main.run("--class-report", home.resolve("profiles").toString()));

        Path report = home.resolve("reports").resolve("class-report.html");
        assertTrue(Files.exists(report), out());
        assertTrue(Files.readString(report).contains("Ada"));
        assertTrue(out().contains("Class report written to:"));
    }

    // ── Card verification ────────────────────────────────────────────────────

    @Test
    void agenuineCardVerifiesAndAnEditedOneDoesNot() throws Exception {
        StudentProfile p = saveProfile("Ada");
        String card = ProgressCard.render(p, List.of(
                new AttemptRecord(LocalDateTime.now(), question(),
                        AttemptRecord.OUTCOME_CORRECT, 30, 0)));
        Path good = home.resolve("card.txt");
        Files.writeString(good, card);

        assertEquals(0, Main.run("--verify-card", good.toString()));
        assertTrue(out().contains("VALID"), out());

        Path edited = home.resolve("edited.txt");
        Files.writeString(edited, card.replace("Questions answered         1",
                                               "Questions answered         9"));
        captured.reset();
        assertEquals(0, Main.run("--verify-card", edited.toString()));
        assertTrue(out().contains("INVALID"), out());
    }

    @Test
    void aCardForAnUnknownStudentSaysSoRatherThanGuessing() throws Exception {
        StudentProfile stranger = new StudentProfile("Elsewhere");
        Path card = home.resolve("card.txt");
        Files.writeString(card, ProgressCard.render(stranger, List.of()));

        assertEquals(0, Main.run("--verify-card", card.toString()));
        assertTrue(out().contains("No local profile named 'Elsewhere'"), out());
    }

    // ── Question verification ────────────────────────────────────────────────

    @Test
    void verifyQuestionsFailsWithANonZeroExitSoItCanGuardCI() throws Exception {
        Path pack = home.resolve("pack");
        Files.createDirectories(pack.resolve("loops"));
        Files.writeString(pack.resolve("loops").resolve("bad.json"), "{ not json");

        assertEquals(1, Main.run("--verify-questions", pack.toString()),
                "a broken pack must fail the build that runs this");
        assertTrue(out().contains("does not parse"), out());
    }

    @Test
    void verifyQuestionsSucceedsOnAHealthyPack() throws Exception {
        Path pack = home.resolve("pack");
        Files.createDirectories(pack.resolve("loops"));
        Files.writeString(pack.resolve("loops").resolve("ok.json"), """
                { "id": "ok-1", "type": "MULTIPLE_CHOICE", "difficulty": 2, "prompt": "Pick a.",
                  "choices": ["alpha", "beta"], "answer": "a", "explanation": "a." }
                """);

        assertEquals(0, Main.run("--verify-questions", pack.toString()), out());
        assertTrue(out().contains("ready to use"), out());
    }

    // ── Presentation flags ───────────────────────────────────────────────────

    @Test
    void presentationFlagsApplyToAnyModeAndDoNotConsumeTheCommand() {
        assertEquals(0, Main.run("--no-color", "--ascii", "--help"),
                "display flags must not be mistaken for the command itself");
        assertTrue(out().contains("Usage:"));
    }
}
