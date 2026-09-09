package com.studyprogram.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for the optional AI helper, read from {@code data/llm.json} (or
 * {@code ~/.javastudy/llm.json}), with environment variables taking precedence.
 *
 * <p>Two provider shapes are supported so that AI help does not require a paid account:
 * <ul>
 *   <li>{@code anthropic} — the Claude Messages API (default).</li>
 *   <li>{@code openai} — any OpenAI-compatible {@code /chat/completions} endpoint, which
 *       includes Ollama, LM Studio and vLLM running locally on a student's machine.</li>
 * </ul>
 *
 * <p>Example {@code data/llm.json} for a local model with no API key and no cost:
 * <pre>{@code
 * { "provider": "openai",
 *   "baseUrl": "http://localhost:11434/v1/chat/completions",
 *   "model": "llama3.1",
 *   "apiKeyEnv": "OLLAMA_API_KEY",
 *   "maxCallsPerSession": 100 }
 * }</pre>
 */
public record LLMConfig(String provider, String baseUrl, String model,
                        String apiKeyEnv, int maxCallsPerSession) {

    public static final String DEFAULT_MODEL = "claude-opus-5";
    private static final String DEFAULT_URL = "https://api.anthropic.com/v1/messages";
    private static final String DEFAULT_KEY_ENV = "ANTHROPIC_API_KEY";
    private static final int DEFAULT_CALL_CAP = 60;

    public static LLMConfig defaults() {
        return new LLMConfig("anthropic", DEFAULT_URL, DEFAULT_MODEL,
                DEFAULT_KEY_ENV, DEFAULT_CALL_CAP);
    }

    /** Loads config from the first file that exists, then applies environment overrides. */
    public static LLMConfig load() {
        LLMConfig config = defaults();
        for (Path candidate : new Path[] {
                Path.of("data", "llm.json"),
                Path.of(System.getProperty("user.home"), ".javastudy", "llm.json")}) {
            if (Files.isRegularFile(candidate)) {
                config = readFile(candidate, config);
                break;
            }
        }
        return config.withEnvOverrides();
    }

    private static LLMConfig readFile(Path file, LLMConfig base) {
        try {
            JsonNode root = new ObjectMapper().readTree(file.toFile());
            return new LLMConfig(
                    root.path("provider").asText(base.provider()),
                    root.path("baseUrl").asText(base.baseUrl()),
                    root.path("model").asText(base.model()),
                    root.path("apiKeyEnv").asText(base.apiKeyEnv()),
                    root.path("maxCallsPerSession").asInt(base.maxCallsPerSession()));
        } catch (Exception e) {
            System.err.println("Warning: could not read " + file + " — " + e.getMessage());
            return base;
        }
    }

    private LLMConfig withEnvOverrides() {
        return new LLMConfig(
                envOr("JAVASTUDY_LLM_PROVIDER", provider),
                envOr("JAVASTUDY_LLM_URL", baseUrl),
                envOr("JAVASTUDY_LLM_MODEL", model),
                apiKeyEnv,
                intEnvOr("JAVASTUDY_LLM_MAX_CALLS", maxCallsPerSession));
    }

    /** The API key for this configuration, or null when none is set. */
    public String apiKey() {
        String key = System.getenv(apiKeyEnv);
        return key == null || key.isBlank() ? null : key;
    }

    /** Local endpoints usually need no key, so treat a localhost URL as usable without one. */
    public boolean isLocalEndpoint() {
        return baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");
    }

    public boolean isOpenAiShape() {
        return "openai".equalsIgnoreCase(provider);
    }

    private static String envOr(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int intEnvOr(String name, int fallback) {
        try {
            String value = System.getenv(name);
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
