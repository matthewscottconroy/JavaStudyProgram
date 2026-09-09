package com.studyprogram.coding;

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
    public enum Backend { BWRAP, NONE }

    private static final String DISABLE_ENV = "JAVASTUDY_SANDBOX";

    private static Backend detected;   // cached; detection touches the filesystem

    private Sandbox() {}

    public static synchronized Backend backend() {
        if (detected == null) {
            String setting = System.getenv(DISABLE_ENV);
            if (setting != null && setting.equalsIgnoreCase("off")) {
                detected = Backend.NONE;
            } else if (executableOnPath("bwrap")) {
                detected = Backend.BWRAP;
            } else {
                detected = Backend.NONE;
            }
        }
        return detected;
    }

    /** Human-readable status for the startup banner. */
    public static String describe() {
        return switch (backend()) {
            case BWRAP -> "bubblewrap (isolated filesystem, private /tmp, no network)";
            case NONE  -> "basic (heap cap + timeout only — install bubblewrap for stronger isolation)";
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
        if (backend() != Backend.BWRAP) return command;

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
