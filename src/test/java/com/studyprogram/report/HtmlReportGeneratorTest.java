package com.studyprogram.report;

import com.studyprogram.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HtmlReportGeneratorTest {

    @TempDir
    Path dir;

    private Question question(Topic t, int difficulty, QuestionType type) {
        Question.Builder b = Question.builder()
                .id("id-" + t + "-" + difficulty).topic(t).type(type).difficulty(difficulty)
                .prompt("p").answer("a");
        if (type == QuestionType.CODING) {
            b.starterCode("public class X {}").testCode("public class XTest {}");
        }
        return b.build();
    }

    @Test
    void generatesSelfContainedReportWithAllSections() throws Exception {
        StudentProfile profile = new StudentProfile("Ada <script>");
        profile.recordAnswer(question(Topic.LOOPS, 2, QuestionType.CODING), true);
        profile.recordAnswer(question(Topic.VARIABLES, 1, QuestionType.MULTIPLE_CHOICE), false);

        List<AttemptRecord> attempts = new ArrayList<>();
        attempts.add(new AttemptRecord(LocalDateTime.now().minusDays(1),
                question(Topic.LOOPS, 2, QuestionType.CODING), AttemptRecord.OUTCOME_CORRECT, 120, 1));
        attempts.add(new AttemptRecord(LocalDateTime.now(),
                question(Topic.VARIABLES, 1, QuestionType.MULTIPLE_CHOICE),
                AttemptRecord.OUTCOME_INCORRECT, 30, 0));
        attempts.add(new AttemptRecord(LocalDateTime.now(),
                question(Topic.LOOPS, 3, QuestionType.CODING),
                AttemptRecord.OUTCOME_CORRECT, 400, 0,
                List.of("missing semicolon", "cannot find symbol")));

        Path out = new HtmlReportGenerator()
                .generate(profile, attempts, dir.resolve("report.html"));
        String html = Files.readString(out);

        assertTrue(html.contains("Ada &lt;script&gt;"), "profile name must be HTML-escaped");
        assertFalse(html.contains("Ada <script>"));
        assertTrue(html.contains("Activity"));
        assertTrue(html.contains("Accuracy over time"));
        assertTrue(html.contains("Mastery by topic"));
        assertTrue(html.contains("What to work on next"));
        assertTrue(html.contains("<svg"), "charts are rendered as inline SVG");
        assertFalse(html.contains("http://"), "report must be self-contained");
        assertFalse(html.contains("https://"), "report must be self-contained");

        // accessibility: the page must be navigable and understandable without sight or colour
        assertTrue(html.contains("<html lang='en'"), "the document language must be declared");
        assertTrue(html.contains("role='img'") && html.contains("aria-label='"),
                "charts need text alternatives");
        assertTrue(html.contains("Show these figures as a table"),
                "the accuracy chart needs a real data table, not just a label claiming one");
        assertTrue(html.contains("scope='row'") && html.contains("scope='col'"),
                "tables need header semantics");
        assertTrue(html.contains("Compile errors you hit most"),
                "recurring compile errors are the most actionable thing a student can see");
        assertTrue(html.contains("missing semicolon"), "the error category must be named");
        assertTrue(html.contains("belongs at the end of the line"),
                "and explained in the same words the terminal used");

        assertTrue(html.contains("mastered") || html.contains("in progress")
                        || html.contains("needs work"),
                "status must be stated in words, not only encoded in bar colour");
    }

    @Test
    void emptyProfileStillGeneratesAReport() throws Exception {
        Path out = new HtmlReportGenerator()
                .generate(new StudentProfile("Fresh"), List.of(), dir.resolve("empty.html"));
        String html = Files.readString(out);
        assertTrue(html.contains("Fresh"));
        assertTrue(html.contains("What to work on next"));
    }
}
