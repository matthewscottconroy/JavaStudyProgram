package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class DebuggingToolsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.DEBUGGING_TOOLS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("dbt-mc-01", Topic.DEBUGGING_TOOLS, 1,
                "A NullPointerException stack trace says 'at MyClass.process(MyClass.java:42)'. What does this tell you?",
                "Line 42 has a syntax error",
                "The NPE was thrown at line 42 of MyClass.java",
                "MyClass has 42 fields",
                "The JVM ran out of memory at line 42",
                "b",
                "Each 'at' line in a stack trace shows a stack frame: class, method, file, and line number. "
                + "The topmost frame is where the exception was thrown. "
                + "Read from top to bottom to trace the call path that led to the error."),

            mc("dbt-mc-02", Topic.DEBUGGING_TOOLS, 2,
                "What is the advantage of using a debugger over adding System.out.println() statements?",
                "println() can only print Strings",
                "A debugger lets you inspect all variables and step through code interactively without modifying source files",
                "Debuggers are faster than println()",
                "println() doesn't work inside loops",
                "b",
                "Print debugging works but requires recompiling after each addition and you must guess what to print ahead of time. "
                + "A debugger lets you pause at any line, inspect any variable, evaluate expressions, and step through logic "
                + "without changing the code. It's more powerful for complex bugs."),

            mc("dbt-mc-03", Topic.DEBUGGING_TOOLS, 2,
                "What is a 'conditional breakpoint'?",
                "A breakpoint that only triggers when a specified condition is true",
                "A breakpoint on an if statement",
                "A breakpoint that pauses for a fixed number of milliseconds",
                "A breakpoint in a catch block",
                "a",
                "Most IDEs let you right-click a breakpoint and add a condition (e.g., i == 500). "
                + "The debugger only pauses when that condition evaluates to true at that point. "
                + "This is invaluable in loops — you don't have to press Resume 499 times to reach the interesting iteration."),

            mc("dbt-mc-04", Topic.DEBUGGING_TOOLS, 3,
                "What does 'watch' (or 'evaluate expression') do in a debugger?",
                "Records all method calls to a log file",
                "Lets you evaluate an arbitrary expression in the current scope while paused",
                "Monitors CPU usage during the debug session",
                "Replaces a variable's value at runtime",
                "b",
                "While paused, 'evaluate expression' lets you type any valid Java expression "
                + "(e.g., list.size(), obj.getField()) and see the result immediately. "
                + "Watches pin expressions so they update every time you step, helping you track values through logic."),

            trace("dbt-tr-01", Topic.DEBUGGING_TOOLS, 2,
                "What does this stack trace tell you about where the bug is?",
                "Exception in thread \"main\" java.lang.NullPointerException\n"
                + "    at com.example.App.getName(App.java:15)\n"
                + "    at com.example.App.printUser(App.java:9)\n"
                + "    at com.example.App.main(App.java:4)",
                "App.java line 15, inside getName()",
                "The NPE was thrown at App.java line 15 in getName(). "
                + "main() called printUser() at line 4, which called getName() at line 9, "
                + "and getName() crashed at line 15. Start debugging at line 15."),

            debug("dbt-db-01", Topic.DEBUGGING_TOOLS, 3,
                "The loop runs 10,000 times but you only want to inspect iteration 9,999. What is the best approach?",
                "for (int i = 0; i < 10000; i++) {\n"
                + "    process(i); // <-- breakpoint here\n"
                + "}",
                "Set a regular breakpoint and press Resume 9,999 times",
                "Add System.out.println(i) and scan the output",
                "Set a conditional breakpoint with condition 'i == 9999'",
                "Temporarily change the loop bound to start at 9999",
                "c",
                "A conditional breakpoint with 'i == 9999' pauses only on that iteration, "
                + "saving you from pressing Resume thousands of times. "
                + "This is far more efficient than print statements or manually pressing Resume repeatedly. "
                + "Option D changes program behavior, which may mask the bug."),

            codegen("dbt-cg-01", Topic.DEBUGGING_TOOLS, 2,
                "Which approach best helps you understand a complex method's behavior before adding a bugfix?",
                "Delete the method and rewrite it",
                "Add a breakpoint at the method entry, run in debug mode, and step through line by line",
                "Add @SuppressWarnings at the top of the class",
                "Rename all variables to shorter names",
                "b",
                "Setting a breakpoint at the method entry and stepping through in debug mode "
                + "lets you see exactly which path is taken, what each variable holds at each step, "
                + "and where the logic diverges from expectations — without changing any code yet."),

            mc("dbt-mc-05", Topic.DEBUGGING_TOOLS, 2,
                "What information does the Variables panel in a debugger show?",
                "All static fields across all loaded classes",
                "The names and current values of local variables and parameters in the current stack frame",
                "The source code of the currently executing method",
                "The heap memory usage histogram",
                "b",
                "The Variables panel updates every time you step, showing the current value of each local variable, "
                + "parameter, and 'this'. You can expand object references to see their fields. "
                + "This is the primary way to inspect state without adding print statements."),

            mc("dbt-mc-06", Topic.DEBUGGING_TOOLS, 3,
                "What is the purpose of 'Run to Cursor' in a debugger?",
                "Moves the cursor to the currently executing line",
                "Runs the program until it reaches the line where the cursor is placed, then pauses",
                "Restarts execution from the cursor position",
                "Sets a permanent breakpoint at the cursor line",
                "b",
                "'Run to Cursor' (Alt+F9 in IntelliJ) temporarily sets a one-shot breakpoint at the cursor line "
                + "and resumes execution. It's convenient for skipping over a block of code you don't want to step through "
                + "without permanently adding a breakpoint."),

            mc("dbt-mc-07", Topic.DEBUGGING_TOOLS, 3,
                "What is an exception breakpoint?",
                "A breakpoint placed inside a catch block",
                "A debugger feature that automatically pauses whenever a specific exception type is thrown",
                "A breakpoint that is triggered only when an uncaught exception reaches main()",
                "A breakpoint that logs the exception and continues",
                "b",
                "Exception breakpoints (IntelliJ: Run → View Breakpoints → add Java Exception Breakpoints) "
                + "pause the debugger the instant an exception is thrown — even if it's caught and swallowed. "
                + "This is invaluable for finding where exceptions originate before they are suppressed."),

            trace("dbt-tr-02", Topic.DEBUGGING_TOOLS, 2,
                "The debugger pauses here. What is the value of 'result' at this point?",
                "int[] nums = {3, 1, 4, 1, 5};\n"
                + "int result = 0;\n"
                + "for (int n : nums) {\n"
                + "    if (n > 3) result += n;  // paused AFTER processing all elements\n"
                + "}",
                "9",
                "Only values > 3 are added: 4 and 5. result = 4 + 5 = 9."),

            trace("dbt-tr-03", Topic.DEBUGGING_TOOLS, 3,
                "A stack trace shows three frames. Which method is currently executing?",
                "Exception in thread \"main\" java.lang.ArithmeticException: / by zero\n"
                + "    at Calc.divide(Calc.java:8)\n"
                + "    at Calc.run(Calc.java:4)\n"
                + "    at Calc.main(Calc.java:1)",
                "Calc.divide at line 8",
                "The top frame is the innermost (currently active) method. "
                + "Calc.divide threw the exception at line 8. run() called divide(), main() called run()."),

            debug("dbt-db-02", Topic.DEBUGGING_TOOLS, 2,
                "A student's conditional breakpoint condition is 'i = 5' instead of 'i == 5'. What happens?",
                "for (int i = 0; i < 10; i++) {\n"
                + "    doWork(i);  // conditional breakpoint: condition = \"i = 5\"\n"
                + "}",
                "The breakpoint triggers only on iteration 5",
                "The breakpoint triggers on every iteration",
                "The IDE rejects the condition because assignment is not allowed in expressions",
                "The debugger modifies i to 5 on every iteration",
                "c",
                "Most debugger condition evaluators reject assignment (=) in conditions and report an error. "
                + "The correct operator for equality check is ==. "
                + "Some environments may accept it as an expression, setting i=5 each time — so always use == for comparisons."),

            codegen("dbt-cg-02", Topic.DEBUGGING_TOOLS, 2,
                "Which is the best first step when a unit test fails with an unexpected value?",
                "Delete the test and rewrite it",
                "Set a breakpoint at the assertion line, run the test in debug mode, and inspect the actual value",
                "Add @Ignore to skip the failing test",
                "Increase the test timeout",
                "b",
                "Running the failing test in debug mode and pausing at the assertion lets you inspect "
                + "the actual computed value and trace back through the logic to find where it diverged from expectation. "
                + "This is faster than adding print statements and recompiling repeatedly.")
        );
    }
}
