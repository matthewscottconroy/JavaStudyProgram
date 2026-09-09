package com.studyprogram.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.studyprogram.model.Question;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holding pen for AI-generated questions.
 *
 * <p>Generated questions never join the live bank directly: they have not passed the content gate
 * that every shipped question must clear, and a model can produce a question whose stated answer is
 * wrong. Instead each one is written to {@code data/generated/<topic>/} for a human to review and
 * move into {@code data/questions/} if it is any good. Nothing here is loaded at startup.
 */
public final class GeneratedQuestionQueue {

    public static final Path DEFAULT_DIR = Path.of("data", "generated");

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path root;

    public GeneratedQuestionQueue() { this(DEFAULT_DIR); }

    public GeneratedQuestionQueue(Path root) { this.root = root; }

    /** Writes one generated question for later human review; failures are non-fatal. */
    public void offer(Question q) {
        try {
            Path dir = root.resolve(q.getTopic().dirSlug());
            Files.createDirectories(dir);
            ObjectNode node = MAPPER.createObjectNode();
            node.put("id", q.getId());
            node.put("type", q.getType().name());
            node.put("difficulty", q.getDifficulty());
            node.put("prompt", q.getPrompt());
            if (q.getCode() != null) node.put("code", q.getCode());
            if (!q.getChoices().isEmpty()) {
                var choices = node.putArray("choices");
                q.getChoices().forEach(choices::add);
            }
            node.put("answer", q.getAnswer());
            node.put("explanation", q.getExplanation());
            node.put("_generated", true);
            node.put("_review", "AI-generated and unverified. Check the answer, then move this file "
                    + "into data/questions/" + q.getTopic().dirSlug() + "/ to use it.");
            MAPPER.writeValue(dir.resolve(q.getId() + ".json").toFile(), node);
        } catch (Exception e) {
            System.err.println("Warning: could not queue generated question — " + e.getMessage());
        }
    }

    public Path directory() { return root; }
}
