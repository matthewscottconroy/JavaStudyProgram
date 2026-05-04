package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ClassHierarchyQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.CLASS_HIERARCHY; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("clh-mc-01", Topic.CLASS_HIERARCHY, 1,
                "What does 'instanceof' check?",
                "Whether two objects are equal in value",
                "Whether an object's reference is null",
                "Whether an object is an instance of a given class or interface",
                "Whether a class has a specific method",
                "c",
                "'instanceof' returns true if the object is an instance of the specified class "
                + "or any of its subclasses. It also works with interfaces."),

            mc("clh-mc-02", Topic.CLASS_HIERARCHY, 2,
                "What is an abstract class?",
                "A class with no fields",
                "A class that cannot be instantiated and may have abstract methods",
                "A class whose constructor is private",
                "A class that implements all interface methods",
                "b",
                "An abstract class cannot be instantiated directly. "
                + "It may declare abstract methods (no body) that subclasses must implement. "
                + "It can also have concrete methods and fields."),

            mc("clh-mc-03", Topic.CLASS_HIERARCHY, 2,
                "Animal a = new Dog(); — assuming Dog extends Animal, which method is called?",
                "Animal's version always, because the variable type is Animal",
                "Dog's version, because the actual object is a Dog",
                "It won't compile because the variable type doesn't match the object",
                "An exception is thrown at runtime",
                "b",
                "Java uses dynamic dispatch: the method called is determined by the actual runtime type "
                + "(Dog), not the declared variable type (Animal). This is the foundation of polymorphism."),

            mc("clh-mc-04", Topic.CLASS_HIERARCHY, 3,
                "What exception is thrown by an invalid cast, e.g. '(Cat) new Dog()'?",
                "NullPointerException", "IllegalArgumentException",
                "ClassCastException", "ArrayIndexOutOfBoundsException",
                "c",
                "Casting an object to an incompatible type throws ClassCastException at runtime. "
                + "Use 'instanceof' before casting to check compatibility: "
                + "if (animal instanceof Dog d) { ... }"),

            trace("clh-tr-01", Topic.CLASS_HIERARCHY, 2,
                "What is printed?",
                "class Shape { String name() { return \"Shape\"; } }\n"
                + "class Circle extends Shape { String name() { return \"Circle\"; } }\n"
                + "// in main:\n"
                + "Shape s = new Circle();\n"
                + "System.out.println(s.name());\n"
                + "System.out.println(s instanceof Circle);",
                "Circle\ntrue",
                "Dynamic dispatch calls Circle's name() even though s is declared as Shape. "
                + "instanceof returns true because the actual object is a Circle."),

            debug("clh-db-01", Topic.CLASS_HIERARCHY, 3,
                "This code throws ClassCastException at runtime. Why?",
                "class Animal {}\n"
                + "class Dog extends Animal {}\n"
                + "class Cat extends Animal {}\n"
                + "// in main:\n"
                + "Animal a = new Dog();\n"
                + "Cat c = (Cat) a;",
                "Dog and Cat are not in the same package",
                "Animal must be abstract to be cast",
                "a holds a Dog object, which cannot be cast to Cat",
                "The cast requires an import statement",
                "c",
                "The actual object is a Dog. Casting it to Cat is invalid because Dog and Cat "
                + "are sibling classes — neither extends the other. "
                + "Check with 'a instanceof Cat' before casting."),

            codegen("clh-cg-01", Topic.CLASS_HIERARCHY, 3,
                "Which code safely casts 'a' to Dog only if it actually is one?",
                "Dog d = (Dog) a;",
                "if (a.getClass() == Dog.class) { Dog d = (Dog) a; d.bark(); }",
                "if (a instanceof Dog d) { d.bark(); }",
                "Dog d = a.cast(Dog.class);",
                "c",
                "Pattern matching instanceof (Java 16+) tests the type and binds in one step, "
                + "eliminating a redundant cast. "
                + "Option A throws ClassCastException if a is not a Dog. "
                + "Option B works but is more verbose. "
                + "Option D: cast() does not exist on Object."),

            mc("clh-mc-05", Topic.CLASS_HIERARCHY, 2,
                "An Animal reference points to a Dog object. You can call which methods on it?",
                "Only methods defined in Dog",
                "Only methods defined in Animal",
                "Methods from both Dog and Animal, but the compiler only sees Animal's interface",
                "No methods — you must cast first",
                "c",
                "The reference type (Animal) determines what the compiler allows you to call. "
                + "You can only call methods declared in Animal. "
                + "At runtime, if Dog overrides a method, the Dog version runs (dynamic dispatch). "
                + "Cast to Dog to access Dog-specific methods."),

            trace("clh-tr-02", Topic.CLASS_HIERARCHY, 3,
                "What is printed?",
                "class Shape { String name() { return \"Shape\"; } }\n"
                + "class Circle extends Shape { @Override String name() { return \"Circle\"; } }\n"
                + "Shape[] arr = { new Shape(), new Circle(), new Circle() };\n"
                + "for (Shape s : arr) System.out.print(s.name() + \" \");",
                "Shape Circle Circle",
                "Dynamic dispatch: the actual runtime type determines which name() runs. "
                + "new Shape() → \"Shape\". new Circle() → \"Circle\" (overridden). "
                + "Output: Shape Circle Circle (with trailing space)."),

            codegen("clh-cg-02", Topic.CLASS_HIERARCHY, 3,
                "Which correctly counts how many elements in a Shape[] are Circle instances?",
                "int count = 0; for (Shape s : shapes) if (s.name().equals(\"Circle\")) count++;",
                "int count = 0; for (Shape s : shapes) if (s instanceof Circle) count++;",
                "int count = (int) Arrays.stream(shapes).filter(Circle.class::isInstance).count();",
                "int count = shapes.length;",
                "b",
                "Both B and C work; B is the classic idiomatic approach. "
                + "instanceof checks if the object is of type Circle or a subclass. "
                + "Option A relies on the name string matching exactly — fragile if the name changes. "
                + "Option D counts all shapes, not just circles."),

            mc("clh-mc-06", Topic.CLASS_HIERARCHY, 2,
                "What does the @Override annotation do?",
                "Makes the method faster by bypassing virtual dispatch",
                "Tells the compiler you intend to override a parent method — errors if the method doesn't actually override anything",
                "Prevents subclasses from overriding the method",
                "Creates a new method that shadows the parent's method",
                "b",
                "@Override is a compile-time safety check. "
                + "If you misspell the method name or have the wrong parameters, the compiler reports an error "
                + "rather than silently creating a new overloaded method. Always use @Override when overriding."),

            mc("clh-mc-07", Topic.CLASS_HIERARCHY, 2,
                "What is the root of the Java class hierarchy?",
                "Serializable", "Comparable", "Object", "Base",
                "c",
                "Every class in Java (except Object itself) implicitly extends java.lang.Object. "
                + "Object provides: equals(), hashCode(), toString(), getClass(), clone(), wait(), notify()."),

            mc("clh-mc-08", Topic.CLASS_HIERARCHY, 3,
                "What does 'final' mean on a class?",
                "The class cannot have fields",
                "The class cannot be subclassed (extended)",
                "The class must implement all methods",
                "The class is loaded first by the JVM",
                "b",
                "A final class cannot be extended. Example: String is final. "
                + "This prevents inheritance-based attacks and ensures behavior is locked. "
                + "Methods can also be final (prevents overriding); fields can be final (assigned once)."),

            mc("clh-mc-09", Topic.CLASS_HIERARCHY, 3,
                "What is the output of: System.out.println(new Dog() instanceof Animal); — given Dog extends Animal?",
                "false", "true", "Depends on whether Dog overrides any method", "Compile error",
                "b",
                "instanceof checks the full type hierarchy. A Dog IS-AN Animal (by inheritance), "
                + "so instanceof returns true. Also true for all implemented interfaces."),

            trace("clh-tr-03", Topic.CLASS_HIERARCHY, 3,
                "What is printed?",
                "class A { String id() { return \"A\"; } }\n"
                + "class B extends A { @Override String id() { return \"B\"; } }\n"
                + "class C extends B { }\n"
                + "// in main:\n"
                + "A obj = new C();\n"
                + "System.out.println(obj.id());",
                "B",
                "C doesn't override id() — it inherits B's version. "
                + "Dynamic dispatch uses the actual type C, which resolves to B's id(). Output: B."),

            debug("clh-db-02", Topic.CLASS_HIERARCHY, 2,
                "This code does not compile. Why?",
                "abstract class Shape {\n"
                + "    abstract double area();\n"
                + "}\n"
                + "Shape s = new Shape();",
                "Shape has no constructor",
                "abstract classes cannot have abstract methods",
                "Abstract classes cannot be instantiated with 'new'",
                "area() must return int, not double",
                "c",
                "Abstract classes cannot be instantiated directly. "
                + "You must create a concrete subclass that implements all abstract methods: "
                + "class Circle extends Shape { double area() { return Math.PI * r * r; } }"),

            debug("clh-db-03", Topic.CLASS_HIERARCHY, 3,
                "This cast doesn't fail at compile time but causes ClassCastException at runtime. Why?",
                "class Animal {}\n"
                + "class Cat extends Animal {}\n"
                + "Animal a = new Animal();\n"
                + "Cat c = (Cat) a;  // runtime error",
                "Animal is not a subclass of Cat",
                "The compiler can't always verify that a runtime cast is safe — it only checks that the types are related",
                "Cat must override equals() for the cast to work",
                "Animal must implement Serializable to be cast",
                "b",
                "The compiler sees Animal→Cat: these are related (Cat extends Animal), so the cast is syntactically valid. "
                + "At runtime, the actual object is an Animal (not a Cat), so ClassCastException is thrown. "
                + "Use instanceof before casting to guard: if (a instanceof Cat c) { c.purr(); }"),

            codegen("clh-cg-03", Topic.CLASS_HIERARCHY, 2,
                "Which correctly defines an abstract class Vehicle with an abstract method fuelType()?",
                "interface Vehicle { String fuelType(); }",
                "abstract class Vehicle { abstract String fuelType(); }",
                "class Vehicle { void fuelType() { } }",
                "abstract class Vehicle { String fuelType() { return \"\"; } }",
                "b",
                "abstract class + abstract method: the method has no body; subclasses must implement it. "
                + "Option A is an interface, not an abstract class. "
                + "Option C is a concrete class — its fuelType() has an empty body but CAN be overridden. "
                + "Option D has a body — it's a concrete method inside an abstract class (legal but not abstract).")
        );
    }
}
