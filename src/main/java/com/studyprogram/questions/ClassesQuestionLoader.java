package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ClassesQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.CLASSES; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("cls-mc-01", Topic.CLASSES, 1,
                "What is a constructor?",
                "A method that destroys an object",
                "A special method called automatically when an object is created",
                "A keyword that defines a class",
                "A method that always returns void",
                "b",
                "A constructor initializes a new object. It has the same name as the class "
                + "and no return type (not even void). It is called via the 'new' keyword."),

            mc("cls-mc-02", Topic.CLASSES, 2,
                "What does 'this' refer to inside an instance method?",
                "The class itself",
                "The current object instance",
                "The parent class",
                "The method's return value",
                "b",
                "'this' is a reference to the current object—the instance on which the method is called. "
                + "It is commonly used to distinguish instance fields from parameters with the same name."),

            mc("cls-mc-03", Topic.CLASSES, 2,
                "What is the output?\n\n"
                + "    class Dog {\n"
                + "        String name;\n"
                + "        Dog(String name) { this.name = name; }\n"
                + "    }\n"
                + "    // in main:\n"
                + "    Dog d = new Dog(\"Rex\");\n"
                + "    System.out.println(d.name);",
                "name", "this.name", "Rex", "null",
                "c",
                "The constructor assigns the parameter 'name' to the field this.name. "
                + "d.name accesses the field, which holds \"Rex\"."),

            mc("cls-mc-04", Topic.CLASSES, 3,
                "How many times is the constructor called in this code?\n\n"
                + "    Dog a = new Dog(\"Rex\");\n"
                + "    Dog b = a;\n"
                + "    Dog c = new Dog(\"Fido\");",
                "3", "2", "1", "0",
                "b",
                "new Dog(...) calls the constructor. 'Dog b = a;' copies the reference, not the object. "
                + "So the constructor is called exactly twice (for 'a' and 'c')."),

            trace("cls-tr-01", Topic.CLASSES, 2,
                "What is printed?",
                "class Counter {\n    int count = 0;\n    void increment() { count++; }\n}\n"
                + "// in main:\nCounter c = new Counter();\nc.increment();\nc.increment();\nSystem.out.println(c.count);",
                "2",
                "count starts at 0. Each increment() call increases it by 1. After two calls, count = 2."),

            debug("cls-db-01", Topic.CLASSES, 2,
                "What is wrong with this class?",
                "class Cat {\n    String name;\n    void Cat(String name) {\n        this.name = name;\n    }\n}",
                "Constructors cannot have parameters",
                "Constructors cannot use 'this'",
                "Constructors must not have a return type — 'void' makes it a regular method, not a constructor",
                "String fields must be initialized to \"\"",
                "c",
                "Adding 'void' makes Cat(String name) a regular method, not a constructor. "
                + "Constructors have no return type at all. "
                + "As written, there is no constructor, so the compiler provides a default no-arg one."),

            codegen("cls-cg-01", Topic.CLASSES, 2,
                "Which code correctly defines a class Person with a String field 'name' "
                + "and a constructor that sets it?",
                "class Person { String name; Person(String n) { name = n; } }",
                "class Person { String name; void Person(String n) { name = n; } }",
                "class Person { constructor(String n) { name = n; } }",
                "Person class { String name; Person(name) { this.name = name; } }",
                "a",
                "Option A is correct. Option B has 'void' on the constructor. "
                + "Option C uses JavaScript syntax. Option D has 'Person class' and untyped parameter."),

            mc("cls-mc-05", Topic.CLASSES, 2,
                "What is method overloading?",
                "A subclass overriding a parent method",
                "Defining multiple methods with the same name but different parameter lists in the same class",
                "A method that calls itself recursively",
                "Making a method private to prevent external access",
                "b",
                "Overloading allows multiple methods with the same name as long as their parameter types or counts differ. "
                + "Example: print(int n) and print(String s) in the same class. "
                + "The compiler selects the right version based on the argument types at the call site."),

            trace("cls-tr-02", Topic.CLASSES, 2,
                "What is printed?",
                "class Box {\n"
                + "    int width, height;\n"
                + "    Box(int w, int h) { width = w; height = h; }\n"
                + "    int area() { return width * height; }\n"
                + "}\n"
                + "Box b = new Box(4, 5);\n"
                + "System.out.println(b.area());",
                "20",
                "Constructor sets width=4, height=5. area() returns 4*5 = 20."),

            mc("cls-mc-06", Topic.CLASSES, 3,
                "What is a getter method?",
                "A method that deletes a field value",
                "A public method that returns the value of a private field",
                "A constructor with no parameters",
                "A static method that creates new instances",
                "b",
                "Getters (e.g., getName()) provide controlled read access to private fields. "
                + "Setters (e.g., setName(String n)) provide controlled write access. "
                + "Together they enforce encapsulation — external code cannot directly access or mutate the field."),

            codegen("cls-cg-02", Topic.CLASSES, 3,
                "Which correctly adds a getter and setter for a private field 'score'?",
                "public int score; public int getScore() { return score; }",
                "private int score; public int getScore() { return score; } public void setScore(int s) { score = s; }",
                "private int score; int get() { return score; } void set(int s) { score = s; }",
                "private int score; public score getScore() { return this; }",
                "b",
                "Private field, public getter returning the field, public setter assigning to it. "
                + "Option A makes the field public (no encapsulation). "
                + "Option C uses non-standard names (Java convention: getX/setX). "
                + "Option D has invalid return type."),

            mc("cls-mc-07", Topic.CLASSES, 2,
                "What is a static field?",
                "A field declared with the 'final' keyword",
                "A field shared by all instances of the class — only one copy exists in memory",
                "A field that cannot be modified",
                "A field that is not visible outside the class",
                "b",
                "Static fields (class variables) belong to the class, not any specific instance. "
                + "All objects share the same static field. "
                + "Example: static int instanceCount = 0; — a common pattern to count created objects."),

            mc("cls-mc-08", Topic.CLASSES, 2,
                "What is the difference between a static method and an instance method?",
                "Static methods are faster; instance methods are thread-safe",
                "Static methods belong to the class and can be called without an object; instance methods require an object",
                "Instance methods can access static fields; static methods cannot",
                "Static methods must return void",
                "b",
                "Math.sqrt(4) — Math.sqrt is static, called on the class. "
                + "list.size() — size() is an instance method called on a specific ArrayList object. "
                + "Static methods cannot use 'this' or access non-static fields directly."),

            mc("cls-mc-09", Topic.CLASSES, 3,
                "What is a no-argument constructor?",
                "A constructor that takes no parameters",
                "A constructor that returns null",
                "The constructor the compiler always generates",
                "A private constructor that prevents instantiation",
                "a",
                "A no-arg constructor takes no parameters: public Dog() { ... }. "
                + "Java provides a default no-arg constructor only if you define no constructors at all. "
                + "If you define any constructor, you must explicitly add a no-arg constructor if you need one."),

            mc("cls-mc-10", Topic.CLASSES, 3,
                "What does the 'static' keyword mean in: public static void main(String[] args)?",
                "main() can be called without instantiating the class",
                "main() is automatically called when any class is compiled",
                "main() can access private fields",
                "main() runs on a separate thread",
                "a",
                "static means the JVM can call main() on the class itself, without creating an instance first. "
                + "That's necessary because no object exists yet when the program starts. "
                + "args contains the command-line arguments passed to the program."),

            mc("cls-mc-11", Topic.CLASSES, 3,
                "What is encapsulation?",
                "Inheriting behavior from a parent class",
                "Hiding the internal state of an object and exposing it only through public methods",
                "Writing methods with multiple overloads",
                "Making all fields public for easy access",
                "b",
                "Encapsulation bundles data (fields) and behavior (methods) together and controls access. "
                + "Private fields with public getters/setters enforce: the class controls how its state is read and changed. "
                + "This prevents external code from putting the object into an invalid state."),

            trace("cls-tr-03", Topic.CLASSES, 3,
                "What is printed?",
                "class Counter {\n"
                + "    static int count = 0;\n"
                + "    Counter() { count++; }\n"
                + "}\n"
                + "new Counter(); new Counter(); new Counter();\n"
                + "System.out.println(Counter.count);",
                "3",
                "Each new Counter() increments the static field count. Static fields are shared — "
                + "after 3 objects created, count = 3. Access via the class name: Counter.count."),

            trace("cls-tr-04", Topic.CLASSES, 2,
                "What is the output?",
                "class Point {\n"
                + "    int x, y;\n"
                + "    Point(int x, int y) { this.x = x; this.y = y; }\n"
                + "}\n"
                + "Point p = new Point(3, 7);\n"
                + "System.out.println(p.x + \",\" + p.y);",
                "3,7",
                "Constructor sets x=3, y=7 using 'this' to distinguish fields from parameters. "
                + "p.x + \",\" + p.y → \"3,7\"."),

            debug("cls-db-02", Topic.CLASSES, 2,
                "The code fails to compile with 'non-static variable count cannot be referenced from a static context'. Why?",
                "class Counter {\n"
                + "    int count = 0;\n"
                + "    public static void main(String[] args) {\n"
                + "        count++;  // error\n"
                + "    }\n"
                + "}",
                "count must be declared public",
                "main() is static and cannot directly access instance field 'count' — an instance of Counter is needed",
                "count++ is not valid for int fields",
                "main() cannot be in the same class as count",
                "b",
                "Static context (main) has no 'this'. count is an instance field that belongs to an object. "
                + "Fix: either make count static, or create Counter c = new Counter(); c.count++;"),

            debug("cls-db-03", Topic.CLASSES, 3,
                "Two Dog objects that seem equal fail equals() check. Why?",
                "class Dog {\n"
                + "    String name;\n"
                + "    Dog(String name) { this.name = name; }\n"
                + "}\n"
                + "Dog a = new Dog(\"Rex\");\n"
                + "Dog b = new Dog(\"Rex\");\n"
                + "System.out.println(a.equals(b));  // false",
                "String comparison fails inside Dog",
                "Dog does not override equals() — the default Object.equals() checks reference identity, not field values",
                "name must be static to be comparable",
                "new Dog() creates cached objects that are different",
                "b",
                "Without overriding equals(), Dog inherits Object.equals() which uses ==. "
                + "a and b are different objects, so == is false. "
                + "Fix: override equals() to compare name fields: return other instanceof Dog d && this.name.equals(d.name);"),

            codegen("cls-cg-03", Topic.CLASSES, 2,
                "Which creates a class Temperature with a double 'celsius' field, a constructor, and a method toCelsius()?",
                "class Temperature { celsius; Temperature(celsius) { this.celsius = celsius; } double toCelsius() { return celsius; } }",
                "class Temperature { double celsius; Temperature(double c) { this.celsius = c; } double toCelsius() { return celsius; } }",
                "class Temperature { double celsius; } Temperature(double c) { celsius = c; }",
                "Temperature class { double celsius; init(double c) { celsius = c; } }",
                "b",
                "Option B: field with type, constructor assigning to the field, method returning the field. "
                + "Option A: fields without types are not valid Java. "
                + "Option C: constructor is outside the class body."),

            codegen("cls-cg-04", Topic.CLASSES, 3,
                "Which correctly defines a static factory method for a class Circle?",
                "Circle c = Circle.create(5.0);",
                "class Circle { double radius; private Circle(double r) { radius = r; } public static Circle withRadius(double r) { return new Circle(r); } }",
                "static Circle new(double r) { return Circle(r); }",
                "class Circle { public Circle factory(double r) { return new Circle(r); } }",
                "b",
                "A static factory method: private constructor (prevents direct new), "
                + "public static method that creates and returns the object. "
                + "This pattern gives meaningful names (withRadius vs just 'new') and can return cached instances. "
                + "Option D: factory is an instance method — needs an existing Circle to call it, defeating the purpose.")
        );
    }
}
