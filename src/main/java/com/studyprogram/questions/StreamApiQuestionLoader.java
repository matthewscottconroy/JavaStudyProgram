package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class StreamApiQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.STREAM_API; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("sapi-mc-01", Topic.STREAM_API, 2,
                "What does Stream.filter() do?",
                "Modifies each element in the stream",
                "Returns a new stream containing only elements that match a Predicate",
                "Removes all elements from the original collection",
                "Converts the stream to a List",
                "b",
                "filter(Predicate) is an intermediate operation that passes through only elements "
                + "where the predicate returns true. It does not modify the source collection."),

            mc("sapi-mc-02", Topic.STREAM_API, 2,
                "What is the difference between map() and flatMap() in streams?",
                "map() works on lists; flatMap() works on arrays",
                "map() transforms each element to one result; flatMap() transforms each element to a stream and flattens them",
                "flatMap() is faster than map()",
                "map() is terminal; flatMap() is intermediate",
                "b",
                "map(f) produces a Stream<R> where each element is transformed by f. "
                + "flatMap(f) expects f to return a Stream for each element, then merges all those streams into one. "
                + "Use flatMap to flatten a stream of lists."),

            mc("sapi-mc-03", Topic.STREAM_API, 2,
                "Which operation is a terminal operation?",
                "filter()", "map()", "sorted()", "collect()",
                "d",
                "Terminal operations trigger stream evaluation and produce a result or side effect. "
                + "collect(), forEach(), reduce(), count(), findFirst() are terminal. "
                + "filter(), map(), sorted(), limit() are intermediate — they return a new Stream and are lazy."),

            mc("sapi-mc-04", Topic.STREAM_API, 3,
                "What does Optional<T> represent?",
                "A container for exactly two values",
                "A container that may or may not hold a non-null value",
                "A generic list with optional elements",
                "A nullable primitive wrapper",
                "b",
                "Optional<T> wraps a value that might be absent, providing a safer alternative to null. "
                + "Methods: isPresent(), get(), orElse(default), orElseGet(Supplier), ifPresent(Consumer). "
                + "stream operations like findFirst() return Optional."),

            trace("sapi-tr-01", Topic.STREAM_API, 3,
                "What is printed?",
                "import java.util.*;\n"
                + "import java.util.stream.*;\n"
                + "List<Integer> nums = List.of(1, 2, 3, 4, 5, 6);\n"
                + "int result = nums.stream()\n"
                + "    .filter(n -> n % 2 == 0)\n"
                + "    .mapToInt(Integer::intValue)\n"
                + "    .sum();\n"
                + "System.out.println(result);",
                "12",
                "filter keeps even numbers: 2, 4, 6. sum() adds them: 2+4+6 = 12."),

            debug("sapi-db-01", Topic.STREAM_API, 3,
                "This code throws IllegalStateException. Why?",
                "Stream<String> s = List.of(\"a\", \"b\").stream();\n"
                + "s.forEach(System.out::println);\n"
                + "s.forEach(System.out::println);",
                "forEach is not a valid Stream method",
                "A stream cannot be reused after a terminal operation has been called on it",
                "List.of() returns an immutable list that cannot be streamed",
                "System.out::println requires a Consumer, not a method reference",
                "b",
                "Streams are single-use. Once a terminal operation (forEach) is called, the stream is consumed. "
                + "Calling any operation on it again throws IllegalStateException. "
                + "Create a new stream from the collection each time."),

            codegen("sapi-cg-01", Topic.STREAM_API, 3,
                "Which collects only names longer than 3 characters into a new List?",
                "names.stream().filter(n -> n.length() > 3);",
                "names.stream().filter(n -> n.length() > 3).collect(Collectors.toList());",
                "names.filter(n -> n.length() > 3).toList();",
                "names.stream().where(n -> n.length() > 3).collect();",
                "b",
                "filter() is intermediate — it must be followed by a terminal operation. "
                + "collect(Collectors.toList()) (or .toList() in Java 16+) materialises the result. "
                + "Option A returns a Stream, not a List. "
                + "Option C: filter() is not a method on List directly. "
                + "Option D: where() does not exist in Java streams."),

            mc("sapi-mc-05", Topic.STREAM_API, 3,
                "What does Stream.reduce(0, (a, b) -> a + b) compute?",
                "The product of all elements",
                "The sum of all elements",
                "The first element only",
                "The number of elements",
                "b",
                "reduce(identity, accumulator) combines elements. "
                + "Starting from identity (0), (a, b) -> a + b adds each element: 0+e1+e2+...+en. "
                + "For [1,2,3,4]: 0+1=1, 1+2=3, 3+3=6, 6+4=10. "
                + "Equivalent to: .mapToInt(Integer::intValue).sum()"),

            trace("sapi-tr-02", Topic.STREAM_API, 3,
                "What is printed?",
                "import java.util.*;\n"
                + "import java.util.stream.*;\n"
                + "List<String> words = List.of(\"the\", \"quick\", \"brown\", \"fox\");\n"
                + "long count = words.stream()\n"
                + "    .filter(w -> w.length() > 3)\n"
                + "    .count();\n"
                + "System.out.println(count);",
                "2",
                "Words with length > 3: \"quick\" (5), \"brown\" (5). \"the\" (3) and \"fox\" (3) are excluded. "
                + "count() returns 2."),

            mc("sapi-mc-06", Topic.STREAM_API, 3,
                "What does Collectors.groupingBy(classifier) produce?",
                "A sorted List grouped by the classifier result",
                "A Map<K, List<T>> grouping elements by the key returned by classifier",
                "A Set of distinct classifier results",
                "A Stream partitioned into two parts",
                "b",
                "groupingBy groups stream elements into a Map where the key is the classifier result. "
                + "Example: .collect(Collectors.groupingBy(String::length)) → Map<Integer, List<String>> "
                + "grouping words by their length."),

            codegen("sapi-cg-02", Topic.STREAM_API, 3,
                "Which pipeline sums the squares of all even numbers in a list?",
                "list.stream().filter(n -> n % 2 == 0).map(n -> n * n).sum();",
                "list.stream().filter(n -> n % 2 == 0).mapToInt(n -> n * n).sum();",
                "list.stream().where(n -> n % 2 == 0).map(n -> n * n).collect();",
                "list.filter(n % 2 == 0).sum(n * n);",
                "b",
                "filter keeps even numbers. mapToInt converts to an IntStream and applies the square. "
                + "sum() is a terminal operation on IntStream. "
                + "Option A: map() returns Stream<Integer> which has no sum() — use mapToInt first. "
                + "Options C and D use non-existent methods."),

            mc("sapi-mc-07", Topic.STREAM_API, 2,
                "What does Stream.distinct() do?",
                "Sorts the stream in alphabetical order",
                "Removes consecutive duplicate elements",
                "Returns a stream with no duplicate elements",
                "Returns only the first element",
                "c",
                "distinct() returns a stream of unique elements (no duplicates), using .equals() for comparison. "
                + "List.of(1,2,2,3,3,3).stream().distinct() → 1, 2, 3. "
                + "It is stateful — it must track all seen elements."),

            mc("sapi-mc-08", Topic.STREAM_API, 2,
                "What does Stream.limit(n) do?",
                "Throws an exception if the stream has fewer than n elements",
                "Returns a stream with at most n elements",
                "Repeats the stream n times",
                "Skips the first n elements",
                "b",
                "limit(n) is an intermediate operation that truncates the stream to n elements. "
                + "Useful for pagination or taking a sample. "
                + "skip(n) discards the first n elements. "
                + "Stream.generate(supplier).limit(5) creates a finite stream from an infinite one."),

            mc("sapi-mc-09", Topic.STREAM_API, 3,
                "What does Collectors.joining(\", \") do?",
                "Concatenates all stream elements into a single String separated by ', '",
                "Joins all streams into one",
                "Groups elements by their delimiter",
                "Splits a String into a Stream",
                "a",
                "Collectors.joining(delimiter) concatenates String stream elements with the delimiter between them. "
                + "Stream.of(\"a\",\"b\",\"c\").collect(Collectors.joining(\", \")) → \"a, b, c\". "
                + "Can also take prefix and suffix: joining(\", \", \"[\", \"]\") → \"[a, b, c]\"."),

            mc("sapi-mc-10", Topic.STREAM_API, 3,
                "What does Stream.peek() do?",
                "Returns the first element and ends the stream",
                "Performs an action on each element as they pass through, without consuming the stream",
                "Checks if the stream is empty",
                "Sorts the stream by comparing adjacent elements",
                "b",
                "peek(Consumer) is an intermediate operation for debugging: "
                + ".peek(System.out::println) prints each element as it flows through the pipeline. "
                + "It does not consume the stream — a terminal operation is still needed."),

            trace("sapi-tr-03", Topic.STREAM_API, 3,
                "What is printed?",
                "import java.util.*;\n"
                + "import java.util.stream.*;\n"
                + "List<String> words = List.of(\"apple\", \"banana\", \"avocado\", \"blueberry\");\n"
                + "Map<Character, List<String>> byFirst = words.stream()\n"
                + "    .collect(Collectors.groupingBy(w -> w.charAt(0)));\n"
                + "System.out.println(byFirst.get('a').size());",
                "2",
                "Groups by first character. 'a' group: [\"apple\", \"avocado\"]. size() = 2."),

            trace("sapi-tr-04", Topic.STREAM_API, 2,
                "What is printed?",
                "import java.util.stream.*;\n"
                + "long count = Stream.of(\"a\", \"bb\", \"ccc\", \"dd\")\n"
                + "    .filter(s -> s.length() >= 2)\n"
                + "    .count();\n"
                + "System.out.println(count);",
                "3",
                "Elements with length >= 2: \"bb\", \"ccc\", \"dd\". Count = 3."),

            debug("sapi-db-02", Topic.STREAM_API, 3,
                "The stream produces no output. Why?",
                "Stream<Integer> s = Stream.of(1, 2, 3);\n"
                + "s.filter(n -> n > 1);  // no terminal operation",
                "filter() is not a valid stream operation",
                "Streams are lazy — without a terminal operation, no elements are processed",
                "Stream.of() requires at least 4 elements",
                "filter() returns null instead of a stream",
                "b",
                "Stream operations are lazy. filter() creates a new stream description but doesn't execute until "
                + "a terminal operation (collect, forEach, count, etc.) is called. "
                + "Fix: s.filter(n -> n > 1).forEach(System.out::println);"),

            debug("sapi-db-03", Topic.STREAM_API, 3,
                "This produces a ClassCastException. Why?",
                "List mixed = List.of(\"hello\", 42, 3.14);\n"
                + "mixed.stream()\n"
                + "    .map(o -> (String) o)\n"
                + "    .forEach(System.out::println);",
                "List.of() doesn't support mixed types",
                "The raw List contains Integer and Double which cannot be cast to String",
                "stream() doesn't work on untyped lists",
                "map() requires a typed stream",
                "b",
                "The raw List contains non-String elements. Casting an Integer or Double to String throws ClassCastException. "
                + "Fix: filter by type first: .filter(o -> o instanceof String).map(o -> (String) o)"),

            codegen("sapi-cg-03", Topic.STREAM_API, 3,
                "Which converts a List<String> to a Map where keys are strings and values are their lengths?",
                "list.stream().collect(Collectors.toMap(s -> s, s -> s.length()));",
                "list.stream().map(s -> s.length()).collect(Collectors.toMap());",
                "list.stream().collect(Collectors.groupingBy(String::length));",
                "list.stream().toMap(String::length);",
                "a",
                "Collectors.toMap(keyMapper, valueMapper) builds a Map<K,V>. "
                + "Here: key = the string itself, value = its length. "
                + "Option C groups by length (producing Map<Integer, List<String>>), not Map<String, Integer>. "
                + "Option D: toMap is not a stream instance method.")
        );
    }
}
