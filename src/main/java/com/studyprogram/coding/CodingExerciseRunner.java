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
 *     timeout so infinite loops and runaway allocation can't take the app down.
 */
public class CodingExerciseRunner {

    public static final Path DEFAULT_WORKSPACE = Path.of("workspace");

    private static final int  RUN_TIMEOUT_SECONDS = 10;
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

    /** The file the student should edit for this exercise. */
    public Path studentFile(Question q) {
        String className = JavaSource.primaryTypeName(q.getStarterCode());
        return workspaceRoot.resolve(q.getId()).resolve(className + ".java");
    }

    /**
     * Ensures the workspace directory and starter file exist.
     * If the student already has a file there, it is left untouched.
     *
     * @return the path to the file the student should edit
     */
    public Path prepareWorkspace(Question q) throws IOException {
        Path file = studentFile(q);
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)) {
            Files.writeString(file, q.getStarterCode(), StandardCharsets.UTF_8);
        }
        return file;
    }

    /** Overwrites the student's file with the original starter code. */
    public Path resetToStarter(Question q) throws IOException {
        Path file = studentFile(q);
        Files.createDirectories(file.getParent());
        Files.writeString(file, q.getStarterCode(), StandardCharsets.UTF_8);
        return file;
    }

    /** Compiles the student's current file with the test harness and runs the tests. */
    public CodingResult run(Question q) {
        try {
            String studentSource = Files.readString(prepareWorkspace(q), StandardCharsets.UTF_8);
            return compileAndTest(studentSource, q.getTestCode());
        } catch (IOException e) {
            return new CodingResult(CodingResult.Status.ENVIRONMENT_ERROR,
                    "Could not read workspace file: " + e.getMessage());
        }
    }

    /**
     * Compiles {@code mainSource} together with {@code testSource} in a throwaway build
     * directory and runs the test class's {@code main}. Package-private core so tests can
     * exercise it without touching the workspace.
     */
    CodingResult compileAndTest(String mainSource, String testSource) {
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

            String mainClass = JavaSource.primaryTypeName(mainSource);
            String testClass = JavaSource.primaryTypeName(testSource);
            Path mainFile = buildDir.resolve(mainClass + ".java");
            Path testFile = buildDir.resolve(testClass + ".java");
            Files.writeString(mainFile, mainSource, StandardCharsets.UTF_8);
            Files.writeString(testFile, testSource, StandardCharsets.UTF_8);

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm =
                         compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> units =
                        fm.getJavaFileObjectsFromFiles(List.of(mainFile.toFile(), testFile.toFile()));
                boolean ok = compiler.getTask(null, fm, diagnostics,
                        List.of("-d", buildDir.toString()), null, units).call();
                if (!ok) {
                    return new CodingResult(CodingResult.Status.COMPILE_ERROR,
                            formatDiagnostics(diagnostics.getDiagnostics()));
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
        // but no exercise can open real windows during a test run)
        Process process = new ProcessBuilder(
                javaExecutable().toString(), MEMORY_CAP, "-Djava.awt.headless=true",
                "-cp", buildDir.toString(), testClass)
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
            if (!process.waitFor(RUN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new CodingResult(CodingResult.Status.TIMEOUT,
                        "Your program ran for more than " + RUN_TIMEOUT_SECONDS
                        + " seconds and was stopped. Check for an infinite loop.\n"
                        + truncate(output.toString()));
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

    private static String formatDiagnostics(List<Diagnostic<? extends JavaFileObject>> diags) {
        List<String> lines = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> d : diags) {
            if (d.getKind() != Diagnostic.Kind.ERROR) continue;
            String source = d.getSource() == null ? "" : Path.of(d.getSource().getName()).getFileName() + ":";
            lines.add(source + d.getLineNumber() + ": " + d.getMessage(null));
        }
        if (lines.isEmpty()) lines.add("Compilation failed (no error details available).");
        return truncate(String.join("\n", lines));
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
