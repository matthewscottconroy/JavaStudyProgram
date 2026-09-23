package com.studyprogram.llm;

import com.studyprogram.model.Misconception;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;

import java.util.ArrayList;
import java.util.List;

/**
 * Help assembled from what the program already knows, for students with no AI configured.
 *
 * <p>Optional AI was the founding constraint of this program, and the help paths quietly broke it:
 * pressing "explain" without an API key printed an advertisement for one, and pressing "hint"
 * printed "No hints available" for the two thirds of questions nobody had written hints for. That
 * is the precise moment a stuck student asks for help, and the answer was nothing.
 *
 * <p>Nothing here is generated. Every sentence is assembled from material already in the bank —
 * the topic's own description, its place in the prerequisite graph, the question's type, and the
 * misconception catalogue — which is why it works offline, costs nothing, and never invents a
 * fact. It is not as good as a model that has read the student's code. It is a great deal better
 * than a blank.
 *
 * <p>The hard rule is that a hint narrows the search without ending it. Nothing here may state the
 * answer, name the correct choice, or quote the solution; {@code HintQualityTest} enforces that
 * against the whole bank.
 */
public final class OfflineHelp {

    private OfflineHelp() {}

    // ── Explaining a concept ─────────────────────────────────────────────────

    /**
     * A short study note on a topic: what it is, what it is built on, and what it leads to.
     * This is the offline answer to "explain this to me".
     */
    public static String explainConcept(Topic topic) {
        if (topic == null) return "";
        StringBuilder out = new StringBuilder();
        out.append(topic.displayName).append(" — ").append(topic.description);

        List<Topic> prerequisites = topic.getPrerequisites();
        if (!prerequisites.isEmpty()) {
            out.append("\n\nIt builds on:");
            for (Topic p : prerequisites) {
                out.append("\n  • ").append(p.displayName).append(" — ").append(p.description);
            }
            out.append("\nIf this topic is not making sense, one of those is usually why.");
        }

        List<Topic> unlocks = dependents(topic);
        if (!unlocks.isEmpty()) {
            out.append("\n\nWhat it leads to: ");
            out.append(String.join(", ", unlocks.stream().limit(6).map(t -> t.displayName).toList()));
            if (unlocks.size() > 6) out.append(", and ").append(unlocks.size() - 6).append(" more");
            out.append('.');
        }
        return out.toString();
    }

    /** Topics that list this one as a prerequisite, directly. */
    private static List<Topic> dependents(Topic topic) {
        List<Topic> found = new ArrayList<>();
        for (Topic other : Topic.visibleValues()) {
            if (other.getPrerequisites().contains(topic)) found.add(other);
        }
        return found;
    }

    // ── Hinting ──────────────────────────────────────────────────────────────

    /**
     * The next hint for a question, given how many the student has already taken.
     *
     * <p>Authored hints come first and in order, because somebody wrote them for this exact
     * question. When they run out — or were never written — the ladder falls back to advice
     * derived from the question's type and topic, which is general but never wrong.
     */
    public static String hint(Question question, int alreadyGiven) {
        if (question == null) return "";
        List<String> authored = question.getHints();
        if (alreadyGiven < authored.size()) return authored.get(alreadyGiven);

        List<String> derived = derivedHints(question);
        int index = alreadyGiven - authored.size();
        // Past the end, the closing rung stands rather than cycling through earlier hints.
        return derived.get(Math.min(index, derived.size() - 1));
    }

    /**
     * Advice built from the question itself, in widening order: what it is about, what it rests
     * on, and how to attack this kind of question.
     */
    static List<String> derivedHints(Question question) {
        List<String> hints = new ArrayList<>();
        Topic topic = question.getTopic();

        // A topic description can happen to contain the answer — "OO Design Patterns" is
        // described as "Singleton, Factory, Strategy, ..." and some of its questions answer
        // exactly "Singleton". Quoting it there would hand the answer over, so it is dropped.
        String opener = "This is a " + question.getType().displayName.toLowerCase()
                + " question about " + topic.displayName + ".";
        hints.add(givesAwayAnswer(topic.description, question)
                ? opener + " Read it with that topic in mind."
                : opener + " " + topic.description);

        if (!topic.getPrerequisites().isEmpty()) {
            Topic first = topic.getPrerequisites().get(0);
            String rests = "It rests on " + first.displayName + ": " + first.description
                    + " If that part is shaky, this question will be too.";
            if (!givesAwayAnswer(rests, question)) hints.add(rests);
        }

        String strategy = strategyFor(question.getType());
        if (strategy != null) hints.add(strategy);

        // A tagged question knows which wrong ideas are in play. Naming the trap without naming
        // the answer is exactly what a good hint does.
        List<String> traps = new ArrayList<>();
        for (Misconception m : question.getDistractors().values()) {
            if (!traps.contains(m.summary)) traps.add(m.summary);
        }
        if (!traps.isEmpty()) {
            hints.add("Some of the choices here are traps. Watch for: " + String.join("; ", traps)
                    + ".");
        }

        // A last rung that always exists. Topics at the bottom of the graph have no prerequisite
        // to point at and untagged questions have no traps to name, so without this a root-topic
        // question ran out after two hints and started repeating itself.
        hints.add("Put the question in your own words and say what a correct answer would have "
                + "to do. If it still will not come, skip it — it comes back, and the answer "
                + "lands better after you have seen the idea somewhere else.");
        return hints;
    }

    /** True when this text would hand the student the answer they were asked for. */
    private static boolean givesAwayAnswer(String text, Question question) {
        String answer = question.getAnswer() == null ? "" : question.getAnswer().strip();
        // Single letters are choice labels, not answers worth protecting.
        return answer.length() >= 4 && text.contains(answer);
    }

    /** How to attack a question of this shape. */
    private static String strategyFor(QuestionType type) {
        return switch (type) {
            case TRACING -> "Work through it one line at a time and write down what each variable "
                    + "holds after each statement. Do not run it in your head — the whole point "
                    + "is that your head takes shortcuts.";
            case DEBUGGING -> "Read what the prompt says the code should do, then read what the "
                    + "code actually does. The bug is the first place those two stories differ.";
            case MULTIPLE_CHOICE, CODE_GENERATION -> "Work by elimination: rule out the choices "
                    + "you can prove wrong, and say why for each one. You often only need to be "
                    + "certain about the ones you rejected.";
            case CLOZE -> "Look at what surrounds the blank. What type must the missing code "
                    + "produce for the lines on either side of it to make sense?";
            case PARSONS -> "Find the line that must come first — usually a declaration — and the "
                    + "line that must come last. Then place the lines that depend on each other.";
            case FADED -> "Each blank is surrounded by working code. Read the lines above and "
                    + "below it and ask what has to happen between them.";
            case CODING -> "Write the smallest version that compiles, run the tests, and let the "
                    + "failures tell you what to do next. A failing test is a to-do list.";
        };
    }

    // ── Why an answer was wrong ──────────────────────────────────────────────

    /**
     * Offline commentary on a wrong answer. Returns empty when there is nothing to add beyond
     * what the grader already said, rather than padding the screen.
     */
    public static String explainAnswer(Question question, String studentAnswer) {
        if (question == null) return "";
        return question.misconceptionFor(studentAnswer)
                .map(m -> m.summary + "\n" + m.explanation)
                .orElse("");
    }
}
