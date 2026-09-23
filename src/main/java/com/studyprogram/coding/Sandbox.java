package com.studyprogram.coding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * OS-level containment for the JVM that runs student and exercise code.
 *
 * <p>Compiling and executing code from a question file is inherently a code-execution feature.
 * {@link CodeSafetyScanner} screens untrusted question packs before they ever run; this class adds
 * the second layer for the code that does run, by wrapping the child JVM in a sandbox when the host
 * provides one.
 *
 * <p>Backends, in order of preference:
 * <ul>
 *   <li><b>bubblewrap</b> ({@code bwrap}) — a private mount namespace with a read-only system, a
 *       tmpfs {@code /tmp}, only the build directory writable, and (unless the exercise needs
 *       loopback sockets) no network namespace.</li>
 *   <li><b>none</b> — the child JVM still runs with a heap cap, a wall-clock timeout, headless AWT
 *       and a throwaway working directory, but without kernel-enforced isolation.</li>
 * </ul>
 *
 * <p>Set {@code JAVASTUDY_SANDBOX=off} to disable wrapping (useful when debugging an exercise).
 */
public final class Sandbox {

    /** Which containment backend is in use. */
    public enum Backend { BWRAP, SANDBOX_EXEC, NONE }

    private static final String DISABLE_ENV = "JAVASTUDY_SANDBOX";

    private static Backend detected;   // cached; detection touches the filesystem

    private Sandbox() {}

    /**
     * Overrides detection, for tests that need to exercise what happens on a machine with no
     * containment (every Windows box, for one) without being on such a machine. Pass null to go
     * back to real detection.
     */
    public static synchronized void forceBackendForTesting(Backend backend) {
        detected = backend;
    }

    public static synchronized Backend backend() {
        if (detected == null) {
            detected = selfCheck(detect());
        }
        return detected;
    }

    private static Backend detect() {
        String setting = System.getenv(DISABLE_ENV);
        if (setting != null && setting.equalsIgnoreCase("off")) return Backend.NONE;
        if (executableOnPath("bwrap")) return Backend.BWRAP;
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") && executableOnPath("sandbox-exec")) return Backend.SANDBOX_EXEC;
        return Backend.NONE;
    }

    /**
     * Proves the chosen backend actually runs a JVM here before trusting it with exercises.
     *
     * <p>A sandbox that is installed is not the same as a sandbox that works: bubblewrap fails
     * inside containers that forbid user namespaces, and macOS sandbox profiles vary by release.
     * Without this check the first exercise a student ran would fail with a baffling error. Here a
     * failure degrades to no containment with a clear warning, so the program still works and the
     * banner stops claiming protection it does not have.
     */
    private static Backend selfCheck(Backend candidate) {
        if (candidate == Backend.NONE) return Backend.NONE;
        Path probe = null;
        try {
            probe = Files.createTempDirectory("javastudy-sandbox-check-");
            List<String> command = wrapWith(candidate,
                    List.of(javaBinary(), "-version"), probe);
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(20, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                warnUnavailable(candidate, "the sandboxed JVM did not start within 20 seconds");
                return Backend.NONE;
            }
            if (process.exitValue() != 0) {
                warnUnavailable(candidate, "a sandboxed JVM exited with status " + process.exitValue());
                return Backend.NONE;
            }
            return candidate;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            warnUnavailable(candidate, e.getMessage());
            return Backend.NONE;
        } finally {
            deleteRecursively(probe);
        }
    }

    private static void warnUnavailable(Backend candidate, String reason) {
        System.err.println("Warning: " + candidate.name().toLowerCase().replace('_', '-')
                + " is installed but cannot run here (" + reason + "). Coding exercises will run "
                + "with the heap cap and timeout only. See docs/SECURITY.md.");
    }

    private static String javaBinary() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static void deleteRecursively(Path root) {
        if (root == null) return;
        try (var walk = Files.walk(root)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) { }
            });
        } catch (IOException ignored) { }
    }

    /** Human-readable status for the startup banner. */
    public static String describe() {
        return switch (backend()) {
            case BWRAP -> "bubblewrap (isolated filesystem, private /tmp, no network)";
            case SANDBOX_EXEC -> "macOS sandbox-exec (no network, writes confined to the build dir)";
            case NONE -> "basic (heap cap + timeout only — "
                    + (System.getProperty("os.name", "").toLowerCase().contains("win")
                        ? "no sandbox backend exists for Windows yet"
                        : "install bubblewrap for stronger isolation") + ")";
        };
    }

    /**
     * Wraps a JVM command line in the available sandbox.
     *
     * @param command  the {@code java ...} command to run
     * @param buildDir the throwaway directory holding sources and classes; the only writable path
     * @return the command to actually execute (unchanged when no backend is available)
     */
    public static List<String> wrap(List<String> command, Path buildDir) {
        return wrapWith(backend(), command, buildDir);
    }

    private static List<String> wrapWith(Backend backend, List<String> command, Path buildDir) {
        if (backend == Backend.SANDBOX_EXEC) return sandboxExec(command, buildDir);
        if (backend != Backend.BWRAP) return command;

        List<String> wrapped = new ArrayList<>(List.of(
                "bwrap",
                "--die-with-parent",     // never outlive the study program
                "--new-session",         // detach from the terminal (blocks TIOCSTI injection)
                "--unshare-pid",
                "--unshare-ipc",
                "--unshare-uts",
                "--unshare-cgroup-try",
                "--unshare-net",         // loopback still works; the outside network does not
                "--proc", "/proc",
                "--dev", "/dev",
                "--tmpfs", "/tmp"));     // private scratch; nothing survives the run

        // Read-only view of the system directories the JVM needs
        for (String dir : List.of("/usr", "/bin", "/sbin", "/lib", "/lib64", "/etc")) {
            if (Files.exists(Path.of(dir))) {
                wrapped.add("--ro-bind");
                wrapped.add(dir);
                wrapped.add(dir);
            }
        }
        // The JDK itself may live outside those (e.g. a downloaded toolchain)
        Path javaHome = Path.of(System.getProperty("java.home"));
        if (!startsWithAny(javaHome, "/usr", "/bin", "/lib")) {
            wrapped.add("--ro-bind");
            wrapped.add(javaHome.toString());
            wrapped.add(javaHome.toString());
        }

        // The single writable location, and the working directory
        wrapped.add("--bind");
        wrapped.add(buildDir.toString());
        wrapped.add(buildDir.toString());
        wrapped.add("--chdir");
        wrapped.add(buildDir.toString());

        wrapped.add("--");
        wrapped.addAll(command);
        return wrapped;
    }

    /**
     * macOS containment via {@code sandbox-exec}. The profile denies the network outright and
     * allows writes only under the throwaway build directory and the system temp area, while
     * leaving reads open so the JVM can load its own runtime.
     */
    private static List<String> sandboxExec(List<String> command, Path buildDir) {
        String profile = String.join("\n",
                "(version 1)",
                "(allow default)",
                "(deny network*)",
                "(deny file-write*)",
                "(allow file-write* (subpath \"" + buildDir.toAbsolutePath() + "\"))",
                "(allow file-write* (subpath \"/private/var/folders\"))",
                "(allow file-write* (subpath \"/tmp\"))");
        List<String> wrapped = new ArrayList<>(List.of("sandbox-exec", "-p", profile));
        wrapped.addAll(command);
        return wrapped;
    }

    private static boolean startsWithAny(Path path, String... prefixes) {
        String s = path.toString();
        for (String p : prefixes) if (s.startsWith(p)) return true;
        return false;
    }

    private static boolean executableOnPath(String name) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String dir : path.split(java.io.File.pathSeparator)) {
            if (Files.isExecutable(Path.of(dir, name))) return true;
        }
        return false;
    }
}
