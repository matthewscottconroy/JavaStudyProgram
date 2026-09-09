package com.studyprogram.coding;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SandboxTest {

    private static final List<String> COMMAND = List.of("/usr/bin/java", "-cp", "/tmp/x", "Main");

    @Test
    void backendIsAlwaysReported() {
        assertNotNull(Sandbox.backend());
        assertFalse(Sandbox.describe().isBlank());
    }

    @Test
    void wrappingPreservesTheOriginalCommand() {
        List<String> wrapped = Sandbox.wrap(COMMAND, Path.of("/tmp/build"));
        assertTrue(wrapped.size() >= COMMAND.size());
        // the java command must survive intact at the end of the wrapped command
        assertEquals(COMMAND, wrapped.subList(wrapped.size() - COMMAND.size(), wrapped.size()));
    }

    @Test
    void bwrapBackendIsolatesNetworkAndFilesystem() {
        if (Sandbox.backend() != Sandbox.Backend.BWRAP) return;   // host without bubblewrap
        List<String> wrapped = Sandbox.wrap(COMMAND, Path.of("/tmp/build"));
        assertEquals("bwrap", wrapped.get(0));
        assertTrue(wrapped.contains("--unshare-net"), "outside network must be unreachable");
        assertTrue(wrapped.contains("--die-with-parent"));
        assertTrue(wrapped.contains("--tmpfs"), "private /tmp");
        assertTrue(wrapped.contains("--ro-bind"), "system directories are read-only");
        int bindIndex = wrapped.indexOf("--bind");
        assertTrue(bindIndex > 0 && wrapped.get(bindIndex + 1).equals("/tmp/build"),
                "only the build directory is writable");
    }

    @Test
    void withoutABackendTheCommandIsUnchanged() {
        if (Sandbox.backend() != Sandbox.Backend.NONE) return;
        assertEquals(COMMAND, Sandbox.wrap(COMMAND, Path.of("/tmp/build")));
    }
}
