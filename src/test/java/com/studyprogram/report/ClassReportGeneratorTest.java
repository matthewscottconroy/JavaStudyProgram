package com.studyprogram.report;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.model.*;
import com.studyprogram.storage.AttemptLog;
import com.studyprogram.storage.JsonProfileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClassReportGeneratorTest {

    @TempDir
    Path dir;

    @Test
    void aggregatesAllProfilesIntoOneReport() throws Exception {
        JsonProfileStorage storage = new JsonProfileStorage(dir.resolve("profiles"));

        StudentProfile alice = new StudentProfile("Alice");
        alice.getOrCreatePerformance(Topic.LOOPS).record("lp-x", true, 3);
        storage.save(alice);
        StudentProfile bob = new StudentProfile("Bob");
        bob.getOrCreatePerformance(Topic.ARRAYS).record("arr-x", false, 3);
        storage.save(bob);

        Question q = Question.builder()
                .id("lp-x").topic(Topic.LOOPS).type(QuestionType.TRACING).difficulty(3)
                .prompt("p").answer("a").build();
        AttemptLog.forProfile(storage.directory(), "Alice")
                .append(new AttemptRecord(LocalDateTime.now(), q,
                        AttemptRecord.OUTCOME_CORRECT, 12, 0));

        // Both students fought the same compile error; only Alice hit the other one.
        Question coding = Question.builder()
                .id("lp-code").topic(Topic.LOOPS).type(QuestionType.CODING).difficulty(3)
                .prompt("p").answer("a").starterCode("class X {}").testCode("class XTest {}").build();
        AttemptLog.forProfile(storage.directory(), "Alice")
                .append(new AttemptRecord(LocalDateTime.now(), coding,
                        AttemptRecord.OUTCOME_CORRECT, 300, 0,
                        java.util.List.of("cannot find symbol", "missing return")));
        AttemptLog.forProfile(storage.directory(), "Bob")
                .append(new AttemptRecord(LocalDateTime.now(), coding,
                        AttemptRecord.OUTCOME_INCORRECT, 300, 0,
                        java.util.List.of("cannot find symbol")));

        Path out = new ClassReportGenerator()
                .generate(storage, new QuestionBank(), dir.resolve("class-report.html"));
        String html = Files.readString(out);

        assertTrue(html.contains("Alice"));
        assertTrue(html.contains("Bob"));
        assertTrue(html.contains("2 profile(s)"));
        assertTrue(html.contains("Class-wide weak spots"));
        assertTrue(html.contains("Arrays"), "Bob's weak topic should surface");

        assertTrue(html.contains("Where the class gets stuck"));
        int shared = html.indexOf("cannot find symbol");
        int single = html.indexOf("missing return");
        assertTrue(shared >= 0 && single >= 0, "both errors should be listed");
        assertTrue(shared < single,
                "the error two students hit must outrank the one only Alice hit");
    }
}
