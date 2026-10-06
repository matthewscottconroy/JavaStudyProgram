package com.studyprogram.coding;

import com.studyprogram.model.Question;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Runs CODING exercises against the student's workspace:
 *
 *  1. {@link #prepareWorkspace} writes the exercise's starter file to
 *     {@code workspace/<question-id>/<ClassName>.java} (never clobbering student edits).
 *  2. The student edits that file in their own editor or IDE.
 *  3. {@link #run} copies the student's file plus the (tamper-proof, held in question
 *     data) test harness into a fresh build directory, compiles both with the JDK
 *     compiler, and executes the test class in a subprocess with a memory cap and a
 *     timeout so endless loops and runaway allocation can't take the app down.
 */
public class CodingExerciseRunner {

    public static final Path DEFAULT_WORKSPACE = Path.of("workspace");

    /**
     * How long a student's program may run before it is killed.
     *
     * <p>Generous on purpose. The only cost of a long limit is that a genuine infinite loop takes
     * this long to report, which happens once and teaches the lesson either way; the cost of a
     * short one is telling a student their correct program has an infinite loop because a Swing
     * exercise needed eight seconds to start AWT on a loaded laptop. That is a far worse failure:
     * it sends them hunting for a bug that is not there. Ten seconds was enough on an idle
     * machine and not enough on a busy one.
     */
    private static final int  RUN_TIMEOUT_SECONDS = 30;

    /**
     * The limit actually applied. Overridable through {@code javastudy.runTimeoutSeconds} so the
     * test that proves an endless loop is caught does not have to sit through the full wait —
     * the behaviour being tested is the kill, not the duration.
     */
    private static int runTimeoutSeconds() {
        String override = System.getProperty("javastudy.runTimeoutSeconds");
        if (override == null) return RUN_TIMEOUT_SECONDS;
        try {
            return Math.max(1, Integer.parseInt(override));
        } catch (NumberFormatException e) {
            return RUN_TIMEOUT_SECONDS;
        }
    }
    private static final int  MAX_OUTPUT_CHARS    = 10_000;
    private static final String MEMORY_CAP        = "-Xmx128m";

    private final Path workspaceRoot;

    public CodingExerciseRunner(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public CodingExerciseRunner() {
        this(DEFAULT_WORKSPACE);
    }

    /** True when a JDK compiler is available in this JVM (false when running on a bare JRE). */
    public static boolean compilerAvailable() {
        return ToolProvider.getSystemJavaCompiler() != null;
    }

    /** This exercise's workspace directory. */
    public Path workspaceDir(Question q) {
        return workspaceRoot.resolve(q.getId());
    }

    /** The file the student should edit (for multi-file projects: the first starter file). */
    public Path studentFile(Question q) {
        String name = q.isMultiFile()
                ? q.getStarterFiles().keySet().iterator().next()
                : JavaSource.primaryTypeName(q.getStarterCode()) + ".java";
        return workspaceDir(q).resolve(name);
    }

    /** All starter files as filename → content (single-file exercises give one entry). */
    private static java.util.Map<String, String> starterSet(Question q) {
        if (q.isMultiFile()) return q.getStarterFiles();
        return java.util.Map.of(
                JavaSource.primaryTypeName(q.getStarterCode()) + ".java", q.getStarterCode());
    }

    /**
     * Ensures the workspace directory and starter file(s) exist.
     * Files the student already has are left untouched.
     *
     * @return the path to the (first) file the student should edit
     */
    public Path prepareWorkspace(Question q) throws IOException {
        Path dir = workspaceDir(q);
        Files.createDirectories(dir);
        for (var entry : starterSet(q).entrySet()) {
            Path file = dir.resolve(entry.getKey());
            if (!Files.exists(file)) {
                Files.writeString(file, entry.getValue(), StandardCharsets.UTF_8);
            }
        }
        return studentFile(q);
    }

    /** Overwrites the student's file(s) with the original starter code. */
    public Path resetToStarter(Question q) throws IOException {
        Path dir = workspaceDir(q);
        Files.createDirectories(dir);
        for (var entry : starterSet(q).entrySet()) {
            Files.writeString(dir.resolve(entry.getKey()), entry.getValue(), StandardCharsets.UTF_8);
        }
        return studentFile(q);
    }

    /**
     * Compiles the student's current workspace with the test harness and runs the tests.
     * Every .java file in the workspace is included, so students can even add their own
     * helper classes to a project exercise.
     */
    public CodingResult run(Question q) {
        try {
            prepareWorkspace(q);
            java.util.Map<String, String> sources = new java.util.LinkedHashMap<>();
            try (var files = Files.list(workspaceDir(q))) {
                for (Path p : files.filter(f -> f.toString().endsWith(".java")).sorted().toList()) {
                    sources.put(p.getFileName().toString(), Files.readString(p, StandardCharsets.UTF_8));
                }
            }
            return compileAndTest(sources, q.getTestCode());
        } catch (IOException e) {
            return new CodingResult(CodingResult.Status.ENVIRONMENT_ERROR,
                    "Could not read workspace file: " + e.getMessage());
        }
    }

    /**
     * The content gate for one coding exercise: the starter must compile cleanly but fail its
     * tests (otherwise there is nothing to do, or nothing to check), and the reference solution
     * must pass them (otherwise the exercise cannot be finished). Returns a description of the
     * problem, or empty when the exercise is healthy. This is the single definition of "healthy"
     * used by the test suite over the shipped bank and by {@code --verify-questions} over an
     * instructor's own pack.
     */
    public java.util.Optional<String> verifyExercise(Question q) {
        if (!compilerAvailable()) {
            return java.util.Optional.of(q.getId() + ": cannot verify without a JDK compiler");
        }
        CodingResult starter = q.isMultiFile()
                ? compileAndTest(q.getStarterFiles(), q.getTestCode())
                : compileAndTest(q.getStarterCode(), q.getTestCode());
        if (starter.status() != CodingResult.Status.TEST_FAILURE) {
            return java.util.Optional.of(q.getId() + ": starter should compile but fail tests — got "
                    + starter.status() + "\n" + starter.output());
        }
        CodingResult solution = q.isMultiFile()
                ? compileAndTest(q.getSolutionFiles(), q.getTestCode())
                : compileAndTest(q.getAnswer(), q.getTestCode());
        if (solution.status() != CodingResult.Status.PASS) {
            return java.util.Optional.of(q.getId() + ": reference solution should pass — got "
                    + solution.status() + "\n" + solution.output());
        }
        return java.util.Optional.empty();
    }

    /** Single-file variant — see {@link #compileAndTest(java.util.Map, String)}. */
    CodingResult compileAndTest(String mainSource, String testSource) {
        return compileAndTest(
                java.util.Map.of(JavaSource.primaryTypeName(mainSource) + ".java", mainSource),
                testSource);
    }

    /**
     * Compiles the given source set (filename → content) together with {@code testSource}
     * in a throwaway build directory and runs the test class's {@code main}.
     * Package-private core so tests can exercise it without touching the workspace.
     */
    CodingResult compileAndTest(java.util.Map<String, String> sources, String testSource) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return new CodingResult(CodingResult.Status.ENVIRONMENT_ERROR,
                    "No Java compiler found. You appear to be running on a JRE — coding "
                    + "exercises need a full JDK (https://adoptium.net). Everything else "
                    + "in the study program still works.");
        }

        Path buildDir = null;
        try {
            buildDir = Files.createTempDirectory("javastudy-build-");

            String testClass = JavaSource.primaryTypeName(testSource);
            List<java.io.File> files = new ArrayList<>();
            for (var entry : sources.entrySet()) {
                Path f = buildDir.resolve(entry.getKey());
                Files.writeString(f, entry.getValue(), StandardCharsets.UTF_8);
                files.add(f.toFile());
            }
            Path testFile = buildDir.resolve(testClass + ".java");
            Files.writeString(testFile, testSource, StandardCharsets.UTF_8);
            files.add(testFile.toFile());

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm =
                         compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromFiles(files);
                boolean ok = compiler.getTask(null, fm, diagnostics,
                        List.of("-d", buildDir.toString()), null, units).call();
                if (!ok) {
                    return compileError(diagnostics.getDiagnostics());
                }
            }

            return execute(buildDir, testClass);
        } catch (IOException e) {
            return new CodingResult(CodingResult.Status.ENVIRONMENT_ERROR,
                    "Build failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return new CodingResult(CodingResult.Status.COMPILE_ERROR,
                    "Could not find a class declaration in your file: " + e.getMessage());
        } finally {
            if (buildDir != null) deleteRecursively(buildDir);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private CodingResult execute(Path buildDir, String testClass) throws IOException {
        // headless=true keeps GUI exercises deterministic everywhere (components work,
        // but no exercise can open real windows during a test run). Sandbox.wrap adds
        // OS-level containment when the host provides it.
        List<String> command = Sandbox.wrap(List.of(
                javaExecutable().toString(), MEMORY_CAP, "-Djava.awt.headless=true",
                "-cp", buildDir.toString(), testClass), buildDir);
        // Always run from the build directory, whatever the sandbox does.
        //
        // bubblewrap passes --chdir and so set this implicitly; without a sandbox the exercise
        // inherited the app's own working directory instead. That made any exercise touching a
        // relative path behave differently depending on whether the student happened to have
        // bubblewrap — and jdoc-code-06, which reads its own source file as
        // Path.of("OwnSourceDocReader.java"), failed for every student on Windows or macOS with a
        // perfectly correct solution. Setting it here makes the working directory part of the
        // contract rather than a side effect of containment.
        Process process = new ProcessBuilder(command)
                .directory(buildDir.toFile())
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        Thread reader = new Thread(() -> {
            try (InputStream in = process.getInputStream()) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1 && output.length() < MAX_OUTPUT_CHARS) {
                    output.append(new String(buf, 0, n, StandardCharsets.UTF_8));
                }
            } catch (IOException ignored) {}
        });
        reader.start();

        try {
            int limit = runTimeoutSeconds();
            if (!process.waitFor(limit, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new CodingResult(CodingResult.Status.TIMEOUT,
                        "Your program ran for more than " + limit
                        + " seconds and was stopped.\n"
                        + "Usually that means a loop that never ends — check that its condition "
                        + "can actually become false, and that whatever it tests really changes "
                        + "inside the loop.\n"
                        + (output.length() == 0
                                ? "Nothing was printed before it was stopped, so it may have "
                                  + "hung before reaching your code.\n"
                                : "Output before it was stopped:\n" + truncate(output.toString())));
            }
            reader.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return new CodingResult(CodingResult.Status.ENVIRONMENT_ERROR, "Interrupted while running tests.");
        }

        CodingResult.Status status = process.exitValue() == 0
                ? CodingResult.Status.PASS : CodingResult.Status.TEST_FAILURE;
        return new CodingResult(status, truncate(output.toString()));
    }

    private static Path javaExecutable() {
        Path base = Path.of(System.getProperty("java.home"), "bin", "java");
        if (Files.exists(base)) return base;
        Path exe = Path.of(System.getProperty("java.home"), "bin", "java.exe");
        if (Files.exists(exe)) return exe;
        // Packaged runtimes may strip launchers — fall back to `java` on PATH
        return Path.of("java");
    }

    /**
     * Builds the COMPILE_ERROR result: javac's own words first (the student will meet them again
     * in any other tool), then a plain-English reading of each distinct error from
     * {@link CompilerErrorDecoder}, and the matched categories for the attempt log.
     */
    private static CodingResult compileError(List<Diagnostic<? extends JavaFileObject>> diags) {
        List<String> located = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> d : diags) {
            if (d.getKind() != Diagnostic.Kind.ERROR) continue;
            String source = d.getSource() == null ? "" : Path.of(d.getSource().getName()).getFileName() + ":";
            String message = d.getMessage(null);
            located.add(source + d.getLineNumber() + ": " + message);
            messages.add(message);
        }
        if (located.isEmpty()) {
            return new CodingResult(CodingResult.Status.COMPILE_ERROR,
                    "Compilation failed (no error details available).");
        }

        StringBuilder out = new StringBuilder(String.join("\n", located));
        List<String> kinds = new ArrayList<>();
        for (CompilerErrorDecoder.Decoded decoded
                : CompilerErrorDecoder.decodeAll(messages)) {
            if (!decoded.isRecognised() || kinds.contains(decoded.kind())) continue;
            kinds.add(decoded.kind());
            out.append("\n\n").append(WHAT_IT_MEANS).append(decoded.kind()).append("\n")
               .append(wrap(decoded.explanation()));
        }
        return new CodingResult(CodingResult.Status.COMPILE_ERROR, truncate(out.toString()), kinds);
    }

    private static final String WHAT_IT_MEANS = "What that means - ";

    /** Wraps decoder prose to a readable width and indents it under its heading. */
    private static String wrap(String text) {
        StringBuilder out = new StringBuilder("  ");
        int column = 0;
        for (String word : text.split(" ")) {
            if (column > 0 && column + word.length() > 74) {
                out.append("\n  ");
                column = 0;
            }
            if (column > 0) { out.append(' '); column++; }
            out.append(word);
            column += word.length();
        }
        return out.toString();
    }

    private static String truncate(String s) {
        return s.length() <= MAX_OUTPUT_CHARS ? s : s.substring(0, MAX_OUTPUT_CHARS) + "\n… (output truncated)";
    }

    private static void deleteRecursively(Path root) {
        try (var walk = Files.walk(root)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }
}
