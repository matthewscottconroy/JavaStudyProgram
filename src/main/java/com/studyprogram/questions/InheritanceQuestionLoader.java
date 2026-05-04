package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class InheritanceQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.INHERITANCE; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("inh-mc-01", Topic.INHERITANCE, 1,
                "Which keyword makes class Dog inherit from class Animal?",
                "implements", "inherits", "extends", "derives",
                "c",
                "'extends' establishes an inheritance (is-a) relationship. "
                + "'implements' is used for interfaces. 'inherits' and 'derives' are not Java keywords."),

            mc("inh-mc-02", Topic.INHERITANCE, 2,
                "What does 'super()' do inside a subclass constructor?",
                "Calls the subclass's own constructor recursively",
                "Calls the parent class's constructor",
                "Creates a new instance of the parent class",
                "Prevents the parent constructor from running",
                "b",
                "super() must be the first statement in a subclass constructor (if used). "
                + "It invokes the parent class constructor to initialise inherited state. "
                + "If you omit it and the parent has no no-arg constructor, a compile error results."),

            mc("inh-mc-03", Topic.INHERITANCE, 2,
                "What does the @Override annotation do?",
                "Forces the method to be called first",
                "Tells the compiler to verify this method actually overrides a parent method",
                "Prevents subclasses from overriding this method",
                "Makes the method run on a separate thread",
                "b",
                "@Override asks the compiler to confirm the method signature matches a parent or interface method. "
                + "If you misspell the method name, the compiler reports an error instead of silently creating a new method. "
                + "It's a best practice to always use @Override when overriding."),

            mc("inh-mc-04", Topic.INHERITANCE, 2,
                "What does 'final' mean on a method?",
                "The method is called once then destroyed",
                "The method cannot be overridden in subclasses",
                "The method must be overridden in subclasses",
                "The method is thread-safe",
                "b",
                "A final method cannot be overridden by any subclass. "
                + "A final class cannot be extended at all. "
                + "A final variable cannot be reassigned after initialisation."),

            trace("inh-tr-01", Topic.INHERITANCE, 2,
                "What is printed?",
                "class Animal {\n"
                + "    String speak() { return \"...\"; }\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    @Override\n"
                + "    String speak() { return \"Woof\"; }\n"
                + "}\n"
                + "// in main:\n"
                + "Animal a = new Dog();\n"
                + "System.out.println(a.speak());",
                "Woof",
                "a is declared as Animal but holds a Dog object. "
                + "Dynamic dispatch calls Dog's overriding speak(), printing \"Woof\"."),

            debug("inh-db-01", Topic.INHERITANCE, 2,
                "This code has a compile error. Why?",
                "class Vehicle {\n"
                + "    Vehicle(String type) { System.out.println(type); }\n"
                + "}\n"
                + "class Car extends Vehicle {\n"
                + "    Car() { }\n"
                + "}",
                "Car cannot extend Vehicle because Vehicle has no interface",
                "Car() implicitly calls super() but Vehicle has no no-arg constructor",
                "Vehicle's constructor must be private",
                "Car needs @Override on its constructor",
                "b",
                "When Car() runs, Java implicitly inserts 'super()' as the first call. "
                + "But Vehicle has only a one-arg constructor — there is no no-arg version. "
                + "Fix: explicitly call super(\"sometype\") as the first line of Car()."),

            codegen("inh-cg-01", Topic.INHERITANCE, 2,
                "Which class correctly extends Shape and overrides area()?",
                "class Circle implements Shape { double area() { return 3.14 * r * r; } }",
                "class Circle extends Shape { @Override double area() { return 3.14 * r * r; } }",
                "class Circle extends Shape { double area; }",
                "class Circle : Shape { double area() { return 3.14 * r * r; } }",
                "b",
                "extends is the keyword for inheritance, and @Override confirms the method overrides area(). "
                + "Option A uses implements (for interfaces). "
                + "Option C declares a field instead of overriding the method. "
                + "Option D uses C# syntax (colon), not valid in Java."),

            mc("inh-mc-05", Topic.INHERITANCE, 2,
                "What does super.methodName() do inside a subclass method?",
                "Calls the method on a newly created parent object",
                "Calls the parent class's version of that method",
                "Makes the method private in the subclass",
                "Prevents the method from being overridden further",
                "b",
                "super.methodName() explicitly invokes the parent's implementation. "
                + "Useful when overriding: you can call super.draw() to run the parent drawing logic "
                + "and then add subclass-specific drawing on top."),

            trace("inh-tr-02", Topic.INHERITANCE, 3,
                "What is printed?",
                "class A {\n"
                + "    String msg() { return \"A\"; }\n"
                + "}\n"
                + "class B extends A {\n"
                + "    @Override\n"
                + "    String msg() { return super.msg() + \"B\"; }\n"
                + "}\n"
                + "class C extends B {\n"
                + "    @Override\n"
                + "    String msg() { return super.msg() + \"C\"; }\n"
                + "}\n"
                + "System.out.println(new C().msg());",
                "ABC",
                "C.msg() calls super.msg() (B.msg()) which calls super.msg() (A.msg()) = \"A\". "
                + "B appends \"B\" → \"AB\". C appends \"C\" → \"ABC\"."),

            mc("inh-mc-06", Topic.INHERITANCE, 3,
                "What is an abstract class?",
                "A class with no methods",
                "A class that cannot be instantiated and may have abstract (unimplemented) methods",
                "A class that all other classes extend",
                "A class whose methods are all private",
                "b",
                "An abstract class cannot be instantiated with new. "
                + "It may declare abstract methods (no body) that subclasses must implement. "
                + "It can also have concrete methods, fields, and constructors. "
                + "Use abstract classes for partial implementations shared across subclasses."),

            codegen("inh-cg-02", Topic.INHERITANCE, 3,
                "Which correctly calls the parent constructor from a subclass?",
                "class Truck extends Vehicle { Truck(int axles) { Vehicle(axles); } }",
                "class Truck extends Vehicle { Truck(int axles) { super(axles); } }",
                "class Truck extends Vehicle { Truck(int axles) { this.Vehicle(axles); } }",
                "class Truck extends Vehicle { Truck(int axles) { new Vehicle(axles); } }",
                "b",
                "super(args) invokes the parent constructor and must be the first statement. "
                + "Option A tries to call Vehicle() as a method (invalid). "
                + "Option C uses this.Vehicle() (invalid syntax). "
                + "Option D creates a new Vehicle object but doesn't initialize the Truck properly."),

            mc("inh-mc-07", Topic.INHERITANCE, 3,
                "What is method hiding (not overriding) in Java?",
                "A subclass declares a static method with the same name as a parent static method",
                "A subclass makes a parent method private",
                "A subclass renames an inherited method",
                "A final method in the parent class",
                "a",
                "Static methods are class-level, not virtual. A subclass declaring a static method with the same signature "
                + "'hides' the parent's version rather than overriding it. "
                + "The version called depends on the reference type, not the object type (no dynamic dispatch)."),

            mc("inh-mc-08", Topic.INHERITANCE, 2,
                "Which is NOT inherited by a subclass?",
                "Public methods", "Protected fields",
                "Private constructors and private fields", "Default (package-private) methods in the same package",
                "c",
                "Private members exist in the parent class's objects but are not directly accessible from the subclass. "
                + "Constructors are never inherited (though super() calls the parent's). "
                + "Public, protected, and same-package members are inherited."),

            mc("inh-mc-09", Topic.INHERITANCE, 2,
                "What is the correct way to prevent a class from being subclassed?",
                "Declare all methods private",
                "Use the 'sealed' or 'final' keyword on the class",
                "Make the class abstract",
                "Remove the default constructor",
                "b",
                "'final class MyClass { }' prevents any class from extending it. "
                + "Java 15+ 'sealed class' restricts which specific classes may extend it. "
                + "Abstract classes actively want to be extended. Making methods private doesn't prevent subclassing."),

            trace("inh-tr-03", Topic.INHERITANCE, 3,
                "What is printed?",
                "class Person {\n"
                + "    String name;\n"
                + "    Person(String n) { name = n; }\n"
                + "    String describe() { return \"Person: \" + name; }\n"
                + "}\n"
                + "class Student extends Person {\n"
                + "    int grade;\n"
                + "    Student(String n, int g) { super(n); grade = g; }\n"
                + "    @Override String describe() { return super.describe() + \", Grade: \" + grade; }\n"
                + "}\n"
                + "System.out.println(new Student(\"Ana\", 10).describe());",
                "Person: Ana, Grade: 10",
                "super(n) calls Person(String). super.describe() returns \"Person: Ana\". "
                + "Student appends \", Grade: 10\"."),

            trace("inh-tr-04", Topic.INHERITANCE, 2,
                "What is printed?",
                "class A { int x = 1; }\n"
                + "class B extends A { int x = 2; }\n"
                + "B obj = new B();\n"
                + "A ref = obj;\n"
                + "System.out.println(obj.x + \" \" + ref.x);",
                "2 1",
                "Fields are NOT polymorphic — they use the declared reference type. "
                + "obj.x uses B's x = 2. ref.x uses A's x = 1 (ref is declared as A). "
                + "This is field hiding, distinct from method overriding."),

            debug("inh-db-02", Topic.INHERITANCE, 3,
                "This code throws NullPointerException. Why?",
                "class Animal {\n"
                + "    Animal() { speak(); }  // calls overridable method in constructor\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    String sound = \"Woof\";\n"
                + "    void speak() { System.out.println(sound); }  // sound is null here!\n"
                + "}\n"
                + "new Dog();",
                "speak() is not accessible from Animal's constructor",
                "Animal's constructor calls speak() before Dog's field 'sound' is initialized — sound is still null",
                "Dog must call super() explicitly to fix this",
                "speak() throws NullPointerException because it returns void",
                "b",
                "Object construction order: 1) parent constructor runs (Animal()); 2) parent constructor calls speak(). "
                + "At this point, Dog's field 'sound' hasn't been set yet — it's null. "
                + "Never call overridable methods from constructors; the subclass fields may not be ready."),

            debug("inh-db-03", Topic.INHERITANCE, 2,
                "This code has a compile error. Why?",
                "class Vehicle {\n"
                + "    private int speed;\n"
                + "    Vehicle(int speed) { this.speed = speed; }\n"
                + "}\n"
                + "class Car extends Vehicle {\n"
                + "    Car(int speed) { }\n"
                + "}",
                "Car cannot extend Vehicle because speed is private",
                "Car() must explicitly call super(speed) since Vehicle has no no-arg constructor",
                "Constructors are not inherited, so Car() cannot exist",
                "speed must be protected, not private",
                "b",
                "Vehicle only defines a one-arg constructor. "
                + "Java inserts super() into Car() if no super call is explicit, but there is no no-arg Vehicle(). "
                + "Fix: Car(int speed) { super(speed); }"),

            codegen("inh-cg-03", Topic.INHERITANCE, 3,
                "Which abstract class requires subclasses to implement calculatePay()?",
                "class Employee { void calculatePay() { } }",
                "abstract class Employee { abstract double calculatePay(); }",
                "interface Employee { double calculatePay(); }",
                "final class Employee { double calculatePay() { return 0; } }",
                "b",
                "abstract class with abstract method: subclasses must implement calculatePay() or be abstract themselves. "
                + "Option A is a concrete class — subclasses can but don't have to override. "
                + "Option C is an interface (valid but not a class). "
                + "Option D is final — cannot be extended.")
        );
    }
}
