package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class LambdasQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.LAMBDAS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("lam-mc-01", Topic.LAMBDAS, 2,
                "What does the lambda expression '(x, y) -> x + y' represent?",
                "A method that declares two variables",
                "An anonymous function that takes two parameters and returns their sum",
                "A block that prints two values",
                "A constructor for a class with two fields",
                "b",
                "A lambda '(params) -> body' is an anonymous function. "
                + "(x, y) -> x + y takes two inputs and returns their sum. "
                + "It can be assigned to a functional interface like BiFunction<Integer, Integer, Integer>."),

            mc("lam-mc-02", Topic.LAMBDAS, 2,
                "Which functional interface accepts one argument and returns a boolean?",
                "Function<T, Boolean>", "Consumer<T>", "Predicate<T>", "Supplier<T>",
                "c",
                "Predicate<T> has the method 'boolean test(T t)'. "
                + "Function<T, R> takes one arg and returns R. "
                + "Consumer<T> takes one arg but returns void. "
                + "Supplier<T> takes no args but returns T."),

            mc("lam-mc-03", Topic.LAMBDAS, 2,
                "What is a method reference, e.g. System.out::println?",
                "A shorthand for a lambda that calls an existing method",
                "A reference to the method's memory address",
                "A way to declare a method inside another method",
                "A deprecated form of anonymous inner class",
                "a",
                "System.out::println is equivalent to the lambda 's -> System.out.println(s)'. "
                + "Method references are more concise when the lambda body is just a single method call."),

            mc("lam-mc-04", Topic.LAMBDAS, 3,
                "A lambda captures a local variable from its enclosing scope. What constraint applies?",
                "The local variable must be declared public",
                "The local variable must be effectively final (not changed after the lambda is created)",
                "The local variable must be static",
                "The local variable must be a primitive type",
                "b",
                "Lambdas can capture variables from the surrounding scope, but those variables "
                + "must be effectively final (never reassigned after initialisation). "
                + "This ensures the lambda has a stable view of the value."),

            trace("lam-tr-01", Topic.LAMBDAS, 2,
                "What is printed?",
                "import java.util.*;\n"
                + "import java.util.function.*;\n"
                + "List<String> names = List.of(\"Charlie\", \"Alice\", \"Bob\");\n"
                + "names.stream()\n"
                + "     .sorted()\n"
                + "     .forEach(System.out::println);",
                "Alice\nBob\nCharlie",
                "sorted() uses natural (alphabetical) order. "
                + "forEach with a method reference prints each name on its own line."),

            debug("lam-db-01", Topic.LAMBDAS, 3,
                "This code has a compile error. Why?",
                "int count = 0;\n"
                + "Runnable r = () -> {\n"
                + "    count++;   // error here\n"
                + "    System.out.println(count);\n"
                + "};",
                "Runnable does not support lambdas with bodies",
                "count++ modifies count, violating the effectively-final requirement for captured variables",
                "Runnable's run() method cannot print",
                "int cannot be captured by a lambda",
                "b",
                "Lambdas can only capture effectively final variables. "
                + "count++ modifies count, making it not effectively final. "
                + "Fix: use AtomicInteger or an int[] wrapper, both of which are themselves effectively final references."),

            codegen("lam-cg-01", Topic.LAMBDAS, 2,
                "Which code correctly sorts a list of strings by length using a lambda?",
                "list.sort(String::length);",
                "list.sort((a, b) -> a.length() - b.length());",
                "list.sort(() -> String.compareLength());",
                "list.sort(Comparator.length());",
                "b",
                "(a, b) -> a.length() - b.length() is a valid Comparator<String> lambda. "
                + "Option A passes a Function reference, not a Comparator. "
                + "Options C and D use non-existent API. "
                + "Also valid: list.sort(Comparator.comparingInt(String::length));"),

            mc("lam-mc-05", Topic.LAMBDAS, 2,
                "What is a method reference like String::toUpperCase equivalent to?",
                "A constructor call: new String()",
                "A lambda: s -> s.toUpperCase()",
                "A static import of toUpperCase",
                "A cast to a String interface",
                "b",
                "String::toUpperCase is an instance method reference. "
                + "It represents: (String s) -> s.toUpperCase(). "
                + "Method references are more readable than lambdas when the lambda simply delegates to an existing method."),

            trace("lam-tr-02", Topic.LAMBDAS, 2,
                "What is printed?",
                "import java.util.function.*;\n"
                + "Function<String, Integer> len = String::length;\n"
                + "Function<Integer, Integer> dbl = x -> x * 2;\n"
                + "Function<String, Integer> lenThenDouble = len.andThen(dbl);\n"
                + "System.out.println(lenThenDouble.apply(\"Hello\"));",
                "10",
                "len: \"Hello\" → 5 (length). dbl: 5 → 10 (doubled). "
                + "andThen chains: apply(\"Hello\") → len(\"Hello\")=5 → dbl(5)=10."),

            mc("lam-mc-06", Topic.LAMBDAS, 3,
                "Which functional interface represents a function that takes no arguments and returns a value?",
                "Runnable", "Consumer<T>", "Supplier<T>", "Function<Void, T>",
                "c",
                "Supplier<T> has one method: T get(). "
                + "Runnable takes nothing and returns void. "
                + "Consumer<T> takes T and returns void. "
                + "Function<A,B> takes A and returns B. "
                + "Example: Supplier<String> now = () -> LocalDateTime.now().toString();"),

            codegen("lam-cg-02", Topic.LAMBDAS, 3,
                "Which uses Predicate.and() to filter strings that are long AND start with 'A'?",
                "Predicate<String> p = s -> s.length() > 5 && s.startsWith(\"A\");",
                "Predicate<String> long_ = s -> s.length() > 5; Predicate<String> aStart = s -> s.startsWith(\"A\"); Predicate<String> both = long_.and(aStart);",
                "Predicate<String> p = s -> s.length() > 5; p.and(s -> s.startsWith(\"A\"));",
                "Predicate<String> p = Predicate.of(s -> s.length() > 5, s -> s.startsWith(\"A\"));",
                "b",
                "Both A and B are logically correct for combining predicates. "
                + "B demonstrates composing named predicates with .and() — more readable and reusable. "
                + "Option C discards the result of .and() (Predicate is immutable — and() returns a new Predicate). "
                + "Option D: Predicate.of() doesn't exist."),

            mc("lam-mc-07", Topic.LAMBDAS, 2,
                "What does Consumer<T> represent?",
                "A function that takes no arguments and returns T",
                "A function that takes T and returns a result",
                "A function that takes T, performs some action, and returns nothing",
                "A function that takes two arguments and returns a boolean",
                "c",
                "Consumer<T> has: void accept(T t). "
                + "Examples: Consumer<String> print = System.out::println; list.forEach(print). "
                + "BiConsumer<K,V> takes two arguments. "
                + "Supplier<T> is the inverse: takes nothing and returns T."),

            mc("lam-mc-08", Topic.LAMBDAS, 3,
                "What does Function.compose() do vs Function.andThen()?",
                "compose() runs the function before the other; andThen() runs after — they produce the same result",
                "compose(g) = g runs first, then this function; andThen(g) = this function runs first, then g",
                "compose() is synchronous; andThen() is asynchronous",
                "They are identical methods — one is just an alias for the other",
                "b",
                "f.andThen(g).apply(x) = g(f(x)) — f first, then g. "
                + "f.compose(g).apply(x) = f(g(x)) — g first, then f. "
                + "Think: andThen = pipeline order, compose = math function composition order."),

            mc("lam-mc-09", Topic.LAMBDAS, 2,
                "Which is a valid four-types lambda for BiFunction<String, Integer, String>?",
                "(s, n) -> s.repeat(n)",
                "s -> s.length()",
                "() -> \"hello\"",
                "(s, n, extra) -> s",
                "a",
                "BiFunction<A, B, R> has method R apply(A a, B b). "
                + "BiFunction<String, Integer, String>: takes String and Integer, returns String. "
                + "s.repeat(n) repeats the string n times — valid. "
                + "Option B: only one parameter. "
                + "Option C: no parameters. "
                + "Option D: three parameters."),

            trace("lam-tr-03", Topic.LAMBDAS, 3,
                "What is printed?",
                "import java.util.function.*;\n"
                + "UnaryOperator<Integer> triple = n -> n * 3;\n"
                + "UnaryOperator<Integer> addTen = n -> n + 10;\n"
                + "System.out.println(triple.andThen(addTen).apply(4));\n"
                + "System.out.println(triple.compose(addTen).apply(4));",
                "22\n42",
                "andThen: triple(4)=12, addTen(12)=22. compose: addTen(4)=14, triple(14)=42."),

            trace("lam-tr-04", Topic.LAMBDAS, 2,
                "What is printed?",
                "import java.util.*;\n"
                + "import java.util.function.*;\n"
                + "List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));\n"
                + "nums.removeIf(n -> n % 2 == 0);\n"
                + "System.out.println(nums);",
                "[1, 3, 5]",
                "removeIf(Predicate) removes all elements satisfying the predicate. "
                + "Even numbers (2, 4) are removed. Remaining: [1, 3, 5]."),

            debug("lam-db-02", Topic.LAMBDAS, 3,
                "This code has a compile error. Why?",
                "Comparator<String> comp = (String a, String b, String c) -> a.compareTo(b);",
                "Comparator cannot be used with lambdas",
                "Comparator<String>.compare() takes exactly two parameters, not three",
                "compareTo() is not a valid method on String for this use",
                "Lambdas cannot have type annotations on parameters",
                "b",
                "Comparator<T> has method int compare(T o1, T o2) — exactly two parameters. "
                + "Providing three parameters doesn't match any method in Comparator's functional interface. "
                + "Fix: (a, b) -> a.compareTo(b)"),

            debug("lam-db-03", Topic.LAMBDAS, 2,
                "This throws a compile error about effectively final variables. What is the fix?",
                "int count = 0;\n"
                + "List<String> names = List.of(\"a\", \"b\", \"c\");\n"
                + "names.forEach(name -> {\n"
                + "    count++;  // error\n"
                + "    System.out.println(count + \": \" + name);\n"
                + "});",
                "Declare count as static",
                "Use an AtomicInteger instead: AtomicInteger count = new AtomicInteger(0); then count.incrementAndGet()",
                "Use a traditional for loop instead of forEach",
                "Declare count as final",
                "b",
                "Lambdas require captured local variables to be effectively final. "
                + "count++ modifies count. "
                + "AtomicInteger is itself final (the reference doesn't change), "
                + "and its incrementAndGet() mutates the object without reassigning the variable."),

            codegen("lam-cg-03", Topic.LAMBDAS, 2,
                "Which uses a Supplier to lazily create a default message?",
                "String msg = getMessage();",
                "Supplier<String> msg = () -> \"Hello, World!\"; System.out.println(msg.get());",
                "Function<String> msg = () -> \"Hello, World!\";",
                "Supplier<String> msg = \"Hello, World!\";",
                "b",
                "Supplier<T> wraps a no-arg function returning T. .get() triggers evaluation. "
                + "This enables lazy initialization — msg isn't evaluated until .get() is called. "
                + "Option C: Function<String> needs two type parameters. "
                + "Option D: you can't assign a String literal to a Supplier.")
        );
    }
}
