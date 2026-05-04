package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class StringsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.STRINGS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("str-mc-01", Topic.STRINGS, 1,
                "Which method returns the number of characters in a String?",
                "size()", "count()", "length()", "len()",
                "c",
                "String.length() returns the number of characters. "
                + "Arrays use .length (no parentheses). ArrayList uses .size(). "
                + "len() is Python syntax."),

            mc("str-mc-02", Topic.STRINGS, 2,
                "What does this print?\n\n    String s = \"Hello\";\n    System.out.println(s.substring(1, 3));",
                "Hel", "el", "ell", "He",
                "b",
                "substring(start, end) returns characters from index 'start' up to but not including 'end'. "
                + "Index 1 = 'e', index 2 = 'l'. Result: \"el\"."),

            mc("str-mc-03", Topic.STRINGS, 2,
                "Why should you use .equals() instead of == to compare String content?",
                "== is slower for Strings",
                "== compares object references, not character content",
                "equals() is case-insensitive",
                ".equals() works on any type, == only works on int",
                "b",
                "== checks if two references point to the same object in memory. "
                + "Two separate String objects with the same characters will fail ==. "
                + "Use .equals() for content comparison; .equalsIgnoreCase() to ignore case."),

            trace("str-tr-01", Topic.STRINGS, 2,
                "What is the output?",
                "String s = \"Java\";\nSystem.out.println(s.toUpperCase() + s.length());",
                "JAVA4",
                "toUpperCase() returns \"JAVA\". length() returns 4. "
                + "String concatenation: \"JAVA\" + 4 = \"JAVA4\"."),

            trace("str-tr-02", Topic.STRINGS, 2,
                "What is the output?",
                "String s = \"  Hello  \";\nSystem.out.println(s.trim().length());",
                "5",
                "trim() removes leading and trailing whitespace: \"  Hello  \" → \"Hello\". "
                + "\"Hello\".length() = 5."),

            debug("str-db-01", Topic.STRINGS, 2,
                "A student checks if two strings are equal but the result is always false. What's wrong?",
                "String a = \"hello\";\nString b = \"hello\";\nif (a == b) System.out.println(\"equal\");",
                "Strings cannot be compared with if",
                "Should use a.equals(b) instead of a == b",
                "\"hello\" is not a valid String literal",
                "The condition needs parentheses around a == b",
                "b",
                "== on String objects checks reference equality (same object in memory), not content. "
                + "a and b are different objects even though they contain the same text. "
                + "Use a.equals(b) or a.equalsIgnoreCase(b)."),

            codegen("str-cg-01", Topic.STRINGS, 2,
                "Which code builds the string \"Hello, Alice\" using a variable?",
                "String name = \"Alice\"; String msg = \"Hello, \" + name;",
                "String name = \"Alice\"; String msg = \"Hello, Alice\";",
                "String name = Alice; String msg = \"Hello, \" + name;",
                "String name = \"Alice\"; String msg = concat(\"Hello, \", name);",
                "a",
                "Option A correctly uses string concatenation with +. "
                + "Option B hardcodes the result. Option C is missing quotes around Alice. "
                + "Option D invents a concat() function that doesn't exist in the same form."),

            mc("str-mc-04", Topic.STRINGS, 2,
                "What does s.charAt(0) return for String s = \"Java\"?",
                "'J'", "'j'", "\"J\"", "0",
                "a",
                "charAt(index) returns the char at that position. Index 0 is the first character. "
                + "'J' is a char literal (single quotes). \"J\" would be a one-character String, not a char."),

            mc("str-mc-05", Topic.STRINGS, 2,
                "What does s.indexOf('z') return if 'z' is not in the string?",
                "0", "-1", "null", "Throws StringIndexOutOfBoundsException",
                "b",
                "indexOf() returns -1 when the character or substring is not found. "
                + "This is a sentinel value — always check for -1 before using the result: "
                + "int i = s.indexOf('z'); if (i != -1) { ... }"),

            trace("str-tr-03", Topic.STRINGS, 2,
                "What is printed?",
                "String s = \"Hello World\";\n"
                + "System.out.println(s.replace(\"World\", \"Java\").toLowerCase());",
                "hello java",
                "replace(\"World\",\"Java\") → \"Hello Java\". toLowerCase() → \"hello java\"."),

            debug("str-db-02", Topic.STRINGS, 3,
                "The contains check always returns false. Why?",
                "String haystack = \"Hello World\";\n"
                + "String needle = \"world\";  // lowercase w\n"
                + "System.out.println(haystack.contains(needle));",
                "contains() only works on single characters",
                "String comparison in Java is case-sensitive — 'World' and 'world' do not match",
                "needle must be a char, not a String",
                "contains() requires the exact index",
                "b",
                "contains() (and all String searches) are case-sensitive. "
                + "'World' != 'world'. "
                + "Fix: haystack.toLowerCase().contains(needle.toLowerCase()) for a case-insensitive check."),

            mc("str-mc-06", Topic.STRINGS, 2,
                "What does String.split(\",\") return for \"a,b,c\"?",
                "A String with commas removed",
                "An array: {\"a\", \"b\", \"c\"}",
                "A List<String> containing a, b, c",
                "The index of the first comma",
                "b",
                "split(regex) splits the string around the pattern and returns a String[]. "
                + "\"a,b,c\".split(\",\") → {\"a\", \"b\", \"c\"} with length 3. "
                + "To split on a dot: split(\"\\\\.\") — the dot is a regex metacharacter and must be escaped."),

            mc("str-mc-07", Topic.STRINGS, 2,
                "What does String.join(\"-\", \"a\", \"b\", \"c\") return?",
                "\"a b c\"", "\"abc\"", "\"a-b-c\"", "\"a, b, c\"",
                "c",
                "String.join(delimiter, elements...) joins the elements with the delimiter between them. "
                + "String.join(\"-\", \"a\", \"b\", \"c\") → \"a-b-c\". "
                + "Also works with a List: String.join(\", \", list)."),

            mc("str-mc-08", Topic.STRINGS, 2,
                "What is the result of \"Hello\".startsWith(\"He\")?",
                "true", "false", "1", "He",
                "a",
                "startsWith(prefix) returns true if the string begins with the given prefix. "
                + "endsWith(suffix) checks the end. "
                + "These are case-sensitive checks."),

            mc("str-mc-09", Topic.STRINGS, 3,
                "Why is StringBuilder preferred over String concatenation in a loop?",
                "StringBuilder is type-safe; String is not",
                "Strings are immutable — each + creates a new object; StringBuilder mutates in place, avoiding many allocations",
                "StringBuilder is guaranteed to be thread-safe",
                "String concatenation with + is not allowed in loops",
                "b",
                "String s = \"\"; for (...) s += item; — each += creates a new String object. In a large loop this is O(n²). "
                + "StringBuilder sb = new StringBuilder(); for (...) sb.append(item); is O(n). "
                + "Use sb.toString() when done."),

            mc("str-mc-10", Topic.STRINGS, 2,
                "What does Integer.parseInt(\"42\") return?",
                "The String \"42\"",
                "The int 42",
                "A Character array",
                "null",
                "b",
                "Integer.parseInt(String) converts a numeric string to an int. "
                + "Throws NumberFormatException if the string is not a valid integer. "
                + "Integer.valueOf(\"42\") returns an Integer object instead of a primitive int."),

            trace("str-tr-04", Topic.STRINGS, 2,
                "What is printed?",
                "String s = \"programming\";\n"
                + "System.out.println(s.substring(0, 4).toUpperCase());",
                "PROG",
                "substring(0,4) extracts characters at indices 0,1,2,3: \"prog\". "
                + "toUpperCase() → \"PROG\"."),

            trace("str-tr-05", Topic.STRINGS, 2,
                "What is printed?",
                "String[] parts = \"one,two,three\".split(\",\");\n"
                + "System.out.println(parts.length + \" \" + parts[1]);",
                "3 two",
                "split(\",\") returns [\"one\", \"two\", \"three\"]. length=3. parts[1]=\"two\"."),

            trace("str-tr-06", Topic.STRINGS, 3,
                "What is printed?",
                "StringBuilder sb = new StringBuilder(\"abc\");\n"
                + "sb.reverse();\n"
                + "System.out.println(sb);",
                "cba",
                "StringBuilder.reverse() reverses the character sequence in place. \"abc\" becomes \"cba\". "
                + "StringBuilder also has append(), insert(), delete(), and replace() methods."),

            debug("str-db-03", Topic.STRINGS, 2,
                "A NullPointerException is thrown on the contains() call. Why?",
                "String input = null;\n"
                + "if (input.contains(\"hello\")) {\n"
                + "    System.out.println(\"found\");\n"
                + "}",
                "contains() does not accept String arguments",
                "input is null — calling any method on null throws NullPointerException",
                "null cannot be compared with Strings",
                "contains() requires the argument to be non-null",
                "b",
                "Calling any method on a null reference throws NullPointerException. "
                + "Guard: if (input != null && input.contains(\"hello\")) { ... } "
                + "Or use Objects.toString(input, \"\").contains(\"hello\") for a null-safe version."),

            debug("str-db-04", Topic.STRINGS, 2,
                "The string replacement doesn't work as expected. Why?",
                "String s = \"Hello\";\n"
                + "s.replace('l', 'L');\n"
                + "System.out.println(s);",
                "replace() uses the wrong method overload",
                "Strings are immutable — replace() returns a new String; the original s is unchanged",
                "char literals cannot be used with replace()",
                "s must be declared as StringBuilder",
                "b",
                "Strings are immutable. replace() returns a NEW String with the replacements. "
                + "Fix: s = s.replace('l', 'L'); — reassign s to the new value."),

            codegen("str-cg-02", Topic.STRINGS, 2,
                "Which converts an int to String, then checks if it starts with '4'?",
                "int n = 42; if (n.startsWith(\"4\")) {}",
                "int n = 42; String s = String.valueOf(n); System.out.println(s.startsWith(\"4\"));",
                "int n = 42; if (Integer.toString(n).charAt(0) == 4) {}",
                "int n = 42; if (n + \"\".startsWith(\"4\")) {}",
                "b",
                "String.valueOf(n) converts int to String. startsWith(\"4\") checks the prefix. "
                + "Option A: int has no startsWith() method. "
                + "Option C: charAt(0) returns '4' (char), not 4 (int) — should compare to '4'. "
                + "Option D: operator precedence makes \"\" the receiver for startsWith, not n+\"\".")
        );
    }
}
