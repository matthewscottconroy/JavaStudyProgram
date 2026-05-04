package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ReflectionQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.REFLECTION; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("ref-mc-01", Topic.REFLECTION, 3,
                "What does Java reflection allow you to do?",
                "Compile Java code at runtime",
                "Inspect and manipulate classes, fields, and methods at runtime",
                "Create faster bytecode than the compiler can",
                "Share objects between JVM instances",
                "b",
                "Reflection (java.lang.reflect) lets code examine its own structure at runtime: "
                + "discover fields, methods, constructors; read/write field values; invoke methods. "
                + "Used by frameworks (Spring, Jackson, JUnit) for dependency injection and serialisation."),

            mc("ref-mc-02", Topic.REFLECTION, 3,
                "How do you get the Class object for a class named 'Dog' at runtime?",
                "Dog.classOf()", "Class.get(Dog.class)", "Dog.class", "Class.forName(\"Dog\")",
                "d",
                "Class.forName(\"com.example.Dog\") loads the class by its fully qualified name at runtime. "
                + "Dog.class is a compile-time class literal (not runtime-dynamic). "
                + "Both are valid depending on the use case, but Class.forName() is the runtime-reflection approach."),

            mc("ref-mc-03", Topic.REFLECTION, 4,
                "What is required to access a private field via reflection?",
                "The field must be made non-private first",
                "Call field.setAccessible(true) to bypass the access check",
                "Use a special ReflectionPermission object",
                "Private fields cannot be accessed via reflection",
                "b",
                "field.setAccessible(true) suppresses the JVM's access control check. "
                + "After this, you can get/set private fields. "
                + "In newer JVM versions with the module system, this may throw InaccessibleObjectException "
                + "for strongly encapsulated modules."),

            mc("ref-mc-04", Topic.REFLECTION, 4,
                "Which method invokes a method dynamically via reflection?",
                "method.call(obj, args)", "method.invoke(obj, args)", "method.run(obj, args)", "method.execute(obj)",
                "b",
                "Method.invoke(Object obj, Object... args) calls the method on obj with the given args. "
                + "If obj is null the method is static. Checked exceptions are wrapped in InvocationTargetException."),

            trace("ref-tr-01", Topic.REFLECTION, 3,
                "What is printed?",
                "import java.lang.reflect.*;\n"
                + "class Point { public int x = 10; public int y = 20; }\n"
                + "// in main:\n"
                + "Point p = new Point();\n"
                + "Field[] fields = p.getClass().getFields();\n"
                + "System.out.println(fields.length);",
                "2",
                "getFields() returns all public fields. Point has two: x and y. "
                + "getDeclaredFields() would also include private ones."),

            debug("ref-db-01", Topic.REFLECTION, 4,
                "This reflection code throws IllegalAccessException. Why?",
                "class Secret { private int pin = 1234; }\n"
                + "// in main:\n"
                + "Secret s = new Secret();\n"
                + "Field f = s.getClass().getDeclaredField(\"pin\");\n"
                + "System.out.println(f.get(s));",
                "getDeclaredField only works on public fields",
                "f.get(s) is missing setAccessible(true) before accessing the private field",
                "Secret needs to implement Serializable",
                "int fields cannot be retrieved via reflection",
                "b",
                "Accessing a private field via reflection requires f.setAccessible(true) before calling f.get(). "
                + "Without it, the JVM enforces the private access modifier and throws IllegalAccessException."),

            codegen("ref-cg-01", Topic.REFLECTION, 4,
                "Which snippet correctly reads the value of a private field 'name' on object 'obj'?",
                "obj.name;",
                "Field f = obj.getClass().getDeclaredField(\"name\"); f.setAccessible(true); String name = (String) f.get(obj);",
                "Field f = obj.getClass().getField(\"name\"); String name = (String) f.get(obj);",
                "String name = obj.reflect().get(\"name\");",
                "b",
                "getDeclaredField gets the private field, setAccessible(true) bypasses the access check, "
                + "and f.get(obj) retrieves the value (cast needed). "
                + "Option A: direct access to private field won't compile. "
                + "Option C: getField() only returns public fields. "
                + "Option D: reflect() does not exist on Object."),

            mc("ref-mc-05", Topic.REFLECTION, 3,
                "What does Class.getMethods() return compared to getDeclaredMethods()?",
                "getMethods() returns private methods; getDeclaredMethods() returns public ones",
                "getMethods() returns all public methods including inherited ones; getDeclaredMethods() returns all methods declared in the class itself",
                "They return the same set of methods",
                "getMethods() requires setAccessible(true); getDeclaredMethods() does not",
                "b",
                "getMethods(): all public methods including those inherited from parent classes. "
                + "getDeclaredMethods(): all methods (public, protected, private) declared directly in this class, "
                + "excluding inherited ones. "
                + "Similar pattern: getFields() vs getDeclaredFields()."),

            trace("ref-tr-02", Topic.REFLECTION, 3,
                "What is printed?",
                "class Foo {\n"
                + "    public void alpha() {}\n"
                + "    public void beta() {}\n"
                + "    private void gamma() {}\n"
                + "}\n"
                + "System.out.println(Foo.class.getDeclaredMethods().length);",
                "3",
                "getDeclaredMethods() returns all methods declared in Foo: alpha, beta, gamma (including private). "
                + "Length = 3. getMethods() would return more, including inherited methods from Object."),

            mc("ref-mc-06", Topic.REFLECTION, 4,
                "What is a common practical use of Java reflection in frameworks?",
                "To make code run faster by bypassing the JIT compiler",
                "To discover and wire together classes (dependency injection, ORM mapping, JSON serialization) without compile-time coupling",
                "To prevent other classes from accessing private fields",
                "To replace the JVM's class loader",
                "b",
                "Spring uses reflection for dependency injection. Jackson uses it to map JSON to Java fields. "
                + "JUnit uses it to find and call @Test methods. "
                + "Reflection allows frameworks to work with user classes without knowing them at compile time, "
                + "but at the cost of compile-time safety and some performance overhead."),

            mc("ref-mc-07", Topic.REFLECTION, 3,
                "What is a downside of using reflection compared to direct code?",
                "Reflection only works with public classes",
                "Reflection bypasses compile-time type checking, is slower, and makes code harder to understand and refactor",
                "Reflection requires a special license",
                "Reflected method calls cannot throw exceptions",
                "b",
                "Reflection loses type safety (errors become runtime, not compile-time), "
                + "has performance overhead (method lookup each time), "
                + "and makes code harder to follow in IDEs. "
                + "Use it as a last resort when direct code or generics won't work."),

            mc("ref-mc-08", Topic.REFLECTION, 3,
                "What does Class.newInstance() do?",
                "Creates a new array of the class type",
                "Creates a new instance of the class by calling the no-arg constructor",
                "Clones an existing instance",
                "Loads the class for the first time",
                "b",
                "Class.newInstance() invokes the no-arg constructor reflectively. "
                + "It is deprecated in Java 9+ because it silently propagates checked exceptions. "
                + "Prefer: clazz.getDeclaredConstructor().newInstance() which gives better control."),

            mc("ref-mc-09", Topic.REFLECTION, 4,
                "What is the module system (JPMS) restriction on reflection?",
                "Reflection is completely disabled inside modules",
                "Modules can limit reflective access to their packages; frameworks may need '--add-opens' to work",
                "Only private fields can be accessed reflectively in a module",
                "Reflection only works across module boundaries",
                "b",
                "Java modules (Java 9+) can use 'opens package to framework' to allow reflective access. "
                + "Without it, setAccessible(true) on a class in a non-opened module throws InaccessibleObjectException. "
                + "The --add-opens JVM flag is a common workaround when using older frameworks."),

            trace("ref-tr-03", Topic.REFLECTION, 3,
                "What is printed?",
                "import java.lang.reflect.*;\n"
                + "class Greeter {\n"
                + "    private String greet(String name) { return \"Hi \" + name; }\n"
                + "}\n"
                + "Greeter g = new Greeter();\n"
                + "Method m = Greeter.class.getDeclaredMethod(\"greet\", String.class);\n"
                + "m.setAccessible(true);\n"
                + "System.out.println(m.invoke(g, \"Alice\"));",
                "Hi Alice",
                "getDeclaredMethod finds the private greet method. setAccessible(true) bypasses the access check. "
                + "invoke(g, \"Alice\") calls greet(\"Alice\") on g, returning \"Hi Alice\"."),

            trace("ref-tr-04", Topic.REFLECTION, 3,
                "What is printed?",
                "class Animal { public int legs = 4; }\n"
                + "Animal a = new Animal();\n"
                + "Field f = a.getClass().getField(\"legs\");\n"
                + "f.set(a, 6);\n"
                + "System.out.println(a.legs);",
                "6",
                "f.set(a, 6) reflectively sets the public field 'legs' to 6. "
                + "a.legs is then 6. Public fields don't require setAccessible(true)."),

            debug("ref-db-02", Topic.REFLECTION, 3,
                "This code throws NoSuchMethodException. Why?",
                "class Calculator { private int add(int a, int b) { return a + b; } }\n"
                + "Method m = Calculator.class.getMethod(\"add\", int.class, int.class);",
                "getMethod returns an array, not a single method",
                "getMethod() only finds public methods — 'add' is private; use getDeclaredMethod()",
                "int.class is not a valid type token for int parameters",
                "Calculator must be public to use reflection",
                "b",
                "getMethod() searches public methods only (including inherited). "
                + "getDeclaredMethod() searches all methods in the class (public, protected, package, private). "
                + "Fix: Calculator.class.getDeclaredMethod(\"add\", int.class, int.class)"),

            debug("ref-db-03", Topic.REFLECTION, 4,
                "Invocation via reflection throws InvocationTargetException. What does this mean?",
                "Method m = String.class.getMethod(\"charAt\", int.class);\n"
                + "m.invoke(\"hello\", 10);  // throws InvocationTargetException",
                "The method does not exist",
                "The invoked method itself threw an exception — it is wrapped in InvocationTargetException",
                "The object is null",
                "The method signature doesn't match",
                "b",
                "InvocationTargetException wraps any exception thrown by the reflectively invoked method. "
                + "To get the actual cause: e.getCause(). "
                + "Contrast with IllegalAccessException (access denied) and NoSuchMethodException (method not found)."),

            codegen("ref-cg-02", Topic.REFLECTION, 4,
                "Which snippet invokes a static method 'double square(double n)' on class Calc reflectively?",
                "Calc.class.getMethod(\"square\", double.class).invoke(null, 5.0);",
                "Calc.class.getDeclaredMethod(\"square\").invoke(new Calc(), 5.0);",
                "Method m = Calc.class.invoke(\"square\", 5.0);",
                "Calc.class.getMethod(\"square\").invoke(5.0);",
                "a",
                "For a static method, the first argument to invoke() is null (no instance needed). "
                + "getMethod(\"square\", double.class) specifies the parameter type to disambiguate overloads. "
                + "Option B uses getDeclaredMethod with no parameter type and passes a new Calc() (wrong for static). "
                + "Options C and D have incorrect API usage.")
        );
    }
}
