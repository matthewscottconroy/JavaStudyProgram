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
 *                        (defaults to ./data/profiles when it exists — i.e. running from a
 *                        checkout — and to ~/.javastudy/profiles otherwise)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        try {
            Path profileDir = resolveProfileDir();

            // Optional instructor customization of the prerequisite graph
            for (String warning : com.studyprogram.core.TopicGraphOverrides
                    .loadAndApply(Path.of("data", "topic-graph.json"))) {
                System.err.println("Warning: " + warning);
            }

            QuestionBank   bank    = new QuestionBank();
            ProfileStorage storage = new JsonProfileStorage(profileDir);
            LLMService     llm     = LLMServiceFactory.create();

            new CLI(bank, storage, llm).run();
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    /**
     * PROFILE_DIR wins when set; otherwise ./data/profiles is kept when it already exists
     * (a repo checkout), and downloaded-jar users get a stable home-directory location.
     */
    private static Path resolveProfileDir() {
        String env = System.getenv("PROFILE_DIR");
        if (env != null && !env.isBlank()) return Path.of(env);
        Path repoDir = Path.of("data", "profiles");
        if (java.nio.file.Files.isDirectory(repoDir)) return repoDir;
        return Path.of(System.getProperty("user.home"), ".javastudy", "profiles");
    }
}
