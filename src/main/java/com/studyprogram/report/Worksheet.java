package com.studyprogram.report;

import com.studyprogram.core.ExamSession;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;

import java.time.LocalDate;
import java.util.List;

/**
 * Renders a set of questions as a worksheet to print, plus a separate answer key.
 *
 * <p>Everything else this program does assumes a computer and a terminal. A lot of teaching does
 * not: a quiz handed out at the start of class, a page to work through on the bus, a study sheet
 * for a student whose laptop died the week before the exam. The bank already holds thousands of
 * verified questions and the exam builder already knows how to draw a balanced paper from them;
 * this just puts that on paper.
 *
 * <p>Two documents come out of one call, and the split matters: the worksheet carries no answers
 * at all, so it can be handed to a class, and the key is a separate file the instructor keeps.
 * The key repeats each question's prompt, because an answer key that is a bare list of letters is
 * useless to mark from.
 */
public final class Worksheet {

    /** A worksheet and its answer key, each a complete HTML document. */
    public record Pair(String worksheet, String answerKey) {}

    /**
     * Which questions belong on paper.
     *
     * <p>Everything except the exercises that need a compiler. "Write a class that reformats Java
     * source, then run the tests" is a fine exercise and a terrible worksheet question — nobody
     * writes forty lines in a box on a handout, and the part that teaches it (running the tests
     * and fixing what breaks) is exactly the part paper cannot do. Reordering scrambled lines,
     * filling in a blank, tracing output and finding the bug all work perfectly on paper.
     */
    public static final java.util.function.Predicate<Question> SUITS_PAPER =
            q -> q.getType() != QuestionType.CODING;

    private Worksheet() {}

    public static Pair render(String title, List<ExamSession.Item> paper) {
        return new Pair(worksheet(title, paper), answerKey(title, paper));
    }

    private static String worksheet(String title, List<ExamSession.Item> paper) {
        StringBuilder h = head(title);
        h.append("<h1>").append(esc(title)).append("</h1>")
         .append("<p class='meta'>Name: ").append(rule(28))
         .append("&nbsp;&nbsp;&nbsp;Date: ").append(rule(14))
         .append("&nbsp;&nbsp;&nbsp;Score: ").append(rule(8)).append("</p>");

        String section = null;
        int number = 0;
        for (ExamSession.Item item : paper) {
            if (!item.section().title().equals(section)) {
                section = item.section().title();
                h.append("<h2>").append(esc(section)).append("</h2>");
            }
            h.append(question(++number, item.question(), false));
        }
        h.append("</body></html>");
        return h.toString();
    }

    private static String answerKey(String title, List<ExamSession.Item> paper) {
        StringBuilder h = head(title + " — answer key");
        h.append("<h1>").append(esc(title)).append(" <span class='key'>answer key</span></h1>")
         .append("<p class='meta'>").append(paper.size()).append(" questions · generated ")
         .append(LocalDate.now()).append("</p>");

        String section = null;
        int number = 0;
        for (ExamSession.Item item : paper) {
            if (!item.section().title().equals(section)) {
                section = item.section().title();
                h.append("<h2>").append(esc(section)).append("</h2>");
            }
            h.append(question(++number, item.question(), true));
        }
        h.append("</body></html>");
        return h.toString();
    }

    private static String question(int number, Question q, boolean withAnswer) {
        StringBuilder h = new StringBuilder("<div class='q'>");
        h.append("<p class='prompt'><b>").append(number).append(".</b> ")
         .append(esc(promptFor(q, withAnswer)).replace("\n", "<br>")).append("</p>");

        if (q.hasCode()) {
            h.append("<pre>").append(esc(q.getCode())).append("</pre>");
        }
        if (q.isMultipleChoice()) {
            h.append("<ol class='choices' type='A'>");
            for (String choice : q.getChoices()) {
                h.append("<li>").append(esc(choice)).append("</li>");
            }
            h.append("</ol>");
        } else if (q.getType() == QuestionType.PARSONS) {
            // A reordering question works on paper by numbering the scrambled lines and asking
            // for the sequence — which is how Parsons problems were taught before they were code.
            h.append("<p class='hint'>Write the line numbers in the correct order.</p>")
             .append("<ol class='lines'>");
            for (String line : q.getShuffledLines()) {
                h.append("<li><code>").append(esc(line)).append("</code></li>");
            }
            h.append("</ol><p class='order'>Order: ").append(rule(30)).append("</p>");
        } else if (q.getType() == QuestionType.CODING) {
            // Kept working in case a caller asks for one anyway; SUITS_PAPER excludes them.
            h.append("<p class='hint'>Write your solution in the space below.</p>")
             .append("<div class='writing'></div>");
        } else {
            h.append("<div class='writing short'></div>");
        }

        if (withAnswer) {
            h.append("<p class='answer'><b>Answer:</b> ").append(esc(answerText(q))).append("</p>");
            if (q.getExplanation() != null && !q.getExplanation().isBlank()) {
                h.append("<p class='why'>").append(esc(q.getExplanation())).append("</p>");
            }
        }
        return h.append("</div>").toString();
    }

    /**
     * The prompt as it should appear. A faded worked example carries a commentary explaining how
     * the solution works, which is exactly right on screen and a giveaway on a quiz — it spells
     * out the lines the blanks ask for. It stays on the answer key, where the explanation belongs.
     */
    private static String promptFor(Question q, boolean withAnswer) {
        String prompt = q.getPrompt() == null ? "" : q.getPrompt();
        if (withAnswer || q.getType() != QuestionType.FADED) return prompt;
        int at = prompt.indexOf(com.studyprogram.questions.FadedExampleDeriver.COMMENTARY_HEADING);
        return at < 0 ? prompt : prompt.substring(0, at).stripTrailing();
    }

    /** The answer as a marker would want it: the letter and its text for a choice question. */
    private static String answerText(Question q) {
        if (q.getType() == QuestionType.PARSONS) {
            // The marker needs the numbers the student was asked for, not the code they spell out.
            List<String> shuffled = q.getShuffledLines();
            StringBuilder order = new StringBuilder();
            boolean[] used = new boolean[shuffled.size()];
            for (String want : q.getAnswer().split("\n")) {
                for (int i = 0; i < shuffled.size(); i++) {
                    if (!used[i] && shuffled.get(i).strip().equals(want.strip())) {
                        used[i] = true;
                        if (order.length() > 0) order.append(' ');
                        order.append(i + 1);
                        break;
                    }
                }
            }
            return order.toString();
        }
        if (q.isMultipleChoice()) {
            String letter = q.getAnswer() == null ? "" : q.getAnswer().trim().toUpperCase();
            int index = letter.isEmpty() ? -1 : letter.charAt(0) - 'A';
            if (index >= 0 && index < q.getChoices().size()) {
                return letter + ") " + q.getChoices().get(index);
            }
            return letter;
        }
        return q.getAnswer() == null ? "" : q.getAnswer();
    }

    private static StringBuilder head(String title) {
        return new StringBuilder(16_000)
                .append("<!DOCTYPE html><html lang='en'><head><meta charset='utf-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
                .append("<title>").append(esc(title)).append("</title><style>")
                .append(CSS).append("</style></head><body>");
    }

    private static String rule(int chars) {
        return "<span class='fill'>" + "&nbsp;".repeat(chars) + "</span>";
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final String CSS = """
            body { font-family: Georgia, 'Times New Roman', serif; color: #000; background: #fff;
                   margin: 0 auto; padding: 24px; max-width: 46em; line-height: 1.45; }
            h1 { font-size: 1.5em; margin-bottom: 2px; }
            h2 { font-size: 1.05em; margin: 22px 0 6px; border-bottom: 1px solid #000;
                 padding-bottom: 2px; text-transform: uppercase; letter-spacing: 0.06em; }
            .key { font-size: 0.7em; letter-spacing: 0.1em; text-transform: uppercase;
                   border: 1px solid #000; padding: 1px 6px; vertical-align: middle; }
            .meta { font-size: 0.9em; margin-top: 0; }
            .fill { border-bottom: 1px solid #000; }
            .q { margin: 0 0 14px; page-break-inside: avoid; break-inside: avoid; }
            .prompt { margin: 0 0 6px; }
            pre { font-family: 'DejaVu Sans Mono', Consolas, monospace; font-size: 0.85em;
                  border: 1px solid #999; padding: 8px; overflow-x: auto; background: #fafafa; }
            .choices { margin: 4px 0 0 1.2em; padding: 0; }
            .choices li { margin: 2px 0; }
            .lines { margin: 4px 0 0 1.6em; padding: 0; font-size: 0.85em; }
            .lines code { font-family: 'DejaVu Sans Mono', Consolas, monospace;
                          white-space: pre; }
            .order { margin: 6px 0 0; }
            .writing { border-bottom: 1px solid #bbb; height: 8em; }
            .writing.short { height: 3.2em; }
            .hint { font-size: 0.85em; font-style: italic; margin: 4px 0; }
            .answer { margin: 4px 0 0; }
            .why { font-size: 0.9em; margin: 2px 0 0; color: #333; }
            @media print {
              body { padding: 0; max-width: none; font-size: 11pt; }
              .writing { border-bottom: 1px solid #000; }
            }
            """;
}
