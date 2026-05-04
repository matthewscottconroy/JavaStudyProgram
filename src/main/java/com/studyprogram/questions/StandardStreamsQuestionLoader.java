package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class StandardStreamsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.STANDARD_STREAMS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ios-mc-01", Topic.STANDARD_STREAMS, 1,
                "What are the three standard streams in Java?",
                "stdin, stdout, stderr",
                "System.in, System.out, System.err",
                "input, output, error",
                "FileInputStream, FileOutputStream, ErrorStream",
                "b",
                "Java exposes: System.in (InputStream for keyboard/pipe input), "
                + "System.out (PrintStream for normal output), and System.err (PrintStream for error messages). "
                + "These map to the OS standard streams (stdin/stdout/stderr) by default."),

            mc("ios-mc-02", Topic.STANDARD_STREAMS, 1,
                "Which class is typically used to read user input from System.in?",
                "InputReader", "BufferedInputStream", "Scanner", "Console",
                "c",
                "Scanner wraps System.in and provides nextLine(), nextInt(), nextDouble() etc. "
                + "for convenient token-based reading. "
                + "BufferedReader(new InputStreamReader(System.in)) is also common and more efficient for large input. "
                + "Console is useful for password reading (masks characters) but isn't available in all environments."),

            mc("ios-mc-03", Topic.STANDARD_STREAMS, 2,
                "What is the difference between System.out and System.err?",
                "System.err automatically formats error messages; System.out does not",
                "System.out is buffered while System.err is typically unbuffered and used for error/diagnostic messages",
                "System.err writes to a separate log file by default",
                "They are identical — just two names for the same stream",
                "b",
                "System.err is intended for error/diagnostic output. It is often unbuffered, "
                + "so messages appear immediately even if the program crashes. "
                + "In a terminal both usually go to the same display, but they can be redirected independently: "
                + "'java App 2>errors.log' sends stderr to a file while stdout still shows on screen."),

            mc("ios-mc-04", Topic.STANDARD_STREAMS, 2,
                "What happens if you call scanner.nextInt() but the user types a word?",
                "It returns 0",
                "It throws InputMismatchException",
                "It blocks and waits for an integer",
                "It skips the input and returns -1",
                "b",
                "nextInt() expects an integer token. If the next token can't be parsed as int, "
                + "InputMismatchException is thrown and the bad token remains in the Scanner's buffer. "
                + "Wrap in try/catch and call scanner.next() to discard the bad token before retrying."),

            trace("ios-tr-01", Topic.STANDARD_STREAMS, 2,
                "The user types '42' and presses Enter. What is printed?",
                "Scanner sc = new Scanner(System.in);\n"
                + "int n = sc.nextInt();\n"
                + "System.out.println(\"Got: \" + n);",
                "Got: 42",
                "nextInt() reads the integer 42. println concatenates \"Got: \" with 42, printing 'Got: 42'."),

            debug("ios-db-01", Topic.STANDARD_STREAMS, 2,
                "After reading an int, nextLine() returns an empty string. Why?",
                "Scanner sc = new Scanner(System.in);\n"
                + "int age = sc.nextInt();\n"
                + "String name = sc.nextLine(); // always empty\n"
                + "System.out.println(name);",
                "nextLine() is not compatible with Scanner",
                "nextInt() consumes the integer but leaves the newline in the buffer — nextLine() reads that leftover newline",
                "name must be declared before age",
                "System.in is closed after nextInt()",
                "b",
                "nextInt() reads the token '25' but stops before the newline character. "
                + "The subsequent nextLine() reads up to the leftover newline, returning an empty string. "
                + "Fix: add an extra sc.nextLine() after nextInt() to consume the newline before reading the name."),

            codegen("ios-cg-01", Topic.STANDARD_STREAMS, 2,
                "Which reads a full line of text from the user, handling the Scanner newline issue correctly?",
                "int ignored = sc.nextInt(); String line = sc.nextLine();",
                "String line = sc.nextLine();",
                "String line = sc.next();",
                "char line = (char) System.in.read();",
                "b",
                "When reading only Strings (not mixing nextInt()/nextDouble() first), sc.nextLine() works correctly. "
                + "It reads the entire line including spaces, stopping at the newline. "
                + "sc.next() only reads a single whitespace-delimited token, so it misses the rest of the line."),

            mc("ios-mc-05", Topic.STANDARD_STREAMS, 2,
                "What does 'System.out.flush()' do?",
                "Clears the console screen",
                "Forces any buffered output to be written to the underlying stream immediately",
                "Resets System.out to the default stream",
                "Closes System.out permanently",
                "b",
                "PrintStream buffers output in some environments. flush() forces the buffer to be written. "
                + "println() implicitly fllushs after each call, but print() may not. "
                + "This matters when output feeds into another process or when output is piped to a file."),

            mc("ios-mc-06", Topic.STANDARD_STREAMS, 2,
                "Which method reads a single character from System.in?",
                "System.in.readChar()",
                "System.in.read() — returns the byte value as an int, or -1 at end of stream",
                "Scanner.readChar()",
                "Console.readChar()",
                "b",
                "InputStream.read() returns the next byte as an int (0–255) or -1 at EOF. "
                + "For text, you must cast: char c = (char) System.in.read(). "
                + "Scanner.next().charAt(0) is more convenient for token-based single-character reading."),

            mc("ios-mc-07", Topic.STANDARD_STREAMS, 3,
                "How can you redirect System.out to write to a file programmatically?",
                "System.out = new FileOutputStream(\"log.txt\");",
                "System.setOut(new PrintStream(new FileOutputStream(\"log.txt\")));",
                "System.out.redirect(\"log.txt\");",
                "PrintStream.setDefault(new FileOutputStream(\"log.txt\"));",
                "b",
                "System.setOut(PrintStream) replaces the standard output stream. "
                + "Similarly System.setErr() replaces stderr and System.setIn() replaces stdin. "
                + "This is used in test frameworks and logging setups. "
                + "Remember to flush and close the stream when done."),

            trace("ios-tr-02", Topic.STANDARD_STREAMS, 2,
                "What is printed to System.out vs System.err?",
                "System.out.println(\"Result: 42\");\n"
                + "System.err.println(\"Warning: value is high\");",
                "Result: 42 (stdout)\nWarning: value is high (stderr)",
                "Both lines print to the terminal by default, but to separate streams. "
                + "In a shell: 'java App > out.txt' would only capture 'Result: 42'; the warning still appears on screen. "
                + "'java App > out.txt 2>&1' redirects both."),

            trace("ios-tr-03", Topic.STANDARD_STREAMS, 2,
                "What is printed?",
                "Scanner sc = new Scanner(System.in);\n"
                + "// User input: \"  hello world  \"\n"
                + "String token = sc.next();\n"
                + "System.out.println(token);",
                "hello",
                "sc.next() skips leading whitespace and reads up to the next whitespace delimiter. "
                + "It returns 'hello', not the full line. sc.nextLine() would return '  hello world  '."),

            debug("ios-db-02", Topic.STANDARD_STREAMS, 2,
                "This program hangs waiting for input even after all data is supposedly passed. Why?",
                "Scanner sc = new Scanner(System.in);\n"
                + "while (sc.hasNextLine()) {\n"
                + "    System.out.println(sc.nextLine());\n"
                + "}\n"
                + "// Run interactively from terminal — program never exits",
                "hasNextLine() does not work in a loop",
                "In a terminal, System.in has no EOF until the user signals it with Ctrl+D (Unix) or Ctrl+Z (Windows)",
                "println() blocks waiting for confirmation",
                "sc.nextLine() must be called inside a try-catch",
                "b",
                "hasNextLine() blocks until there is more input or EOF. "
                + "In an interactive terminal, EOF is only sent when the user presses Ctrl+D (Unix) or Ctrl+Z+Enter (Windows). "
                + "When reading from a file ('java App < data.txt'), EOF is detected at end of file and the loop terminates."),

            codegen("ios-cg-02", Topic.STANDARD_STREAMS, 2,
                "Which snippet safely reads an integer from stdin, recovering from non-integer input?",
                "int n = Integer.parseInt(System.in.read() + \"\");",
                "Scanner sc = new Scanner(System.in); int n = sc.nextInt();",
                "Scanner sc = new Scanner(System.in); while (!sc.hasNextInt()) sc.next(); int n = sc.nextInt();",
                "int n = (int) System.in.read();",
                "c",
                "Discarding non-integer tokens with sc.next() in a loop until sc.hasNextInt() is true "
                + "prevents InputMismatchException. Option B crashes on bad input. "
                + "Option A reads a raw byte (not a full number). Option D reads a single byte, not an int.")
        );
    }
}
