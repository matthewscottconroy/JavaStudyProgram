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
 * Calls a chat completion endpoint for optional hints and explanations.
 *
 * <p>Configured by {@link LLMConfig}, so the same class serves the Claude Messages API and any
 * OpenAI-compatible endpoint — including a local Ollama or LM Studio server, which lets students
 * without an API budget still get AI help. A per-session call cap bounds cost.
 *
 * <p>All public methods catch exceptions and return empty/fallback strings, so the rest of the
 * program never needs to handle AI failures specially.
 */
public class AnthropicService implements LLMService {

    private static final String API_VERSION = "2023-06-01";
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    private final LLMConfig config;
    private final String apiKey;
    private final OkHttpClient http;
    private final ObjectMapper json = new ObjectMapper();
    private boolean reachable = true;
    private int consecutiveFailures = 0;
    private int callsMade = 0;

    public AnthropicService(String apiKey) {
        this(LLMConfig.defaults(), apiKey);
    }

    public AnthropicService(LLMConfig config, String apiKey) {
        this.config = config;
        this.apiKey = apiKey;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean isAvailable() {
        boolean haveCredentials = (apiKey != null && !apiKey.isBlank()) || config.isLocalEndpoint();
        return haveCredentials && reachable && callsMade < config.maxCallsPerSession();
    }

    /** Calls used so far this session, against the configured cap. */
    public String usage() {
        return callsMade + "/" + config.maxCallsPerSession() + " AI calls used";
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
            callsMade++;
            ObjectNode body = json.createObjectNode();
            body.put("model", config.model());
            if (config.isOpenAiShape()) {
                body.put("max_tokens", maxTokens);
            } else {
                body.put("max_tokens", maxTokens);
            }

            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", userMessage);

            RequestBody requestBody = RequestBody.create(
                    json.writeValueAsBytes(body),
                    MediaType.get("application/json"));

            Request.Builder builder = new Request.Builder()
                    .url(config.baseUrl())
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody);
            if (config.isOpenAiShape()) {
                if (apiKey != null && !apiKey.isBlank()) {
                    builder.addHeader("Authorization", "Bearer " + apiKey);
                }
            } else {
                builder.addHeader("x-api-key", apiKey == null ? "" : apiKey)
                       .addHeader("anthropic-version", API_VERSION);
            }
            Request request = builder.build();

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
                String text = config.isOpenAiShape()
                        ? root.path("choices").path(0).path("message").path("content").asText()
                        : root.path("content").path(0).path("text").asText();
                return text.isBlank() ? Optional.empty() : Optional.of(text.trim());
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
