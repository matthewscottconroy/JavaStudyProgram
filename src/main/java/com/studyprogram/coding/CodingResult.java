package com.studyprogram.coding;

/** Outcome of compiling and testing a student's coding-exercise submission. */
public record CodingResult(Status status, String output) {

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

    public boolean passed() { return status == Status.PASS; }
}
