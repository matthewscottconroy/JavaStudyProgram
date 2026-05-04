package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class FunctionalParadigmQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.FUNCTIONAL_PARADIGM; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("fpar-mc-01", Topic.FUNCTIONAL_PARADIGM, 2,
                "What is a pure function?",
                "A function declared with the 'pure' keyword",
                "A function that always returns the same output for the same input and has no side effects",
                "A function with no parameters",
                "A function that only operates on primitive types",
                "b",
                "A pure function is deterministic (same inputs → same output) and free of side effects "
                + "(no I/O, no mutation of external state, no random, no time). "
                + "Pure functions are easy to test, compose, and reason about. "
                + "Java doesn't enforce purity in the language, but you can write functions that follow the discipline."),

            mc("fpar-mc-02", Topic.FUNCTIONAL_PARADIGM, 2,
                "What does 'immutability' mean in the functional paradigm?",
                "Variables cannot be reassigned once created",
                "Methods cannot call other methods",
                "Objects cannot be garbage-collected",
                "Classes cannot be extended",
                "a",
                "Immutability means state is never modified after creation. "
                + "Instead of changing a list, you produce a new list with the modification. "
                + "In Java: prefer final fields, use List.of() / Map.of() for immutable collections, "
                + "return new objects rather than mutating arguments."),

            mc("fpar-mc-03", Topic.FUNCTIONAL_PARADIGM, 3,
                "What is a higher-order function?",
                "A function defined in a subclass",
                "A function that takes functions as arguments or returns a function",
                "A function with more than 3 parameters",
                "A recursive function",
                "b",
                "Higher-order functions treat functions as first-class values. "
                + "In Java, methods that accept Function<T,R>, Predicate<T>, or Consumer<T> are higher-order. "
                + "Stream methods like map(Function) and filter(Predicate) are examples. "
                + "This enables composable, reusable pipelines without imperative loops."),

            mc("fpar-mc-04", Topic.FUNCTIONAL_PARADIGM, 3,
                "What is referential transparency?",
                "An expression that refers to a variable in an outer scope",
                "An expression that can be replaced by its value without changing program behavior",
                "A transparent (see-through) reference type",
                "A method that returns 'this'",
                "b",
                "Referential transparency means calling f(x) is equivalent to substituting its result everywhere it appears. "
                + "This holds for pure functions: 2 + 2 is transparently replaceable with 4. "
                + "Non-referentially-transparent: Math.random(), new Date() — same call, different result each time."),

            trace("fpar-tr-01", Topic.FUNCTIONAL_PARADIGM, 3,
                "What is printed? (Assume add is a pure function: int add(int a, int b) { return a + b; })",
                "int x = add(2, 3);\n"
                + "int y = add(2, 3);\n"
                + "System.out.println(x == y);\n"
                + "System.out.println(x + y);",
                "true\n10",
                "true\n10",
                "add(2,3) always returns 5 (pure, deterministic). x and y are both 5. "
                + "x == y → true. x + y = 10."),

            debug("fpar-db-01", Topic.FUNCTIONAL_PARADIGM, 3,
                "This 'pure' function is not actually pure. Why?",
                "import java.util.*;\n"
                + "static List<Integer> addItem(List<Integer> list, int item) {\n"
                + "    list.add(item);  // mutation!\n"
                + "    return list;\n"
                + "}",
                "add() is not a valid method on List",
                "The function modifies the caller's list (side effect) — it is not pure",
                "The function should be non-static",
                "Returning the same list is illegal",
                "b",
                "list.add(item) mutates the List passed in — a side effect that is visible to the caller. "
                + "A pure version would return a new list: "
                + "List<Integer> newList = new ArrayList<>(list); newList.add(item); return newList. "
                + "The original list remains unchanged."),

            codegen("fpar-cg-01", Topic.FUNCTIONAL_PARADIGM, 3,
                "Which approach best follows functional style to double all numbers in a list?",
                "for (int i = 0; i < list.size(); i++) list.set(i, list.get(i) * 2);",
                "list.stream().map(n -> n * 2).collect(Collectors.toList());",
                "list.forEach(n -> n = n * 2);",
                "Collections.sort(list); list.replaceAll(n -> n * 2);",
                "b",
                "stream().map().collect() is functional: it produces a new list of doubled values without modifying 'list'. "
                + "Option A mutates in place. Option C does nothing useful (n is a local variable). "
                + "Option D sorts first (unnecessary) and mutates in place with replaceAll."),

            mc("fpar-mc-05", Topic.FUNCTIONAL_PARADIGM, 2,
                "What is function composition?",
                "Calling the same function recursively",
                "Combining two functions so the output of one becomes the input of the next",
                "Declaring multiple functions with the same name (overloading)",
                "Writing a function inside another function",
                "b",
                "Composition chains functions: g ∘ f means apply f first, then g. "
                + "In Java: Function<A,C> composed = f.andThen(g); — applies f, then passes the result to g. "
                + "This builds complex transformations from simple, reusable pieces."),

            mc("fpar-mc-06", Topic.FUNCTIONAL_PARADIGM, 3,
                "What is a side effect in a function?",
                "An exception thrown by the function",
                "Any observable action beyond returning a value — I/O, mutation of external state, logging",
                "A return value that is ignored by the caller",
                "A parameter that has a default value",
                "b",
                "Side effects include: writing to a file, printing, modifying a field or argument, calling a network API. "
                + "Functions with side effects are harder to test and reason about because their behavior depends on "
                + "and changes the world outside the function."),

            mc("fpar-mc-07", Topic.FUNCTIONAL_PARADIGM, 3,
                "What is partial application (or currying) in functional programming?",
                "Applying a function to only some of its arguments, producing a new function that takes the remaining arguments",
                "Applying a function to all its arguments at once",
                "Splitting a function into two overloaded versions",
                "Using default parameters in a method",
                "a",
                "Currying transforms f(a, b) into f(a)(b). Partial application binds some arguments early. "
                + "In Java, you can simulate this with lambdas: "
                + "Function<Integer,Integer> add5 = x -> add(5, x); — 'add' is partially applied with a=5."),

            trace("fpar-tr-02", Topic.FUNCTIONAL_PARADIGM, 3,
                "What is printed?",
                "import java.util.function.*;\n"
                + "Function<Integer, Integer> triple = x -> x * 3;\n"
                + "Function<Integer, Integer> addTen = x -> x + 10;\n"
                + "Function<Integer, Integer> combined = triple.andThen(addTen);\n"
                + "System.out.println(combined.apply(4));",
                "22",
                "triple.apply(4) = 12. addTen.apply(12) = 22. andThen chains: first triple, then addTen."),

            trace("fpar-tr-03", Topic.FUNCTIONAL_PARADIGM, 3,
                "What is printed?",
                "import java.util.function.*;\n"
                + "Predicate<String> isLong = s -> s.length() > 5;\n"
                + "Predicate<String> startsWithA = s -> s.startsWith(\"A\");\n"
                + "Predicate<String> both = isLong.and(startsWithA);\n"
                + "System.out.println(both.test(\"Algorithm\"));\n"
                + "System.out.println(both.test(\"Ant\"));",
                "true\nfalse",
                "\"Algorithm\" has length 9 > 5 and starts with 'A' → true. "
                + "\"Ant\" has length 3, not > 5 → false. Predicate.and() requires both conditions."),

            debug("fpar-db-02", Topic.FUNCTIONAL_PARADIGM, 3,
                "This code intends to filter even numbers but always returns an empty list. Why?",
                "List<Integer> nums = List.of(1, 2, 3, 4, 5);\n"
                + "List<Integer> evens = nums.stream()\n"
                + "    .filter(n -> n % 2 == 1)  // intention: keep evens\n"
                + "    .collect(Collectors.toList());",
                "filter() removes elements matching the predicate",
                "Collectors.toList() is deprecated",
                "The predicate n % 2 == 1 keeps odd numbers, not even ones — use n % 2 == 0",
                "stream() is not available on List.of()",
                "c",
                "n % 2 == 1 is true for odd numbers. To keep evens, the predicate should be n % 2 == 0. "
                + "filter() keeps elements for which the predicate returns true. "
                + "This is a logic error, not a Java error — the code compiles and runs but produces the wrong result."),

            codegen("fpar-cg-02", Topic.FUNCTIONAL_PARADIGM, 3,
                "Which correctly uses Function.compose() to apply subtractOne BEFORE double?",
                "Function<Integer,Integer> result = doubleIt.andThen(subtractOne);",
                "Function<Integer,Integer> result = doubleIt.compose(subtractOne);",
                "Function<Integer,Integer> result = subtractOne.andThen(doubleIt);",
                "Both b and c produce the same result",
                "b",
                "compose(g) applies g first, then the outer function. So doubleIt.compose(subtractOne) = doubleIt(subtractOne(x)). "
                + "andThen(g) applies g after: subtractOne.andThen(doubleIt) = doubleIt(subtractOne(x)). "
                + "Options B and C are equivalent — but the question asks specifically about compose().")
        );
    }
}
