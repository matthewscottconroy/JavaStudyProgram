package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class InheritancePolymorphismQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ipm-mc-01", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 2,
                "What is dynamic dispatch (runtime polymorphism)?",
                "The compiler chooses which method to call based on the variable's declared type",
                "The JVM chooses which method to call based on the actual object type at runtime",
                "Methods are dispatched to multiple threads simultaneously",
                "Only abstract methods can be dispatched at runtime",
                "b",
                "In Java, method calls on an object are resolved at runtime based on the actual type, "
                + "not the declared type of the variable. This lets you write code against base-type variables "
                + "and have the right subclass behaviour execute automatically."),

            mc("ipm-mc-02", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "The Liskov Substitution Principle (LSP) states that…",
                "Every subclass must override all parent methods",
                "Subtypes must be substitutable for their base types without breaking program correctness",
                "A class should have only one reason to change",
                "Interfaces should be small and focused",
                "b",
                "LSP: wherever you use a base type, a subtype should work correctly in its place. "
                + "A Square that extends Rectangle violates LSP if setting width independently breaks the area contract. "
                + "LSP violations cause surprising bugs when polymorphism is used."),

            mc("ipm-mc-03", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 2,
                "Encapsulation means…",
                "A class can only have private fields",
                "All methods must be static",
                "Hiding internal state and requiring interaction through a public interface",
                "Only one class is allowed per file",
                "c",
                "Encapsulation bundles data and the methods that operate on it, hiding implementation details. "
                + "Private fields + public getters/setters is the classic Java pattern. "
                + "It protects invariants and lets you change the implementation without affecting callers."),

            mc("ipm-mc-04", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "Which scenario violates encapsulation?",
                "A class with private fields and public getters/setters",
                "A class that returns a reference to its internal mutable list from a getter",
                "A class that validates input in its setter",
                "A class with a package-private helper method",
                "b",
                "Returning a reference to an internal mutable list lets callers modify it directly, "
                + "bypassing any validation in the class. "
                + "Fix: return a defensive copy — Collections.unmodifiableList(internalList) or new ArrayList<>(internalList)."),

            trace("ipm-tr-01", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "What is printed?",
                "class Payment {\n"
                + "    String process() { return \"base\"; }\n"
                + "}\n"
                + "class CreditCard extends Payment {\n"
                + "    String process() { return \"credit\"; }\n"
                + "}\n"
                + "class PayPal extends Payment {\n"
                + "    String process() { return \"paypal\"; }\n"
                + "}\n"
                + "// in main:\n"
                + "Payment[] payments = { new CreditCard(), new PayPal(), new CreditCard() };\n"
                + "for (Payment p : payments) System.out.println(p.process());",
                "credit\npaypal\ncredit",
                "Each element's actual type (CreditCard or PayPal) determines which process() is called. "
                + "This is polymorphism: the same call produces different behaviour based on the real object."),

            debug("ipm-db-01", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "Why does this Rectangle/Square design violate LSP?",
                "class Rectangle {\n"
                + "    int width, height;\n"
                + "    void setWidth(int w) { width = w; }\n"
                + "    void setHeight(int h) { height = h; }\n"
                + "    int area() { return width * height; }\n"
                + "}\n"
                + "class Square extends Rectangle {\n"
                + "    void setWidth(int w) { width = height = w; }\n"
                + "    void setHeight(int h) { width = height = h; }\n"
                + "}",
                "Square cannot extend Rectangle in Java",
                "Square overrides setWidth/setHeight, breaking code that expects them to be independent",
                "area() returns wrong values for Square",
                "Square must also override area()",
                "b",
                "Code written for Rectangle assumes setting width doesn't change height. "
                + "Square violates that contract. A method expecting a Rectangle may compute the wrong area "
                + "when a Square is substituted. LSP requires subtypes to honour the contracts of their supertypes."),

            codegen("ipm-cg-01", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "Which design correctly encapsulates a list of items?",
                "class Inventory { public List<String> items = new ArrayList<>(); }",
                "class Inventory { private List<String> items = new ArrayList<>(); public List<String> getItems() { return items; } }",
                "class Inventory { private List<String> items = new ArrayList<>(); public List<String> getItems() { return Collections.unmodifiableList(items); } public void addItem(String s) { items.add(s); } }",
                "class Inventory { protected List<String> items; }",
                "c",
                "Option C encapsulates: the list is private, callers get an unmodifiable view (no backdoor), "
                + "and modification goes through addItem() which can enforce invariants. "
                + "Options A and D expose the list directly. "
                + "Option B lets callers mutate the internal list via the returned reference."),

            mc("ipm-mc-05", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 2,
                "What is polymorphism?",
                "A class that can have only one form",
                "The ability of different objects to respond to the same method call in different ways",
                "Multiple constructors in one class",
                "A variable that can hold only one type",
                "b",
                "Polymorphism: one interface, many forms. "
                + "A collection of Shape objects can each respond to draw() differently — "
                + "Circle draws a circle, Rectangle draws a rectangle. "
                + "Code that calls draw() on a Shape works for all subtypes without knowing the specific type."),

            trace("ipm-tr-02", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "What is printed?",
                "abstract class Vehicle {\n"
                + "    abstract String fuel();\n"
                + "    String describe() { return \"Runs on \" + fuel(); }\n"
                + "}\n"
                + "class Car extends Vehicle { String fuel() { return \"gasoline\"; } }\n"
                + "class Tesla extends Vehicle { String fuel() { return \"electricity\"; } }\n"
                + "Vehicle[] v = { new Car(), new Tesla() };\n"
                + "for (Vehicle x : v) System.out.println(x.describe());",
                "Runs on gasoline\nRuns on electricity",
                "describe() calls fuel() which dispatches polymorphically. "
                + "Car.fuel() = \"gasoline\", Tesla.fuel() = \"electricity\". "
                + "The template method pattern: the abstract class defines the algorithm, subclasses fill in the steps."),

            codegen("ipm-cg-02", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "Which array holds mixed Animal subclasses and calls speak() on each?",
                "Dog[] animals = {new Dog(), new Cat()}; for (Dog a : animals) a.speak();",
                "Animal[] animals = {new Dog(), new Cat()}; for (Animal a : animals) a.speak();",
                "Object[] animals = {new Dog(), new Cat()}; for (Object a : animals) ((Animal)a).speak();",
                "List<Object> animals = List.of(new Dog(), new Cat()); animals.forEach(a -> a.speak());",
                "b",
                "Declare the array as Animal[] — the common supertype. "
                + "The for-each uses Animal reference, and polymorphism dispatches speak() to the right implementation. "
                + "Option A: can't put a Cat in Dog[]. "
                + "Option C works but requires a cast. "
                + "Option D: Object has no speak() method."),

            mc("ipm-mc-06", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "What is the difference between method overloading and method overriding?",
                "Overloading is faster; overriding is type-safe",
                "Overloading = same name, different parameters in the same class; overriding = redefining a parent method in a subclass",
                "Overriding is resolved at compile time; overloading at runtime",
                "Overloading requires @Override; overriding does not",
                "b",
                "Overloading: print(int), print(String), print(double) — resolved at compile time (static dispatch). "
                + "Overriding: a subclass provides its own version of an inherited method — resolved at runtime (dynamic dispatch). "
                + "@Override annotation is used with overriding, not overloading."),

            mc("ipm-mc-07", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 2,
                "What is the principle 'program to an interface, not an implementation'?",
                "Always use abstract classes instead of interfaces",
                "Declare variables and parameters using interface or abstract types rather than concrete types",
                "Only write code that uses Java standard library classes",
                "Avoid using classes with more than one method",
                "b",
                "List<String> list = new ArrayList<>(); — not ArrayList<String> list = new ArrayList<>(). "
                + "This allows swapping ArrayList for LinkedList without touching the rest of the code. "
                + "The caller only knows about the List interface contract."),

            mc("ipm-mc-08", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "Which scenario best demonstrates polymorphism?",
                "A class with two constructors",
                "An array of Shape objects where each element responds to draw() based on its actual type",
                "A class that extends both Animal and Vehicle",
                "A method that prints a String",
                "b",
                "Polymorphism: same message (draw()), different behavior per actual type. "
                + "Shape[] shapes = {new Circle(), new Rect()}; for (Shape s : shapes) s.draw(); "
                + "Each element draws itself without the caller needing to know the specific type."),

            trace("ipm-tr-03", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "What is printed?",
                "class Animal { String type = \"animal\"; String describe() { return type; } }\n"
                + "class Cat extends Animal {\n"
                + "    String type = \"cat\";  // hides Animal.type\n"
                + "    @Override String describe() { return type; }\n"
                + "}\n"
                + "Animal a = new Cat();\n"
                + "System.out.println(a.type);\n"
                + "System.out.println(a.describe());",
                "animal\ncat",
                "Fields are not polymorphic: a.type uses Animal.type = \"animal\" (declared reference type). "
                + "Methods are polymorphic: a.describe() calls Cat's override, returning Cat.type = \"cat\"."),

            trace("ipm-tr-04", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 2,
                "What is printed?",
                "class Beverage {\n"
                + "    private double price;\n"
                + "    Beverage(double p) { price = p; }\n"
                + "    public double getPrice() { return price; }\n"
                + "    public void setPrice(double p) {\n"
                + "        if (p > 0) price = p;\n"
                + "    }\n"
                + "}\n"
                + "Beverage b = new Beverage(2.5);\n"
                + "b.setPrice(-1);\n"
                + "System.out.println(b.getPrice());",
                "2.5",
                "setPrice(-1) is rejected by the validation guard (p > 0). "
                + "price remains 2.5. getPrice() returns 2.5."),

            debug("ipm-db-02", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "This code is supposed to use polymorphism but always prints 'Animal'. Why?",
                "class Animal { static String name() { return \"Animal\"; } }\n"
                + "class Dog extends Animal { static String name() { return \"Dog\"; } }\n"
                + "// in main:\n"
                + "Animal a = new Dog();\n"
                + "System.out.println(a.name());",
                "Dog must implement name() from Animal",
                "Static methods are class-level and not polymorphic — the declared reference type (Animal) determines which name() runs",
                "name() must be overridden with @Override",
                "a must be cast to Dog to use polymorphism",
                "b",
                "Static methods use static dispatch (method hiding, not overriding). "
                + "a.name() resolves to Animal.name() because a is declared as Animal. "
                + "To use polymorphism, make name() an instance method (non-static) and override it."),

            debug("ipm-db-03", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "A getter for a List field leaks encapsulation. What is the fix?",
                "class Team {\n"
                + "    private List<String> members = new ArrayList<>();\n"
                + "    public List<String> getMembers() { return members; }\n"
                + "}",
                "Make the field public instead",
                "Return a defensive copy or unmodifiable view: Collections.unmodifiableList(members)",
                "Change the return type to Object[]",
                "Make the getter private",
                "b",
                "Returning the internal list directly lets callers call members.add(), members.clear(), etc. — bypassing Team's control. "
                + "Fix: return Collections.unmodifiableList(members) or return new ArrayList<>(members). "
                + "Callers can read the data but can't modify the internal state."),

            codegen("ipm-cg-03", Topic.INHERITANCE_POLYMORPHISM_ENCAPSULATION, 3,
                "Which factory method uses polymorphism to return the right Animal subtype?",
                "Animal make(String type) { return new Animal(); }",
                "Animal make(String type) { if (type.equals(\"dog\")) return new Dog(); if (type.equals(\"cat\")) return new Cat(); throw new IllegalArgumentException(type); }",
                "Dog make(String type) { return new Dog(); }",
                "Animal make(String type) { return (Animal) Class.forName(type).newInstance(); }",
                "b",
                "The factory returns an Animal reference pointing to the appropriate subtype. "
                + "The caller uses the Animal interface without knowing the concrete type. "
                + "Option A always returns base Animal. "
                + "Option C always returns Dog. "
                + "Option D uses reflection (works but is fragile and exception-prone).")
        );
    }
}
