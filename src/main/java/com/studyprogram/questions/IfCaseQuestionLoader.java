package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class IfCaseQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.IF_CASE; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("if-mc-01", Topic.IF_CASE, 1,
                "Which keyword introduces the alternative branch in an if statement?",
                "otherwise", "else", "or", "default",
                "b",
                "Java uses 'else' for the alternative branch. "
                + "'default' is used in switch statements. 'otherwise' and 'or' are not Java keywords."),

            mc("if-mc-02", Topic.IF_CASE, 2,
                "What is the output?\n\n    int x = 5;\n    System.out.println(x > 3 ? \"big\" : \"small\");",
                "big", "small", "true", "x > 3",
                "a",
                "The ternary operator: condition ? valueIfTrue : valueIfFalse. "
                + "5 > 3 is true, so \"big\" is returned and printed."),

            mc("if-mc-03", Topic.IF_CASE, 2,
                "Which comparison operator tests for equality in Java?",
                "=", "==", "equals", "!=",
                "b",
                "== tests equality for primitives and object references. "
                + "= is assignment. != tests inequality. "
                + "For String content comparison use .equals(), not ==."),

            mc("if-mc-04", Topic.IF_CASE, 3,
                "A switch expression uses 'yield' to return a value. "
                + "What does the following print?\n\n"
                + "    int day = 2;\n"
                + "    String name = switch (day) {\n"
                + "        case 1 -> \"Monday\";\n"
                + "        case 2 -> \"Tuesday\";\n"
                + "        default -> \"Other\";\n"
                + "    };\n"
                + "    System.out.println(name);",
                "Monday", "Tuesday", "Other", "2",
                "b",
                "The switch expression matches day=2, returns \"Tuesday\", and assigns it to name. "
                + "Arrow syntax (->) does not fall through."),

            // ── Tracing ───────────────────────────────────────────────────────

            trace("if-tr-01", Topic.IF_CASE, 1,
                "What is printed?",
                "int x = 10;\nif (x > 5) {\n    System.out.println(\"big\");\n} else {\n    System.out.println(\"small\");\n}",
                "big",
                "x = 10 > 5 is true, so the if-block runs and prints \"big\"."),

            trace("if-tr-02", Topic.IF_CASE, 1,
                "What is printed?",
                "int n = 4;\nif (n % 2 == 0) {\n    System.out.println(\"even\");\n} else {\n    System.out.println(\"odd\");\n}",
                "even",
                "4 % 2 = 0, so the condition is true and \"even\" is printed."),

            trace("if-tr-03", Topic.IF_CASE, 2,
                "What is printed?",
                "int x = 5;\nif (x > 10) {\n    System.out.println(\"A\");\n} else if (x > 3) {\n    System.out.println(\"B\");\n} else {\n    System.out.println(\"C\");\n}",
                "B",
                "x=5 is not > 10 (skip first branch). x=5 is > 3 (enter second branch), print \"B\". "
                + "Once a branch is taken, the rest are skipped."),

            trace("if-tr-04", Topic.IF_CASE, 3,
                "What is printed?",
                "boolean a = true, b = false;\nif (a && !b) {\n    System.out.println(\"yes\");\n} else {\n    System.out.println(\"no\");\n}",
                "yes",
                "a = true, !b = !false = true. true && true = true. So \"yes\" is printed."),

            // ── Debugging ────────────────────────────────────────────────────

            debug("if-db-01", Topic.IF_CASE, 1,
                "What is wrong with this code?",
                "int x = 5;\nif x > 3 {\n    System.out.println(\"big\");\n}",
                "Missing braces around the body",
                "x > 3 is not a valid condition",
                "The condition must be in parentheses: if (x > 3)",
                "int cannot be compared with >",
                "c",
                "In Java, the condition of an if statement must be enclosed in parentheses. "
                + "The correct syntax is: if (x > 3) { ... }"),

            debug("if-db-02", Topic.IF_CASE, 2,
                "A student wants to check if x equals 5. What is the bug?",
                "int x = 5;\nif (x = 5) {\n    System.out.println(\"five\");\n}",
                "Should use == not =",
                "Should use .equals() for integers",
                "Missing else branch",
                "5 needs quotes because it's a number",
                "a",
                "x = 5 is an assignment, not a comparison. The condition would always be truthy "
                + "(non-zero int), and it also modifies x as a side effect. "
                + "Use == for equality: if (x == 5)."),

            // ── Code Generation ───────────────────────────────────────────────

            codegen("if-cg-01", Topic.IF_CASE, 1,
                "Which code prints \"pass\" if score >= 60, otherwise \"fail\"?",
                "if score >= 60 { println(\"pass\"); } else { println(\"fail\"); }",
                "if (score >= 60) { System.out.println(\"pass\"); } else { System.out.println(\"fail\"); }",
                "if (score >= 60) System.out.println(\"pass\"); System.out.println(\"fail\");",
                "if (score > 60) { System.out.println(\"pass\"); } else { System.out.println(\"fail\"); }",
                "b",
                "Option A is missing parentheses and uses wrong print syntax. "
                + "Option C always prints \"fail\" because the else is missing. "
                + "Option D uses > instead of >= so score == 60 would print \"fail\".",
                "Condition must be in ( ), and >= includes the boundary."),

            mc("if-mc-05", Topic.IF_CASE, 2,
                "What is the output?\n\n    int x = 0;\n    System.out.println(x == 0 ? \"zero\" : x > 0 ? \"positive\" : \"negative\");",
                "zero", "positive", "negative", "0",
                "a",
                "x == 0 is true, so the outer ternary short-circuits and returns \"zero\". "
                + "Nested ternaries are evaluated left to right."),

            mc("if-mc-06", Topic.IF_CASE, 2,
                "What is the difference between && and & for boolean expressions?",
                "They are identical",
                "&& short-circuits (stops evaluation if the first operand determines the result); & always evaluates both operands",
                "& is for bitwise AND on integers; && can only be used with booleans in conditions",
                "&&  evaluates right to left; & evaluates left to right",
                "b",
                "&& is the short-circuit AND: if the left side is false, the right side is not evaluated. "
                + "& always evaluates both sides (useful if the right side has a side effect, rare in practice). "
                + "Similarly: || is short-circuit OR; | always evaluates both."),

            mc("if-mc-07", Topic.IF_CASE, 2,
                "What is the output?\n\n"
                + "    int n = 15;\n"
                + "    if (n > 10 && n < 20) System.out.println(\"in range\");",
                "in range", "nothing is printed", "compile error", "false",
                "a",
                "n=15 is > 10 AND < 20, so the combined condition is true. \"in range\" is printed. "
                + "No braces needed for a single-statement body."),

            mc("if-mc-08", Topic.IF_CASE, 3,
                "In a traditional switch statement (not switch expression), what happens if break is omitted?",
                "The switch exits after the first matching case",
                "A compile error occurs",
                "Execution falls through to the next case",
                "The default case always runs",
                "c",
                "Traditional switch statements fall through from one case to the next if break is missing. "
                + "This is intentional in some patterns (e.g., cases sharing behavior) "
                + "but usually a bug. Switch expressions with -> arrows do not fall through."),

            trace("if-tr-05", Topic.IF_CASE, 2,
                "What is printed?",
                "int score = 85;\n"
                + "String grade;\n"
                + "if (score >= 90) grade = \"A\";\n"
                + "else if (score >= 80) grade = \"B\";\n"
                + "else if (score >= 70) grade = \"C\";\n"
                + "else grade = \"F\";\n"
                + "System.out.println(grade);",
                "B",
                "score=85 fails the first check (85 < 90) but passes the second (85 >= 80). grade = \"B\"."),

            trace("if-tr-06", Topic.IF_CASE, 2,
                "What is printed?",
                "int x = 5;\n"
                + "switch (x) {\n"
                + "    case 1: System.out.println(\"one\"); break;\n"
                + "    case 5: System.out.println(\"five\"); break;\n"
                + "    default: System.out.println(\"other\");\n"
                + "}",
                "five",
                "x=5 matches case 5. \"five\" is printed. break prevents fall-through. default is not reached."),

            trace("if-tr-07", Topic.IF_CASE, 3,
                "What is printed? (fall-through in switch)",
                "int x = 2;\n"
                + "switch (x) {\n"
                + "    case 1:\n"
                + "    case 2:\n"
                + "    case 3: System.out.println(\"low\"); break;\n"
                + "    default: System.out.println(\"high\");\n"
                + "}",
                "low",
                "x=2 matches case 2. There is no break, so it falls through to case 3 which prints \"low\" and breaks."),

            trace("if-tr-08", Topic.IF_CASE, 2,
                "What is printed?",
                "boolean flag = false;\n"
                + "if (!flag) {\n"
                + "    System.out.println(\"flag is false\");\n"
                + "} else {\n"
                + "    System.out.println(\"flag is true\");\n"
                + "}",
                "flag is false",
                "!flag = !false = true. The if-block runs and prints \"flag is false\"."),

            debug("if-db-03", Topic.IF_CASE, 2,
                "The code always prints 'not equal' even when x is 5. Why?",
                "String x = \"5\";\n"
                + "if (x == \"5\") {\n"
                + "    System.out.println(\"equal\");\n"
                + "} else {\n"
                + "    System.out.println(\"not equal\");\n"
                + "}",
                "\"5\" is not a valid String in Java",
                "== compares object references for Strings — two String objects may not share the same reference",
                "The if condition needs parentheses around the String",
                "String comparison requires a semicolon after the condition",
                "b",
                "For String content comparison, use equals(): if (x.equals(\"5\")). "
                + "== checks if both variables point to the same object in memory, which may or may not be true "
                + "depending on whether the JVM interns the string."),

            debug("if-db-04", Topic.IF_CASE, 2,
                "This code prints 'big' even when n=3. Why?",
                "int n = 3;\n"
                + "if (n > 5)\n"
                + "    System.out.println(\"big\");\n"
                + "    System.out.println(\"and something else\");",
                "The condition n > 5 evaluates incorrectly",
                "Without braces, only the first statement belongs to the if — the second always executes",
                "Two println calls cannot appear after an if without else",
                "n > 5 should be n >= 5",
                "b",
                "Without curly braces, only the immediately following statement is the if-body. "
                + "\"and something else\" always prints regardless of the condition. "
                + "Always use braces to avoid this common pitfall."),

            codegen("if-cg-02", Topic.IF_CASE, 2,
                "Which uses a switch expression to convert a month number (1–12) to its name for January and February?",
                "switch (month) { case 1: return \"January\"; case 2: return \"February\"; }",
                "String name = switch (month) { case 1 -> \"January\"; case 2 -> \"February\"; default -> \"Other\"; };",
                "if (month == 1 || month == 2) return month == 1 ? \"January\" : \"February\";",
                "name = month.equals(1) ? \"January\" : \"February\";",
                "b",
                "Switch expression with arrow syntax. Each case returns a value directly. default is required. "
                + "Option A uses return inside a switch (only valid inside a method returning String). "
                + "Option D: month is int, not an object, so .equals() won't compile."),

            codegen("if-cg-03", Topic.IF_CASE, 2,
                "Which correctly checks that a number is between 1 and 100 inclusive?",
                "if (1 < n < 100)",
                "if (n > 1 && n < 100)",
                "if (n >= 1 && n <= 100)",
                "if (n.between(1, 100))",
                "c",
                "Option A is invalid Java syntax (chained comparisons don't work like that). "
                + "Option B excludes both 1 and 100 (uses > and <). "
                + "Option C correctly uses >= and <= to include the boundaries. "
                + "Option D: int has no between() method.")
        );
    }
}
