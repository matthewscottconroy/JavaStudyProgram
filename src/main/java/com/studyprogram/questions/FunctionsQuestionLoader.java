package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class FunctionsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.FUNCTIONS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("fn-mc-01", Topic.FUNCTIONS, 1,
                "What return type do you use for a method that returns nothing?",
                "null", "empty", "void", "None",
                "c",
                "void indicates a method does not return a value. "
                + "null is a value, not a type. 'empty' and 'None' are not Java types."),

            mc("fn-mc-02", Topic.FUNCTIONS, 2,
                "Two methods with the same name but different parameter lists is called:",
                "Overriding", "Overloading", "Shadowing", "Polymorphism",
                "b",
                "Overloading = same method name, different parameter lists (type/count) in the same class. "
                + "Overriding = redefining an inherited method in a subclass."),

            mc("fn-mc-03", Topic.FUNCTIONS, 2,
                "What does this method return when called as double(4)?\n\n"
                + "    static int double(int n) { return n * 2; }",
                "4", "8", "2", "Won't compile — 'double' is a keyword",
                "d",
                "'double' is a reserved keyword in Java and cannot be used as a method name. "
                + "The code will not compile."),

            mc("fn-mc-04", Topic.FUNCTIONS, 3,
                "What is the output?\n\n"
                + "    public static int add(int a, int b) { return a + b; }\n"
                + "    // in main:\n"
                + "    int result = add(add(2, 3), 4);\n"
                + "    System.out.println(result);",
                "234", "9", "14", "Won't compile",
                "b",
                "add(2,3) = 5. add(5, 4) = 9. The inner call resolves first."),

            // ── Tracing ───────────────────────────────────────────────────────

            trace("fn-tr-01", Topic.FUNCTIONS, 2,
                "What is printed?",
                "static int square(int n) { return n * n; }\n// in main:\nSystem.out.println(square(5));",
                "25",
                "square(5) returns 5 * 5 = 25, which is then printed."),

            trace("fn-tr-02", Topic.FUNCTIONS, 3,
                "What is printed?",
                "static int factorial(int n) {\n    if (n <= 1) return 1;\n    return n * factorial(n - 1);\n}\n// in main:\nSystem.out.println(factorial(4));",
                "24",
                "factorial(4) = 4 * factorial(3) = 4 * 3 * factorial(2) = 4*3*2*factorial(1) "
                + "= 4*3*2*1 = 24. This is recursion."),

            // ── Debugging ────────────────────────────────────────────────────

            debug("fn-db-01", Topic.FUNCTIONS, 2,
                "The method should return the sum but always returns 0. Why?",
                "static int sum(int a, int b) {\n    int result = a + b;\n}\n",
                "int cannot hold a sum",
                "Missing return statement — the result is computed but never returned",
                "a and b should be doubles",
                "result needs to be declared outside the method",
                "b",
                "The method computes the sum into 'result' but never returns it. "
                + "In Java, a non-void method must return a value. Add: return result; before the closing brace."),

            // ── Code Generation ───────────────────────────────────────────────

            codegen("fn-cg-01", Topic.FUNCTIONS, 2,
                "Which method signature correctly defines a static method named 'max' "
                + "that takes two ints and returns an int?",
                "void max(int a, int b) { }",
                "static int max(int a, int b) { }",
                "int static max(a, b) { }",
                "static max(int a, int b) -> int { }",
                "b",
                "Order: modifiers (static) then return type (int) then name (max) then parameters. "
                + "Option A returns void. Option C has wrong order and missing types. "
                + "Option D uses arrow syntax which is not valid Java method syntax.",
                "Pattern: [modifiers] returnType methodName(paramType param, ...)"),

            mc("fn-mc-05", Topic.FUNCTIONS, 2,
                "What is a varargs parameter?",
                "A parameter that has a default value",
                "A parameter that accepts zero or more arguments of the same type",
                "A parameter that is passed by reference",
                "A parameter list that can hold any number of different types",
                "b",
                "void log(String... messages) — the caller can pass zero, one, or many Strings. "
                + "Inside the method, messages is treated as a String[]. "
                + "Varargs must be the last parameter: void log(int level, String... messages)."),

            mc("fn-mc-06", Topic.FUNCTIONS, 2,
                "What is the output?\n\n"
                + "    static int counter = 0;\n"
                + "    static void increment() { counter++; }\n"
                + "    // in main:\n"
                + "    increment(); increment(); increment();\n"
                + "    System.out.println(counter);",
                "0", "1", "3", "undefined",
                "c",
                "counter is a static field shared by all calls to increment(). "
                + "Three calls increment it from 0 to 3."),

            mc("fn-mc-07", Topic.FUNCTIONS, 3,
                "What is a stack overflow in the context of recursive methods?",
                "Running out of memory in the heap",
                "Too many recursive calls filling the call stack, causing a StackOverflowError",
                "A method calling itself with the correct base case missing from compilation",
                "An arithmetic overflow in a recursive formula",
                "b",
                "Each method call adds a frame to the call stack. Deep or infinite recursion fills the stack, "
                + "causing java.lang.StackOverflowError. "
                + "The fix: ensure a correct base case is reached, or convert deep recursion to iteration."),

            mc("fn-mc-08", Topic.FUNCTIONS, 3,
                "What does 'pass by value' mean for method parameters in Java?",
                "Methods can modify variables passed to them",
                "Java copies the value (or reference) — the caller's variable is never directly modified",
                "Primitive values are passed by reference; objects are passed by value",
                "Parameters are passed in stack order, last-in first-out",
                "b",
                "Java is always pass-by-value. For primitives: the method gets a copy of the number. "
                + "For objects: the method gets a copy of the reference (both point to the same object, "
                + "so field mutations are visible — but reassigning the parameter variable doesn't affect the caller)."),

            trace("fn-tr-03", Topic.FUNCTIONS, 2,
                "What is printed?",
                "static void doubleIt(int x) { x = x * 2; }\n"
                + "// in main:\n"
                + "int n = 5;\n"
                + "doubleIt(n);\n"
                + "System.out.println(n);",
                "5",
                "Java passes ints by value. doubleIt() doubles its local copy of x, but n in main is unchanged. "
                + "n is still 5."),

            trace("fn-tr-04", Topic.FUNCTIONS, 2,
                "What is printed?",
                "static String greet(String name) {\n"
                + "    return \"Hello, \" + name + \"!\";\n"
                + "}\n"
                + "System.out.println(greet(\"Bob\"));",
                "Hello, Bob!",
                "greet(\"Bob\") returns \"Hello, Bob!\". println prints it with a newline."),

            debug("fn-db-02", Topic.FUNCTIONS, 2,
                "The method should work for any number of values but only accepts 3. What should change?",
                "static int sum(int a, int b, int c) {\n"
                + "    return a + b + c;\n"
                + "}",
                "Change return type to double",
                "Change parameters to (int[] values) or (int... values) to accept variable number of ints",
                "Rename the method to sumAll",
                "Add more overloads: sum(int a, int b) and sum(int a, int b, int c, int d)",
                "b",
                "Using int... values (varargs) allows calling sum(1), sum(1,2,3), sum(1,2,3,4,5). "
                + "Inside the method: for (int v : values) total += v; "
                + "Adding overloads (option D) doesn't scale — you'd need overloads for every count."),

            debug("fn-db-03", Topic.FUNCTIONS, 3,
                "This recursive method causes a StackOverflowError. Why?",
                "static int count(int n) {\n"
                + "    return 1 + count(n - 1);\n"
                + "}",
                "n - 1 is an invalid expression inside recursion",
                "The method has no base case — it recurses infinitely without stopping",
                "return type should be void for recursive methods",
                "int cannot hold recursive counts",
                "b",
                "There is no base case. count(0) calls count(-1) calls count(-2) forever. "
                + "Fix: add if (n <= 0) return 0; before the recursive call. "
                + "Without a base case, the call stack fills and StackOverflowError is thrown.")
        );
    }
}
