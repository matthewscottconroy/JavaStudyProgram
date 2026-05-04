package com.studyprogram.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.studyprogram.model.Question;
import com.studyprogram.model.QuestionType;
import com.studyprogram.model.Topic;
import okhttp3.*;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Calls the Anthropic Messages API (claude-sonnet-4-6 by default).
 * Requires ANTHROPIC_API_KEY environment variable.
 *
 * All public methods catch exceptions and return empty/fallback strings so the
 * rest of the program never needs to handle LLM failures specially.
 */
public class AnthropicService implements LLMService {

    private static final String API_URL     = "https://api.anthropic.com/v1/messages";
    private static final String MODEL       = "claude-sonnet-4-6";
    private static final String API_VERSION = "2023-06-01";

    private final String apiKey;
    private final OkHttpClient http;
    private final ObjectMapper json = new ObjectMapper();
    private boolean reachable = true;
    private int consecutiveFailures = 0;
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    public AnthropicService(String apiKey) {
        this.apiKey = apiKey;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && reachable;
    }

    @Override
    public String explainAnswer(Question question, String studentAnswer) {
        String prompt = """
                A student answered a Java programming question incorrectly.

                Question: %s
                %s
                Correct answer: %s
                Student's answer: %s

                In 2-3 sentences, explain why the student's answer is wrong and clarify the correct concept.
                Be encouraging and educational. No code blocks unless essential.
                """.formatted(
                question.getPrompt(),
                question.hasCode() ? "Code:\n" + question.getCode() : "",
                question.getAnswer(),
                studentAnswer);

        return callAPI(prompt, 300).orElse("");
    }

    @Override
    public String generateHint(Question question) {
        if (!question.getHints().isEmpty()) return question.getHints().get(0);

        String prompt = """
                Give one short hint (one sentence) for this Java question without revealing the answer:
                %s
                """.formatted(question.getPrompt());

        return callAPI(prompt, 100).orElse("Think carefully about the question.");
    }

    @Override
    public String explainConcept(Topic topic, String concept) {
        String prompt = """
                Explain "%s" in the context of Java's %s topic.
                Keep the explanation under 150 words. Use a concrete, simple example if helpful.
                Assume the student is a beginner.
                """.formatted(concept, topic.displayName);

        return callAPI(prompt, 400).orElse("LLM explanation unavailable.");
    }

    @Override
    public Optional<Question> generateQuestion(Topic topic, QuestionType type, int difficulty) {
        if (!isAvailable()) return Optional.empty();

        boolean isTrace = (type == QuestionType.TRACING);
        String typeDesc = isTrace
                ? "code tracing (student predicts printed output)"
                : "multiple choice concept";

        String formatNote = isTrace
                ? "\"choices\" must be an empty array []. \"answer\" is the exact printed output."
                : "\"choices\" must be exactly 4 options. \"answer\" is the lowercase letter a/b/c/d.";

        String prompt = """
                Generate a %s Java question about the topic "%s" at difficulty %d out of 5.

                Respond with ONLY valid JSON (no markdown, no surrounding text):
                {
                  "prompt": "the question text shown to the student",
                  "code": "the Java code snippet, or null if not needed",
                  "choices": [],
                  "answer": "correct answer",
                  "explanation": "brief explanation of why the answer is correct"
                }

                %s
                Make the question accurate, educational, and appropriate for the difficulty level.
                """.formatted(typeDesc, topic.displayName, difficulty, formatNote);

        Optional<String> response = callAPI(prompt, 700);
        if (response.isEmpty()) return Optional.empty();

        try {
            String text  = response.get();
            int start    = text.indexOf('{');
            int end      = text.lastIndexOf('}') + 1;
            if (start < 0 || end <= start) return Optional.empty();

            JsonNode root = json.readTree(text.substring(start, end));

            String promptText = root.path("prompt").asText("").trim();
            String answer     = root.path("answer").asText("").trim();
            if (promptText.isBlank() || answer.isBlank()) return Optional.empty();

            Question.Builder b = Question.builder()
                    .id("llm-" + topic.name().toLowerCase() + "-" + System.currentTimeMillis())
                    .topic(topic)
                    .type(type)
                    .difficulty(Math.max(1, Math.min(5, difficulty)))
                    .prompt(promptText)
                    .answer(answer)
                    .explanation(root.path("explanation").asText(""));

            String code = root.path("code").asText(null);
            if (code != null && !code.isBlank() && !code.equals("null")) b.code(code);

            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() == 4) {
                b.choices(choices.get(0).asText(), choices.get(1).asText(),
                          choices.get(2).asText(), choices.get(3).asText());
            }

            return Optional.of(b.build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private Optional<String> callAPI(String userMessage, int maxTokens) {
        if (!isAvailable()) return Optional.empty();

        try {
            ObjectNode body = json.createObjectNode();
            body.put("model", MODEL);
            body.put("max_tokens", maxTokens);

            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", userMessage);

            RequestBody requestBody = RequestBody.create(
                    json.writeValueAsBytes(body),
                    MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", API_VERSION)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            try (Response response = http.newCall(request).execute()) {
                if (response.code() == 401 || response.code() == 403) {
                    reachable = false;
                    return Optional.empty();
                }
                if (!response.isSuccessful() || response.body() == null) {
                    recordFailure();
                    return Optional.empty();
                }
                consecutiveFailures = 0;
                JsonNode root = json.readTree(response.body().string());
                String text = root.path("content").get(0).path("text").asText();
                return Optional.of(text.trim());
            }
        } catch (IOException e) {
            recordFailure();
            return Optional.empty();
        }
    }

    private void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            reachable = false;
        }
    }
}
