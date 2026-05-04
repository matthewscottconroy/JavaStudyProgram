package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class InterfacesQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.INTERFACES; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ifc-mc-01", Topic.INTERFACES, 1,
                "Can an interface have instance fields (non-static, non-final)?",
                "Yes, interfaces can have any kind of field",
                "Yes, but only private fields",
                "No — interface fields are implicitly public static final (constants)",
                "No, interfaces cannot have fields at all",
                "c",
                "All fields declared in an interface are implicitly 'public static final'. "
                + "They are constants, not instance variables. "
                + "To share state, you need a class."),

            mc("ifc-mc-02", Topic.INTERFACES, 2,
                "What is a default method in an interface?",
                "A method that must be overridden",
                "A method with a body provided in the interface, used when a class doesn't override it",
                "A method with package-private access",
                "A method the JVM calls automatically on startup",
                "b",
                "Default methods (Java 8+) let you add methods with implementations to interfaces "
                + "without breaking existing implementors. "
                + "Classes can override them but don't have to."),

            mc("ifc-mc-03", Topic.INTERFACES, 2,
                "How many interfaces can a Java class implement?",
                "Only 1", "At most 2", "At most 3", "As many as needed",
                "d",
                "A class can implement multiple interfaces, separated by commas: "
                + "'class MyClass implements Runnable, Comparable<MyClass>, AutoCloseable { ... }'. "
                + "This is how Java achieves a form of multiple inheritance."),

            mc("ifc-mc-04", Topic.INTERFACES, 2,
                "What is a functional interface?",
                "An interface with no methods",
                "An interface with exactly one abstract method",
                "An interface whose methods return functions",
                "An interface that extends Function",
                "b",
                "A functional interface has exactly one abstract method (SAM — Single Abstract Method). "
                + "This allows lambda expressions to implement it. "
                + "Examples: Runnable, Comparator, Predicate, Function. "
                + "@FunctionalInterface annotation enforces the constraint."),

            trace("ifc-tr-01", Topic.INTERFACES, 2,
                "What is printed?",
                "interface Greeter {\n"
                + "    String greet(String name);\n"
                + "    default String farewell(String name) { return \"Bye, \" + name; }\n"
                + "}\n"
                + "class FormalGreeter implements Greeter {\n"
                + "    public String greet(String name) { return \"Good day, \" + name; }\n"
                + "}\n"
                + "// in main:\n"
                + "Greeter g = new FormalGreeter();\n"
                + "System.out.println(g.greet(\"Alice\"));\n"
                + "System.out.println(g.farewell(\"Alice\"));",
                "Good day, Alice\nBye, Alice",
                "FormalGreeter provides greet(). farewell() is inherited from the interface default. "
                + "Both are available on the Greeter reference."),

            debug("ifc-db-01", Topic.INTERFACES, 2,
                "This class has a compile error. Why?",
                "interface Shape {\n"
                + "    double area();\n"
                + "    double perimeter();\n"
                + "}\n"
                + "class Square implements Shape {\n"
                + "    double side;\n"
                + "    Square(double s) { side = s; }\n"
                + "    public double area() { return side * side; }\n"
                + "}",
                "Square cannot implement Shape because Square is not abstract",
                "Square is missing an implementation of perimeter()",
                "interface methods must be called differently in implementing classes",
                "double is not a valid return type for interface methods",
                "b",
                "Square implements Shape but only provides area(). "
                + "perimeter() is abstract and unimplemented. "
                + "Fix: add 'public double perimeter() { return 4 * side; }' or declare Square abstract."),

            codegen("ifc-cg-01", Topic.INTERFACES, 2,
                "Which is a valid lambda implementation of the Runnable interface?",
                "Runnable r = new Runnable(System.out.println(\"run\"));",
                "Runnable r = () -> System.out.println(\"run\");",
                "Runnable r = run -> System.out.println(\"run\");",
                "Runnable r = { System.out.println(\"run\"); };",
                "b",
                "Runnable has one abstract method: void run(). "
                + "A no-parameter lambda '() -> ...' implements it directly. "
                + "Option A misuses the constructor syntax. "
                + "Option C has an erroneous parameter. "
                + "Option D is not valid Java lambda syntax."),

            mc("ifc-mc-05", Topic.INTERFACES, 2,
                "Can a class inherit from two parent classes in Java?",
                "Yes, using 'extends ClassA, ClassB'",
                "Yes, using 'extends ClassA implements ClassB'",
                "No — Java does not support multiple class inheritance; use multiple interface implementation instead",
                "Yes, but only if both parent classes are abstract",
                "c",
                "Java intentionally disallows extending multiple classes to avoid the diamond problem. "
                + "A class can extend one class but implement many interfaces. "
                + "Interfaces support multiple inheritance for types (without state conflicts)."),

            trace("ifc-tr-02", Topic.INTERFACES, 3,
                "What does this print?",
                "interface Loud { default String sound() { return \"LOUD\"; } }\n"
                + "interface Soft { default String sound() { return \"soft\"; } }\n"
                + "class Voice implements Loud, Soft {\n"
                + "    @Override public String sound() { return Loud.super.sound(); }\n"
                + "}\n"
                + "System.out.println(new Voice().sound());",
                "LOUD",
                "When two interfaces both provide a default method with the same signature, "
                + "the class must override it to resolve the conflict. "
                + "Loud.super.sound() explicitly picks the Loud interface's default, returning \"LOUD\"."),

            codegen("ifc-cg-02", Topic.INTERFACES, 3,
                "Which correctly defines an interface Printable with a default print() and abstract format()?",
                "abstract interface Printable { void format(); default void print() { System.out.println(format()); } }",
                "interface Printable { String format(); default void print() { System.out.println(format()); } }",
                "interface Printable { default String format() { return \"\"; } void print(); }",
                "class Printable { abstract String format(); void print() { System.out.println(format()); } }",
                "b",
                "format() is abstract (no body) so implementing classes must provide it. "
                + "print() is default — it provides a reusable implementation calling format(). "
                + "Option A: 'abstract interface' is redundant and format() returns void (can't use in println). "
                + "Option D uses class, not interface."),

            mc("ifc-mc-06", Topic.INTERFACES, 2,
                "What does the @FunctionalInterface annotation enforce?",
                "The interface must have at least one method",
                "The interface must have exactly one abstract method — a compile error if violated",
                "All methods in the interface must be default",
                "The interface can only be implemented by lambda expressions",
                "b",
                "@FunctionalInterface is a marker that causes the compiler to verify the interface has exactly one SAM. "
                + "If you accidentally add a second abstract method, the compiler reports an error. "
                + "Without the annotation, the interface is still functional — it just isn't protected against accidental expansion."),

            mc("ifc-mc-07", Topic.INTERFACES, 3,
                "What is a static method in an interface?",
                "A method that belongs to all implementing classes",
                "A utility method that belongs to the interface itself and is called via the interface name",
                "A method that cannot be overridden",
                "A method that is automatically inherited by all implementing classes",
                "b",
                "Interface static methods (Java 8+) are called via the interface: Validator.isValidEmail(str). "
                + "They are NOT inherited by implementing classes. "
                + "They are useful for factory methods or utilities related to the interface."),

            mc("ifc-mc-08", Topic.INTERFACES, 3,
                "What is the 'marker interface' pattern?",
                "An interface that marks methods as deprecated",
                "An empty interface used to tag a class as having some property",
                "An interface with only default methods",
                "An interface that extends multiple other interfaces",
                "b",
                "Marker interfaces have no methods — they exist purely to tag a class. "
                + "Examples: java.io.Serializable (marks a class as serializable), "
                + "java.lang.Cloneable (marks a class as cloneable). "
                + "Modern Java often uses annotations instead of marker interfaces."),

            trace("ifc-tr-03", Topic.INTERFACES, 3,
                "What is printed?",
                "interface Drawable { void draw(); }\n"
                + "interface Resizable { void resize(int factor); }\n"
                + "class Widget implements Drawable, Resizable {\n"
                + "    public void draw() { System.out.println(\"drawing\"); }\n"
                + "    public void resize(int f) { System.out.println(\"resize \" + f); }\n"
                + "}\n"
                + "Widget w = new Widget();\n"
                + "w.draw();\n"
                + "w.resize(2);",
                "drawing\nresize 2",
                "Widget implements both interfaces and provides concrete implementations for both methods."),

            trace("ifc-tr-04", Topic.INTERFACES, 2,
                "What is printed?",
                "interface Validator {\n"
                + "    boolean isValid(String s);\n"
                + "    static boolean isEmpty(String s) { return s == null || s.isEmpty(); }\n"
                + "}\n"
                + "System.out.println(Validator.isEmpty(\"\"));\n"
                + "System.out.println(Validator.isEmpty(\"hello\"));",
                "true\nfalse",
                "Static interface methods are called on the interface name itself. "
                + "isEmpty(\"\") → true (empty string). isEmpty(\"hello\") → false."),

            debug("ifc-db-02", Topic.INTERFACES, 3,
                "This class has a compile error. Why?",
                "interface Animal { void speak(); }\n"
                + "interface Pet { void speak(); }\n"
                + "class Dog implements Animal, Pet { }",
                "A class cannot implement two interfaces with the same method",
                "Dog must provide one speak() implementation — and that single method satisfies both interfaces",
                "speak() has different return types in Animal and Pet",
                "Dog must declare which speak() it implements",
                "b",
                "When two interfaces declare the same abstract method signature, the implementing class provides one method "
                + "that satisfies both. There is no conflict because the signatures are identical. "
                + "The compile error is the missing implementation: add 'public void speak() { ... }'"),

            debug("ifc-db-03", Topic.INTERFACES, 3,
                "A class implements two interfaces that both provide conflicting default methods. What must the class do?",
                "interface A { default String name() { return \"A\"; } }\n"
                + "interface B { default String name() { return \"B\"; } }\n"
                + "class C implements A, B { }  // compile error",
                "C must extend A to resolve the conflict",
                "C must override name() to resolve the ambiguity",
                "C can remove one interface to compile",
                "The last interface listed takes precedence",
                "b",
                "When two implemented interfaces provide conflicting default methods, the class must override the method. "
                + "Inside the override it can delegate to a specific interface: A.super.name() or B.super.name(). "
                + "class C implements A, B { public String name() { return A.super.name(); } }"),

            codegen("ifc-cg-03", Topic.INTERFACES, 2,
                "Which lambda correctly implements the Comparator<String> interface to sort by string length?",
                "Comparator<String> c = (a, b) -> a.length() - b.length();",
                "Comparator<String> c = String::compareTo;",
                "Comparator<String> c = () -> String.length();",
                "Comparator<String> c = (a, b) -> a > b;",
                "a",
                "Comparator.compare(a, b) returns negative if a < b, 0 if equal, positive if a > b. "
                + "a.length() - b.length() achieves this for length ordering. "
                + "Option B sorts lexicographically by content (String::compareTo is a method reference to compareTo). "
                + "Option C has wrong parameter count. "
                + "Option D: > on String is a compile error.")
        );
    }
}
