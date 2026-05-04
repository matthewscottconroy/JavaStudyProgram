package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class VariablesQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.VARIABLES; }

    @Override
    public List<Question> load() {
        return List.of(

            // ── Multiple Choice ───────────────────────────────────────────────

            mc("var-mc-01", Topic.VARIABLES, 1,
                "Which of the following is a valid Java variable declaration?",
                "int myAge = 25;",
                "integer myAge = 25;",
                "Int myAge = 25;",
                "int my age = 25;",
                "a",
                "Java primitive types are lowercase: int, double, boolean, char, etc. "
                + "'integer' and 'Int' are not valid types (Integer is a class). "
                + "Variable names cannot contain spaces.",
                "Java type names for primitives are all lowercase."),

            mc("var-mc-02", Topic.VARIABLES, 1,
                "What is the default value of an int variable declared as a class field?",
                "null", "0", "1", "undefined",
                "b",
                "Class-level (instance and static) int fields default to 0. "
                + "Local variables inside a method have no default—using them before "
                + "assignment causes a compile error.",
                "Think about what 'nothing' looks like for a number."),

            mc("var-mc-03", Topic.VARIABLES, 1,
                "Which of the following is a valid Java variable name?",
                "2ndValue", "_myVar", "my-var", "class",
                "b",
                "_myVar is valid. Variable names must start with a letter, underscore, or dollar sign. "
                + "They cannot start with a digit (2ndValue), contain hyphens (my-var), "
                + "or use reserved keywords (class).",
                "Reserved words and operators cannot be used as identifiers."),

            mc("var-mc-04", Topic.VARIABLES, 1,
                "What type would you use to store the value 3.14?",
                "int", "boolean", "double", "String",
                "c",
                "double (and float) hold decimal numbers. int holds whole numbers only. "
                + "boolean holds true/false. String holds text.",
                "Think: 3.14 has a decimal point—which type handles that?"),

            mc("var-mc-05", Topic.VARIABLES, 2,
                "After the following code runs, what is the type and value of x?\n\n    var x = 42;",
                "String, \"42\"",
                "int, 42",
                "double, 42.0",
                "var, 42",
                "b",
                "Java's 'var' keyword (Java 10+) uses type inference. The literal 42 is an int "
                + "literal, so x is inferred as int with value 42. 'var' is not itself a type."),

            mc("var-mc-06", Topic.VARIABLES, 2,
                "Which statement correctly swaps the values of two int variables a and b?",
                "a = b; b = a;",
                "int temp = a; a = b; b = temp;",
                "a <-> b;",
                "swap(a, b);",
                "b",
                "You need a temporary variable. 'a = b; b = a;' overwrites a's original value "
                + "before it's saved, so b ends up with b's original value again."),

            // ── Tracing ───────────────────────────────────────────────────────

            trace("var-tr-01", Topic.VARIABLES, 1,
                "What is the value of z after this code runs?",
                "int x = 5;\nint y = 3;\nint z = x + y;",
                "8",
                "z = 5 + 3 = 8. The + operator adds the values stored in x and y."),

            trace("var-tr-02", Topic.VARIABLES, 1,
                "What is the value of x after this code runs?",
                "int x = 10;\nx = x + 2;\nx = x - 1;",
                "11",
                "Step 1: x = 10. Step 2: x = 10 + 2 = 12. Step 3: x = 12 - 1 = 11."),

            trace("var-tr-03", Topic.VARIABLES, 2,
                "What is the value of result after this code runs?",
                "int a = 7;\nint b = 3;\nint result = a % b;",
                "1",
                "% is the modulo (remainder) operator. 7 divided by 3 is 2 remainder 1. "
                + "So 7 % 3 = 1.",
                "1"),

            trace("var-tr-04", Topic.VARIABLES, 2,
                "What is the value of x after this code?",
                "double x = 9 / 2;",
                "4.0",
                "9 and 2 are both integer literals, so Java performs integer division: 9/2 = 4. "
                + "The result 4 is then widened to double 4.0 and stored in x.",
                "4"),

            trace("var-tr-05", Topic.VARIABLES, 2,
                "What is the value of c?",
                "char c = 'A';\nc++;",
                "B",
                "char is an integer type. Incrementing 'A' (Unicode 65) gives 66, which is 'B'. "
                + "char arithmetic moves through the Unicode table."),

            // ── Debugging ────────────────────────────────────────────────────

            debug("var-db-01", Topic.VARIABLES, 1,
                "What is wrong with this declaration?",
                "int x = \"hello\";",
                "Missing semicolon",
                "Wrong type: cannot assign a String to an int",
                "Variable name is invalid",
                "Nothing is wrong",
                "b",
                "int holds numbers only. \"hello\" is a String literal. "
                + "Java is strongly typed—you cannot assign a String to an int."),

            debug("var-db-02", Topic.VARIABLES, 1,
                "What is wrong with this code?",
                "Int count = 0;",
                "Int should be lowercase: int",
                "count is a reserved word",
                "The value 0 cannot be assigned to a variable",
                "Nothing is wrong",
                "a",
                "Java primitive type names are all lowercase: int, double, boolean, char, etc. "
                + "'Int' with a capital I does not exist as a type."),

            debug("var-db-03", Topic.VARIABLES, 2,
                "A student writes this code and gets a compile error. Why?",
                "public void example() {\n    int x;\n    System.out.println(x);\n}",
                "int is not valid inside a method",
                "println cannot print int values",
                "Local variable x may not have been initialized",
                "x needs to be declared as a class field",
                "c",
                "Local variables do not get a default value in Java. "
                + "You must assign a value before reading one. "
                + "Class-level fields do get defaults (int → 0), but local variables do not."),

            // ── Code Generation ───────────────────────────────────────────────

            codegen("var-cg-01", Topic.VARIABLES, 1,
                "Which line correctly declares an integer variable named 'score' with the value 100?",
                "int score = 100;",
                "integer score = 100;",
                "int score == 100;",
                "var int score = 100;",
                "a",
                "The correct syntax is: type name = value; "
                + "Java uses = for assignment, not ==. "
                + "'integer' is not a type; 'Integer' is a class. "
                + "'var int' is not valid syntax.",
                "Type comes first, then name, then = then value."),

            codegen("var-cg-02", Topic.VARIABLES, 2,
                "You want to store the result of 22 divided by 7 as a decimal number. "
                + "Which declaration is best?",
                "int result = 22 / 7;",
                "double result = 22.0 / 7;",
                "double result = 22 / 7;",
                "float result = \"22/7\";",
                "b",
                "To get decimal division, at least one operand must be a floating-point number. "
                + "22.0 / 7 forces floating-point division. "
                + "22 / 7 performs integer division (result: 3), then widens to 3.0.",
                "If both operands are ints, Java does integer division even into a double."),

            mc("var-mc-07", Topic.VARIABLES, 1,
                "What is the difference between int and long?",
                "int is for decimals; long is for whole numbers",
                "int is 32 bits (up to ~2.1 billion); long is 64 bits (much larger range)",
                "long is for positive numbers only; int allows negatives",
                "They are identical — just different names",
                "b",
                "int: -2,147,483,648 to 2,147,483,647 (32-bit). "
                + "long: -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807 (64-bit). "
                + "Use long for population counts, file sizes, timestamps. Long literals end with L: 100_000_000_000L."),

            mc("var-mc-08", Topic.VARIABLES, 2,
                "What does the 'final' keyword do when applied to a variable?",
                "Makes the variable automatically thread-safe",
                "Prevents the variable from being reassigned after initialization",
                "Makes the variable visible to all classes",
                "Causes the variable to be stored in the heap",
                "b",
                "final variables must be assigned exactly once and cannot be changed afterward. "
                + "final int MAX = 100; — MAX can never be reassigned. "
                + "For objects, final prevents reassigning the reference, but the object's fields can still change."),

            mc("var-mc-09", Topic.VARIABLES, 2,
                "What is the result of integer overflow in Java?",
                "The program throws ArithmeticException",
                "The value wraps around (modular arithmetic)",
                "The variable is automatically promoted to long",
                "The result is undefined behavior",
                "b",
                "Java integer arithmetic wraps silently on overflow. "
                + "Integer.MAX_VALUE + 1 wraps to Integer.MIN_VALUE (-2,147,483,648). "
                + "There is no overflow exception for int/long. Use Math.addExact() if you need overflow detection."),

            mc("var-mc-10", Topic.VARIABLES, 2,
                "Which of these is a valid way to write 1,000,000 as an int literal in Java 7+?",
                "1,000,000", "1_000_000", "1.000.000", "1E6",
                "b",
                "Java 7 introduced underscores in numeric literals for readability: 1_000_000, 0xFF_FF, 0b1010_0101. "
                + "Commas are not valid. 1E6 is a double literal, not an int."),

            mc("var-mc-11", Topic.VARIABLES, 2,
                "What is the difference between '==' and 'equals()' for comparing variables?",
                "They are identical for all types",
                "== compares primitive values or object references; equals() compares object content",
                "equals() only works on String; == works on all types",
                "== compares by content; equals() compares by reference",
                "b",
                "For primitives (int, double, etc.): == compares values correctly. "
                + "For objects (String, Integer, etc.): == checks if both variables point to the SAME object. "
                + "equals() checks if the content is the same (if overridden). "
                + "Always use equals() (or Objects.equals()) to compare String and object values."),

            mc("var-mc-12", Topic.VARIABLES, 3,
                "What is implicit widening conversion?",
                "Casting a larger type to a smaller type",
                "Automatically converting a smaller numeric type to a larger one (e.g., int to double)",
                "Converting a String to a number",
                "Converting a double to an int by truncation",
                "b",
                "Java automatically widens: byte → short → int → long → float → double. "
                + "int x = 5; double d = x; — no cast needed, x is widened to 5.0. "
                + "Narrowing (e.g., double to int) requires an explicit cast: int n = (int) 3.7;"),

            trace("var-tr-06", Topic.VARIABLES, 2,
                "What is the output?",
                "int a = 5;\n"
                + "int b = a;\n"
                + "b = 10;\n"
                + "System.out.println(a);",
                "5",
                "Primitive assignment copies the value. b = a copies the value 5 into b. "
                + "Changing b to 10 does not affect a. a remains 5."),

            trace("var-tr-07", Topic.VARIABLES, 2,
                "What is the output?",
                "int x = Integer.MAX_VALUE;\n"
                + "System.out.println(x + 1);",
                "-2147483648",
                "Integer.MAX_VALUE is 2,147,483,647. Adding 1 overflows and wraps to Integer.MIN_VALUE: -2,147,483,648. "
                + "Java int arithmetic wraps silently."),

            trace("var-tr-08", Topic.VARIABLES, 2,
                "What is the output?",
                "double x = 1.0 / 3.0;\n"
                + "System.out.printf(\"%.4f%n\", x);",
                "0.3333",
                "1.0 / 3.0 is floating-point division: 0.333333... %.4f formats to 4 decimal places: 0.3333."),

            trace("var-tr-09", Topic.VARIABLES, 3,
                "What is the output?",
                "final int MAX = 10;\n"
                + "int[] arr = new int[MAX];\n"
                + "System.out.println(arr.length);",
                "10",
                "final MAX = 10 is a compile-time constant. new int[MAX] creates an array of length 10. "
                + "arr.length returns 10."),

            debug("var-db-04", Topic.VARIABLES, 2,
                "The result is 0.0 instead of the expected decimal. Why?",
                "int a = 7;\n"
                + "int b = 2;\n"
                + "double result = a / b;\n"
                + "System.out.println(result);",
                "result was not initialized before division",
                "double cannot store the result of int division",
                "a / b performs integer division (3) before widening to double; result is 3.0 not 3.5",
                "The variable name 'result' is reserved",
                "c",
                "a and b are both int, so a / b = 3 (integer division, truncated). "
                + "The int 3 is then widened to 3.0. "
                + "Fix: (double) a / b or a / (double) b forces floating-point division."),

            debug("var-db-05", Topic.VARIABLES, 2,
                "The compiler reports 'cannot assign a value to final variable x'. Why?",
                "final int x = 5;\n"
                + "x = 10;  // error here",
                "x must be declared static to be reassigned",
                "final prevents reassignment — once assigned, x cannot be changed",
                "int literals cannot be assigned to final variables",
                "x should be declared as constant",
                "b",
                "The final keyword makes a variable a constant: assigned once, never changed. "
                + "To make x changeable, remove 'final'. "
                + "By convention, final constants use UPPER_CASE names: final int MAX_SIZE = 10;"),

            codegen("var-cg-03", Topic.VARIABLES, 2,
                "Which correctly declares a constant for the number of days in a week?",
                "int DAYS_IN_WEEK = 7;",
                "const int DAYS_IN_WEEK = 7;",
                "final int DAYS_IN_WEEK = 7;",
                "static DAYS_IN_WEEK = 7;",
                "c",
                "Java uses 'final' not 'const' (C++ syntax). final int prevents reassignment. "
                + "By convention, constants use UPPER_CASE_SNAKE_CASE. "
                + "Class-level constants are usually: private static final int DAYS_IN_WEEK = 7;"),

            codegen("var-cg-04", Topic.VARIABLES, 2,
                "Which correctly computes the average of three integers as a decimal?",
                "int avg = (a + b + c) / 3;",
                "double avg = (double)(a + b + c) / 3;",
                "double avg = a + b + c / 3;",
                "double avg = (a + b + c) / 3;",
                "b",
                "Cast the sum to double before dividing. (double)(a+b+c) promotes the result to double, "
                + "so the division gives a decimal. Option A does integer division. "
                + "Option C has wrong precedence: divides c by 3 first. Option D is still integer division.")
        );
    }
}
