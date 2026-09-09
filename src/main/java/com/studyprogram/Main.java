package com.studyprogram;

import com.studyprogram.core.QuestionBank;
import com.studyprogram.core.TopicGraphOverrides;
import com.studyprogram.llm.LLMService;
import com.studyprogram.llm.LLMServiceFactory;
import com.studyprogram.report.ClassReportGenerator;
import com.studyprogram.report.ProgressCard;
import com.studyprogram.storage.JsonProfileStorage;
import com.studyprogram.storage.ProfileStorage;
import com.studyprogram.storage.ProfileTransfer;
import com.studyprogram.ui.CLI;
import com.studyprogram.ui.Display;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Entry point.
 *
 * <p>Interactive:  {@code java -jar java-study-program.jar}
 *
 * <p>Instructor and scripting modes are flags — see {@link #usage()}.
 *
 * <p>Environment:
 * <ul>
 *   <li>{@code ANTHROPIC_API_KEY} — optional AI hints and explanations</li>
 *   <li>{@code PROFILE_DIR} — where profiles live (default ./data/profiles in a checkout,
 *       else ~/.javastudy/profiles)</li>
 *   <li>{@code STUDY_SIGNING_KEY} — HMAC key for progress cards (instructor machines)</li>
 *   <li>{@code JAVASTUDY_SANDBOX=off} — disable OS sandboxing of exercise runs</li>
 *   <li>{@code JAVASTUDY_TRUST_EXTERNAL=1} — load external questions that fail safety screening</li>
 *   <li>{@code NO_COLOR}, {@code JAVASTUDY_ASCII=1} — accessibility fallbacks</li>
 * </ul>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        List<String> argv = new java.util.ArrayList<>(List.of(args));

        // Presentation flags apply to every mode
        if (argv.remove("--no-color")) Display.disableColor();
        if (argv.remove("--ascii")) Display.useAsciiGlyphs();

        String command = argv.isEmpty() ? "" : argv.get(0);
        String value = argv.size() > 1 ? argv.get(1) : null;

        switch (command) {
            case "--help", "-h" -> System.out.println(usage());
            case "--class-report" -> classReport(value);
            case "--export-profile" -> exportProfile(value, argv.size() > 2 ? argv.get(2) : null);
            case "--import-profile" -> importProfile(value);
            case "--verify-card" -> verifyCard(value);
            case "" -> interactive();
            default -> {
                System.out.println("Unknown option: " + command);
                System.out.println();
                System.out.println(usage());
            }
        }
    }

    private static void interactive() throws IOException {
        AnsiConsole.systemInstall();
        try {
            for (String warning : TopicGraphOverrides.loadAndApply(Path.of("data", "topic-graph.json"))) {
                System.err.println("Warning: " + warning);
            }
            QuestionBank   bank    = new QuestionBank();
            ProfileStorage storage = new JsonProfileStorage(resolveProfileDir());
            LLMService     llm     = LLMServiceFactory.create();
            new CLI(bank, storage, llm).run();
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    /** Aggregate every profile into one HTML page; optionally from a directory of imports. */
    private static void classReport(String dir) throws IOException {
        Path profileDir = dir == null ? resolveProfileDir() : Path.of(dir);
        TopicGraphOverrides.loadAndApply(Path.of("data", "topic-graph.json"));
        Path out = new ClassReportGenerator().generate(
                new JsonProfileStorage(profileDir), new QuestionBank(),
                profileDir.resolveSibling("reports").resolve("class-report.html"));
        System.out.println("Class report written to: " + out.toAbsolutePath());
    }

    private static void exportProfile(String name, String outPath) throws IOException {
        if (name == null) {
            System.out.println("Usage: --export-profile <profile-name> [output-file]");
            return;
        }
        ProfileStorage storage = new JsonProfileStorage(resolveProfileDir());
        Path out = Path.of(outPath == null
                ? name.replaceAll("[^a-zA-Z0-9_\\-]", "_") + "-bundle.json" : outPath);
        System.out.println("Exported to: " + ProfileTransfer.export(storage, name, out).toAbsolutePath());
    }

    private static void importProfile(String source) throws IOException {
        if (source == null) {
            System.out.println("Usage: --import-profile <bundle.json | directory>");
            return;
        }
        ProfileStorage storage = new JsonProfileStorage(resolveProfileDir());
        Path path = Path.of(source);
        if (Files.isDirectory(path)) {
            List<String> names = ProfileTransfer.importAll(storage, path);
            System.out.println("Imported " + names.size() + " profile(s): " + String.join(", ", names));
        } else {
            System.out.println("Imported: " + ProfileTransfer.importBundle(storage, path));
        }
    }

    private static void verifyCard(String cardPath) throws IOException {
        if (cardPath == null) {
            System.out.println("Usage: --verify-card <card.txt> [profile-name]");
            return;
        }
        String card = Files.readString(Path.of(cardPath));
        String student = card.lines()
                .filter(l -> l.contains("Student"))
                .map(l -> l.replaceAll(".*Student\\s+", "").replaceAll("[│|]\\s*$", "").trim())
                .findFirst().orElse("");
        ProfileStorage storage = new JsonProfileStorage(resolveProfileDir());
        String profileId = storage.load(student).map(p -> p.getId()).orElse(null);
        if (profileId == null) {
            System.out.println("No local profile named '" + student
                    + "' — a card can only be verified where that profile lives.");
            return;
        }
        System.out.println(ProgressCard.verify(card, profileId));
    }

    static String usage() {
        return """
               Java Study Program

               Usage:
                 java -jar java-study-program.jar [options]

               Modes:
                 (no arguments)                 start the interactive study program
                 --class-report [dir]           write an HTML report over every profile
                                                (optionally in a given profile directory)
                 --export-profile <name> [out]  bundle one profile + its attempt log to a file
                 --import-profile <file|dir>    import one bundle, or every bundle in a folder
                 --verify-card <card.txt>       check a progress card against its local profile
                 --help                         show this message

               Display:
                 --no-color                     disable ANSI colour (also honours NO_COLOR)
                 --ascii                        plain-ASCII glyphs for limited terminals
               """;
    }

    /**
     * PROFILE_DIR wins when set; otherwise ./data/profiles is kept when it already exists
     * (a repo checkout), and downloaded-jar users get a stable home-directory location.
     */
    private static Path resolveProfileDir() {
        String env = System.getenv("PROFILE_DIR");
        if (env != null && !env.isBlank()) return Path.of(env);
        Path repoDir = Path.of("data", "profiles");
        if (Files.isDirectory(repoDir)) return repoDir;
        return Path.of(System.getProperty("user.home"), ".javastudy", "profiles");
    }
}
