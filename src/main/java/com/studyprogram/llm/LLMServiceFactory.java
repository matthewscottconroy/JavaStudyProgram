package com.studyprogram.llm;

/**
 * Creates the AI helper described by {@link LLMConfig}, or a no-op service when nothing is
 * configured. AI support is always optional: with no key and no local endpoint the program is
 * fully functional and simply falls back to the hints authored with each question.
 */
public class LLMServiceFactory {

    public static LLMService create() {
        return create(LLMConfig.load());
    }

    public static LLMService create(LLMConfig config) {
        String apiKey = config.apiKey();
        boolean usable = (apiKey != null) || config.isLocalEndpoint();
        return usable ? new AnthropicService(config, apiKey) : new NullLLMService();
    }

    /** One-line description of the configured backend, for the startup banner. */
    public static String describe(LLMConfig config) {
        if (config.apiKey() == null && !config.isLocalEndpoint()) {
            return "disabled (set " + config.apiKeyEnv() + ", or point data/llm.json at a local model)";
        }
        return config.model() + " via " + config.baseUrl()
                + " (max " + config.maxCallsPerSession() + " calls/session)";
    }
}
