package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class CompositionQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.COMPOSITION; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("cpn-mc-01", Topic.COMPOSITION, 1,
                "Which phrase describes a composition (has-a) relationship?",
                "A Car IS-A Vehicle",
                "A Car HAS-A Engine",
                "A SportsCar IS-A Car",
                "A Dog IS-A Animal",
                "b",
                "Composition models a HAS-A relationship: a Car has-an Engine. "
                + "IS-A relationships (Car is-a Vehicle) are modelled with inheritance (extends)."),

            mc("cpn-mc-02", Topic.COMPOSITION, 2,
                "Why is composition often preferred over inheritance?",
                "Composition is faster at runtime",
                "Composition avoids the fragile base-class problem and allows behaviour to change at runtime",
                "Composition uses less memory",
                "Composition is required by the Java specification",
                "b",
                "Inheritance tightly couples a subclass to its parent — changes to the base class can break subclasses. "
                + "Composition lets you swap implementations (e.g. different Engine types) without changing the Car class, "
                + "and avoids deep inheritance hierarchies."),

            mc("cpn-mc-03", Topic.COMPOSITION, 2,
                "In delegation, when class A delegates to class B…",
                "A extends B",
                "A contains a reference to B and calls B's methods to handle requests",
                "B calls A's methods internally",
                "A and B implement the same interface",
                "b",
                "Delegation: the delegating class (A) holds a reference to the delegate (B) and forwards "
                + "method calls to it. A doesn't inherit from B — it uses B as a helper."),

            trace("cpn-tr-01", Topic.COMPOSITION, 2,
                "What is printed?",
                "class Engine {\n"
                + "    String sound() { return \"vroom\"; }\n"
                + "}\n"
                + "class Car {\n"
                + "    private Engine engine = new Engine();\n"
                + "    String drive() { return \"Driving: \" + engine.sound(); }\n"
                + "}\n"
                + "// in main:\n"
                + "System.out.println(new Car().drive());",
                "Driving: vroom",
                "Car delegates the sound to its Engine via composition. "
                + "drive() calls engine.sound() which returns \"vroom\"."),

            debug("cpn-db-01", Topic.COMPOSITION, 3,
                "Why is this design fragile?",
                "class Logger {\n"
                + "    void log(String msg) { System.out.println(msg); }\n"
                + "}\n"
                + "class Service extends Logger {\n"
                + "    void process(String data) { log(\"Processing: \" + data); }\n"
                + "}",
                "Service cannot call log() because it is package-private",
                "Service inherits Logger, tightly coupling them — any change to Logger affects Service",
                "extends creates a circular dependency",
                "Logger must be abstract to be extended",
                "b",
                "Service doesn't need to BE a Logger — it only needs to USE one. "
                + "The inheritance creates a tight coupling. "
                + "Prefer: 'class Service { private Logger logger; }' and inject it."),

            codegen("cpn-cg-01", Topic.COMPOSITION, 2,
                "Which design correctly uses composition to give a Robot a Speaker?",
                "class Robot extends Speaker { void speak() { say(\"Hi\"); } }",
                "class Robot { private Speaker speaker; Robot(Speaker s) { speaker = s; } void speak() { speaker.say(\"Hi\"); } }",
                "class Robot { static Speaker speaker = new Speaker(); }",
                "class Robot implements Speaker { void say(String s) {} }",
                "b",
                "Option B injects a Speaker via the constructor (dependency injection) and delegates say() to it. "
                + "Option A uses inheritance (is-a, not has-a). "
                + "Option C uses a static field (global state, not composition). "
                + "Option D implements Speaker (makes Robot a Speaker, not a has-a relationship)."),

            mc("cpn-mc-04", Topic.COMPOSITION, 3,
                "What is dependency injection?",
                "A compiler feature that automatically creates object fields",
                "Providing an object's dependencies from outside rather than having the object create them itself",
                "A way to inject code at runtime using reflection",
                "A pattern where one class inherits from multiple parents",
                "b",
                "DI: instead of 'class Car { Engine e = new Engine(); }', write 'class Car { Engine e; Car(Engine e) { this.e = e; } }'. "
                + "The caller decides which Engine to use. This makes Car testable (pass a mock Engine), "
                + "configurable (pass a diesel or electric Engine), and loosely coupled."),

            trace("cpn-tr-02", Topic.COMPOSITION, 3,
                "What is printed?",
                "class Printer { void print(String s) { System.out.println(\"[\" + s + \"]\"); } }\n"
                + "class Report {\n"
                + "    private Printer printer;\n"
                + "    Report(Printer p) { this.printer = p; }\n"
                + "    void generate() { printer.print(\"Annual Report\"); }\n"
                + "}\n"
                + "new Report(new Printer()).generate();",
                "[Annual Report]",
                "Report delegates printing to its Printer. printer.print(\"Annual Report\") → System.out.println(\"[Annual Report]\")."),

            codegen("cpn-cg-02", Topic.COMPOSITION, 3,
                "Given: class Animal { void breathe(){} } / class Dog extends Animal { void bark(){} }\n"
                + "Which refactors Dog to use composition instead of inheritance?",
                "class Dog extends Animal, Breathing { }",
                "class Dog { private Animal animal = new Animal(); void bark() { System.out.println(\"woof\"); } void breathe() { animal.breathe(); } }",
                "class Dog implements Animal { void breathe() { } }",
                "abstract class Dog { abstract void bark(); }",
                "b",
                "Instead of extending Animal, Dog holds an Animal reference and delegates breathe() to it. "
                + "This is composition + delegation. Dog no longer IS-A Animal — it uses Animal's behavior. "
                + "Option A: Java doesn't support multiple class inheritance. "
                + "Option C: implements requires an interface, not a class."),

            mc("cpn-mc-05", Topic.COMPOSITION, 2,
                "What is the Adapter pattern?",
                "A pattern that adds new methods to a class without modifying it",
                "A wrapper class that translates one interface into another so incompatible classes can work together",
                "A class that creates objects for you",
                "A pattern that restricts instantiation to one object",
                "b",
                "Adapter wraps an existing class to match a required interface. "
                + "Example: wrapping a legacy Logger with a new LoggingService interface so new code can use either. "
                + "The adapter translates calls from the new interface to the old one."),

            mc("cpn-mc-06", Topic.COMPOSITION, 2,
                "What is the Decorator pattern?",
                "A pattern for creating class hierarchies",
                "A pattern that adds responsibilities to an object dynamically by wrapping it",
                "A pattern that ensures only one instance exists",
                "A pattern for cloning objects",
                "b",
                "Decorator wraps an object in another object with the same interface, adding behavior. "
                + "Example: BufferedReader wraps FileReader — same Reader interface, but adds buffering. "
                + "You can stack decorators: logging + caching + compression all wrapping the same base object."),

            trace("cpn-tr-03", Topic.COMPOSITION, 2,
                "What is printed?",
                "class Greeter {\n"
                + "    String greet(String name) { return \"Hello, \" + name; }\n"
                + "}\n"
                + "class PoliteGreeter {\n"
                + "    private Greeter greeter = new Greeter();\n"
                + "    String greet(String name) { return greeter.greet(name) + \"!\"; }\n"
                + "}\n"
                + "System.out.println(new PoliteGreeter().greet(\"Alice\"));",
                "Hello, Alice!",
                "PoliteGreeter decorates Greeter's output by appending '!'. "
                + "greeter.greet(\"Alice\") returns \"Hello, Alice\"; + \"!\" gives \"Hello, Alice!\"."),

            trace("cpn-tr-04", Topic.COMPOSITION, 3,
                "What is printed?",
                "class Wheel { String type; Wheel(String t) { type = t; } }\n"
                + "class Bicycle {\n"
                + "    private Wheel front, rear;\n"
                + "    Bicycle(String f, String r) {\n"
                + "        front = new Wheel(f);\n"
                + "        rear = new Wheel(r);\n"
                + "    }\n"
                + "    String describe() { return front.type + \"+\" + rear.type; }\n"
                + "}\n"
                + "System.out.println(new Bicycle(\"road\", \"road\").describe());",
                "road+road",
                "Bicycle is composed of two Wheel objects. describe() concatenates both wheel types."),

            debug("cpn-db-02", Topic.COMPOSITION, 3,
                "Why is this design problematic?",
                "class Report {\n"
                + "    void generate() {\n"
                + "        Database db = new Database(\"prod-server\");\n"
                + "        List<Row> data = db.fetchAll();\n"
                + "        // format and print data\n"
                + "    }\n"
                + "}",
                "Database is not a standard Java class",
                "Report creates its own Database dependency internally — makes it untestable and hard to configure",
                "generate() should be static",
                "The method is too long",
                "b",
                "Hardcoding the Database inside generate() means you can't swap it for a test database or mock. "
                + "Fix: inject the Database: class Report { Report(Database db) { ... } void generate() { db.fetchAll(); } } "
                + "Now tests can pass a mock Database without touching a real server."),

            debug("cpn-db-03", Topic.COMPOSITION, 2,
                "This produces 'null' instead of the expected sound. Why?",
                "class Engine { String sound; }\n"
                + "class Car {\n"
                + "    private Engine engine;\n"
                + "    String drive() { return engine.sound; }\n"
                + "}\n"
                + "Car c = new Car();\n"
                + "System.out.println(c.drive());",
                "Engine.sound is private",
                "Car's engine field is never initialized — it is null, and engine.sound throws NullPointerException",
                "String fields default to empty string, not null",
                "drive() must be static",
                "b",
                "Car declares 'private Engine engine' but never assigns it. "
                + "engine is null. engine.sound throws NullPointerException (not null output). "
                + "Fix: private Engine engine = new Engine(); or accept one via the constructor."),

            codegen("cpn-cg-03", Topic.COMPOSITION, 3,
                "Which correctly applies the Decorator pattern to add logging to a DataService?",
                "class LoggingService extends DataService { @Override void save(String d) { log(d); super.save(d); } }",
                "interface DataService { void save(String d); }\n"
                + "class LoggingService implements DataService {\n"
                + "    private DataService delegate;\n"
                + "    LoggingService(DataService ds) { this.delegate = ds; }\n"
                + "    public void save(String d) { System.out.println(\"Saving: \" + d); delegate.save(d); }\n"
                + "}",
                "class DataService { static void log(String d) { System.out.println(d); } }",
                "class DataService { void save(String d) { } void logAndSave(String d) { log(); save(d); } }",
                "b",
                "Decorator: implements the same interface as the wrapped object, holds a reference to it, "
                + "and adds behavior before/after delegation. "
                + "Option A uses inheritance (extends) — valid but breaks if DataService is final or has many methods. "
                + "Option C adds a static method, not a decorator. "
                + "Option D modifies the original class instead of decorating it.")
        );
    }
}
