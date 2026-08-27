package com.studyprogram.storage;

import com.studyprogram.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttemptLogTest {

    @TempDir
    Path dir;

    private Question question() {
        return Question.builder()
                .id("lp-code-01").topic(Topic.LOOPS).type(QuestionType.CODING).difficulty(2)
                .prompt("p").answer("a")
                .starterCode("public class X {}").testCode("public class XTest {}")
                .build();
    }

    @Test
    void appendAndReadRoundTrip() {
        AttemptLog log = AttemptLog.forProfile(dir, "Test Student");
        log.append(new AttemptRecord(LocalDateTime.now(), question(),
                AttemptRecord.OUTCOME_CORRECT, 42, 1));
        log.append(new AttemptRecord(LocalDateTime.now(), question(),
                AttemptRecord.OUTCOME_SKIPPED, 5, 0));

        List<AttemptRecord> all = log.readAll();
        assertEquals(2, all.size());
        assertEquals("lp-code-01", all.get(0).getQuestionId());
        assertEquals(Topic.LOOPS, all.get(0).getTopic());
        assertEquals(QuestionType.CODING, all.get(0).getType());
        assertTrue(all.get(0).isCorrect());
        assertEquals(42, all.get(0).getSeconds());
        assertEquals(1, all.get(0).getHintsUsed());
        assertTrue(all.get(1).isSkipped());
        assertFalse(all.get(1).isAnswered());
    }

    @Test
    void profileNameIsSanitizedForFilename() {
        AttemptLog log = AttemptLog.forProfile(dir, "a/b\\c d");
        assertEquals("a_b_c_d.attempts.jsonl", log.getFile().getFileName().toString());
    }

    @Test
    void corruptLinesAreSkipped() throws Exception {
        AttemptLog log = AttemptLog.forProfile(dir, "X");
        log.append(new AttemptRecord(LocalDateTime.now(), question(),
                AttemptRecord.OUTCOME_INCORRECT, 3, 0));
        Files.writeString(log.getFile(),
                Files.readString(log.getFile()) + "{not valid json\n");
        assertEquals(1, log.readAll().size());
    }

    @Test
    void missingFileReadsEmpty() {
        assertTrue(AttemptLog.forProfile(dir, "Nobody").readAll().isEmpty());
    }
}
