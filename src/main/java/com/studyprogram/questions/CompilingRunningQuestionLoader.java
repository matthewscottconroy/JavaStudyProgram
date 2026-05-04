package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class CompilingRunningQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.COMPILING_RUNNING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("cmp-mc-01", Topic.COMPILING_RUNNING, 1,
                "What does the 'javac' command do?",
                "Runs a compiled Java program",
                "Compiles Java source (.java) files into bytecode (.class) files",
                "Packages Java classes into a JAR file",
                "Starts the Java Virtual Machine",
                "b",
                "'javac' is the Java compiler. It converts human-readable .java source files "
                + "into .class bytecode files that the JVM can execute."),

            mc("cmp-mc-02", Topic.COMPILING_RUNNING, 1,
                "Which command runs a compiled Java class named 'Hello'?",
                "javac Hello", "javac Hello.class", "java Hello", "java Hello.java",
                "c",
                "'java Hello' launches the JVM and runs the bytecode in Hello.class. "
                + "'javac' compiles, 'java' runs. Don't include .class when running."),

            mc("cmp-mc-03", Topic.COMPILING_RUNNING, 2,
                "What is the classpath?",
                "The directory where the JDK is installed",
                "A list of locations the JVM searches for .class files and JARs",
                "The path to the Java source file being compiled",
                "The runtime memory layout of a Java program",
                "b",
                "The classpath tells the JVM and compiler where to find compiled classes and libraries. "
                + "It can be set with the -cp flag, the CLASSPATH variable, or implicitly from the current directory."),

            mc("cmp-mc-04", Topic.COMPILING_RUNNING, 2,
                "A missing semicolon causes a…",
                "Runtime exception", "Compile-time error", "Logic error", "Stack overflow",
                "b",
                "A missing semicolon is a syntax error caught by the compiler (javac) before the program runs. "
                + "Runtime exceptions happen during execution. Logic errors produce wrong output but compile."),

            trace("cmp-tr-01", Topic.COMPILING_RUNNING, 1,
                "What file does 'javac Hello.java' produce if compilation succeeds?",
                "// No code — this is a conceptual question.\n"
                + "// Command: javac Hello.java",
                "Hello.class",
                "The Java compiler produces a .class file containing JVM bytecode with the same base name."),

            debug("cmp-db-01", Topic.COMPILING_RUNNING, 2,
                "Running 'java Hello.class' produces an error. Why?",
                "javac Hello.class",
                "Hello is not a valid class name",
                "The .class extension should not be included when running with 'java'",
                "'java' requires a .jar argument",
                "Hello needs a package declaration",
                "b",
                "'java' takes the class name, not the filename. "
                + "Use 'java Hello' (without .class). Including .class makes Java look for a class named 'Hello.class'."),

            codegen("cmp-cg-01", Topic.COMPILING_RUNNING, 2,
                "What is the correct sequence to compile and run 'Greeting.java' from the terminal?",
                "java Greeting.java && javac Greeting",
                "javac Greeting.java && java Greeting",
                "compile Greeting.java && run Greeting",
                "javac Greeting && java Greeting.class",
                "b",
                "Compile first with javac (producing Greeting.class), then run with java. "
                + "Option A has the order reversed. Option C uses fictional commands. "
                + "Option D omits .java for javac and incorrectly adds .class for java."),

            mc("cmp-mc-05", Topic.COMPILING_RUNNING, 2,
                "What is a compile-time error vs a runtime error?",
                "Compile-time errors occur only on Windows; runtime errors only on Mac",
                "Compile-time errors are caught by javac before the program runs; runtime errors occur while the program is executing",
                "Compile-time errors crash the JVM; runtime errors are silent",
                "They are the same thing — both prevent the program from running",
                "b",
                "Compile-time errors (syntax errors, type mismatches) are caught by javac. "
                + "The program cannot run until they are fixed. "
                + "Runtime errors (NullPointerException, ArrayIndexOutOfBoundsException) occur during execution — "
                + "they may only appear under certain conditions."),

            trace("cmp-tr-02", Topic.COMPILING_RUNNING, 1,
                "Does this produce a compile error or a runtime error?",
                "public class Main {\n"
                + "    public static void main(String[] args) {\n"
                + "        int x = \"hello\";  // assigning String to int\n"
                + "    }\n"
                + "}",
                "Compile error",
                "Assigning a String to an int is a type mismatch caught by the compiler at compile time. "
                + "javac reports: incompatible types: String cannot be converted to int. "
                + "The program never runs."),

            mc("cmp-mc-06", Topic.COMPILING_RUNNING, 2,
                "What does the JVM (Java Virtual Machine) do?",
                "Compiles .java files into .class files",
                "Executes Java bytecode (.class files) on any platform",
                "Links C libraries with Java code",
                "Manages the pom.xml dependencies",
                "b",
                "javac compiles .java → .class (bytecode). "
                + "The JVM executes the bytecode on the specific platform. "
                + "This is why Java is 'write once, run anywhere' — the same .class runs on any JVM. "
                + "The JVM also manages memory (garbage collection) and provides the standard library."),

            mc("cmp-mc-07", Topic.COMPILING_RUNNING, 2,
                "What type of error is: accessing arr[10] in an array of length 5?",
                "Compile-time error", "Logic error", "Runtime exception", "Syntax error",
                "c",
                "The compiler cannot know array bounds violations ahead of time. "
                + "ArrayIndexOutOfBoundsException is thrown at runtime when the invalid index is accessed."),

            mc("cmp-mc-08", Topic.COMPILING_RUNNING, 2,
                "What does a JAR file contain?",
                "Java source (.java) files",
                "Compiled Java bytecode (.class files) and metadata, packaged in a ZIP-format archive",
                "JVM configuration settings",
                "Only the main class, not dependencies",
                "b",
                "JAR (Java ARchive) bundles .class files, resources, and a META-INF/MANIFEST.MF. "
                + "It's a ZIP archive. Fat/uber JARs also include all dependencies. "
                + "Run with: java -jar myapp.jar"),

            mc("cmp-mc-09", Topic.COMPILING_RUNNING, 3,
                "What is the difference between JDK, JRE, and JVM?",
                "They are three names for the same tool",
                "JVM runs bytecode; JRE = JVM + standard library; JDK = JRE + compiler (javac) + dev tools",
                "JDK is for Linux; JRE for Windows; JVM for Mac",
                "JRE is for compiling; JDK is for running; JVM handles networking",
                "b",
                "JVM: the virtual machine that executes .class bytecode. "
                + "JRE (Java Runtime Environment): JVM + standard class libraries — for running Java programs. "
                + "JDK (Java Development Kit): JRE + javac + javadoc + jdb + other tools — for developing."),

            trace("cmp-tr-03", Topic.COMPILING_RUNNING, 2,
                "What type of error will the compiler report for this code?",
                "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        int x = 5\n"
                + "        System.out.println(x);\n"
                + "    }\n"
                + "}",
                "Compile-time syntax error (missing semicolon)",
                "Missing semicolons are caught by javac before the program runs. "
                + "The program cannot be compiled or run until the syntax error is fixed."),

            trace("cmp-tr-04", Topic.COMPILING_RUNNING, 3,
                "What does 'java -cp lib/mylib.jar Main' do?",
                "// No code — command-line question.\n"
                + "// Command: java -cp lib/mylib.jar Main",
                "Runs the Main class with lib/mylib.jar added to the classpath",
                "-cp (classpath) tells the JVM where to look for classes. "
                + "lib/mylib.jar is added to the search path so Main and any classes it imports from mylib.jar can be found."),

            debug("cmp-db-02", Topic.COMPILING_RUNNING, 2,
                "This command fails with 'class not found'. Why?",
                "// After: javac com/example/Hello.java\n"
                + "// Running from project root:\n"
                + "java Hello",
                "Hello.class was not generated by javac",
                "The class is in the com.example package — it must be run as 'java com.example.Hello' from the project root",
                "java requires the .java extension",
                "com/example is not a valid directory structure",
                "b",
                "When a class is in a package, you must supply the fully-qualified name: java com.example.Hello. "
                + "Running from the project root ensures the com/example directory structure is on the classpath. "
                + "Running just 'java Hello' only works for classes in the default (unnamed) package."),

            debug("cmp-db-03", Topic.COMPILING_RUNNING, 3,
                "The compilation succeeds but the program crashes with StackOverflowError. Why?",
                "public class Fib {\n"
                + "    static int fib(int n) { return fib(n - 1) + fib(n - 2); }\n"
                + "    public static void main(String[] args) { System.out.println(fib(10)); }\n"
                + "}",
                "The compiler didn't detect infinite recursion",
                "fib(n) has no base case — it recurses infinitely, filling the call stack",
                "Integer arithmetic overflows in the recursion",
                "The JVM cannot handle recursive methods",
                "b",
                "Syntax is valid so it compiles. At runtime, fib(10) calls fib(9) and fib(8), "
                + "which call fib(8) and fib(7) and fib(7) and fib(6)... with no base case to stop. "
                + "Fix: add if (n <= 1) return n; as the base case."),

            codegen("cmp-cg-02", Topic.COMPILING_RUNNING, 2,
                "Which command compiles all .java files in the src/ directory and outputs .class files to out/?",
                "javac src/ -o out/",
                "javac -d out src/*.java",
                "java -compile src/ out/",
                "javac src/*.java > out/",
                "b",
                "-d specifies the output directory for .class files. "
                + "src/*.java matches all .java files in src/. "
                + "Option A: javac doesn't use -o. "
                + "Option C: java doesn't have a -compile flag. "
                + "Option D: > redirects stdout, not the compiled output.")
        );
    }
}
