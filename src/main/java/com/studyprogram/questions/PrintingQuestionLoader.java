package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class PrintingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.PRINTING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("prn-mc-01", Topic.PRINTING, 1,
                "Which method prints output and moves the cursor to the next line?",
                "System.out.print()",
                "System.out.println()",
                "System.out.write()",
                "Console.print()",
                "b",
                "println (print-line) appends a newline after the output. "
                + "print() leaves the cursor on the same line. "
                + "System.out.write() works with byte streams. "
                + "Console.print() is not a standard Java method."),

            mc("prn-mc-02", Topic.PRINTING, 1,
                "What is the output of this code?\n\n    System.out.print(\"A\");\n    System.out.print(\"B\");",
                "A\nB", "AB", "A B", "A\nB\n",
                "b",
                "print() does not add a newline. Both A and B are printed on the same line with no space between them."),

            mc("prn-mc-03", Topic.PRINTING, 2,
                "What does this line print?\n\n    System.out.println(\"2 + 3\");",
                "5", "2 + 3", "\"2 + 3\"", "2+3",
                "b",
                "The argument is a String literal (in quotes). "
                + "Java prints the string exactly as written. "
                + "To evaluate the expression, omit the quotes: System.out.println(2 + 3);"),

            mc("prn-mc-04", Topic.PRINTING, 2,
                "What does this print?\n\n    System.out.printf(\"%.2f%n\", 3.14159);",
                "3.14159", "3.14", "3.1", "%.2f",
                "b",
                "%.2f formats a floating-point number to 2 decimal places. "
                + "%n is a platform-neutral newline. "
                + "So 3.14159 is rounded to 3.14."),

            // ── Tracing ───────────────────────────────────────────────────────

            trace("prn-tr-01", Topic.PRINTING, 1,
                "What is the exact output of this code? (include newlines as separate lines)",
                "System.out.println(\"Hello\");\nSystem.out.println(\"World\");",
                "hello\nworld",
                "println prints the string then moves to the next line. "
                + "Each call produces one line of output.",
                "Hello\nWorld"),

            trace("prn-tr-02", Topic.PRINTING, 1,
                "What is the output?",
                "System.out.println(2 + 3);",
                "5",
                "Here 2 + 3 is an arithmetic expression (no quotes), so Java evaluates it to 5, "
                + "then prints the integer 5."),

            trace("prn-tr-03", Topic.PRINTING, 2,
                "What is the output?",
                "String name = \"Alice\";\nSystem.out.println(\"Hello, \" + name + \"!\");",
                "Hello, Alice!",
                "The + operator between strings performs concatenation. "
                + "\"Hello, \" + \"Alice\" + \"!\" → \"Hello, Alice!\""),

            trace("prn-tr-04", Topic.PRINTING, 2,
                "What is the output?",
                "int x = 5;\nSystem.out.println(\"x = \" + x);",
                "x = 5",
                "When a String is concatenated with an int using +, Java automatically converts "
                + "the int to its String representation. \"x = \" + 5 → \"x = 5\"."),

            trace("prn-tr-05", Topic.PRINTING, 3,
                "What is the output?",
                "System.out.println(1 + 2 + \" cats\");\nSystem.out.println(\"cats \" + 1 + 2);",
                "3 cats\ncats 12",
                "Java evaluates left to right. In the first line: 1+2=3, then 3+\" cats\"=\"3 cats\". "
                + "In the second line: \"cats \"+1=\"cats 1\", then \"cats 1\"+2=\"cats 12\". "
                + "Order matters with mixed + and string concatenation."),

            // ── Debugging ────────────────────────────────────────────────────

            debug("prn-db-01", Topic.PRINTING, 1,
                "What is wrong with this code?",
                "system.out.println(\"Hello\");",
                "Missing semicolon",
                "system should be System (capital S)",
                "println should be PrintLn",
                "\"Hello\" needs to be a char not a String",
                "b",
                "Java is case-sensitive. The class is System (capital S) in java.lang. "
                + "system (lowercase) is not a valid identifier here."),

            debug("prn-db-02", Topic.PRINTING, 1,
                "What is wrong?",
                "System.out.println(\"Hello\")",
                "Quotes are wrong type",
                "println is misspelled",
                "Missing semicolon at end of statement",
                "System should be lowercase",
                "c",
                "Every Java statement must end with a semicolon. "
                + "Missing the ; will cause a compile error."),

            debug("prn-db-03", Topic.PRINTING, 2,
                "A student wants to print the value of x on its own line. What is wrong?",
                "int x = 42;\nSystem.out.println(\"x\");",
                "int cannot be printed",
                "\"x\" prints the letter x, not the value of the variable x",
                "println needs two arguments",
                "Nothing is wrong",
                "b",
                "Putting x in quotes makes it a String literal. "
                + "To print the variable's value, write: System.out.println(x); (no quotes)."),

            // ── Code Generation ───────────────────────────────────────────────

            codegen("prn-cg-01", Topic.PRINTING, 1,
                "Which line prints exactly: Hello, World!",
                "System.out.println(Hello, World!);",
                "System.out.print(\"Hello, World!\");",
                "System.out.println(\"Hello, World!\");",
                "console.log(\"Hello, World!\");",
                "c",
                "String literals must be enclosed in double quotes. "
                + "println adds a newline at the end. "
                + "console.log is JavaScript syntax, not Java.",
                "String values always go in double quotes in Java."),

            mc("prn-mc-05", Topic.PRINTING, 2,
                "What does System.out.printf(\"%d items at $%.2f each\", 3, 1.5) print?",
                "3 items at $1.50 each",
                "%d items at $%.2f each",
                "3 items at $1.5 each",
                "3 items at $%.2f each",
                "a",
                "%d formats an integer argument (3). %.2f formats a float to 2 decimal places (1.50). "
                + "Arguments are substituted left to right for each format specifier."),

            mc("prn-mc-06", Topic.PRINTING, 2,
                "What is the output of: System.out.println(\"10\" + 5 + 3)?",
                "18", "1053", "105 3", "1053",
                "b",
                "String concatenation is left-to-right: \"10\" + 5 = \"105\", then \"105\" + 3 = \"1053\". "
                + "Once one operand is a String, all subsequent + become concatenation, not addition."),

            mc("prn-mc-07", Topic.PRINTING, 2,
                "What does the escape sequence \\t do in a String?",
                "Ends the string", "Inserts a tab character", "Inserts a newline", "Escapes the next character",
                "b",
                "Escape sequences in Java strings: \\n = newline, \\t = tab, \\\\ = backslash, \\\" = double-quote. "
                + "System.out.println(\"A\\tB\") prints: A    B (with a tab between A and B)."),

            mc("prn-mc-08", Topic.PRINTING, 3,
                "What does String.format() return compared to printf()?",
                "They are identical — both print to stdout",
                "String.format() returns a formatted String; printf() prints to stdout",
                "printf() returns a String; String.format() prints",
                "String.format() only works with integers",
                "b",
                "String.format(\"%.2f\", 3.14) returns the formatted string \"3.14\" — it doesn't print. "
                + "Use it to build strings for labels, files, or messages. "
                + "printf(format, args) formats and prints to stdout in one step."),

            trace("prn-tr-06", Topic.PRINTING, 2,
                "What is printed?",
                "System.out.printf(\"%05d%n\", 42);",
                "00042",
                "%05d: format as integer with width 5, padded with zeros on the left. "
                + "42 takes 2 digits, so 3 zeros are prepended: 00042."),

            trace("prn-tr-07", Topic.PRINTING, 2,
                "What is printed?",
                "System.out.print(\"Line1\\n\");\n"
                + "System.out.print(\"Line2\");",
                "Line1\nLine2",
                "\\n in a string literal is a newline character. print() does not add its own newline. "
                + "Output: 'Line1' on one line, 'Line2' on the next (on the same line as the cursor after \\n)."),

            trace("prn-tr-08", Topic.PRINTING, 3,
                "What is printed?",
                "int x = 5;\n"
                + "System.out.println(\"Value: \" + x * 2);",
                "Value: 10",
                "x * 2 is evaluated first (multiplication before string concatenation). "
                + "x * 2 = 10. \"Value: \" + 10 = \"Value: 10\"."),

            debug("prn-db-04", Topic.PRINTING, 2,
                "The output shows quotes around the name instead of just the name. Why?",
                "String name = \"Alice\";\n"
                + "System.out.println(\"\\\"\" + name + \"\\\"\");",
                "println does not support String variables",
                "The code is intentionally adding escaped quotes — \\\" prints a literal double-quote character",
                "name is being treated as a char not a String",
                "The + operator inserts quotes automatically",
                "b",
                "\\\" is the escape sequence for a literal double-quote character inside a String. "
                + "So this prints: \"Alice\" — the name surrounded by actual quote marks. "
                + "The code is correct if that's the intent."),

            debug("prn-db-05", Topic.PRINTING, 2,
                "The printf format string produces wrong output. Why?",
                "double price = 9.99;\n"
                + "System.out.printf(\"Price: %d%n\", price);",
                "price is too large for %d",
                "%d is for integers — using it with a double throws IllegalFormatConversionException",
                "printf does not support double values",
                "The %n specifier conflicts with %d",
                "b",
                "%d expects an integer (int, long, etc.). Passing a double causes IllegalFormatConversionException. "
                + "Use %f for doubles: System.out.printf(\"Price: %.2f%n\", price); → Price: 9.99"),

            codegen("prn-cg-02", Topic.PRINTING, 2,
                "Which prints a person's name and age on one line, formatted as 'Alice is 30 years old.'?",
                "System.out.println(name + \"is\" + age + \"years old.\");",
                "System.out.printf(\"%s is %d years old.%n\", name, age);",
                "System.out.print(name, age);",
                "System.out.format(name + age);",
                "b",
                "%s substitutes the String (name), %d substitutes the int (age), %n adds a newline. "
                + "Option A is missing spaces around 'is'. "
                + "Option C: println/print take one argument. "
                + "Option D: format needs a format string with specifiers."),

            codegen("prn-cg-03", Topic.PRINTING, 2,
                "Which builds a formatted string (not printing) from a name and score?",
                "System.out.printf(\"%s: %d\", name, score);",
                "String result = String.format(\"%s: %d\", name, score);",
                "String result = name + \": \" + score;",
                "String result = printf(\"%s: %d\", name, score);",
                "b",
                "String.format() returns the formatted String without printing. "
                + "Option A prints to stdout (doesn't return). "
                + "Option C works but loses the type-safe formatting benefits. "
                + "Option D: printf is not a standalone method."),

            codegen("prn-cg-04", Topic.PRINTING, 3,
                "Which prints a table row with name left-aligned in a 15-char field and score right-aligned in 5 chars?",
                "System.out.printf(\"%s %d%n\", name, score);",
                "System.out.printf(\"%-15s%5d%n\", name, score);",
                "System.out.printf(\"%15s%5d%n\", name, score);",
                "System.out.printf(\"%s%d%n\", name, score);",
                "b",
                "%-15s: left-align (-) in 15-character field. %5d: right-align integer in 5-char field. "
                + "Option C uses %15s which right-aligns the name. "
                + "For table formatting, left-align text and right-align numbers.")
        );
    }
}
