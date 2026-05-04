package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ExceptionHandlingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.TRY_CATCH; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ex-mc-01", Topic.TRY_CATCH, 2,
                "Which block always executes, whether or not an exception occurs?",
                "catch", "try", "finally", "throws",
                "c",
                "The 'finally' block runs after the try and any matching catch, regardless of whether "
                + "an exception was thrown. Commonly used to close resources."),

            mc("ex-mc-02", Topic.TRY_CATCH, 2,
                "What is the difference between checked and unchecked exceptions?",
                "Checked exceptions are faster at runtime",
                "Checked exceptions must be declared or caught; unchecked need not be",
                "Unchecked exceptions can only occur in catch blocks",
                "Checked exceptions extend RuntimeException",
                "b",
                "Checked exceptions (e.g., IOException) are checked at compile time — you must "
                + "either catch them or declare them with 'throws'. "
                + "Unchecked exceptions (RuntimeException and its subclasses) can be left unhandled."),

            mc("ex-mc-03", Topic.TRY_CATCH, 3,
                "What is printed?\n\n"
                + "    try {\n"
                + "        int x = 1 / 0;\n"
                + "        System.out.println(\"A\");\n"
                + "    } catch (ArithmeticException e) {\n"
                + "        System.out.println(\"B\");\n"
                + "    } finally {\n"
                + "        System.out.println(\"C\");\n"
                + "    }",
                "A C", "B", "B C", "A B C",
                "c",
                "1/0 throws ArithmeticException. \"A\" is never reached. "
                + "The catch block prints \"B\". finally always runs, printing \"C\"."),

            trace("ex-tr-01", Topic.TRY_CATCH, 3,
                "What is printed?",
                "try {\n    String s = null;\n    System.out.println(s.length());\n"
                + "} catch (NullPointerException e) {\n    System.out.println(\"NPE\");\n}",
                "NPE",
                "null.length() throws NullPointerException. The catch block prints \"NPE\"."),

            debug("ex-db-01", Topic.TRY_CATCH, 3,
                "Why won't this code compile?",
                "try {\n    System.out.println(\"ok\");\n} catch (Exception e) {\n"
                + "} catch (IOException e) {\n}",
                "IOException cannot be caught",
                "catch blocks cannot be empty",
                "IOException is a subclass of Exception — it is unreachable after Exception is caught",
                "try block must throw an exception",
                "c",
                "Catch blocks are evaluated top-to-bottom. Since Exception catches everything, "
                + "the IOException catch can never be reached. "
                + "Put more specific exception types before more general ones."),

            codegen("ex-cg-01", Topic.TRY_CATCH, 3,
                "Which code correctly opens a file and uses try-with-resources to ensure it closes?",
                "try (BufferedReader br = new BufferedReader(new FileReader(\"f.txt\"))) { /* read */ }",
                "try { BufferedReader br = new BufferedReader(new FileReader(\"f.txt\")); } finally { br.close(); }",
                "using (BufferedReader br = new BufferedReader(new FileReader(\"f.txt\"))) { /* read */ }",
                "open (BufferedReader br = new FileReader(\"f.txt\")) { /* read */ }",
                "a",
                "try-with-resources (Java 7+) automatically calls close() on the resource when the block exits. "
                + "Option B doesn't compile (br is out of scope in finally). "
                + "Options C and D use non-Java syntax."),

            mc("ex-mc-04", Topic.TRY_CATCH, 2,
                "What does 'throws' do in a method signature?",
                "Immediately throws the named exception",
                "Declares that this method may propagate the named checked exception to its caller",
                "Catches the named exception inside the method",
                "Converts a runtime exception to a checked exception",
                "b",
                "'throws' in the signature is a compile-time declaration: "
                + "public void readFile(String path) throws IOException means the caller must handle IOException. "
                + "It does not throw anything by itself — the actual throw happens with the 'throw' keyword."),

            trace("ex-tr-02", Topic.TRY_CATCH, 3,
                "What is printed?",
                "try {\n"
                + "    System.out.println(\"try\");\n"
                + "    return;\n"
                + "} finally {\n"
                + "    System.out.println(\"finally\");\n"
                + "}",
                "try\nfinally",
                "try\nfinally",
                "The finally block runs even when the try block executes a return statement. "
                + "Output order: 'try' first, then 'finally' before the method actually returns."),

            mc("ex-mc-05", Topic.TRY_CATCH, 3,
                "What is a multi-catch block?",
                "Multiple try blocks in sequence",
                "A catch clause that catches two or more exception types using |",
                "A finally block that handles multiple exceptions",
                "Catching the same exception in two separate catch blocks",
                "b",
                "Multi-catch (Java 7+) allows: catch (IOException | SQLException e) { ... } "
                + "to handle multiple unrelated exception types with one handler. "
                + "The caught exception variable is implicitly final in a multi-catch."),

            codegen("ex-cg-02", Topic.TRY_CATCH, 3,
                "Which defines a custom checked exception called 'InsufficientFundsException'?",
                "class InsufficientFundsException { double amount; }",
                "class InsufficientFundsException extends RuntimeException { public InsufficientFundsException(String msg) { super(msg); } }",
                "class InsufficientFundsException extends Exception { public InsufficientFundsException(String msg) { super(msg); } }",
                "interface InsufficientFundsException extends Throwable { }",
                "c",
                "Extending Exception (not RuntimeException) makes it checked — callers must handle or declare it. "
                + "Calling super(msg) passes the message to Exception for use in getMessage() and stack traces. "
                + "Option B creates an unchecked exception. Option D uses interface syntax."),

            mc("ex-mc-06", Topic.TRY_CATCH, 2,
                "What does the 'throw' keyword do?",
                "Declares that a method may throw an exception",
                "Catches an exception from inside a catch block",
                "Explicitly throws an exception object",
                "Rethrows the last caught exception automatically",
                "c",
                "'throw new IllegalArgumentException(\"bad input\")' creates and immediately throws the exception. "
                + "'throws' in the method signature only declares; 'throw' actually fires the exception."),

            mc("ex-mc-07", Topic.TRY_CATCH, 3,
                "Which exception is thrown by Integer.parseInt(\"hello\")?",
                "ClassCastException",
                "NumberFormatException",
                "IllegalArgumentException",
                "ArithmeticException",
                "b",
                "NumberFormatException is a subclass of IllegalArgumentException, thrown when a string "
                + "cannot be parsed as a number. Always wrap parseInt calls in try-catch when reading untrusted input."),

            mc("ex-mc-08", Topic.TRY_CATCH, 3,
                "What is exception chaining?",
                "Catching multiple exceptions in one catch block with |",
                "Wrapping a caught exception inside a new exception to preserve the original cause",
                "A chain of catch blocks from most specific to most general",
                "Automatically rethrowing exceptions from a finally block",
                "b",
                "Exception chaining: catch (IOException e) { throw new ServiceException(\"failed\", e); } "
                + "The original IOException is stored as the cause. "
                + "new ServiceException(\"failed\", e) — the second arg is the cause, accessible via getCause()."),

            mc("ex-mc-09", Topic.TRY_CATCH, 2,
                "What is printed if no exception is thrown in a try-catch-finally block?",
                "Only the try block output",
                "Only the finally block output",
                "Both the try block output and the finally block output",
                "Nothing — a try must always throw to be useful",
                "c",
                "If the try block completes normally (no exception), the catch block is skipped "
                + "and the finally block still executes. Both produce output."),

            trace("ex-tr-03", Topic.TRY_CATCH, 2,
                "What is printed?",
                "try {\n"
                + "    int[] arr = new int[3];\n"
                + "    arr[5] = 1;\n"
                + "} catch (ArrayIndexOutOfBoundsException e) {\n"
                + "    System.out.println(\"caught: \" + e.getMessage());\n"
                + "}",
                "caught: Index 5 out of bounds for length 3",
                "arr[5] throws ArrayIndexOutOfBoundsException. e.getMessage() returns the JVM's description of the error."),

            trace("ex-tr-04", Topic.TRY_CATCH, 3,
                "What is printed?",
                "try {\n"
                + "    Object o = \"hello\";\n"
                + "    Integer n = (Integer) o;\n"
                + "} catch (ClassCastException | NullPointerException e) {\n"
                + "    System.out.println(e.getClass().getSimpleName());\n"
                + "}",
                "ClassCastException",
                "Casting a String to Integer throws ClassCastException. "
                + "The multi-catch catches it. getSimpleName() returns just the class name, not the full package path."),

            debug("ex-db-02", Topic.TRY_CATCH, 2,
                "This method is supposed to rethrow the exception but it doesn't. Why?",
                "static void process() throws Exception {\n"
                + "    try {\n"
                + "        riskyOp();\n"
                + "    } catch (Exception e) {\n"
                + "        System.out.println(\"Error: \" + e.getMessage());\n"
                + "    }\n"
                + "}",
                "The method must use 'throws RuntimeException' instead",
                "The catch block swallows the exception — it logs but never rethrows, so callers never see it",
                "finally block is missing",
                "System.out.println is not allowed inside catch",
                "b",
                "Catching an exception and not rethrowing it is called 'swallowing'. "
                + "The method declares 'throws Exception' but the catch prevents it from propagating. "
                + "Fix: add 'throw e;' or 'throw new RuntimeException(\"failed\", e);' after logging."),

            debug("ex-db-03", Topic.TRY_CATCH, 3,
                "Why does this code not compile?",
                "static void open() {\n"
                + "    FileReader fr = new FileReader(\"data.txt\");\n"
                + "}",
                "FileReader is not part of Java's standard library",
                "FileReader requires a try-with-resources block",
                "FileReader constructor throws checked IOException which must be caught or declared",
                "String literals cannot be passed to constructors",
                "c",
                "FileReader(String) throws IOException, which is a checked exception. "
                + "The compiler requires you to either surround with try-catch or declare 'throws IOException' on the method. "
                + "Unchecked (Runtime) exceptions have no such requirement."),

            codegen("ex-cg-03", Topic.TRY_CATCH, 2,
                "Which correctly reads a file and handles the potential IOException?",
                "FileReader fr = new FileReader(\"f.txt\");",
                "try { FileReader fr = new FileReader(\"f.txt\"); } catch (Exception e) {}",
                "try (FileReader fr = new FileReader(\"f.txt\")) { /* use fr */ } catch (IOException e) { e.printStackTrace(); }",
                "FileReader fr = (FileReader) new File(\"f.txt\");",
                "c",
                "try-with-resources closes fr automatically. "
                + "Catching IOException (not bare Exception) is more specific and best practice. "
                + "Option A doesn't compile (uncaught checked exception). "
                + "Option B silently swallows the error. "
                + "Option D is an invalid cast.")
        );
    }
}
