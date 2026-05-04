package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class FileProcessingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.FILE_PROCESSING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("fp-mc-01", Topic.FILE_PROCESSING, 2,
                "Which class reads a text file line by line efficiently?",
                "FileReader", "BufferedReader", "FileInputStream", "Scanner",
                "b",
                "BufferedReader wraps a FileReader and buffers input, making readLine() efficient. "
                + "FileReader reads character by character. FileInputStream reads bytes. "
                + "Scanner works but is slower for large files."),

            mc("fp-mc-02", Topic.FILE_PROCESSING, 2,
                "What exception must you handle (or declare) when working with File I/O?",
                "RuntimeException", "NullPointerException",
                "IOException", "FileException",
                "c",
                "IOException is a checked exception thrown by most File I/O operations. "
                + "You must either catch it with try-catch or declare it in the method signature with 'throws IOException'. "
                + "FileException does not exist in Java."),

            mc("fp-mc-03", Topic.FILE_PROCESSING, 2,
                "What does try-with-resources ensure?",
                "The resource is created lazily",
                "The resource is automatically closed when the try block exits",
                "Exceptions inside the block are suppressed",
                "The resource is shared across threads",
                "b",
                "try-with-resources (Java 7+) calls close() on any AutoCloseable resource "
                + "when the block ends, even if an exception is thrown. "
                + "This prevents resource leaks without a finally block."),

            mc("fp-mc-04", Topic.FILE_PROCESSING, 3,
                "Which modern API reads all lines of a text file into a List<String>?",
                "File.readLines(path)",
                "Files.readAllLines(path)",
                "BufferedReader.readAllLines(path)",
                "FileReader.lines(path)",
                "b",
                "java.nio.file.Files.readAllLines(Path) returns all lines as List<String>. "
                + "The other options are either non-existent or incorrect. "
                + "For large files, prefer Files.lines(path) which returns a Stream and doesn't load everything at once."),

            trace("fp-tr-01", Topic.FILE_PROCESSING, 3,
                "What does this code print if data.txt contains exactly the line 'Hello'?",
                "try (BufferedReader br = new BufferedReader(new FileReader(\"data.txt\"))) {\n"
                + "    String line = br.readLine();\n"
                + "    System.out.println(line);\n"
                + "} catch (IOException e) {\n"
                + "    System.out.println(\"error\");\n"
                + "}",
                "Hello",
                "readLine() reads the first line without the line terminator. "
                + "The try-with-resources block closes the reader automatically when done."),

            debug("fp-db-01", Topic.FILE_PROCESSING, 2,
                "This code compiles but leaks the file handle. Why?",
                "FileReader fr = new FileReader(\"notes.txt\");\n"
                + "BufferedReader br = new BufferedReader(fr);\n"
                + "System.out.println(br.readLine());",
                "FileReader cannot be wrapped in BufferedReader",
                "readLine() throws IOException which must be caught",
                "The reader is never closed — the file handle leaks if an exception occurs",
                "BufferedReader requires an explicit buffer size",
                "c",
                "If readLine() throws, the reader is never closed. "
                + "Fix: use try-with-resources: 'try (BufferedReader br = new BufferedReader(new FileReader(...))) { ... }'"),

            codegen("fp-cg-01", Topic.FILE_PROCESSING, 3,
                "Which code correctly writes a String to a file using modern Java?",
                "new File(\"out.txt\").write(\"hello\");",
                "Files.writeString(Path.of(\"out.txt\"), \"hello\");",
                "FileWriter fw = new FileWriter(\"out.txt\"); fw.write(\"hello\");",
                "PrintStream.out.write(\"out.txt\", \"hello\");",
                "b",
                "Files.writeString(Path, String) (Java 11+) writes a string to a file in one call, handling close automatically. "
                + "Option A: File has no write() method. "
                + "Option C works but forgets to close the FileWriter. "
                + "Option D is not valid API."),

            mc("fp-mc-05", Topic.FILE_PROCESSING, 2,
                "What does Files.readAllLines(Path.of(\"data.txt\")) return?",
                "A single String with all file content",
                "A List<String> where each element is one line",
                "A byte[] of the raw file bytes",
                "A Scanner ready to read the file",
                "b",
                "Files.readAllLines() reads the entire file and returns each line as a String in a List. "
                + "Convenient for small files. For large files, use Files.lines() which returns a lazy Stream<String> "
                + "that reads line by line without loading everything into memory at once."),

            trace("fp-tr-02", Topic.FILE_PROCESSING, 3,
                "What is the result of running this code on a file containing exactly two lines: 'hello' and 'world'?",
                "List<String> lines = Files.readAllLines(Path.of(\"file.txt\"));\n"
                + "System.out.println(lines.size() + \": \" + lines.get(1));",
                "2: world",
                "The file has 2 lines. lines.size()=2. lines.get(1) = \"world\" (0-indexed, second line)."),

            debug("fp-db-02", Topic.FILE_PROCESSING, 3,
                "The file is written but appears empty. Why?",
                "FileWriter fw = new FileWriter(\"output.txt\");\n"
                + "fw.write(\"Hello\");\n"
                + "// fw.close() is never called",
                "FileWriter cannot write Strings",
                "FileWriter buffers output and flushes only when close() (or flush()) is called",
                "write() requires a byte array, not a String",
                "output.txt must exist before writing",
                "b",
                "FileWriter (and most Writers) buffer writes for efficiency. "
                + "The buffer is flushed to disk only when flush() or close() is called. "
                + "If close() is never called, the buffered data is lost when the program exits. "
                + "Use try-with-resources to guarantee close(): try (FileWriter fw = new FileWriter(...)) { ... }"),

            codegen("fp-cg-02", Topic.FILE_PROCESSING, 3,
                "Which reads all lines from 'data.csv' and prints each one?",
                "File f = new File(\"data.csv\"); System.out.println(f.read());",
                "try (Stream<String> lines = Files.lines(Path.of(\"data.csv\"))) { lines.forEach(System.out::println); }",
                "Files.readAllLines(\"data.csv\").print();",
                "Scanner sc = new Scanner(\"data.csv\"); while (sc.hasNext()) System.out.println(sc.next());",
                "b",
                "Files.lines() returns a lazy Stream<String>. forEach prints each line. "
                + "Wrapping in try-with-resources closes the underlying file stream. "
                + "Option C: readAllLines requires a Path, not a String; and List has no print() method. "
                + "Option D: new Scanner(String) treats the string as data, not a file path."),

            mc("fp-mc-06", Topic.FILE_PROCESSING, 2,
                "Which class is used to write text to a file, creating it if it doesn't exist?",
                "FileReader", "BufferedReader", "FileWriter", "FileInputStream",
                "c",
                "FileWriter writes characters to a file. new FileWriter(\"f.txt\") creates/overwrites. "
                + "new FileWriter(\"f.txt\", true) appends. "
                + "Wrap in BufferedWriter for efficiency: new BufferedWriter(new FileWriter(\"f.txt\"))."),

            mc("fp-mc-07", Topic.FILE_PROCESSING, 2,
                "What does Files.exists(Path.of(\"data.txt\")) return if the file doesn't exist?",
                "null", "throws FileNotFoundException", "false", "0",
                "c",
                "Files.exists(Path) returns false if the path doesn't exist (or if there's an I/O error). "
                + "It doesn't throw — it's a safe check. "
                + "Use it before opening files: if (Files.exists(path)) { ... }"),

            mc("fp-mc-08", Topic.FILE_PROCESSING, 3,
                "What is the difference between FileInputStream and FileReader?",
                "FileInputStream reads bytes; FileReader reads characters (handles encoding)",
                "FileInputStream is faster; FileReader is safer",
                "FileReader reads binary data; FileInputStream reads text",
                "They are identical — just different names for the same class",
                "a",
                "FileInputStream reads raw bytes — best for binary data (images, serialized objects). "
                + "FileReader reads characters — best for text files (handles charset encoding). "
                + "Wrap both in buffered variants for efficiency."),

            trace("fp-tr-03", Topic.FILE_PROCESSING, 2,
                "What does this code do if output.txt already exists?",
                "Files.writeString(Path.of(\"output.txt\"), \"Hello\");",
                "Overwrites the file with 'Hello'",
                "Files.writeString with no options uses TRUNCATE_EXISTING and CREATE by default. "
                + "The existing file is replaced. To append: Files.writeString(path, text, StandardOpenOption.APPEND)."),

            trace("fp-tr-04", Topic.FILE_PROCESSING, 3,
                "What is printed if notes.txt contains: line1\\nline2\\nline3?",
                "long count = Files.lines(Path.of(\"notes.txt\")).count();\n"
                + "System.out.println(count);",
                "3",
                "Files.lines() returns a Stream of each line. count() terminal operation returns the number of elements: 3."),

            debug("fp-db-03", Topic.FILE_PROCESSING, 2,
                "This throws FileNotFoundException. Why?",
                "BufferedReader br = new BufferedReader(new FileReader(\"missing.txt\"));\n"
                + "System.out.println(br.readLine());",
                "BufferedReader cannot wrap FileReader",
                "FileReader throws FileNotFoundException if the file doesn't exist",
                "readLine() requires the file to be opened in read mode first",
                "missing.txt must be on the classpath",
                "b",
                "FileReader(String filename) throws FileNotFoundException (a checked exception) "
                + "if the file doesn't exist or can't be opened. "
                + "This is a checked exception — it must be caught or declared in the method signature."),

            debug("fp-db-04", Topic.FILE_PROCESSING, 3,
                "Using Files.readAllLines() crashes on a large log file. Why?",
                "List<String> lines = Files.readAllLines(Path.of(\"/var/log/system.log\"));",
                "Files.readAllLines() does not support large files",
                "Files.readAllLines() loads the entire file into memory as a List — large files cause OutOfMemoryError",
                "readAllLines() requires the file to be UTF-8 encoded",
                "The method is deprecated for files > 1MB",
                "b",
                "readAllLines() reads the entire file into a List<String> in memory. "
                + "A 10 GB log file would require 10+ GB of heap. "
                + "Fix: use Files.lines(path) which returns a lazy Stream<String>, processing one line at a time."),

            codegen("fp-cg-03", Topic.FILE_PROCESSING, 3,
                "Which appends a line to an existing file without overwriting it?",
                "Files.writeString(Path.of(\"log.txt\"), \"new line\");",
                "Files.writeString(Path.of(\"log.txt\"), \"new line\\n\", StandardOpenOption.APPEND);",
                "new FileWriter(\"log.txt\").write(\"new line\");",
                "Files.appendString(Path.of(\"log.txt\"), \"new line\");",
                "b",
                "StandardOpenOption.APPEND opens the file in append mode. "
                + "Option A overwrites the file. "
                + "Option C also overwrites (no append flag) and leaks the FileWriter. "
                + "Option D: appendString() does not exist in the standard Java API.")
        );
    }
}
