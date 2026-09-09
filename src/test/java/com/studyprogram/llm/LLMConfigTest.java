package com.studyprogram.llm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LLMConfigTest {

    @Test
    void defaultsTargetTheClaudeMessagesApi() {
        LLMConfig config = LLMConfig.defaults();
        assertEquals("anthropic", config.provider());
        assertTrue(config.baseUrl().contains("api.anthropic.com"));
        assertEquals(LLMConfig.DEFAULT_MODEL, config.model());
        assertFalse(config.isOpenAiShape());
        assertFalse(config.isLocalEndpoint());
        assertTrue(config.maxCallsPerSession() > 0, "a cost cap is always in force");
    }

    @Test
    void anOpenAiCompatibleLocalEndpointNeedsNoApiKey() {
        LLMConfig local = new LLMConfig("openai",
                "http://localhost:11434/v1/chat/completions", "llama3.1", "NO_SUCH_KEY_ENV", 50);
        assertTrue(local.isOpenAiShape());
        assertTrue(local.isLocalEndpoint(), "local models are how students without a budget get AI help");
        assertNull(local.apiKey());
    }

    @Test
    void serviceIsCreatedForLocalEndpointsAndSkippedWhenNothingIsConfigured() {
        LLMConfig local = new LLMConfig("openai",
                "http://127.0.0.1:1234/v1/chat/completions", "local", "NO_SUCH_KEY_ENV", 10);
        assertInstanceOf(AnthropicService.class, LLMServiceFactory.create(local));

        LLMConfig unconfigured = new LLMConfig("anthropic",
                "https://api.anthropic.com/v1/messages", "m", "NO_SUCH_KEY_ENV", 10);
        assertInstanceOf(NullLLMService.class, LLMServiceFactory.create(unconfigured));
    }

    @Test
    void describeTellsTheStudentWhatToDo() {
        LLMConfig unconfigured = new LLMConfig("anthropic",
                "https://api.anthropic.com/v1/messages", "m", "NO_SUCH_KEY_ENV", 10);
        String described = LLMServiceFactory.describe(unconfigured);
        assertTrue(described.contains("disabled"));
        assertTrue(described.contains("NO_SUCH_KEY_ENV"));
        assertTrue(described.contains("local model"));
    }

    @Test
    void nullServiceStillOffersAuthoredHints() {
        var q = com.studyprogram.model.Question.builder()
                .id("q").topic(com.studyprogram.model.Topic.LOOPS).difficulty(1)
                .prompt("p").answer("a").hint("try a for loop").build();
        assertEquals("try a for loop", new NullLLMService().generateHint(q));
        assertFalse(new NullLLMService().isAvailable());
    }
}
