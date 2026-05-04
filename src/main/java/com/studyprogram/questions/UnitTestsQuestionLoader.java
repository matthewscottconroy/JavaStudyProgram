package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class UnitTestsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.UNIT_TESTS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ut-mc-01", Topic.UNIT_TESTS, 1,
                "Which annotation marks a method as a JUnit 5 test?",
                "@Test", "@Run", "@UnitTest", "@Check",
                "a",
                "@Test (from org.junit.jupiter.api) marks a method as a test case. "
                + "JUnit 5 discovers and runs all such methods when the test suite executes."),

            mc("ut-mc-02", Topic.UNIT_TESTS, 1,
                "What does assertEquals(expected, actual) do?",
                "Checks that expected and actual refer to the same object",
                "Checks that expected equals actual using .equals(), failing the test if they differ",
                "Assigns expected to actual",
                "Prints both values for comparison",
                "b",
                "assertEquals calls .equals() to compare values. If they differ, the test fails with a message. "
                + "assertSame checks reference identity (==). "
                + "Always put the expected (known) value first, then the actual (computed) value."),

            mc("ut-mc-03", Topic.UNIT_TESTS, 2,
                "What is the purpose of @BeforeEach?",
                "Runs the annotated method once before all tests in the class",
                "Runs the annotated method before each individual test method",
                "Marks a method that must not fail",
                "Skips the next test if it fails",
                "b",
                "@BeforeEach runs the setup method before every @Test in the class. "
                + "Use it to initialise fresh state for each test. "
                + "@BeforeAll runs once before all tests and must be static (in standard JUnit 5 config)."),

            mc("ut-mc-04", Topic.UNIT_TESTS, 2,
                "Which assertion verifies that a specific exception is thrown?",
                "assertException(MyEx.class, () -> code())",
                "try { code(); } catch (MyEx e) { assertTrue(true); }",
                "assertThrows(MyEx.class, () -> code())",
                "assertEquals(MyEx.class, code())",
                "c",
                "assertThrows(ExpectedException.class, executable) verifies the executable throws "
                + "exactly that exception type. It also returns the exception so you can inspect its message. "
                + "Option B is fragile — if no exception is thrown, the test passes silently."),

            trace("ut-tr-01", Topic.UNIT_TESTS, 2,
                "This test fails. What message does JUnit show?",
                "@Test\n"
                + "void testAdd() {\n"
                + "    int result = 2 + 2;\n"
                + "    assertEquals(5, result);\n"
                + "}",
                "expected: <5> but was: <4>",
                "assertEquals(5, result) fails because result is 4, not 5. "
                + "JUnit reports the expected value (first arg) vs actual (second arg)."),

            debug("ut-db-01", Topic.UNIT_TESTS, 2,
                "This test always passes even when it should fail. Why?",
                "@Test\n"
                + "void testDivide() {\n"
                + "    try {\n"
                + "        int result = 10 / 0;\n"
                + "        assertEquals(2, result);\n"
                + "    } catch (ArithmeticException e) {\n"
                + "        // ignored\n"
                + "    }\n"
                + "}",
                "assertEquals passes when result is 0",
                "The catch block swallows the exception — the test passes without asserting anything",
                "JUnit ignores tests that throw exceptions",
                "10 / 0 returns Infinity in Java",
                "b",
                "The ArithmeticException is caught and silently ignored. "
                + "No assertion is ever reached. JUnit sees no failure and marks the test as passed. "
                + "Fix: use assertThrows(ArithmeticException.class, () -> 10 / 0) or let the exception propagate."),

            codegen("ut-cg-01", Topic.UNIT_TESTS, 2,
                "Which JUnit 5 test correctly checks that a Calculator.add(2, 3) returns 5?",
                "@Test void testAdd() { Calculator.add(2, 3); }",
                "@Test void testAdd() { assertEquals(5, new Calculator().add(2, 3)); }",
                "@Test void testAdd() { assertTrue(new Calculator().add(2, 3)); }",
                "@Check void testAdd() { verify(new Calculator().add(2, 3) == 5); }",
                "b",
                "assertEquals(5, ...) verifies the correct result. "
                + "Option A calls add but asserts nothing. "
                + "Option C uses assertTrue on an int, which won't compile (need a boolean). "
                + "Option D uses fictional @Check and verify()."),

            mc("ut-mc-05", Topic.UNIT_TESTS, 2,
                "What is the purpose of @AfterEach?",
                "Runs once after all tests in the class",
                "Runs after each individual test method — used for cleanup",
                "Skips the next test if the current one passed",
                "Marks a test as expected to fail",
                "b",
                "@AfterEach (like @BeforeEach's inverse) runs after every @Test. "
                + "Use it to tear down shared state: close files, clear caches, reset mocks. "
                + "@AfterAll runs once after all tests (must be static in standard config)."),

            mc("ut-mc-06", Topic.UNIT_TESTS, 3,
                "What does assertNotNull(obj) check?",
                "That obj is not a primitive",
                "That obj is not an empty collection",
                "That obj is not null",
                "That obj.hashCode() is not zero",
                "c",
                "assertNotNull(obj) fails the test if obj == null. "
                + "Use it when you expect an object to be created or returned but want to guard "
                + "against a null result before calling methods on it."),

            trace("ut-tr-02", Topic.UNIT_TESTS, 3,
                "What happens when this test runs?",
                "@Test\n"
                + "void testUpperCase() {\n"
                + "    String result = \"hello\".toUpperCase();\n"
                + "    assertEquals(\"HELLO\", result);\n"
                + "    assertFalse(result.isEmpty());\n"
                + "}",
                "The test passes",
                "\"hello\".toUpperCase() = \"HELLO\". assertEquals passes. "
                + "\"HELLO\".isEmpty() = false, so assertFalse(false) passes. "
                + "Both assertions pass — the test passes."),

            codegen("ut-cg-02", Topic.UNIT_TESTS, 3,
                "Which test verifies that a method throws IllegalArgumentException for negative input?",
                "@Test void test() { calculator.sqrt(-1); }",
                "@Test void test() { try { calculator.sqrt(-1); } catch (Exception e) { assertTrue(true); } }",
                "@Test void test() { assertThrows(IllegalArgumentException.class, () -> calculator.sqrt(-1)); }",
                "@Test void test() { assertEquals(IllegalArgumentException.class, calculator.sqrt(-1)); }",
                "c",
                "assertThrows(ExType.class, executable) verifies the executable throws exactly that exception type. "
                + "It also returns the exception for further inspection. "
                + "Option A: if no exception is thrown, test passes silently. "
                + "Option B: fragile — passes even if no exception is thrown."),

            mc("ut-mc-07", Topic.UNIT_TESTS, 2,
                "What is a test fixture?",
                "A tool for running tests in parallel",
                "The set of objects and state prepared before each test (set up in @BeforeEach)",
                "A mock object that simulates a dependency",
                "A test that has no assertions",
                "b",
                "A test fixture is the consistent, known state in which a test runs. "
                + "@BeforeEach methods build the fixture — creating objects, setting up databases, etc. "
                + "@AfterEach methods tear it down. Consistent fixtures make tests reproducible."),

            mc("ut-mc-08", Topic.UNIT_TESTS, 3,
                "What is a mock object in unit testing?",
                "A real object with additional logging",
                "A fake implementation of a dependency that records calls and returns configured values",
                "A test class that extends the production class",
                "An object created by @BeforeEach",
                "b",
                "Mocks (e.g., via Mockito) replace real dependencies with controllable fakes. "
                + "You can verify that a method was called, how many times, and with what arguments. "
                + "This isolates the unit under test from external systems like databases or services."),

            mc("ut-mc-09", Topic.UNIT_TESTS, 2,
                "What does @Disabled do in JUnit 5?",
                "Deletes the test permanently",
                "Marks the test to be skipped during the current test run",
                "Makes the test run last",
                "Causes the test to be expected to fail",
                "b",
                "@Disabled(\"reason\") skips the test — it appears in the report as ignored. "
                + "Use when a test is known broken and fixing it is deferred. "
                + "Leave a reason string so future developers know why it was disabled."),

            mc("ut-mc-10", Topic.UNIT_TESTS, 3,
                "What is the AAA pattern in unit testing?",
                "Annotation, Assertion, Argument",
                "Arrange, Act, Assert — a structure for writing clear tests",
                "Async, Await, Assert",
                "Authentication, Authorization, Auditing",
                "b",
                "AAA: Arrange (set up inputs and mocks), Act (call the method under test), Assert (verify the result). "
                + "Example: Arrange: Calculator c = new Calculator(); "
                + "Act: int result = c.add(2, 3); "
                + "Assert: assertEquals(5, result);"),

            trace("ut-tr-03", Topic.UNIT_TESTS, 3,
                "This test uses @ParameterizedTest. What does it test?",
                "@ParameterizedTest\n"
                + "@ValueSource(ints = {2, 4, 6, 8})\n"
                + "void testIsEven(int n) {\n"
                + "    assertEquals(0, n % 2);\n"
                + "}\n",
                "Whether 2, 4, 6, and 8 are each even (n % 2 == 0)",
                "@ParameterizedTest runs the test once for each value in @ValueSource. "
                + "4 values → 4 test executions. Each asserts the input is even. "
                + "If any assertion fails, that specific value is reported."),

            trace("ut-tr-04", Topic.UNIT_TESTS, 2,
                "What is the test result?",
                "@Test\n"
                + "void testContains() {\n"
                + "    List<String> list = new ArrayList<>(List.of(\"a\", \"b\", \"c\"));\n"
                + "    list.remove(\"b\");\n"
                + "    assertFalse(list.contains(\"b\"));\n"
                + "    assertEquals(2, list.size());\n"
                + "}",
                "Test passes",
                "After remove(\"b\"), the list is [\"a\", \"c\"]. "
                + "assertFalse(false) passes. assertEquals(2, 2) passes. Both assertions pass."),

            debug("ut-db-02", Topic.UNIT_TESTS, 3,
                "This test is brittle. Why?",
                "@Test\n"
                + "void testGetUser() {\n"
                + "    UserService service = new UserService();\n"
                + "    User user = service.getUser(1);\n"
                + "    assertEquals(\"Alice\", user.getName());\n"
                + "}",
                "The test has no @BeforeEach",
                "The test relies on real database state — if user ID 1 changes or doesn't exist, the test fails for external reasons",
                "assertEquals should use assertSame for objects",
                "UserService must be mocked",
                "b",
                "A brittle test is fragile: it may fail for reasons unrelated to the code under test. "
                + "Depending on a real database means a test failure could indicate a DB issue, not a code bug. "
                + "Fix: mock the data layer so service.getUser(1) returns a controlled User."),

            debug("ut-db-03", Topic.UNIT_TESTS, 2,
                "This test has no assertions. Why is that a problem?",
                "@Test\n"
                + "void testSomething() {\n"
                + "    Calculator c = new Calculator();\n"
                + "    c.add(2, 3);\n"
                + "}",
                "The test will always pass even if add() is broken",
                "Tests with no assertions cause compilation errors",
                "JUnit requires at least one assertTrue",
                "The method name must start with 'assert'",
                "a",
                "A test with no assertions always passes unless an exception is thrown. "
                + "Even if add() returns the wrong answer or infinite loop, the test 'passes'. "
                + "Fix: assertEquals(5, c.add(2, 3));"),

            codegen("ut-cg-03", Topic.UNIT_TESTS, 3,
                "Which JUnit 5 test uses @ParameterizedTest to check that a method returns true for 'a', 'b', 'c'?",
                "@Test void testIn() { assertTrue(contains('a')); assertTrue(contains('b')); assertTrue(contains('c')); }",
                "@ParameterizedTest @ValueSource(strings={\"a\",\"b\",\"c\"}) void testIn(String s) { assertTrue(isVowelOrConsonant(s)); }",
                "@ParameterizedTest void testIn(char c) { assertTrue(contains(c)); }",
                "@Test @Values({\"a\",\"b\",\"c\"}) void testIn(String s) { assertTrue(isLetter(s)); }",
                "b",
                "@ParameterizedTest + @ValueSource runs the test method once per value. "
                + "Option A manually duplicates — doesn't scale. "
                + "Option C: @ParameterizedTest needs a source annotation like @ValueSource. "
                + "Option D: @Values doesn't exist in JUnit 5 — it's @ValueSource.")
        );
    }
}
