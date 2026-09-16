package com.studyprogram.coding;

import java.util.List;

/**
 * Outcome of compiling and testing a student's coding-exercise submission.
 *
 * <p>{@code errorKinds} names the categories of compile error the attempt hit (see
 * {@link CompilerErrorDecoder}). It is empty for anything but a compile failure, and it is what
 * the attempt log records so reports can show a student the mistakes they keep making.
 */
public record CodingResult(Status status, String output, List<String> errorKinds) {

    public enum Status {
        /** Compiled and every test passed. */
        PASS,
        /** Compiled, but one or more tests failed. */
        TEST_FAILURE,
        /** The student's code (or the test harness against it) did not compile. */
        COMPILE_ERROR,
        /** The program ran too long and was killed (likely an infinite loop). */
        TIMEOUT,
        /** The environment cannot run exercises (e.g. no JDK compiler available). */
        ENVIRONMENT_ERROR
    }

    public CodingResult {
        errorKinds = errorKinds == null ? List.of() : List.copyOf(errorKinds);
    }

    public CodingResult(Status status, String output) {
        this(status, output, List.of());
    }

    public boolean passed() { return status == Status.PASS; }
}
