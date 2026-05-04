package com.studyprogram.llm;

/** Creates the appropriate LLMService based on available configuration. */
public class LLMServiceFactory {

    private static final String ENV_KEY = "ANTHROPIC_API_KEY";

    public static LLMService create() {
        String apiKey = System.getenv(ENV_KEY);
        if (apiKey != null && !apiKey.isBlank()) {
            return new AnthropicService(apiKey);
        }
        return new NullLLMService();
    }
}
