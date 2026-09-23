package com.studyprogram.storage;

import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The profile file is the one place a student's work can be destroyed, and the program writes to
 * it after every answered question. These tests are about what happens when that goes wrong.
 */
class ProfileDurabilityTest {

    @TempDir Path dir;

    private static Question q(Topic t, int difficulty) {
        return Question.builder().id("q-" + t + "-" + difficulty).topic(t)
                .type(QuestionType.MULTIPLE_CHOICE).difficulty(difficulty)
                .prompt("p").answer("a").build();
    }

    // ── Atomic writes ────────────────────────────────────────────────────────

    @Test
    void aSaveLeavesNoTemporaryFilesBehind() throws Exception {
        JsonProfileStorage storage = new JsonProfileStorage(dir);
        storage.save(new StudentProfile("Ada"));

        try (var files = Files.list(dir)) {
            List<String> names = files.map(p -> p.getFileName().toString()).sorted().toList();
            assertEquals(List.of("Ada.json"), names,
                    "a half-written temp file left in the directory would show up as a profile");
        }
    }

    @Test
    void aFailedSaveLeavesThePreviousProfileIntact() throws Exception {
        JsonProfileStorage storage = new JsonProfileStorage(dir);
        StudentProfile good = new StudentProfile("Ada");
        good.getOrCreatePerformance(Topic.LOOPS).record("q1", true, 3);
        storage.save(good);
        String before = Files.readString(dir.resolve("Ada.json"));

        // A profile Jackson cannot serialise: the write fails after the file would have been
        // truncated by a naive write-in-place.
        StudentProfile broken = new StudentProfile("Ada") {
            @SuppressWarnings("unused")
            public Object getBoom() { throw new IllegalStateException("cannot serialise"); }
        };
        assertThrows(Exception.class, () -> storage.save(broken));

        assertEquals(before, Files.readString(dir.resolve("Ada.json")),
                "the old profile must survive a failed write untouched");
        assertTrue(storage.load("Ada").isPresent());
    }

    // ── Forgiving reads ──────────────────────────────────────────────────────

    @Test
    void oneCorruptProfileDoesNotHideTheOthers() throws Exception {
        JsonProfileStorage storage = new JsonProfileStorage(dir);
        storage.save(new StudentProfile("Good"));
        Files.writeString(dir.resolve("Broken.json"), "{ truncated mid-w");

        assertTrue(storage.load("Broken").isEmpty(), "a damaged profile must not throw");
        assertTrue(storage.load("Good").isPresent(),
                "and must not take the student's other profiles down with it");
        assertTrue(storage.listProfileNames().contains("Good"));
    }

    @Test
    void aCorruptProfileIsQuarantinedAndReported() throws Exception {
        JsonProfileStorage storage = new JsonProfileStorage(dir);
        Files.writeString(dir.resolve("Broken.json"), "{ truncated mid-w");

        storage.load("Broken");
        List<String> warnings = storage.getWarnings();
        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("Broken"), warnings.get(0));
        assertTrue(warnings.get(0).contains("corrupt-"), warnings.get(0));

        assertFalse(Files.exists(dir.resolve("Broken.json")),
                "the unreadable file is moved out of the way");
        try (var files = Files.list(dir)) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().contains(".corrupt-")),
                    "but kept, so a human can look at it");
        }
        assertTrue(storage.getWarnings().isEmpty(), "warnings are cleared once reported");
    }

    // ── Recovery from the attempt log ────────────────────────────────────────

    @Test
    void aProfileIsRebuiltFromItsAttemptLog() {
        List<AttemptRecord> log = new ArrayList<>();
        LocalDateTime t0 = LocalDateTime.of(2026, 3, 1, 9, 0);
        for (int i = 0; i < 6; i++) {
            log.add(new AttemptRecord(t0.plusMinutes(i), q(Topic.LOOPS, 3),
                    i < 5 ? AttemptRecord.OUTCOME_CORRECT : AttemptRecord.OUTCOME_INCORRECT, 30, 0));
        }
        log.add(new AttemptRecord(t0.plusMinutes(9), q(Topic.ARRAYS, 2),
                AttemptRecord.OUTCOME_SKIPPED, 5, 0));

        var rebuilt = ProfileRecovery.rebuild("Ada", log);

        assertEquals(6, rebuilt.attemptsReplayed(), "skips are history, not answers");
        assertEquals(6, rebuilt.profile().getTotalQuestionsAnswered());
        assertEquals(5, rebuilt.profile().getTotalCorrect());
        assertTrue(rebuilt.profile().getPerformance().get(Topic.LOOPS).getMasteryScore() > 0,
                "mastery must come back");
        assertEquals(t0, rebuilt.profile().getCreatedAt());
        assertEquals(t0.plusMinutes(9), rebuilt.profile().getLastStudied());
        assertTrue(rebuilt.profile().getSelectedTopics().contains(Topic.LOOPS));
    }

    @Test
    void replayingTheLogReproducesTheMasteryTheLiveRunComputed() {
        // The rebuild must use the same scoring the program uses, or a recovered profile would
        // quietly differ from the one it replaced.
        StudentProfile live = new StudentProfile("Live");
        List<AttemptRecord> log = new ArrayList<>();
        LocalDateTime t0 = LocalDateTime.of(2026, 3, 1, 9, 0);
        boolean[] outcomes = {true, false, true, true, false, true, true};
        for (int i = 0; i < outcomes.length; i++) {
            Question question = q(Topic.STRINGS, (i % 5) + 1);
            live.recordAnswer(question, outcomes[i]);
            log.add(new AttemptRecord(t0.plusMinutes(i), question,
                    outcomes[i] ? AttemptRecord.OUTCOME_CORRECT : AttemptRecord.OUTCOME_INCORRECT,
                    20, 0));
        }

        var rebuilt = ProfileRecovery.rebuild("Live", log);
        assertEquals(live.getPerformance().get(Topic.STRINGS).getMasteryScore(),
                rebuilt.profile().getPerformance().get(Topic.STRINGS).getMasteryScore(), 1e-9);
        assertEquals(live.getTotalCorrect(), rebuilt.profile().getTotalCorrect());
    }

    @Test
    void recoveryIsHonestAboutWhatItCannotBringBack() {
        List<AttemptRecord> log = List.of(new AttemptRecord(
                LocalDateTime.now(), q(Topic.LOOPS, 3), AttemptRecord.OUTCOME_CORRECT, 10, 0));
        String summary = ProfileRecovery.summary(ProfileRecovery.rebuild("Ada", log));

        assertTrue(summary.contains("Bosses cleared"), summary);
        assertTrue(summary.contains("have not come back"),
                "a recovered profile must not pretend to be the original: " + summary);
    }

    @Test
    void thereIsNothingToRecoverWithoutALog() throws Exception {
        assertFalse(ProfileRecovery.canRecover(dir, "Nobody"));
        var rebuilt = ProfileRecovery.rebuild(dir, "Nobody");
        assertTrue(rebuilt.isEmpty());
        assertTrue(ProfileRecovery.summary(rebuilt).contains("no recorded attempts"));
    }

    @Test
    void aDamagedProfileCanBeRebuiltFromItsLogEndToEnd() throws Exception {
        JsonProfileStorage storage = new JsonProfileStorage(dir);
        AttemptLog log = AttemptLog.forProfile(dir, "Ada");
        for (int i = 0; i < 4; i++) {
            log.append(new AttemptRecord(LocalDateTime.now().minusMinutes(4 - i),
                    q(Topic.LOOPS, 3), AttemptRecord.OUTCOME_CORRECT, 30, 0));
        }
        Files.writeString(dir.resolve("Ada.json"), "{ half a fi");

        assertTrue(storage.load("Ada").isEmpty());
        assertTrue(ProfileRecovery.canRecover(dir, "Ada"));

        var rebuilt = ProfileRecovery.rebuild(dir, "Ada");
        assertEquals(4, rebuilt.attemptsReplayed());
        storage.save(rebuilt.profile());
        assertTrue(storage.load("Ada").isPresent(), "the rebuilt profile must load cleanly");
    }
}
