package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class JavaClassHierarchyQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.JAVA_CLASS_HIERARCHY; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("jch-mc-01", Topic.JAVA_CLASS_HIERARCHY, 2,
                "Every class in Java implicitly extends which class?",
                "Class", "Serializable", "Object", "Base",
                "c",
                "java.lang.Object is the root of the Java class hierarchy. "
                + "Every class that doesn't explicitly extend another class implicitly extends Object. "
                + "This provides default implementations of toString(), equals(), hashCode(), and others."),

            mc("jch-mc-02", Topic.JAVA_CLASS_HIERARCHY, 2,
                "What does the default Object.equals() method compare?",
                "The fields of both objects using reflection",
                "Reference identity — whether both variables point to the same object",
                "The result of toString() on both objects",
                "The hash codes of both objects",
                "b",
                "The default equals() uses == (reference equality). "
                + "Override it (together with hashCode()) to compare by field values. "
                + "If equals() returns true, hashCode() MUST return the same value for both objects."),

            mc("jch-mc-03", Topic.JAVA_CLASS_HIERARCHY, 3,
                "What is the contract between equals() and hashCode()?",
                "If hashCode() is the same, equals() must return true",
                "If equals() returns true, hashCode() must return the same value",
                "They are independent and need not be consistent",
                "hashCode() must always return a unique value",
                "b",
                "The contract: if a.equals(b) then a.hashCode() == b.hashCode(). "
                + "The reverse is NOT required — collisions (same hash, not equal) are allowed. "
                + "Violating this breaks HashMap and HashSet — objects can get lost in a bucket."),

            mc("jch-mc-04", Topic.JAVA_CLASS_HIERARCHY, 3,
                "What does Object.toString() return by default?",
                "The object's field values formatted as JSON",
                "null",
                "The class name followed by '@' and the hex hash code",
                "An empty string",
                "c",
                "Default toString() returns something like 'com.example.Dog@1a2b3c'. "
                + "Override toString() to return a human-readable representation of your object's state, "
                + "which also improves debugging since System.out.println(obj) calls toString() automatically."),

            trace("jch-tr-01", Topic.JAVA_CLASS_HIERARCHY, 2,
                "What is printed?",
                "Object a = new Object();\n"
                + "Object b = a;\n"
                + "System.out.println(a.equals(b));\n"
                + "System.out.println(a == b);",
                "true\ntrue",
                "true\ntrue",
                "a and b reference the same object, so both == and equals() (default: reference equality) return true."),

            debug("jch-db-01", Topic.JAVA_CLASS_HIERARCHY, 3,
                "The HashSet behaves unexpectedly. Why?",
                "class Point {\n"
                + "    int x, y;\n"
                + "    Point(int x, int y) { this.x = x; this.y = y; }\n"
                + "    @Override public boolean equals(Object o) {\n"
                + "        Point p = (Point) o;\n"
                + "        return x == p.x && y == p.y;\n"
                + "    }\n"
                + "}\n"
                + "Set<Point> set = new HashSet<>();\n"
                + "set.add(new Point(1,2));\n"
                + "System.out.println(set.contains(new Point(1,2)));",
                "HashSet does not support custom types",
                "equals() cast will throw ClassCastException",
                "hashCode() is not overridden — points with equal fields land in different buckets",
                "equals() must call super.equals()",
                "c",
                "HashSet first compares hashCode() to find the bucket, then uses equals(). "
                + "Without overriding hashCode(), two Point(1,2) objects have different hashes "
                + "and are stored in different buckets — contains() returns false even though equals() would return true. "
                + "Always override hashCode() when you override equals()."),

            codegen("jch-cg-01", Topic.JAVA_CLASS_HIERARCHY, 3,
                "Which correctly overrides toString() for a class with fields 'name' and 'age'?",
                "public String toString() { return name + age; }",
                "@Override public String toString() { return \"Person{name='\" + name + \"', age=\" + age + \"}\"; }",
                "public static String toString(Person p) { return p.name; }",
                "@Override public void toString() { System.out.println(name); }",
                "b",
                "toString() must be public, non-static, return String, and use @Override. "
                + "A readable format like 'ClassName{field=value}' helps debugging. "
                + "Option A omits @Override and separator. Option C is static. Option D returns void."),

            mc("jch-mc-05", Topic.JAVA_CLASS_HIERARCHY, 2,
                "Which Object method should always be overridden together with equals()?",
                "toString()", "compareTo()", "hashCode()", "clone()",
                "c",
                "The equals-hashCode contract: if two objects are equal (equals() returns true), "
                + "they must have the same hashCode(). HashMap and HashSet rely on this. "
                + "If you override equals() without hashCode(), equal objects may end up in different hash buckets, "
                + "breaking collections."),

            mc("jch-mc-06", Topic.JAVA_CLASS_HIERARCHY, 3,
                "What does the Comparable interface require a class to implement?",
                "equals(Object o)",
                "compareTo(T other) returning a negative, zero, or positive int",
                "hashCode()",
                "clone()",
                "b",
                "Comparable<T> defines natural ordering via compareTo(). "
                + "Return negative if this < other, 0 if equal, positive if this > other. "
                + "Collections.sort() and TreeSet use compareTo() when no explicit Comparator is given."),

            mc("jch-mc-07", Topic.JAVA_CLASS_HIERARCHY, 3,
                "What is the difference between Comparable and Comparator in Java?",
                "Comparable is for primitives; Comparator is for objects",
                "Comparable defines the class's own natural ordering; Comparator defines an external/alternate ordering",
                "Comparable can only compare Strings; Comparator can compare any type",
                "They are identical — Comparator extends Comparable",
                "b",
                "Comparable is implemented by the class itself (intrinsic ordering). "
                + "Comparator is a separate object defining an alternate sort order — useful when you need "
                + "multiple orderings (e.g., sort by name, then by age) or when you can't modify the class."),

            trace("jch-tr-02", Topic.JAVA_CLASS_HIERARCHY, 2,
                "What is printed?",
                "class Animal {\n"
                + "    @Override public String toString() { return \"Animal\"; }\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    @Override public String toString() { return \"Dog\"; }\n"
                + "}\n"
                + "Animal a = new Dog();\n"
                + "System.out.println(a);",
                "Dog",
                "println calls toString() on the actual runtime type (Dog), not the declared type (Animal). "
                + "This is polymorphism — the JVM dispatches to Dog.toString()."),

            trace("jch-tr-03", Topic.JAVA_CLASS_HIERARCHY, 3,
                "What is printed?",
                "Object x = \"hello\";\n"
                + "Object y = \"hello\";\n"
                + "System.out.println(x.equals(y));\n"
                + "System.out.println(x == y);",
                "true\ntrue",
                "String overrides equals() for content comparison (true). "
                + "String literals are interned — both x and y reference the same object in the string pool, so == is also true. "
                + "With 'new String(\"hello\")' the == would be false."),

            debug("jch-db-02", Topic.JAVA_CLASS_HIERARCHY, 3,
                "Why does this compareTo() violate the Comparable contract?",
                "class Score implements Comparable<Score> {\n"
                + "    int value;\n"
                + "    @Override public int compareTo(Score other) {\n"
                + "        return this.value - other.value; // integer subtraction\n"
                + "    }\n"
                + "}",
                "compareTo() must return only -1, 0, or 1",
                "compareTo() is not allowed in classes that also implement Serializable",
                "Integer subtraction can overflow, producing wrong sign for large negative values",
                "The method must be static",
                "c",
                "Subtracting integers overflows when values are far apart (e.g., Integer.MIN_VALUE - 1 wraps to a positive). "
                + "Use Integer.compare(this.value, other.value) instead — it handles all cases safely."),

            codegen("jch-cg-02", Topic.JAVA_CLASS_HIERARCHY, 3,
                "Which snippet correctly implements equals() for a class with a single int field 'id'?",
                "@Override public boolean equals(Object o) { return this.id == o.id; }",
                "@Override public boolean equals(Object o) { if (!(o instanceof MyClass c)) return false; return this.id == c.id; }",
                "@Override public boolean equals(MyClass o) { return this.id == o.id; }",
                "public static boolean equals(MyClass a, MyClass b) { return a.id == b.id; }",
                "b",
                "equals() takes Object, not the concrete type. You must check instanceof before casting. "
                + "Option A: o.id won't compile — o is Object. "
                + "Option C: overloads but doesn't override equals(Object). "
                + "Option D: static equals doesn't override Object.equals().")
        );
    }
}
