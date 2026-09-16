package com.studyprogram.questions;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A hint should point at the idea the student is missing, not paste the answer.
 *
 * <p>Naming an API or a concept is teaching ("Arrays.copyOf makes a copy rather than an alias");
 * reproducing a whole statement from the reference solution is just giving it away. Short
 * fragments are fine — sometimes the fragment <em>is</em> the concept.
 */
class HintQualityTest {

    /** Long enough that reproducing it verbatim is handing over the answer, not naming an idea. */
    private static final int GIVEAWAY_LENGTH = 25;

    @Test
    void noHintPastesALineOfItsOwnSolution() {
        List<String> offenders = new ArrayList<>();
        for (Question q : new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))) {
            if (q.getType() != QuestionType.CODING) continue;
            for (String solutionLine : solutionBody(q)) {
                if (solutionLine.length() <= GIVEAWAY_LENGTH) continue;
                for (String hint : q.getHints()) {
                    if (hint.contains(solutionLine)) {
                        offenders.add(q.getId() + " -> \"" + solutionLine + "\"");
                    }
                }
            }
        }
        assertTrue(offenders.isEmpty(), offenders.size() + " hint(s) give away a solution line: "
                + offenders.subList(0, Math.min(5, offenders.size())));
    }

    @Test
    void everyCodingExerciseOffersProgressiveHelp() {
        for (Question q : new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))) {
            if (q.getType() != QuestionType.CODING) continue;
            assertFalse(q.getHints().isEmpty(), q.getId() + " has no hints");
            assertFalse(q.getExplanation().isBlank(), q.getId() + " has no explanation");
        }
    }

    /** Solution statements, skipping boilerplate that would match harmlessly. */
    private static List<String> solutionBody(Question q) {
        String source = q.isMultiFile()
                ? String.join("\n", q.getSolutionFiles().values())
                : q.getAnswer();
        List<String> body = new ArrayList<>();
        for (String raw : source.split("\n")) {
            String line = raw.strip();
            if (line.isEmpty()
                    || line.startsWith("//") || line.startsWith("*") || line.startsWith("/*")
                    || line.startsWith("public class") || line.startsWith("import")
                    || line.equals("}") || line.equals("{")) {
                continue;
            }
            body.add(line);
        }
        return body;
    }
}
