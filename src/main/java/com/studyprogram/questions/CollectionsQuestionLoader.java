package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class CollectionsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.COLLECTIONS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("col-mc-01", Topic.COLLECTIONS, 1,
                "Which collection does NOT allow duplicate elements?",
                "ArrayList", "LinkedList", "Set", "ArrayDeque",
                "c",
                "Set (e.g. HashSet, TreeSet) enforces uniqueness — adding a duplicate is silently ignored. "
                + "ArrayList, LinkedList, and ArrayDeque all allow duplicates."),

            mc("col-mc-02", Topic.COLLECTIONS, 2,
                "Which Map implementation maintains insertion order?",
                "HashMap", "TreeMap", "LinkedHashMap", "EnumMap",
                "c",
                "LinkedHashMap maintains the order in which keys were inserted. "
                + "HashMap makes no ordering guarantee. TreeMap sorts by key (natural or Comparator). "
                + "EnumMap maintains enum declaration order."),

            mc("col-mc-03", Topic.COLLECTIONS, 2,
                "What does Map.getOrDefault(key, defaultValue) return?",
                "null if the key is absent",
                "The value for the key, or defaultValue if the key is absent",
                "A new entry with defaultValue if the key is absent",
                "It throws NoSuchElementException if the key is absent",
                "b",
                "getOrDefault returns the associated value if the key exists, otherwise defaultValue. "
                + "It does NOT add the default to the map. Use computeIfAbsent() to add."),

            mc("col-mc-04", Topic.COLLECTIONS, 3,
                "Which collection is best for a LIFO (last-in, first-out) stack?",
                "ArrayList", "LinkedList used as Deque", "PriorityQueue", "TreeSet",
                "b",
                "Deque (e.g. ArrayDeque) supports push/pop for LIFO semantics efficiently. "
                + "The legacy java.util.Stack class exists but is discouraged. "
                + "PriorityQueue is a min-heap (ordering by priority). "
                + "TreeSet is sorted, not stack-like."),

            trace("col-tr-01", Topic.COLLECTIONS, 2,
                "What is printed?",
                "import java.util.*;\n"
                + "Map<String, Integer> scores = new HashMap<>();\n"
                + "scores.put(\"Alice\", 90);\n"
                + "scores.put(\"Bob\", 80);\n"
                + "scores.put(\"Alice\", 95);\n"
                + "System.out.println(scores.get(\"Alice\"));\n"
                + "System.out.println(scores.size());",
                "95\n2",
                "The second put(\"Alice\", 95) replaces the first value 90. "
                + "The map has 2 entries: Alice=95, Bob=80."),

            debug("col-db-01", Topic.COLLECTIONS, 3,
                "This code throws NullPointerException. Why?",
                "TreeMap<String, Integer> map = new TreeMap<>();\n"
                + "map.put(null, 1);",
                "TreeMap does not allow integer values",
                "TreeMap does not allow null keys because it must compare/sort them",
                "put() is not a valid method on TreeMap",
                "Integer must be unboxed before storing",
                "b",
                "TreeMap sorts keys using Comparable or a Comparator. "
                + "Comparing null to another key throws NullPointerException. "
                + "Use HashMap if you need null key support (allows one null key)."),

            codegen("col-cg-01", Topic.COLLECTIONS, 2,
                "Which code counts word frequencies in a String[] array?",
                "Map<String,Integer> freq = new HashMap<>();\n"
                + "for (String w : words) freq.put(w, freq.getOrDefault(w,0) + 1);",
                "Set<String> freq = new HashSet<>();\n"
                + "for (String w : words) freq.add(w);",
                "Map<String,Integer> freq = new TreeMap<>();\n"
                + "for (String w : words) freq.put(w, 1);",
                "Map<String,Integer> freq = new HashMap<>();\n"
                + "for (String w : words) freq.get(w)++;",
                "a",
                "getOrDefault(w, 0) + 1 correctly increments or initialises the count. "
                + "Option B only tracks presence, not count. "
                + "Option C always sets count to 1 (overwrites). "
                + "Option D: get() returns an Integer (immutable), incrementing it doesn't update the map."),

            mc("col-mc-05", Topic.COLLECTIONS, 2,
                "What does List.of() return?",
                "A mutable ArrayList",
                "An immutable list that throws UnsupportedOperationException on add/remove",
                "null",
                "An empty LinkedList",
                "b",
                "List.of() (Java 9+) returns a compact, immutable list. "
                + "Calling add(), remove(), or set() throws UnsupportedOperationException. "
                + "To get a mutable list from it: new ArrayList<>(List.of(1, 2, 3))."),

            trace("col-tr-02", Topic.COLLECTIONS, 2,
                "What is printed?",
                "import java.util.*;\n"
                + "Set<String> s = new TreeSet<>();\n"
                + "s.add(\"banana\");\n"
                + "s.add(\"apple\");\n"
                + "s.add(\"cherry\");\n"
                + "s.add(\"apple\");\n"
                + "System.out.println(s.size() + \" \" + s.first());",
                "3 apple",
                "TreeSet sorts alphabetically and rejects duplicates. "
                + "After 4 adds: {apple, banana, cherry}. size()=3. first()=apple (smallest)."),

            mc("col-mc-06", Topic.COLLECTIONS, 3,
                "Which Queue method retrieves-and-removes the head, returning null if empty (vs throwing on empty)?",
                "remove()", "pop()", "poll()", "peek()",
                "c",
                "poll() retrieves and removes the head, returning null if the queue is empty. "
                + "remove() does the same but throws NoSuchElementException if empty. "
                + "peek() retrieves without removing (returns null if empty). "
                + "offer() adds an element (returns false if capacity-constrained vs add() which throws)."),

            codegen("col-cg-02", Topic.COLLECTIONS, 3,
                "Which iterates over a Map's entries printing each key=value pair?",
                "for (String k : map) System.out.println(k + \"=\" + map.get(k));",
                "for (Map.Entry<String,Integer> e : map.entrySet()) System.out.println(e.getKey() + \"=\" + e.getValue());",
                "map.forEach((k,v) -> System.out.println(k + \"=\" + map.get(k)));",
                "for (int i = 0; i < map.size(); i++) System.out.println(map.get(i));",
                "b",
                "entrySet() gives a Set of Map.Entry pairs — the standard way to iterate keys and values together. "
                + "Option A iterates the map directly (not valid — Map doesn't implement Iterable). "
                + "Option C forEach is correct but uses map.get(k) unnecessarily (use v instead). "
                + "Option D: Map has no integer index access."),

            mc("col-mc-07", Topic.COLLECTIONS, 2,
                "Which collection maintains elements in sorted (natural) order automatically?",
                "HashSet", "ArrayList", "TreeSet", "LinkedHashSet",
                "c",
                "TreeSet stores elements in sorted order (natural ordering or Comparator). "
                + "HashSet: no order guarantee. "
                + "LinkedHashSet: insertion order. "
                + "ArrayList: insertion order, allows duplicates."),

            mc("col-mc-08", Topic.COLLECTIONS, 2,
                "What does HashMap.put(key, value) return if the key already exists?",
                "null", "The new value", "The old (previous) value", "Throws DuplicateKeyException",
                "c",
                "put() returns the previous value associated with the key, or null if the key was not present. "
                + "This lets you detect overwrites: String old = map.put(\"x\", newVal); if (old != null) { ... }"),

            mc("col-mc-09", Topic.COLLECTIONS, 3,
                "Which interface does HashMap implement?",
                "List", "Map", "Set", "Queue",
                "b",
                "HashMap implements Map<K,V>. "
                + "The Map interface has: put(), get(), remove(), containsKey(), entrySet(), keySet(), values(). "
                + "HashMap is the most common implementation; TreeMap is sorted; LinkedHashMap is insertion-ordered."),

            mc("col-mc-10", Topic.COLLECTIONS, 3,
                "What is the time complexity of HashMap.get() on average?",
                "O(n)", "O(log n)", "O(1)", "O(n log n)",
                "c",
                "HashMap provides O(1) average-case get/put/remove using hash buckets. "
                + "Worst case is O(n) if all keys hash to the same bucket (very unlikely with good hashCode). "
                + "TreeMap is O(log n) for these operations but maintains sorted order."),

            trace("col-tr-03", Topic.COLLECTIONS, 2,
                "What is printed?",
                "import java.util.*;\n"
                + "Map<String,Integer> map = new HashMap<>();\n"
                + "String[] words = {\"a\",\"b\",\"a\",\"c\",\"b\",\"a\"};\n"
                + "for (String w : words) map.put(w, map.getOrDefault(w, 0) + 1);\n"
                + "System.out.println(map.get(\"a\"));",
                "3",
                "Counting frequencies: a appears 3 times, b appears 2, c appears 1. map.get(\"a\") = 3."),

            trace("col-tr-04", Topic.COLLECTIONS, 3,
                "What is printed?",
                "import java.util.*;\n"
                + "Deque<Integer> stack = new ArrayDeque<>();\n"
                + "stack.push(1);\n"
                + "stack.push(2);\n"
                + "stack.push(3);\n"
                + "System.out.println(stack.pop() + \" \" + stack.peek());",
                "3 2",
                "push adds to front (LIFO). Stack: top→[3,2,1]. pop() removes and returns 3. peek() looks at 2."),

            debug("col-db-02", Topic.COLLECTIONS, 2,
                "This code may throw NullPointerException. Why?",
                "Map<String,Integer> map = new HashMap<>();\n"
                + "map.put(\"score\", null);\n"
                + "int n = map.get(\"score\");",
                "HashMap does not allow null values",
                "Auto-unboxing null Integer to int throws NullPointerException",
                "get() requires a cast to Integer",
                "The key \"score\" must be set before calling get()",
                "b",
                "HashMap allows null values. map.get(\"score\") returns null (an Integer). "
                + "Auto-unboxing null to int throws NullPointerException. "
                + "Fix: Integer n = map.get(\"score\"); — keep it boxed, or use getOrDefault(\"score\", 0)."),

            debug("col-db-03", Topic.COLLECTIONS, 3,
                "The HashSet contains duplicates. Why?",
                "class Point { int x, y; Point(int x, int y) { this.x=x; this.y=y; } }\n"
                + "Set<Point> pts = new HashSet<>();\n"
                + "pts.add(new Point(1, 2));\n"
                + "pts.add(new Point(1, 2));\n"
                + "System.out.println(pts.size());  // prints 2, not 1",
                "HashSet allows two elements that are equal",
                "Point does not override hashCode() and equals() — so the two objects are treated as different by the Set",
                "int fields must be boxed before use in HashSet",
                "HashSet requires a Comparator to detect duplicates",
                "b",
                "HashSet uses hashCode() for bucketing and equals() for equality checks. "
                + "Without overriding both, two Point(1,2) objects have different hash codes (from Object) "
                + "and are treated as different. "
                + "Override both: @Override int hashCode() { return Objects.hash(x,y); } "
                + "and @Override boolean equals(Object o) { ... }"),

            codegen("col-cg-03", Topic.COLLECTIONS, 2,
                "Which creates a Map from names to ages and looks up 'Bob' with a default of -1 if absent?",
                "Map<String,Integer> m = new HashMap<>(); m.put(\"Alice\",30); int age = m[\"Bob\"];",
                "Map<String,Integer> m = new HashMap<>(); m.put(\"Alice\",30); int age = m.getOrDefault(\"Bob\",-1);",
                "Map<String,Integer> m = new HashMap<>(); m.put(\"Alice\",30); int age = m.get(\"Bob\",-1);",
                "Map<String,Integer> m = new HashMap<>(); m.put(\"Alice\",30); int age = m.getOrDefault(\"Bob\", null);",
                "b",
                "getOrDefault(key, default) returns the value if present, or the default otherwise. "
                + "Option A uses array syntax (invalid for Map). "
                + "Option C: get() takes only one argument. "
                + "Option D: null as the default would cause NullPointerException when unboxing to int.")
        );
    }
}
