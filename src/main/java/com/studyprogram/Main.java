package com.studyprogram;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.llm.LLMService;
import com.studyprogram.llm.LLMServiceFactory;
import com.studyprogram.storage.JsonProfileStorage;
import com.studyprogram.storage.ProfileStorage;
import com.studyprogram.ui.CLI;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point.
 *
 * Run with:  mvn package && java -jar target/java-study-program.jar
 *
 * Optional environment variables:
 *   ANTHROPIC_API_KEY  — enables AI-powered hints, explanations, and dynamic question generation
 *   PROFILE_DIR        — override the directory where profiles are stored
 *                        (defaults to ./data/profiles)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        try {
            Path profileDir = Path.of(System.getenv().getOrDefault("PROFILE_DIR", "data/profiles"));

            QuestionBank   bank    = new QuestionBank();
            ProfileStorage storage = new JsonProfileStorage(profileDir);
            LLMService     llm     = LLMServiceFactory.create();

            new CLI(bank, storage, llm).run();
        } finally {
            AnsiConsole.systemUninstall();
        }
    }
}
