package com.studyprogram.coding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Static screening of exercise source for capabilities a practice exercise should not need.
 *
 * <p>The threat this addresses is a hostile <em>question pack</em>: the app deliberately lets
 * anyone drop JSON files into {@code data/questions/}, and those files carry code that is
 * compiled and executed. Questions bundled with the app are trusted (they pass the content gate
 * in CI); questions loaded from an external directory are untrusted and screened here. A flagged
 * untrusted question is refused rather than run, unless the user explicitly opts in.
 *
 * <p>This is a coarse lexical screen, not a proof of safety — obfuscated reflection can defeat
 * any such scan. It is the first of two layers; {@link Sandbox} provides OS-level containment
 * where the platform offers it.
 */
public final class CodeSafetyScanner {

    /** One flagged capability found in exercise source. */
    public record Finding(String category, String evidence) {
        @Override public String toString() { return category + " (" + evidence + ")"; }
    }

    private static final Map<String, Pattern> RULES = Map.of(
            "process execution", Pattern.compile(
                    "\\bProcessBuilder\\b|Runtime\\s*\\.\\s*getRuntime\\s*\\(\\s*\\)|\\bProcessHandle\\b"),
            "native code", Pattern.compile(
                    "System\\s*\\.\\s*load(Library)?\\s*\\(|\\bUnsafe\\b|\\bMethodHandles\\b"),
            "filesystem writes outside temp", Pattern.compile(
                    "Files\\s*\\.\\s*(delete|deleteIfExists|move|copy|write|writeString|newOutputStream)\\s*\\(|"
                    + "\\bFileOutputStream\\b|\\bFileWriter\\b|\\.\\s*renameTo\\s*\\(|\\.\\s*setWritable\\s*\\("),
            "network access", Pattern.compile(
                    "\\bSocket\\b|\\bServerSocket\\b|\\bHttpClient\\b|\\bURLConnection\\b|"
                    + "\\bDatagramSocket\\b|\\bInetAddress\\b|\\.\\s*openConnection\\s*\\(|\\.\\s*openStream\\s*\\("),
            "reflective access override", Pattern.compile(
                    "\\.\\s*setAccessible\\s*\\(\\s*true|\\bClassLoader\\b|Class\\s*\\.\\s*forName\\s*\\("),
            "system exit or shutdown", Pattern.compile(
                    "System\\s*\\.\\s*exit\\s*\\(|Runtime\\s*\\.\\s*halt|addShutdownHook"),
            "environment and system property writes", Pattern.compile(
                    "System\\s*\\.\\s*setProperty\\s*\\(|System\\s*\\.\\s*getenv\\s*\\("),
            "arbitrary code loading", Pattern.compile(
                    "javax\\s*\\.\\s*tools|ScriptEngine|\\bProxy\\s*\\.\\s*newProxyInstance"));

    private static final Pattern NETWORK = RULES.get("network access");

    private CodeSafetyScanner() {}

    /**
     * Screens every code string of a question.
     *
     * @param sources source texts to screen (nulls ignored)
     * @return findings, empty when nothing was flagged
     */
    public static List<Finding> scan(Iterable<String> sources) {
        List<Finding> findings = new ArrayList<>();
        for (String source : sources) {
            if (source == null || source.isBlank()) continue;
            String stripped = stripCommentsAndStrings(source);
            RULES.forEach((category, pattern) -> {
                var matcher = pattern.matcher(stripped);
                if (matcher.find() && findings.stream().noneMatch(f -> f.category().equals(category))) {
                    findings.add(new Finding(category, matcher.group()));
                }
            });
        }
        return findings;
    }

    /** True when the source appears to use sockets — such exercises need loopback networking. */
    public static boolean usesNetwork(String source) {
        return source != null && NETWORK.matcher(stripCommentsAndStrings(source)).find();
    }

    /**
     * Removes comments and string literals so that prose ("never call System.exit here") and
     * sample SQL or HTTP text in string constants do not trigger findings.
     */
    static String stripCommentsAndStrings(String source) {
        StringBuilder out = new StringBuilder(source.length());
        boolean inLine = false, inBlock = false, inString = false, inChar = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';
            if (inLine) {
                if (c == '\n') { inLine = false; out.append(c); }
            } else if (inBlock) {
                if (c == '*' && next == '/') { inBlock = false; i++; }
            } else if (inString) {
                if (c == '\\') i++;
                else if (c == '"') inString = false;
            } else if (inChar) {
                if (c == '\\') i++;
                else if (c == '\'') inChar = false;
            } else if (c == '/' && next == '/') { inLine = true; i++; }
            else if (c == '/' && next == '*') { inBlock = true; i++; }
            else if (c == '"') inString = true;
            else if (c == '\'') inChar = true;
            else out.append(c);
        }
        return out.toString();
    }
}
