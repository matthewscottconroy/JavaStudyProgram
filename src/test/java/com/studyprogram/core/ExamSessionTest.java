package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExamSessionTest {

    private static Question q(String id, Topic topic, QuestionType type, int difficulty) {
        Question.Builder b = Question.builder()
                .id(id).topic(topic).type(type).difficulty(difficulty).prompt("p").answer("a");
        if (type == QuestionType.CODING) b.starterCode("class X {}").testCode("class XT {}");
        return b.build();
    }

    private static QuestionBank bank() {
        List<Question> questions = new ArrayList<>();
        for (Topic t : List.of(Topic.VARIABLES, Topic.LOOPS, Topic.ARRAYS)) {
            for (int i = 1; i <= 6; i++) {
                questions.add(q(t + "-mc-" + i, t, QuestionType.MULTIPLE_CHOICE, (i % 5) + 1));
                questions.add(q(t + "-code-" + i, t, QuestionType.CODING, 3));
            }
        }
        return QuestionBank.of(questions);
    }

    private static final List<ExamSession.Section> SECTIONS = List.of(
            new ExamSession.Section("Unit 1 · Basics", List.of(Topic.VARIABLES)),
            new ExamSession.Section("Unit 2 · Control", List.of(Topic.LOOPS)),
            new ExamSession.Section("Unit 3 · Data", List.of(Topic.ARRAYS)));

    @Test
    void thePaperIsTheRequestedLengthAndSpreadsAcrossEverySection() {
        List<ExamSession.Item> paper = ExamSession.build(bank(), SECTIONS, 12, new Random(1));

        assertEquals(12, paper.size());
        Set<String> sections = new LinkedHashSet<>();
        paper.forEach(i -> sections.add(i.section().title()));
        assertEquals(3, sections.size(), "every section must be examined");

        Set<String> ids = new LinkedHashSet<>();
        paper.forEach(i -> ids.add(i.question().getId()));
        assertEquals(12, ids.size(), "no question may appear twice on one paper");

        for (ExamSession.Item item : paper) {
            assertTrue(item.section().topics().contains(item.question().getTopic()),
                    "a question must be scored under a section that covers its topic");
        }
    }

    @Test
    void anExamMakesStudentsWriteCodeAndNotOnlyRecogniseIt() {
        List<ExamSession.Item> paper = ExamSession.build(bank(), SECTIONS, 12, new Random(7));
        long coding = paper.stream()
                .filter(i -> i.question().getType() == QuestionType.CODING).count();
        assertTrue(coding >= 3, "expected roughly 40% coding, got " + coding + "/12");
    }

    @Test
    void thePaperIsReproducibleFromItsSeedSoAClassCanSitTheSameOne() {
        List<String> first = ExamSession.build(bank(), SECTIONS, 9, new Random(42))
                .stream().map(i -> i.question().getId()).toList();
        List<String> again = ExamSession.build(bank(), SECTIONS, 9, new Random(42))
                .stream().map(i -> i.question().getId()).toList();
        assertEquals(first, again);
    }

    @Test
    void scoringReportsEachSectionSeparatelyAndNamesTheWeakOnes() {
        List<ExamSession.Item> paper = ExamSession.build(bank(), SECTIONS, 12, new Random(3));

        // Everything in the first section right, nothing in the last.
        Set<String> correct = new LinkedHashSet<>();
        Set<String> attempted = new LinkedHashSet<>();
        for (ExamSession.Item item : paper) {
            attempted.add(item.question().getId());
            if (item.section().title().startsWith("Unit 1")) correct.add(item.question().getId());
        }

        List<ExamSession.SectionScore> scores = ExamSession.score(paper, correct, attempted);
        assertEquals(3, scores.size());
        assertEquals("solid", scores.get(0).verdict());
        assertEquals("needs work", scores.get(2).verdict());
        assertEquals(1.0, scores.get(0).ratio(), 1e-9);
        assertEquals(0.0, scores.get(2).ratio(), 1e-9);

        List<String> weak = ExamSession.weakestSections(scores);
        assertFalse(weak.contains(scores.get(0).title()), "a perfect section is not a weak spot");
        assertTrue(weak.contains(scores.get(2).title()));
    }

    @Test
    void questionsNeverReachedCountAgainstTheScoreButAreReportedSeparately() {
        List<ExamSession.Item> paper = ExamSession.build(bank(), SECTIONS, 12, new Random(5));
        // The student ran out of time after the first four questions and got them all right.
        Set<String> attempted = new LinkedHashSet<>();
        paper.subList(0, 4).forEach(i -> attempted.add(i.question().getId()));

        List<ExamSession.SectionScore> scores = ExamSession.score(paper, attempted, attempted);
        assertEquals(4.0 / 12, ExamSession.overall(scores), 1e-9,
                "unreached questions still count against the paper");

        int unreached = 0;
        for (ExamSession.SectionScore s : scores) unreached += s.total() - s.answered();
        assertEquals(8, unreached, "and are reported as unreached rather than as wrong answers");
    }

    @Test
    void worldSectionsCoverTheCurriculumWhenThereIsNoSyllabus() {
        List<ExamSession.Section> worlds = ExamSession.worldSections(1, 3);
        assertEquals(3, worlds.size());
        assertTrue(worlds.get(0).title().startsWith("World 1"));
        for (ExamSession.Section s : worlds) assertFalse(s.topics().isEmpty());
    }

    @Test
    void unitSectionsComeFromTheCourseOverlayAndSkipUnitsOutsideTheRange() throws Exception {
        java.nio.file.Path file = java.nio.file.Files.createTempFile("course", ".json");
        java.nio.file.Files.writeString(file, """
                { "name": "Java II", "units": [
                    { "number": 1, "title": "Review",  "topics": ["variables"] },
                    { "number": 2, "title": "Arrays",  "topics": ["arrays"] },
                    { "number": 3, "title": "Objects", "topics": ["loops"] } ] }
                """);
        CourseOverlay course = CourseOverlay.load(file);

        List<ExamSession.Section> sections = ExamSession.sectionsFor(course, 1, 2);
        assertEquals(2, sections.size());
        assertEquals("Unit 1 · Review", sections.get(0).title());
        assertEquals(List.of(Topic.ARRAYS), sections.get(1).topics());
    }

    @Test
    void codingQuestionsGetMoreTimeThanMultipleChoice() {
        List<ExamSession.Item> allCoding = ExamSession.build(
                QuestionBank.of(List.of(
                        q("c1", Topic.LOOPS, QuestionType.CODING, 3),
                        q("c2", Topic.LOOPS, QuestionType.CODING, 3))),
                List.of(new ExamSession.Section("S", List.of(Topic.LOOPS))), 2, new Random(1));
        List<ExamSession.Item> allChoice = ExamSession.build(
                QuestionBank.of(List.of(
                        q("m1", Topic.LOOPS, QuestionType.MULTIPLE_CHOICE, 3),
                        q("m2", Topic.LOOPS, QuestionType.MULTIPLE_CHOICE, 3))),
                List.of(new ExamSession.Section("S", List.of(Topic.LOOPS))), 2, new Random(1));

        assertTrue(ExamSession.suggestedMinutes(allCoding)
                        > ExamSession.suggestedMinutes(allChoice),
                "two programs to write must be allowed more time than two questions to tick");
    }

    @Test
    void anEmptyRangeYieldsNoPaperRatherThanAnEmptyOne() {
        assertTrue(ExamSession.build(bank(), List.of(), 10, new Random(1)).isEmpty());
        assertTrue(ExamSession.build(bank(), SECTIONS, 0, new Random(1)).isEmpty());
    }
}
