package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ArgumentsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.ARGUMENTS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("arg-mc-01", Topic.ARGUMENTS, 1,
                "What is the type of 'args' in 'public static void main(String[] args)'?",
                "String", "int[]", "String[]", "List<String>",
                "c",
                "args is a String array (String[]). Each command-line token becomes one element. "
                + "Running 'java MyApp hello world' gives args = {\"hello\", \"world\"}."),

            mc("arg-mc-02", Topic.ARGUMENTS, 1,
                "Running 'java MyApp hello world' — what is args[0]?",
                "MyApp", "java", "hello", "world",
                "c",
                "The program name and 'java' are not included in args. "
                + "args[0] = \"hello\", args[1] = \"world\"."),

            mc("arg-mc-03", Topic.ARGUMENTS, 2,
                "How do you convert args[0] to an int?",
                "int n = args[0];",
                "int n = (int) args[0];",
                "int n = Integer.parseInt(args[0]);",
                "int n = args[0].toInt();",
                "c",
                "Integer.parseInt(String) converts a numeric string to int. "
                + "There is no cast from String to int, and Java has no toInt() method on String."),

            trace("arg-tr-01", Topic.ARGUMENTS, 2,
                "If run as 'java Counter 3', what is printed?",
                "public static void main(String[] args) {\n"
                + "    int n = Integer.parseInt(args[0]);\n"
                + "    for (int i = 0; i < n; i++) System.out.print(i + \" \");\n"
                + "}",
                "0 1 2",
                "parseInt(\"3\") gives 3. The loop prints 0, 1, 2, each with a trailing space.",
                "0 1 2 "),

            debug("arg-db-01", Topic.ARGUMENTS, 2,
                "Running 'java MyApp' (no args) crashes. Why?",
                "public static void main(String[] args) {\n"
                + "    System.out.println(args[0]);\n}",
                "args is null when no arguments are given",
                "args[0] throws ArrayIndexOutOfBoundsException when args is empty",
                "main requires at least one argument to compile",
                "println cannot print strings from arrays",
                "b",
                "With no arguments args is an empty array (length 0), not null. "
                + "Accessing args[0] throws ArrayIndexOutOfBoundsException. "
                + "Guard with: if (args.length > 0) before reading args[0]."),

            codegen("arg-cg-01", Topic.ARGUMENTS, 2,
                "Which snippet safely prints the first argument or 'default' if none is given?",
                "System.out.println(args[0]);",
                "System.out.println(args.length > 0 ? args[0] : \"default\");",
                "System.out.println(args != null ? args[0] : \"default\");",
                "System.out.println(Integer.parseInt(args[0]));",
                "b",
                "args is never null — check args.length > 0 instead. "
                + "Option A crashes with no args. Option C checks null unnecessarily. "
                + "Option D also parses as int, which breaks for non-numeric input."),

            mc("arg-mc-04", Topic.ARGUMENTS, 2,
                "Running 'java App 10 20 30' — what is args.length?",
                "4", "3", "2", "1",
                "b",
                "args contains the tokens after the class name: \"10\", \"20\", \"30\". "
                + "The class name 'App' and the command 'java' are not included. "
                + "args.length = 3. Indices: args[0]=\"10\", args[1]=\"20\", args[2]=\"30\"."),

            trace("arg-tr-02", Topic.ARGUMENTS, 3,
                "Run as 'java Adder 5 8'. What is printed?",
                "public static void main(String[] args) {\n"
                + "    int a = Integer.parseInt(args[0]);\n"
                + "    int b = Integer.parseInt(args[1]);\n"
                + "    System.out.println(a + b);\n"
                + "}",
                "13",
                "args[0]=\"5\" → parseInt → 5. args[1]=\"8\" → parseInt → 8. 5+8=13."),

            debug("arg-db-02", Topic.ARGUMENTS, 2,
                "Running 'java App hello' throws NumberFormatException. Why?",
                "public static void main(String[] args) {\n"
                + "    int n = Integer.parseInt(args[0]);\n"
                + "    System.out.println(n * 2);\n"
                + "}",
                "args[0] is null when a word is passed",
                "Integer.parseInt throws NumberFormatException when the string is not a valid integer",
                "hello is a reserved keyword in Java",
                "parseInt can only be called on numbers, not variables",
                "b",
                "parseInt(\"hello\") throws NumberFormatException because \"hello\" cannot be parsed as an integer. "
                + "Always validate: try { int n = Integer.parseInt(args[0]); } catch (NumberFormatException e) { ... }"),

            codegen("arg-cg-02", Topic.ARGUMENTS, 3,
                "Which iterates over all command-line arguments and prints each one?",
                "for (int i = 0; i <= args.length; i++) System.out.println(args[i]);",
                "for (String arg : args) System.out.println(arg);",
                "System.out.println(args);",
                "args.forEach(System.out::println);",
                "b",
                "Enhanced for loop over args iterates each String element cleanly. "
                + "Option A uses <= which causes ArrayIndexOutOfBoundsException on the last iteration. "
                + "Option C prints the array reference (e.g., [Ljava.lang.String;@...). "
                + "Option D: arrays don't have a forEach method (ArrayList does)."),

            mc("arg-mc-05", Topic.ARGUMENTS, 2,
                "What does 'java MyApp' print if main is: System.out.println(args.length)?",
                "1", "0", "null", "Throws NullPointerException",
                "b",
                "When no arguments are passed, args is an empty String[] with length 0. "
                + "It is never null. args.length == 0."),

            mc("arg-mc-06", Topic.ARGUMENTS, 2,
                "Running 'java MyApp a b c d' — what is args[3]?",
                "a", "b", "c", "d",
                "d",
                "args: [0]=\"a\", [1]=\"b\", [2]=\"c\", [3]=\"d\". "
                + "args[3] is \"d\". The program name is not included."),

            mc("arg-mc-07", Topic.ARGUMENTS, 3,
                "A user passes '5 10' as two separate arguments. Which code correctly adds them?",
                "int sum = Integer.parseInt(args[0] + args[1]);",
                "int sum = Integer.parseInt(args[0]) + Integer.parseInt(args[1]);",
                "int sum = args[0] + args[1];",
                "int sum = Integer.parseInt(args[0] + \" \" + args[1]);",
                "b",
                "Each argument must be parsed independently. "
                + "Option A concatenates the strings first: '510', then parses — wrong. "
                + "Option C concatenates strings (gives \"510\"). "
                + "Option D: parseInt(\"5 10\") throws NumberFormatException (space in string)."),

            mc("arg-mc-08", Topic.ARGUMENTS, 3,
                "What is printed by 'java App hello world' if main does: System.out.println(String.join(\"-\", args))?",
                "hello world", "hello-world", "args[0]-args[1]", "Throws exception",
                "b",
                "String.join(\"-\", args) joins all elements of the String[] with \"-\" as delimiter. "
                + "args = [\"hello\", \"world\"] → \"hello-world\"."),

            trace("arg-tr-03", Topic.ARGUMENTS, 3,
                "Run as 'java App 3 7 2'. What is printed?",
                "public static void main(String[] args) {\n"
                + "    int max = Integer.MIN_VALUE;\n"
                + "    for (String a : args) {\n"
                + "        int n = Integer.parseInt(a);\n"
                + "        if (n > max) max = n;\n"
                + "    }\n"
                + "    System.out.println(max);\n"
                + "}",
                "7",
                "Parses each arg: 3, 7, 2. Tracks maximum. 7 > 3 → max=7. 2 not > 7. Prints 7."),

            trace("arg-tr-04", Topic.ARGUMENTS, 2,
                "Run as 'java Greeter Alice Bob'. What is printed?",
                "public static void main(String[] args) {\n"
                + "    for (String name : args) {\n"
                + "        System.out.println(\"Hello, \" + name + \"!\");\n"
                + "    }\n"
                + "}",
                "Hello, Alice!\nHello, Bob!",
                "Iterates over args [\"Alice\", \"Bob\"], printing a greeting for each."),

            debug("arg-db-03", Topic.ARGUMENTS, 2,
                "Running 'java App' (no args) throws ArrayIndexOutOfBoundsException. What is the fix?",
                "public static void main(String[] args) {\n"
                + "    System.out.println(\"Hello, \" + args[0]);\n"
                + "}",
                "Check args != null before accessing",
                "Check args.length > 0 before accessing args[0]",
                "Declare args as String instead of String[]",
                "Use args.get(0) instead of args[0]",
                "b",
                "args is never null but may be empty. "
                + "args.length > 0 guards against the empty case. "
                + "Fix: if (args.length > 0) System.out.println(\"Hello, \" + args[0]); else System.out.println(\"No name given.\");"),

            debug("arg-db-04", Topic.ARGUMENTS, 3,
                "Running 'java Calc 10 add 5' throws NumberFormatException. What is wrong?",
                "public static void main(String[] args) {\n"
                + "    int a = Integer.parseInt(args[0]);\n"
                + "    int b = Integer.parseInt(args[1]);  // bug\n"
                + "    System.out.println(a + b);\n"
                + "}",
                "parseInt does not work on args",
                "args[1] is \"add\", a non-numeric String — parseInt cannot parse it",
                "10 and 5 cannot be added with +",
                "parseInt requires args to have exactly 2 elements",
                "b",
                "The arguments are: args[0]=\"10\", args[1]=\"add\", args[2]=\"5\". "
                + "Parsing \"add\" as int throws NumberFormatException. "
                + "The code should parse args[0] and args[2]: int b = Integer.parseInt(args[2]);"),

            codegen("arg-cg-03", Topic.ARGUMENTS, 3,
                "Which safely sums all numeric command-line arguments, skipping non-numeric ones?",
                "int sum = 0; for (String a : args) sum += Integer.parseInt(a);",
                "int sum = 0; for (String a : args) { try { sum += Integer.parseInt(a); } catch (NumberFormatException e) {} }",
                "int sum = Arrays.stream(args).sum();",
                "int sum = Integer.parseInt(String.join(\"\", args));",
                "b",
                "Wrapping parseInt in try-catch skips non-numeric tokens gracefully. "
                + "Option A crashes on any non-numeric argument. "
                + "Option C: arrays don't have a sum() method on String[]. "
                + "Option D: joining all args into one string produces a concat, not a sum.")
        );
    }
}
