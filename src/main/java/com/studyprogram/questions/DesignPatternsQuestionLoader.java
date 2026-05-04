package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class DesignPatternsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.OO_DESIGN_PATTERNS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("dp-mc-01", Topic.OO_DESIGN_PATTERNS, 2,
                "The Singleton pattern ensures…",
                "A class has at most one subclass",
                "Only one instance of a class exists in the JVM",
                "All methods in a class are static",
                "A class cannot be instantiated at all",
                "b",
                "Singleton restricts instantiation so only one object of the class exists. "
                + "It provides a global access point via a static method (e.g. getInstance()). "
                + "Overuse of Singleton makes testing difficult."),

            mc("dp-mc-02", Topic.OO_DESIGN_PATTERNS, 2,
                "The Factory pattern is used to…",
                "Ensure only one object is created",
                "Decouple object creation from the code that uses the object",
                "Add behaviour to objects without modifying their class",
                "Notify observers when an object changes state",
                "b",
                "Factory methods or Factory classes create objects, hiding the exact class instantiated "
                + "from the caller. This allows swapping implementations without changing client code."),

            mc("dp-mc-03", Topic.OO_DESIGN_PATTERNS, 3,
                "The Observer pattern is best described as…",
                "An object wrapping another to add behaviour",
                "A one-to-many dependency where all dependents are notified of state changes",
                "A class that controls access to another object",
                "An algorithm family where each variant is encapsulated",
                "b",
                "Observer (also called publish-subscribe) lets multiple observers register on a subject. "
                + "When the subject changes, it notifies all observers automatically. "
                + "Used in event systems, GUI listeners, and reactive programming."),

            mc("dp-mc-04", Topic.OO_DESIGN_PATTERNS, 3,
                "The Strategy pattern allows…",
                "An algorithm to be selected at runtime by encapsulating it in a class",
                "A class to wrap another to add extra features",
                "Lazy initialisation of expensive objects",
                "Sharing state between many fine-grained objects",
                "a",
                "Strategy encapsulates a family of algorithms behind an interface. "
                + "The client chooses the algorithm at runtime by injecting the desired strategy object. "
                + "E.g. different sorting strategies for a Sorter class."),

            trace("dp-tr-01", Topic.OO_DESIGN_PATTERNS, 3,
                "What design pattern does this code use?",
                "class Config {\n"
                + "    private static Config instance;\n"
                + "    private Config() {}\n"
                + "    public static Config getInstance() {\n"
                + "        if (instance == null) instance = new Config();\n"
                + "        return instance;\n"
                + "    }\n"
                + "}",
                "Singleton",
                "Private constructor + static getInstance() that returns (or creates) one shared instance = Singleton. "
                + "Note: this basic form is not thread-safe; use double-checked locking or an enum for thread safety."),

            debug("dp-db-01", Topic.OO_DESIGN_PATTERNS, 4,
                "What is wrong with this Singleton in a multi-threaded environment?",
                "class Singleton {\n"
                + "    private static Singleton instance;\n"
                + "    private Singleton() {}\n"
                + "    public static Singleton getInstance() {\n"
                + "        if (instance == null) instance = new Singleton();\n"
                + "        return instance;\n"
                + "    }\n}",
                "The constructor is wrongly private",
                "Two threads can both see instance == null and create two separate instances",
                "static fields cannot hold object references",
                "getInstance must be non-static",
                "b",
                "Without synchronisation, two threads can simultaneously pass the null check and both create an instance. "
                + "Fix: synchronise the method, use double-checked locking with volatile, or use an enum singleton."),

            codegen("dp-cg-01", Topic.OO_DESIGN_PATTERNS, 3,
                "Which is the simplest thread-safe Singleton in Java?",
                "class S { public static S instance = new S(); }",
                "enum S { INSTANCE; }",
                "class S { private static S i; public static synchronized S get() { if(i==null)i=new S(); return i; } }",
                "class S { private S(){} public static S get() { return new S(); } }",
                "b",
                "An enum Singleton (Joshua Bloch's recommendation) is thread-safe by JVM guarantees, "
                + "serialisation-safe, and immune to reflection attacks. "
                + "Option A uses a public field (accessible without a method but not lazy). "
                + "Option C is correct but verbose. "
                + "Option D creates a new instance each call — not a Singleton."),

            mc("dp-mc-05", Topic.OO_DESIGN_PATTERNS, 3,
                "What problem does the Builder pattern solve?",
                "Creating only one instance of a class",
                "Telescoping constructor problem — classes with many optional fields require many constructor overloads",
                "Decoupling the interface from its implementation",
                "Notifying observers of state changes",
                "b",
                "Builder lets you construct an object step-by-step: Person.builder().name(\"Alice\").age(30).email(\"a@b.com\").build(). "
                + "This avoids constructors with many nullable parameters and makes the construction readable. "
                + "Lombok's @Builder generates this automatically."),

            mc("dp-mc-06", Topic.OO_DESIGN_PATTERNS, 3,
                "Which pattern wraps an object to add behavior without modifying its class?",
                "Singleton", "Factory", "Decorator", "Builder",
                "c",
                "Decorator wraps an object implementing the same interface and adds behavior. "
                + "Example: BufferedReader wraps a FileReader to add buffering. "
                + "Java I/O streams use Decorator extensively. "
                + "It's a flexible alternative to subclassing for extending behavior."),

            trace("dp-tr-02", Topic.OO_DESIGN_PATTERNS, 3,
                "Which pattern is demonstrated here?",
                "interface Sorter { void sort(int[] arr); }\n"
                + "class BubbleSorter implements Sorter { public void sort(int[] arr) { /* bubble sort */ } }\n"
                + "class QuickSorter implements Sorter { public void sort(int[] arr) { /* quick sort */ } }\n"
                + "class Context {\n"
                + "    private Sorter sorter;\n"
                + "    Context(Sorter s) { sorter = s; }\n"
                + "    void doSort(int[] arr) { sorter.sort(arr); }\n"
                + "}",
                "Strategy",
                "Strategy pattern: defines a family of algorithms (BubbleSorter, QuickSorter) behind a common interface, "
                + "and lets the client switch algorithms at runtime via the Context. "
                + "The algorithm is encapsulated and interchangeable."),

            codegen("dp-cg-02", Topic.OO_DESIGN_PATTERNS, 3,
                "Which implements the Observer pattern to notify listeners of a price change?",
                "class Stock { double price; void setPrice(double p) { price = p; } }",
                "class Stock { List<PriceListener> listeners = new ArrayList<>(); void addListener(PriceListener l) { listeners.add(l); } void setPrice(double p) { this.price = p; listeners.forEach(l -> l.onPriceChange(p)); } double price; }",
                "class Stock { static PriceListener listener; void setPrice(double p) { listener.onPriceChange(p); } }",
                "interface Stock { void setPrice(double p); }",
                "b",
                "Observer: the Subject (Stock) maintains a list of observers (PriceListener). "
                + "When state changes, it notifies all registered observers. "
                + "Option A changes state without notifying anyone. "
                + "Option C uses a static single observer (not a list, not extensible)."),

            mc("dp-mc-07", Topic.OO_DESIGN_PATTERNS, 3,
                "The Template Method pattern defines…",
                "A static method used as a factory",
                "The skeleton of an algorithm in a base class, with steps that can be overridden by subclasses",
                "A method that builds objects step by step",
                "A method that delegates behavior to a strategy object",
                "b",
                "Template Method: the abstract base class defines the algorithm steps in a final method. "
                + "Subclasses override specific steps (hooks) without changing the overall structure. "
                + "Example: a report generator where formatHeader() and formatBody() are overridable."),

            mc("dp-mc-08", Topic.OO_DESIGN_PATTERNS, 2,
                "What does the Proxy pattern do?",
                "Creates objects for the client",
                "Provides a surrogate or placeholder for another object to control access to it",
                "Converts one interface to another",
                "Shares objects to reduce memory",
                "b",
                "Proxy controls access to the real object. Common uses: "
                + "lazy loading (create real object only when first used), "
                + "access control, logging, caching. "
                + "Dynamic proxies in Java (java.lang.reflect.Proxy) create proxies at runtime."),

            mc("dp-mc-09", Topic.OO_DESIGN_PATTERNS, 3,
                "Which SOLID principle says: a class should have only one reason to change?",
                "Open/Closed Principle", "Single Responsibility Principle",
                "Liskov Substitution Principle", "Interface Segregation Principle",
                "b",
                "SRP: each class should do one thing and have one reason to change. "
                + "A class mixing business logic with file I/O violates SRP. "
                + "Split it into a data class and a file handler."),

            mc("dp-mc-10", Topic.OO_DESIGN_PATTERNS, 3,
                "The Open/Closed Principle states that software entities should be…",
                "Open for modification, closed for extension",
                "Open for extension, closed for modification",
                "Open to all packages, closed to reflection",
                "Closed at compile time, open at runtime",
                "b",
                "OCP: you should be able to add new behavior by adding new code (extension), "
                + "not by changing existing code (modification). "
                + "Example: use Strategy pattern so new algorithms can be added without touching the Context class."),

            trace("dp-tr-03", Topic.OO_DESIGN_PATTERNS, 3,
                "What pattern does this demonstrate?",
                "abstract class Game {\n"
                + "    final void play() { setup(); turns(); teardown(); }\n"
                + "    abstract void setup();\n"
                + "    abstract void turns();\n"
                + "    void teardown() { System.out.println(\"Game over\"); }\n"
                + "}",
                "Template Method",
                "play() defines the algorithm skeleton (setup → turns → teardown) and is declared final to prevent override. "
                + "Subclasses implement setup() and turns() — the variable steps — without changing the flow."),

            debug("dp-db-02", Topic.OO_DESIGN_PATTERNS, 3,
                "This violates the Single Responsibility Principle. Why?",
                "class Report {\n"
                + "    void generateContent() { /* business logic */ }\n"
                + "    void saveToFile(String path) { /* file I/O */ }\n"
                + "    void sendByEmail(String addr) { /* networking */ }\n"
                + "}",
                "Report has too many methods",
                "Report has three separate responsibilities (content, persistence, delivery) — three reasons to change",
                "generateContent() and saveToFile() should be static",
                "The class mixes public and private methods",
                "b",
                "SRP: Report changes if the content format changes, if the file system changes, or if the email system changes. "
                + "Fix: split into Report (content), ReportWriter (file I/O), and ReportMailer (email). "
                + "Each class has one reason to change."),

            debug("dp-db-03", Topic.OO_DESIGN_PATTERNS, 3,
                "This violates the Open/Closed Principle. Why?",
                "class AreaCalculator {\n"
                + "    double area(Object shape) {\n"
                + "        if (shape instanceof Circle c) return Math.PI * c.r * c.r;\n"
                + "        if (shape instanceof Rect r) return r.w * r.h;\n"
                + "        return 0;\n"
                + "    }\n"
                + "}",
                "instanceof is not allowed with generics",
                "Adding a new shape requires modifying AreaCalculator — the class must be changed for extension",
                "The method should be static",
                "Circle and Rect must extend AreaCalculator",
                "b",
                "Every new shape type requires modifying AreaCalculator's if-chain. "
                + "Fix: use polymorphism — define an interface 'interface Shape { double area(); }'. "
                + "Each shape implements area(). AreaCalculator just calls shape.area() — no changes needed for new shapes."),

            codegen("dp-cg-03", Topic.OO_DESIGN_PATTERNS, 3,
                "Which implements a Builder for a Pizza class with fields 'size' and 'crust'?",
                "class Pizza { String size, crust; Pizza(String size, String crust) { this.size=size; this.crust=crust; } }",
                "class Pizza {\n"
                + "    String size, crust;\n"
                + "    private Pizza() {}\n"
                + "    static class Builder {\n"
                + "        String size, crust;\n"
                + "        Builder size(String s) { this.size=s; return this; }\n"
                + "        Builder crust(String c) { this.crust=c; return this; }\n"
                + "        Pizza build() { Pizza p=new Pizza(); p.size=size; p.crust=crust; return p; }\n"
                + "    }\n"
                + "}",
                "class Pizza { void setSize(String s) {} void setCrust(String c) {} }",
                "interface PizzaBuilder { Pizza build(String size, String crust); }",
                "b",
                "Builder pattern: static inner Builder class with fluent setter methods (return this), "
                + "and a build() that creates the final object. "
                + "Usage: new Pizza.Builder().size(\"large\").crust(\"thin\").build(). "
                + "Option A is just a constructor — no builder.")
        );
    }
}
