package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class GenericsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.GENERICS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("gen-mc-01", Topic.GENERICS, 2,
                "What does List<String> guarantee compared to a raw List?",
                "The list is immutable",
                "Only String elements can be added — enforced at compile time",
                "The list is thread-safe",
                "Elements are stored in sorted order",
                "b",
                "Generics provide compile-time type safety. List<String> only accepts String elements; "
                + "the compiler rejects any attempt to add a non-String. "
                + "A raw List accepts Object and may produce ClassCastException at runtime."),

            mc("gen-mc-02", Topic.GENERICS, 2,
                "What does the type parameter <T> mean in 'public <T> T first(List<T> list)'?",
                "T must extend Throwable",
                "T is a placeholder for any type, resolved when the method is called",
                "T can only be a primitive type",
                "T stands for 'Type-safe'",
                "b",
                "Type parameters like <T> are placeholders resolved at compile time by the caller's argument. "
                + "If you call first(List<Integer> list), T becomes Integer. "
                + "T can be any reference type."),

            mc("gen-mc-03", Topic.GENERICS, 3,
                "What does 'List<? extends Number>' accept?",
                "Only List<Number>",
                "A List of Number or any subclass of Number (Integer, Double, etc.)",
                "A List of any type",
                "A List of Number or any superclass of Number",
                "b",
                "'? extends Number' is an upper-bounded wildcard. It accepts List<Number>, "
                + "List<Integer>, List<Double>, etc. You can read elements as Number, but you cannot add elements "
                + "(except null) because the exact subtype is unknown."),

            mc("gen-mc-04", Topic.GENERICS, 3,
                "What does type erasure mean in Java generics?",
                "Generic types are checked at runtime",
                "Generic type information is removed at compile time and unavailable at runtime",
                "Only primitive types are erased",
                "The compiler inserts extra memory for generic types",
                "b",
                "Java generics are implemented via type erasure: the compiler uses generic info for type checking, "
                + "then removes it. At runtime, List<String> and List<Integer> are both just List. "
                + "This means you cannot use instanceof with a generic type parameter."),

            trace("gen-tr-01", Topic.GENERICS, 3,
                "What is printed?",
                "public static <T extends Comparable<T>> T max(T a, T b) {\n"
                + "    return a.compareTo(b) >= 0 ? a : b;\n"
                + "}\n"
                + "// in main:\n"
                + "System.out.println(max(3, 7));\n"
                + "System.out.println(max(\"apple\", \"banana\"));",
                "7\nbanana",
                "T is bound to Comparable<T>. For Integer, compareTo uses numeric order: 7 > 3, returns 7. "
                + "For String, compareTo uses lexicographic order: 'b' > 'a', returns \"banana\"."),

            debug("gen-db-01", Topic.GENERICS, 3,
                "This code produces an unchecked cast warning. Why?",
                "List list = new ArrayList();\n"
                + "list.add(\"hello\");\n"
                + "String s = (String) list.get(0);",
                "ArrayList cannot hold String objects",
                "list is a raw type; the compiler cannot verify the cast is safe at compile time",
                "get() returns Object and cannot be cast",
                "The assignment to String is not allowed without generics",
                "b",
                "Using a raw type (List without <>) bypasses compile-time type checking. "
                + "The cast to String might fail at runtime if the list contains non-Strings. "
                + "Fix: use List<String> list = new ArrayList<>()  to eliminate the cast and warning."),

            codegen("gen-cg-01", Topic.GENERICS, 3,
                "Which generic class correctly stores a pair of values of any two types?",
                "class Pair { Object first; Object second; }",
                "class Pair<A, B> { A first; B second; Pair(A a, B b) { first=a; second=b; } }",
                "class Pair<T> { T first; T second; }",
                "class Pair extends Object<A, B> { A first; B second; }",
                "b",
                "Two separate type parameters <A, B> allow the two fields to have independent types. "
                + "Option A uses Object (no type safety). "
                + "Option C uses one type parameter (both fields must be the same type). "
                + "Option D: 'extends Object<A, B>' is not valid Java syntax."),

            mc("gen-mc-05", Topic.GENERICS, 3,
                "What does 'List<? super Integer>' accept?",
                "A List of Integer only",
                "A List of Integer or any subtype of Integer",
                "A List of Integer or any supertype of Integer (Number, Object, etc.)",
                "A List of any type",
                "c",
                "'? super Integer' is a lower-bounded wildcard — accepts List<Integer>, List<Number>, List<Object>. "
                + "You can safely add Integer elements (they satisfy the lower bound). "
                + "Reading gives Object (the only guaranteed common type). "
                + "Mnemonic: PECS — Producer Extends, Consumer Super."),

            trace("gen-tr-02", Topic.GENERICS, 3,
                "What is printed?",
                "List<Integer> ints = new ArrayList<>(List.of(3, 1, 4, 1, 5));\n"
                + "Collections.sort(ints);\n"
                + "System.out.println(ints.get(0) + \" \" + ints.get(ints.size()-1));",
                "1 5",
                "Collections.sort() sorts in ascending order: [1, 1, 3, 4, 5]. "
                + "get(0) = 1 (smallest), get(4) = 5 (largest)."),

            codegen("gen-cg-02", Topic.GENERICS, 3,
                "Which method compiles and returns the first element of any List?",
                "public Object first(List list) { return list.get(0); }",
                "public <T> T first(List<T> list) { return list.get(0); }",
                "public T first(List<T> list) { return list.get(0); }",
                "public <T> List<T> first(List<T> list) { return list.get(0); }",
                "b",
                "Declare the type parameter <T> before the return type. "
                + "The return type T and parameter List<T> are consistent. "
                + "Option A uses raw type (compiles but loses type safety). "
                + "Option C: T is not declared as a type parameter. "
                + "Option D: return type List<T> doesn't match list.get(0) which is T."),

            mc("gen-mc-06", Topic.GENERICS, 2,
                "Why can't you write 'new T()' inside a generic class?",
                "T might be abstract",
                "Due to type erasure, T is replaced by Object at runtime — the JVM doesn't know which constructor to call",
                "Generic types cannot have constructors",
                "You must use 'new T<>()' instead",
                "b",
                "Type erasure removes T at runtime. The JVM sees Object, not the actual type. "
                + "Workaround: pass a Class<T> or a Supplier<T> to the constructor and use it to create instances."),

            mc("gen-mc-07", Topic.GENERICS, 3,
                "Which is a valid generic method declaration?",
                "public T swap(T a, T b)",
                "public <T> void swap(T[] arr, int i, int j)",
                "public generic void swap(Object a, Object b)",
                "public void <T> swap(T a, T b)",
                "b",
                "The type parameter <T> must be declared between the modifiers and the return type: "
                + "'public <T> void swap(...)'. "
                + "Option A: T is not declared. "
                + "Option C: 'generic' is not a keyword. "
                + "Option D: <T> is after the return type (invalid)."),

            mc("gen-mc-08", Topic.GENERICS, 3,
                "What does Collections.unmodifiableList(list) return?",
                "A new copy of the list that cannot be changed",
                "A view of the list that throws UnsupportedOperationException on mutating operations",
                "A null-safe wrapper around the list",
                "A synchronized version of the list",
                "b",
                "unmodifiableList() returns a view backed by the original list. "
                + "Reads work; any mutating call (add, remove, set) throws UnsupportedOperationException. "
                + "Changes to the underlying list ARE reflected in the view. "
                + "For a true immutable copy: List.copyOf(list) (Java 10+)."),

            trace("gen-tr-03", Topic.GENERICS, 3,
                "What is printed?",
                "List<String> list = new ArrayList<>(List.of(\"b\", \"a\", \"c\"));\n"
                + "list.sort(Comparator.naturalOrder());\n"
                + "System.out.println(list);",
                "[a, b, c]",
                "list.sort() with Comparator.naturalOrder() sorts Strings lexicographically. "
                + "[b, a, c] → [a, b, c]. ArrayList.toString() → [a, b, c]."),

            trace("gen-tr-04", Topic.GENERICS, 3,
                "What is printed?",
                "Map<String, List<Integer>> map = new HashMap<>();\n"
                + "map.put(\"scores\", new ArrayList<>(List.of(10, 20, 30)));\n"
                + "map.get(\"scores\").add(40);\n"
                + "System.out.println(map.get(\"scores\").size());",
                "4",
                "The list stored under \"scores\" is the same object referenced by get(). "
                + "add(40) modifies it in-place. size() = 4."),

            debug("gen-db-02", Topic.GENERICS, 3,
                "This causes a compile error. Why?",
                "List<Integer> ints = new ArrayList<>();\n"
                + "List<Number> nums = ints;  // error",
                "ArrayList cannot be assigned to List",
                "List<Integer> is not a subtype of List<Number> — generics are invariant",
                "Number must be imported before use",
                "Integer cannot be widened to Number in generics",
                "b",
                "Java generics are invariant: List<Integer> is not a List<Number>, even though Integer extends Number. "
                + "Allowing this would break type safety (you could add a Double to what the code thinks is a List<Integer>). "
                + "Use wildcards: List<? extends Number> nums = ints; — read-only, no add."),

            debug("gen-db-03", Topic.GENERICS, 3,
                "This compiles but may throw ClassCastException at runtime. Why?",
                "List rawList = new ArrayList();\n"
                + "rawList.add(42);\n"
                + "List<String> strings = rawList;\n"
                + "String s = strings.get(0);",
                "ArrayList cannot hold Integer and String",
                "Assigning a raw list to a typed list bypasses compile-time checks — the cast to String fails at get(0)",
                "rawList must be cast to List<String> explicitly",
                "String and Integer cannot both be in a List",
                "b",
                "Using a raw type and assigning it to List<String> generates an unchecked cast warning. "
                + "At runtime, strings.get(0) returns an Integer (boxed 42) and the implicit cast to String fails. "
                + "Always use properly typed generics: List<Integer> = new ArrayList<>()"),

            codegen("gen-cg-03", Topic.GENERICS, 3,
                "Which generic method correctly sums a List of any Number subtype?",
                "public <T> double sum(List<T> list) { double s=0; for(T n : list) s += n; return s; }",
                "public <T extends Number> double sum(List<T> list) { double s=0; for(T n : list) s += n.doubleValue(); return s; }",
                "public double sum(List<Number> list) { double s=0; for(Number n : list) s += n.doubleValue(); return s; }",
                "public double sum(List<?> list) { double s=0; for(Object n : list) s += (Number)n; return s; }",
                "b",
                "Bounding T to Number (<T extends Number>) allows calling doubleValue(). "
                + "Option A: T has no arithmetic methods. "
                + "Option C: only accepts List<Number>, not List<Integer>. "
                + "Option D: casts are unsafe and '(Number)n' gives a Number, not a double for +=.")
        );
    }
}
