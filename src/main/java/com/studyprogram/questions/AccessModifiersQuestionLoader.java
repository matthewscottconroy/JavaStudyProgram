package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class AccessModifiersQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.PUBLIC_PRIVATE_PROTECTED; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("acm-mc-01", Topic.PUBLIC_PRIVATE_PROTECTED, 1,
                "Which access modifier makes a member visible everywhere?",
                "private", "protected", "public", "package-private",
                "c",
                "'public' means any class in any package can access it. "
                + "'private' restricts to the declaring class. "
                + "'protected' allows subclasses and same-package access. "
                + "Package-private (no modifier) allows same-package access only."),

            mc("acm-mc-02", Topic.PUBLIC_PRIVATE_PROTECTED, 1,
                "Which modifier restricts access to the declaring class only?",
                "protected", "private", "public", "final",
                "b",
                "'private' is the most restrictive modifier — only code inside the same class can access it. "
                + "Not even subclasses can access private members."),

            mc("acm-mc-03", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "A field is declared 'protected'. Which code CAN access it?",
                "Any class in any package",
                "Only the declaring class",
                "A subclass in a different package",
                "Only classes in the same file",
                "c",
                "'protected' grants access to: the same class, same package, AND subclasses (even in other packages). "
                + "It does NOT allow arbitrary classes in different packages."),

            mc("acm-mc-04", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "What is the access level when no modifier keyword is written?",
                "public", "private", "protected", "package-private",
                "d",
                "Omitting an access modifier gives 'package-private' (also called default) access. "
                + "Only classes in the same package can access it."),

            trace("acm-tr-01", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "What is printed?",
                "class Counter {\n"
                + "    private int count = 0;\n"
                + "    public void increment() { count++; }\n"
                + "    public int getCount() { return count; }\n"
                + "}\n"
                + "// in main:\n"
                + "Counter c = new Counter();\n"
                + "c.increment();\n"
                + "c.increment();\n"
                + "System.out.println(c.getCount());",
                "2",
                "count is private but accessible via the public methods. "
                + "Two increment() calls raise count to 2, which getCount() returns."),

            debug("acm-db-01", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "This code has a compile error. Why?",
                "class Box {\n    private int width = 10;\n}\n\n"
                + "class Main {\n    public static void main(String[] args) {\n"
                + "        Box b = new Box();\n        System.out.println(b.width);\n    }\n}",
                "Box must be public to be usable from Main",
                "width is private — it cannot be accessed from outside Box",
                "println cannot print int values",
                "Box has no constructor",
                "b",
                "'width' is private, so only code inside Box can read it. "
                + "Main is a separate class and cannot access private members of Box. "
                + "Add a public getter like 'public int getWidth() { return width; }' to fix this."),

            codegen("acm-cg-01", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "Which class correctly encapsulates the 'name' field with a getter?",
                "class Person { public String name; public String getName() { return name; } }",
                "class Person { private String name; public String getName() { return name; } }",
                "class Person { private String name; private String getName() { return name; } }",
                "class Person { protected String name; }",
                "b",
                "Encapsulation: the field is private (hidden), access is provided via a public getter. "
                + "Option A exposes the field directly. "
                + "Option C makes the getter private (inaccessible from outside). "
                + "Option D leaves the field visible to subclasses without a controlled accessor."),

            mc("acm-mc-05", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "What is the default access level when no modifier is specified?",
                "public", "private", "protected", "package-private (accessible within the same package only)",
                "d",
                "No modifier = package-private (also called default access). "
                + "The member is accessible to all classes in the same package, "
                + "but not to classes in other packages (even subclasses in other packages)."),

            trace("acm-tr-02", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "Which lines (if any) cause compile errors?",
                "// Package: com.example\n"
                + "class Animal {\n"
                + "    private String dna = \"ATCG\";\n"
                + "    protected int legs = 4;\n"
                + "    public String name = \"generic\";\n"
                + "}\n"
                + "// Same package:\n"
                + "class Zoo {\n"
                + "    Animal a = new Animal();\n"
                + "    void test() {\n"
                + "        System.out.println(a.name);   // line A\n"
                + "        System.out.println(a.legs);   // line B\n"
                + "        System.out.println(a.dna);    // line C\n"
                + "    }\n"
                + "}",
                "Line C only",
                "dna is private — only accessible within Animal itself. "
                + "name (public) and legs (protected, same package) are both accessible from Zoo."),

            mc("acm-mc-06", Topic.PUBLIC_PRIVATE_PROTECTED, 3,
                "A subclass in a different package can access which members of its parent?",
                "public and private members",
                "public members only",
                "public and protected members",
                "public, protected, and package-private members",
                "c",
                "Protected members are accessible to subclasses regardless of package. "
                + "Package-private members are NOT accessible from a different package — even to subclasses. "
                + "Private members are never accessible outside their declaring class."),

            codegen("acm-cg-02", Topic.PUBLIC_PRIVATE_PROTECTED, 3,
                "Which modifier should be used for a utility method that should only be used inside its class?",
                "public static void helper() { }",
                "protected static void helper() { }",
                "private static void helper() { }",
                "void helper() { }",
                "c",
                "private limits access to the declaring class — perfect for internal helper methods "
                + "that are implementation details. This prevents callers from depending on internal behavior "
                + "that might change. Option D (package-private) exposes it to the whole package unnecessarily."),

            mc("acm-mc-07", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "Which statement about 'private' constructors is true?",
                "A class with a private constructor cannot be instantiated from outside the class",
                "A private constructor means the class is abstract",
                "Private constructors prevent subclassing only",
                "A class may have at most one private constructor",
                "a",
                "Private constructors are used in patterns like Singleton (only one instance ever created) "
                + "and utility classes (e.g., Math — all static, never instantiated). "
                + "Calling 'new' from outside throws a compile error."),

            mc("acm-mc-08", Topic.PUBLIC_PRIVATE_PROTECTED, 3,
                "What is the effect of 'final' on a field vs 'private' on a field?",
                "Both prevent modification from outside the class",
                "final prevents reassignment; private prevents external access — they are orthogonal",
                "private implies final — if a field is private it cannot be changed",
                "final makes a field public; private makes it inaccessible",
                "b",
                "private field: only the declaring class can access it — but code inside can still change it. "
                + "final field: can only be assigned once (in declaration or constructor), by any code that can access it. "
                + "private final: both restricted — common for immutable value objects."),

            mc("acm-mc-09", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "Can a subclass access a private field of its parent class?",
                "Yes, subclasses always inherit private fields",
                "Yes, but only via super.field",
                "No, private fields are invisible to subclasses",
                "Yes, but only if the subclass is in the same package",
                "c",
                "private is strictly class-scoped. Subclasses cannot directly see or modify private fields. "
                + "They can access them indirectly through public/protected getters and setters defined in the parent."),

            mc("acm-mc-10", Topic.PUBLIC_PRIVATE_PROTECTED, 3,
                "Which members does a subclass in the SAME package inherit and can access?",
                "Only public members",
                "Public and protected members",
                "Public, protected, and package-private members",
                "Public, protected, package-private, and private members",
                "c",
                "Within the same package: public, protected, and package-private (no-modifier) members are accessible. "
                + "In a different package: only public and protected are accessible to subclasses. "
                + "Private members are never inherited."),

            trace("acm-tr-03", Topic.PUBLIC_PRIVATE_PROTECTED, 3,
                "Does this compile? If not, why?",
                "class Animal {\n"
                + "    protected String sound = \"...\";\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    void bark() { System.out.println(sound); }\n"
                + "}",
                "Yes — compiles and prints the inherited protected field value",
                "Dog is a subclass of Animal, so it inherits the protected 'sound' field. "
                + "Accessing 'sound' inside Dog is valid. bark() prints whatever sound holds."),

            trace("acm-tr-04", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "What is printed?",
                "class Vehicle {\n"
                + "    public String brand = \"Ford\";\n"
                + "    protected int year = 2020;\n"
                + "    private double price = 30000;\n"
                + "}\n"
                + "// in same package:\n"
                + "Vehicle v = new Vehicle();\n"
                + "System.out.println(v.brand + \" \" + v.year);",
                "Ford 2020",
                "brand (public) and year (protected, same package) are both accessible from same-package code. "
                + "price (private) is not accessible. Output: Ford 2020."),

            debug("acm-db-02", Topic.PUBLIC_PRIVATE_PROTECTED, 3,
                "This code is in a different package from Animal. Why won't it compile?",
                "// package com.other;\n"
                + "import com.example.Animal;\n"
                + "class Test {\n"
                + "    void check() {\n"
                + "        Animal a = new Animal();\n"
                + "        System.out.println(a.legs);  // legs is protected in Animal\n"
                + "    }\n"
                + "}",
                "Animal must be public to be imported",
                "protected members can only be accessed from a different package via a subclass, not from an unrelated class",
                "legs must be declared static to be accessed by Test",
                "System.out.println cannot print int from another package",
                "b",
                "Protected access from a different package is only allowed within a subclass context. "
                + "Test is unrelated to Animal — it is not a subclass. "
                + "Fix: either make legs public, or have Test extend Animal."),

            debug("acm-db-03", Topic.PUBLIC_PRIVATE_PROTECTED, 2,
                "Why does this Singleton compile but allow multiple instances?",
                "class Singleton {\n"
                + "    static Singleton instance;\n"
                + "    public Singleton() { }  // bug here\n"
                + "    static Singleton getInstance() {\n"
                + "        if (instance == null) instance = new Singleton();\n"
                + "        return instance;\n"
                + "    }\n"
                + "}",
                "getInstance() is not declared public",
                "The instance field is not volatile",
                "The constructor is public — external code can call new Singleton() to create extra instances",
                "Static fields cannot store object references",
                "c",
                "A Singleton should have a private constructor so external code cannot use 'new'. "
                + "With a public constructor, the factory method is bypassed. "
                + "Fix: change 'public Singleton()' to 'private Singleton()'."),

            codegen("acm-cg-03", Topic.PUBLIC_PRIVATE_PROTECTED, 3,
                "Which correctly implements the Singleton pattern?",
                "class Config { public Config() { } static Config instance = new Config(); }",
                "class Config { private static Config instance; private Config() { } public static Config getInstance() { if (instance == null) instance = new Config(); return instance; } }",
                "class Config { protected Config() { } public static Config getInstance() { return new Config(); } }",
                "class Config { private Config instance = new Config(); public Config get() { return instance; } }",
                "b",
                "Singleton: private constructor (prevents external instantiation), "
                + "private static field (holds the single instance), "
                + "public static factory method (returns or creates the instance). "
                + "Option A: public constructor allows bypassing. "
                + "Option C: protected constructor still leaks. "
                + "Option D: instance is not static — each Config would have its own.")
        );
    }
}
