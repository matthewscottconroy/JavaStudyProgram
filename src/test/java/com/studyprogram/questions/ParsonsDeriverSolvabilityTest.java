package com.studyprogram.questions;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.grading.ParsonsGrader;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Content gate for derived Parsons puzzles: every one must be solvable through the real grader.
 *
 * <p>This exists because of a genuine near-miss. Extending derivation to method bodies produced
 * puzzles whose first line was indented, and {@code Question.Builder.answer()} trims the stored
 * answer — so the grader's string comparison could never match and every such puzzle was
 * unwinnable. Whole-program puzzles start at column 0 and hid the bug. Solving each puzzle the way
 * a correct student would is the only check that would have caught it.
 */
class ParsonsDeriverSolvabilityTest {

    @Test
    void everyDerivedPuzzleIsSolvableThroughTheGrader() {
        List<Question> parsons = new QuestionBank()
                .getQuestionsForTopics(List.of(Topic.values())).stream()
                .filter(q -> q.getType() == QuestionType.PARSONS)
                .toList();
        assertFalse(parsons.isEmpty(), "expected derived Parsons puzzles");

        ParsonsGrader grader = new ParsonsGrader();
        List<String> unsolvable = new ArrayList<>();
        for (Question p : parsons) {
            String order = correctOrderFor(p);
            if (order == null) {
                unsolvable.add(p.getId() + " (a solution line is missing from the shuffle)");
            } else if (!grader.grade(p, order).correct()) {
                unsolvable.add(p.getId() + " (grader rejected the correct order)");
            }
        }
        assertTrue(unsolvable.isEmpty(),
                unsolvable.size() + " unsolvable puzzle(s): " + unsolvable.subList(
                        0, Math.min(5, unsolvable.size())));
    }

    @Test
    void puzzlesAreWellFormed() {
        for (Question p : new QuestionBank().getQuestionsForTopics(List.of(Topic.values()))) {
            if (p.getType() != QuestionType.PARSONS) continue;
            List<String> answerLines = List.of(p.getAnswer().split("\n"));
            assertEquals(answerLines.size(), p.getShuffledLines().size(),
                    p.getId() + ": shuffle must hold exactly the answer's lines");
            assertTrue(answerLines.size() >= 4, p.getId() + ": too small to be a puzzle");
            assertNotEquals(answerLines, p.getShuffledLines(),
                    p.getId() + ": the puzzle must actually be shuffled");
            assertEquals(0, netBraces(p.getAnswer()),
                    p.getId() + ": a puzzle must be brace-balanced to make sense");
        }
    }

    /** The line numbers a student who knows the answer would type, or null if unreachable. */
    private static String correctOrderFor(Question p) {
        List<String> shuffled = p.getShuffledLines();
        boolean[] used = new boolean[shuffled.size()];
        StringBuilder order = new StringBuilder();
        for (String want : p.getAnswer().split("\n")) {
            int found = -1;
            for (int i = 0; i < shuffled.size(); i++) {
                if (!used[i] && shuffled.get(i).strip().equals(want.strip())) { found = i; break; }
            }
            if (found < 0) return null;
            used[found] = true;
            order.append(found + 1).append(' ');
        }
        return order.toString().trim();
    }

    private static int netBraces(String source) {
        int net = 0;
        boolean inString = false, inChar = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (inString) {
                if (c == '\\') i++;
                else if (c == '"') inString = false;
            } else if (inChar) {
                if (c == '\\') i++;
                else if (c == '\'') inChar = false;
            } else if (c == '"') inString = true;
            else if (c == '\'') inChar = true;
            else if (c == '{') net++;
            else if (c == '}') net--;
        }
        return net;
    }
}
