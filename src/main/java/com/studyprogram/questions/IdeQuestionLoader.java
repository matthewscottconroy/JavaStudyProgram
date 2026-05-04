package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class IdeQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.IDE; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ide-mc-01", Topic.IDE, 1,
                "What does 'Step Over' do in an IDE debugger?",
                "Skips the current line entirely",
                "Executes the current line and moves to the next, without entering called methods",
                "Enters the method call on the current line",
                "Restarts execution from the beginning",
                "b",
                "Step Over (F8 in IntelliJ, F6 in Eclipse) executes the current statement and advances to the next. "
                + "If the line calls a method, it runs that method completely without pausing inside it. "
                + "Use 'Step Into' (F7) if you want to enter the method."),

            mc("ide-mc-02", Topic.IDE, 1,
                "What is a breakpoint?",
                "A syntax error the compiler found",
                "A marker that pauses program execution at a specific line so you can inspect state",
                "A point where the program automatically saves its output",
                "A comment that documents a known bug",
                "b",
                "A breakpoint tells the debugger to suspend execution when it reaches that line. "
                + "Once paused, you can inspect local variables, the call stack, evaluate expressions, "
                + "and step through code line by line. Set one by clicking the gutter next to the line number."),

            mc("ide-mc-03", Topic.IDE, 2,
                "What does 'Step Into' do differently from 'Step Over'?",
                "It skips the current method and goes to the next class",
                "It enters the body of the method being called on the current line",
                "It runs until the next breakpoint",
                "It steps backwards to the previous line",
                "b",
                "Step Into (F7 in IntelliJ) descends into the method call on the current line. "
                + "This lets you trace execution inside helper methods or library code. "
                + "Step Over is preferred when you trust a called method and don't want to trace inside it."),

            mc("ide-mc-04", Topic.IDE, 2,
                "What does the call stack panel show while paused at a breakpoint?",
                "A list of all classes in the project",
                "The sequence of method calls that led to the current execution point",
                "All variables declared in the program",
                "The compile errors in the current file",
                "b",
                "The call stack (stack trace) shows the chain of method invocations — "
                + "innermost frame at the top, outermost (main) at the bottom. "
                + "Clicking a frame lets you see local variables at that level. "
                + "Unhandled exceptions print this stack to stderr."),

            trace("ide-tr-01", Topic.IDE, 2,
                "The debugger is paused at the marked line. What is the value of 'sum'?",
                "int sum = 0;\n"
                + "for (int i = 1; i <= 3; i++) {\n"
                + "    sum += i;  // <-- breakpoint, AFTER this executes on i=3\n"
                + "}\n"
                + "// paused just before the loop exits",
                "6",
                "Each iteration adds i to sum: 0+1=1, 1+2=3, 3+3=6. "
                + "After the third iteration completes, sum is 6."),

            debug("ide-db-01", Topic.IDE, 2,
                "A student sets a breakpoint but the program never pauses. What is the most likely cause?",
                "int x = 5;\n"
                + "// Breakpoint set here\n"
                + "System.out.println(x);\n"
                + "// Student runs via 'Run' button, not 'Debug' button",
                "The breakpoint is on a comment line",
                "The program was launched with 'Run' instead of 'Debug' — breakpoints only trigger in debug mode",
                "Local variables cannot have breakpoints",
                "println() clears all breakpoints",
                "b",
                "Breakpoints only activate when the program is started via the Debug action (Shift+F9 / green bug icon). "
                + "Running normally ignores breakpoints entirely. "
                + "Also check: the breakpoint icon should be solid red; a hollow icon means the class hasn't been loaded."),

            codegen("ide-cg-01", Topic.IDE, 2,
                "Which IntelliJ shortcut renames a variable across all its usages in the file?",
                "Ctrl+R",
                "Shift+F6",
                "Alt+Enter",
                "Ctrl+Shift+F",
                "b",
                "Shift+F6 triggers 'Rename' refactoring in IntelliJ, updating all references to the symbol across the project. "
                + "Alt+Enter shows context actions (quick-fixes). Ctrl+R is Find/Replace. "
                + "Always prefer IDE refactoring over manual find-replace to avoid missed usages."),

            mc("ide-mc-05", Topic.IDE, 2,
                "What does 'Step Out' (also called 'Step Return') do in a debugger?",
                "Terminates the current debug session",
                "Finishes the current method and pauses at the line that called it",
                "Moves back one line in execution",
                "Skips all remaining breakpoints",
                "b",
                "Step Out runs the rest of the current method without pausing, then pauses at the call site. "
                + "Useful when you accidentally stepped into a method you didn't need to examine. "
                + "In IntelliJ: Shift+F8; in Eclipse: F7."),

            mc("ide-mc-06", Topic.IDE, 2,
                "What is 'code completion' in an IDE?",
                "A feature that automatically fixes compile errors",
                "A suggestion popup that offers valid completions for the code you are typing",
                "A tool that formats your code according to a style guide",
                "A shortcut that runs all unit tests",
                "b",
                "Code completion (IntelliJ: Ctrl+Space) shows possible method names, field names, "
                + "types, and keywords based on the current context. "
                + "It reduces typos and helps you discover API methods without leaving the editor."),

            mc("ide-mc-07", Topic.IDE, 3,
                "What does 'Extract Method' refactoring do?",
                "Copies a method to a new file",
                "Moves selected code into a new named method, replacing the original code with a call to it",
                "Generates a method signature from its call site",
                "Deletes unused methods",
                "b",
                "Extract Method (IntelliJ: Ctrl+Alt+M) identifies the selected code block, "
                + "creates a new private method with appropriate parameters and return type, "
                + "and replaces the original code with a call to the new method. "
                + "It reduces duplication and improves readability."),

            trace("ide-tr-02", Topic.IDE, 2,
                "The debugger shows variables panel: x=5, y=null. The next line is 'int z = x + y.length();'. What happens?",
                "int x = 5;\n"
                + "String y = null;\n"
                + "int z = x + y.length();  // <-- about to execute",
                "NullPointerException",
                "y is null, so calling y.length() throws NullPointerException. "
                + "The debugger will highlight the exception; inspect the variables panel to confirm y is null before executing."),

            trace("ide-tr-03", Topic.IDE, 1,
                "You run a program in IntelliJ with the Debug button. A red dot appears next to line 7. What does that dot mean?",
                "// line 7 of Main.java:\n"
                + "System.out.println(\"Hello\");  // <-- red filled circle in gutter",
                "An active breakpoint is set at line 7",
                "A filled red circle in the gutter is a breakpoint marker. "
                + "The program will pause before executing that line when run in debug mode."),

            debug("ide-db-02", Topic.IDE, 2,
                "After renaming method 'calculate' to 'compute' using Find-Replace, the project fails to compile. Why?",
                "// Used Find/Replace to rename 'calculate' → 'compute'\n"
                + "// Error: cannot find symbol: method calculate()\n"
                + "// in ExternalLib.java which calls: obj.calculate()",
                "Find-Replace renames in all files automatically",
                "Find-Replace missed a call site in another file — IDE Rename refactoring updates all usages",
                "Renaming methods requires restarting the IDE",
                "Method names cannot be changed after compilation",
                "b",
                "Find-Replace is text-based and may miss call sites in other files, skip overrides in subclasses, "
                + "or incorrectly rename unrelated strings that contain the same word. "
                + "IDE refactoring (Shift+F6) performs a semantic rename aware of the class hierarchy and all usages."),

            codegen("ide-cg-02", Topic.IDE, 2,
                "Which action in IntelliJ generates getters and setters for a class's private fields?",
                "Right-click → Refactor → Extract Field",
                "Alt+Insert → Getter and Setter",
                "Ctrl+Shift+G",
                "Edit → Generate → Implements",
                "b",
                "Alt+Insert (Generate menu) lets you choose Getter, Setter, or Getter and Setter. "
                + "IntelliJ generates correct boilerplate based on the field names and types. "
                + "This avoids typos and is faster than writing them manually.")
        );
    }
}
