package com.studyprogram.storage;

import com.studyprogram.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTransferTest {

    @TempDir Path home;

    private Question question() {
        return Question.builder()
                .id("lp-code-01").topic(Topic.LOOPS).type(QuestionType.CODING).difficulty(2)
                .prompt("p").answer("a")
                .starterCode("public class X {}").testCode("public class XTest {}")
                .build();
    }

    private JsonProfileStorage storageAt(String dir) throws Exception {
        return new JsonProfileStorage(home.resolve(dir));
    }

    @Test
    void bundleRoundTripsProfileAndAttempts() throws Exception {
        JsonProfileStorage source = storageAt("source");
        StudentProfile ada = new StudentProfile("Ada");
        ada.recordAnswer(question(), true);
        ada.getBossesCleared().add(2);
        source.save(ada);
        AttemptLog.forProfile(source.directory(), "Ada").append(
                new AttemptRecord(LocalDateTime.now(), question(),
                        AttemptRecord.OUTCOME_CORRECT, 42, 1));

        Path bundle = ProfileTransfer.export(source, "Ada", home.resolve("ada-bundle.json"));
        assertTrue(Files.exists(bundle));

        JsonProfileStorage target = storageAt("target");
        String imported = ProfileTransfer.importBundle(target, bundle);
        assertEquals("Ada", imported);

        StudentProfile restored = target.load("Ada").orElseThrow();
        assertEquals(1, restored.getTotalQuestionsAnswered());
        assertTrue(restored.getBossesCleared().contains(2));
        assertTrue(restored.getPerformance().containsKey(Topic.LOOPS));

        List<AttemptRecord> attempts = AttemptLog.forProfile(target.directory(), "Ada").readAll();
        assertEquals(1, attempts.size());
        assertEquals("lp-code-01", attempts.get(0).getQuestionId());
        assertEquals(42, attempts.get(0).getSeconds());
    }

    @Test
    void importingTwiceNeverOverwritesAnExistingStudent() throws Exception {
        JsonProfileStorage source = storageAt("source");
        source.save(new StudentProfile("Sam"));
        Path bundle = ProfileTransfer.export(source, "Sam", home.resolve("sam.json"));

        JsonProfileStorage target = storageAt("target");
        assertEquals("Sam", ProfileTransfer.importBundle(target, bundle));
        assertEquals("Sam (2)", ProfileTransfer.importBundle(target, bundle));
        assertEquals("Sam (3)", ProfileTransfer.importBundle(target, bundle));
        assertEquals(3, target.listProfileNames().size());
    }

    @Test
    void aWholeFolderOfBundlesImportsAtOnce() throws Exception {
        JsonProfileStorage source = storageAt("source");
        Path inbox = home.resolve("inbox");
        Files.createDirectories(inbox);
        for (String name : List.of("Ada", "Bo", "Cy")) {
            source.save(new StudentProfile(name));
            ProfileTransfer.export(source, name, inbox.resolve(name + ".json"));
        }

        JsonProfileStorage target = storageAt("target");
        List<String> imported = ProfileTransfer.importAll(target, inbox);
        assertEquals(3, imported.size());
        assertTrue(target.listProfileNames().containsAll(List.of("Ada", "Bo", "Cy")));
    }

    @Test
    void nonBundleFilesAreRejectedClearly() throws Exception {
        Path junk = home.resolve("junk.json");
        Files.writeString(junk, "{\"hello\":\"world\"}");
        assertThrows(java.io.IOException.class,
                () -> ProfileTransfer.importBundle(storageAt("target"), junk));
    }
}
