package com.studyprogram.report;

import com.studyprogram.core.ExamSession;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorksheetTest {

    private static Question mc(String id, String answer) {
        return Question.builder()
                .id(id).topic(Topic.LOOPS).type(QuestionType.MULTIPLE_CHOICE).difficulty(2)
                .prompt("Which loop runs at least once?")
                .choices("while", "do-while", "for", "for-each")
                .answer(answer)
                .explanation("A do-while tests its condition after the body.")
                .build();
    }

    private static Question written() {
        return Question.builder()
                .id("tr-1").topic(Topic.LOOPS).type(QuestionType.TRACING).difficulty(2)
                .prompt("What does this print?")
                .code("for (int i = 0; i < 3; i++) System.out.print(i);")
                .answer("012")
                .build();
    }

    private static final ExamSession.Section UNIT =
            new ExamSession.Section("Unit 1 · Loops", List.of(Topic.LOOPS));

    private static List<ExamSession.Item> paper(Question... questions) {
        return java.util.Arrays.stream(questions)
                .map(q -> new ExamSession.Item(UNIT, q)).toList();
    }

    @Test
    void theWorksheetCarriesNoAnswersAtAll() {
        var pair = Worksheet.render("Quiz 1", paper(mc("q1", "b"), written()));

        assertFalse(pair.worksheet().contains("Answer:"),
                "a worksheet handed to a class must not carry the answers");
        assertFalse(pair.worksheet().contains("do-while tests its condition"),
                "nor the explanations, which give the answer away");
        assertTrue(pair.worksheet().contains("Which loop runs at least once?"));
        assertTrue(pair.worksheet().contains("012") == false,
                "the written answer must not appear either");
    }

    @Test
    void theAnswerKeyGivesTheLetterItsTextAndTheReasoning() {
        var pair = Worksheet.render("Quiz 1", paper(mc("q1", "b")));

        assertTrue(pair.answerKey().contains("B) do-while"),
                "a key of bare letters is useless to mark from: " + pair.answerKey());
        assertTrue(pair.answerKey().contains("do-while tests its condition"));
        assertTrue(pair.answerKey().contains("Which loop runs at least once?"),
                "the key repeats the question so it can be marked without the worksheet");
        assertTrue(pair.answerKey().contains("answer key"));
    }

    @Test
    void questionsAreNumberedAndGroupedBySection() {
        var mixed = List.of(
                new ExamSession.Item(UNIT, mc("q1", "a")),
                new ExamSession.Item(new ExamSession.Section("Unit 2 · Arrays", List.of(Topic.ARRAYS)),
                        mc("q2", "a")));
        String sheet = Worksheet.render("Mixed", mixed).worksheet();

        assertTrue(sheet.indexOf("Unit 1 · Loops") < sheet.indexOf("Unit 2 · Arrays"));
        assertTrue(sheet.contains("<b>1.</b>") && sheet.contains("<b>2.</b>"),
                "numbering must run across sections, not restart");
    }

    @Test
    void thereIsSomewhereToWrite() {
        String sheet = Worksheet.render("Quiz", paper(written())).worksheet();
        assertTrue(sheet.contains("class='writing"), "a paper question needs blank space");
        assertTrue(sheet.contains("Name:") && sheet.contains("Score:"),
                "a handed-out worksheet needs a name line");
    }

    @Test
    void aCodingExerciseBecomesAWriteItOutQuestion() {
        Question coding = Question.builder()
                .id("c1").topic(Topic.LOOPS).type(QuestionType.CODING).difficulty(3)
                .prompt("Write a method that sums an array.")
                .starterCode("public class Sum {}").testCode("public class SumTest {}")
                .answer("public class Sum { }")
                .build();
        String sheet = Worksheet.render("Quiz", paper(coding)).worksheet();

        assertTrue(sheet.contains("Write your solution in the space below"));
        assertFalse(sheet.contains("public class Sum { }"), "the solution stays in the key");
    }

    @Test
    void aReorderingQuestionWorksOnPaper() {
        Question parsons = Question.builder()
                .id("p1").topic(Topic.LOOPS).type(QuestionType.PARSONS).difficulty(2)
                .prompt("Put the program back in order.")
                .shuffledLines(List.of("    }", "int total = 0;", "    total += n;",
                                       "for (int n : xs) {"))
                .answer("int total = 0;\nfor (int n : xs) {\n    total += n;\n    }")
                .build();
        var pair = Worksheet.render("Quiz", paper(parsons));

        assertTrue(pair.worksheet().contains("Write the line numbers in the correct order"));
        assertTrue(pair.worksheet().contains("Order:"), "there must be somewhere to write it");
        assertTrue(pair.worksheet().contains("total += n;"),
                "the scrambled lines have to be printed to be reordered");
        assertEquals("2 4 3 1", answerFrom(pair.answerKey()),
                "the key gives the numbers the student was asked for, not the code");
    }

    /** The text after the key's "Answer:" label. */
    private static String answerFrom(String key) {
        int at = key.indexOf("<b>Answer:</b> ");
        return key.substring(at + "<b>Answer:</b> ".length(), key.indexOf("</p>", at)).trim();
    }

    @Test
    void aFadedExamplesCommentaryIsStrippedFromTheHandoutButKeptOnTheKey() {
        Question faded = Question.builder()
                .id("f1").topic(Topic.LOOPS).type(QuestionType.FADED).difficulty(2)
                .prompt("Supply the missing line.\n\n"
                        + com.studyprogram.questions.FadedExampleDeriver.COMMENTARY_HEADING
                        + "\nThe count is 2 * i + 1, which is the answer to the blank.")
                .code("for (int i = 0; i < n; i++) {\n>>> blank 1 <<<\n}")
                .answer("total += 2 * i + 1;")
                .build();
        var pair = Worksheet.render("Quiz", paper(faded));

        assertFalse(pair.worksheet().contains("2 * i + 1, which is the answer"),
                "the commentary explains the very line the blank asks for: " + pair.worksheet());
        assertTrue(pair.worksheet().contains("Supply the missing line"));
        assertTrue(pair.worksheet().contains("blank 1"), "the listing itself must stay");
        assertTrue(pair.answerKey().contains("2 * i + 1, which is the answer"),
                "on the key it is the explanation, which is what a key is for");
    }

    @Test
    void codingExercisesAreKeptOffPaper() {
        Question coding = Question.builder()
                .id("c1").topic(Topic.LOOPS).type(QuestionType.CODING).difficulty(3)
                .prompt("Write it.").starterCode("class A {}").testCode("class AT {}")
                .answer("class A {}").build();

        assertFalse(Worksheet.SUITS_PAPER.test(coding),
                "nobody writes forty lines in a box, and paper cannot run the tests");
        assertTrue(Worksheet.SUITS_PAPER.test(mc("q", "a")));
        assertTrue(Worksheet.SUITS_PAPER.test(written()));
    }

    @Test
    void bothDocumentsAreSelfContainedAndPrintable() {
        var pair = Worksheet.render("Quiz", paper(mc("q1", "a")));
        for (String doc : List.of(pair.worksheet(), pair.answerKey())) {
            assertTrue(doc.startsWith("<!DOCTYPE html>"));
            assertTrue(doc.contains("<html lang='en'"), "the document language must be declared");
            assertFalse(doc.contains("http://") || doc.contains("https://"),
                    "a worksheet must print without a network");
            assertTrue(doc.contains("@media print"), "it exists to be printed");
            assertTrue(doc.contains("page-break-inside: avoid"),
                    "a question split across two pages is a bad worksheet");
        }
    }

    @Test
    void studentTextIsEscaped() {
        Question nasty = Question.builder()
                .id("x").topic(Topic.LOOPS).type(QuestionType.MULTIPLE_CHOICE).difficulty(1)
                .prompt("What is <script>alert(1)</script>?")
                .choice("a & b").choice("c").answer("a").build();
        String sheet = Worksheet.render("Q", paper(nasty)).worksheet();

        assertTrue(sheet.contains("&lt;script&gt;"));
        assertFalse(sheet.contains("<script>alert"));
        assertTrue(sheet.contains("a &amp; b"));
    }
}
