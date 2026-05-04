package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class MemoryReferenceStaticQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.MEMORY_REFERENCE_STATIC; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("mem-mc-01", Topic.MEMORY_REFERENCE_STATIC, 1,
                "Where are objects created with 'new' stored in Java?",
                "Stack", "Heap", "Static memory", "Method area",
                "b",
                "Objects are allocated on the heap and managed by the garbage collector. "
                + "Local variables and method call frames are on the stack. "
                + "The stack stores references (pointers) to objects, not the objects themselves."),

            mc("mem-mc-02", Topic.MEMORY_REFERENCE_STATIC, 1,
                "What does 'null' represent in Java?",
                "Zero as an integer",
                "An empty string",
                "The absence of an object reference (pointing to nothing)",
                "A deleted object",
                "c",
                "null is a special value meaning a reference variable points to no object. "
                + "Accessing a method or field on a null reference throws NullPointerException."),

            mc("mem-mc-03", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What is a static field?",
                "A field that cannot be changed",
                "A field shared by all instances of the class, belonging to the class itself",
                "A field only accessible within the same method",
                "A field that is automatically null",
                "b",
                "Static fields belong to the class, not to any instance. "
                + "All instances share the same static field. "
                + "Change it in one place and all instances see the change."),

            mc("mem-mc-04", Topic.MEMORY_REFERENCE_STATIC, 2,
                "Two variables point to the same object: 'Dog a = new Dog(); Dog b = a;'. "
                + "What happens when you call 'a.setName(\"Rex\")'?",
                "Only a.name is changed",
                "Both a.name and b.name reflect 'Rex' because a and b point to the same object",
                "A copy of the Dog is created for b",
                "A NullPointerException is thrown",
                "b",
                "Java uses pass-by-reference-value. a and b hold the same reference (memory address). "
                + "Mutating the object through a also affects the state seen through b."),

            trace("mem-tr-01", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What is printed?",
                "class Counter {\n"
                + "    static int count = 0;\n"
                + "    Counter() { count++; }\n"
                + "}\n"
                + "// in main:\n"
                + "new Counter();\n"
                + "new Counter();\n"
                + "new Counter();\n"
                + "System.out.println(Counter.count);",
                "3",
                "count is static — shared across all Counter instances. "
                + "Each constructor call increments the same field. After 3 new Counter() calls, count == 3."),

            debug("mem-db-01", Topic.MEMORY_REFERENCE_STATIC, 2,
                "This code throws NullPointerException. Why?",
                "String s = null;\n"
                + "System.out.println(s.length());",
                "String has no length() method",
                "null is not a valid String value",
                "s is null — calling any method on null throws NullPointerException",
                "length() is a static method and cannot be called on instances",
                "c",
                "s holds null (no object). Calling a method on null throws NullPointerException. "
                + "Fix: check for null first: 'if (s != null) { ... }' "
                + "or use 'Objects.requireNonNullElse(s, \"\")'."),

            codegen("mem-cg-01", Topic.MEMORY_REFERENCE_STATIC, 2,
                "Which is the correct way to call a static method 'add(int a, int b)' in class 'Math'?",
                "Math m = new Math(); m.add(2, 3);",
                "Math.add(2, 3);",
                "static.Math.add(2, 3);",
                "new Math.add(2, 3);",
                "b",
                "Static methods are called on the class name, not on an instance. "
                + "Math.add(2, 3) is correct. "
                + "Creating an instance first (option A) works but is misleading and wasteful."),

            mc("mem-mc-05", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What is the heap?",
                "The area of memory where local variables and method call frames are stored",
                "A sorted tree data structure",
                "The area of memory where objects created with 'new' are stored",
                "The region of memory reserved for static variables",
                "c",
                "Heap memory stores objects (created with new). It is managed by the garbage collector. "
                + "Stack memory holds method frames: local variables and parameters. "
                + "When a method returns, its stack frame is popped. "
                + "Heap objects persist until no references to them remain."),

            trace("mem-tr-02", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What is printed?",
                "class Dog {\n"
                + "    static int count = 0;\n"
                + "    Dog() { count++; }\n"
                + "}\n"
                + "new Dog();\n"
                + "new Dog();\n"
                + "new Dog();\n"
                + "System.out.println(Dog.count);",
                "3",
                "count is static — shared by all Dog instances. "
                + "Each new Dog() increments the same count. After 3 instantiations, count = 3."),

            mc("mem-mc-06", Topic.MEMORY_REFERENCE_STATIC, 3,
                "What happens to an object with no remaining references?",
                "It immediately disappears from memory",
                "It becomes eligible for garbage collection",
                "It is moved to a special 'dead objects' area",
                "It is stored in a global weak reference table",
                "b",
                "When no references point to an object, it is eligible for garbage collection. "
                + "The JVM's garbage collector may reclaim its memory at some future point. "
                + "You cannot predict exactly when — and you should never rely on finalize() (deprecated)."),

            codegen("mem-cg-02", Topic.MEMORY_REFERENCE_STATIC, 3,
                "Which correctly uses a static counter to track how many Widgets have been created?",
                "class Widget { int count = 0; Widget() { count++; } }",
                "class Widget { static int count = 0; Widget() { count++; } static int getCount() { return count; } }",
                "class Widget { final int count; Widget() { count = Widget.count + 1; } }",
                "class Widget { static int count; }",
                "b",
                "Static fields are shared across all instances — ideal for a creation counter. "
                + "The constructor increments the shared count. A static getter provides access. "
                + "Option A uses an instance field (each Widget has its own count, always 1). "
                + "Option C uses final incorrectly and doesn't persist the count."),

            mc("mem-mc-07", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What is the difference between == and .equals() for object references?",
                "== is faster; equals() is more accurate",
                "== checks if two references point to the same object; .equals() checks logical equality",
                "== checks value equality; .equals() checks reference equality",
                "They behave identically for all types",
                "b",
                "String a = new String(\"hi\"); String b = new String(\"hi\"); "
                + "a == b is false (different objects). a.equals(b) is true (same content). "
                + "For primitives, == compares values directly."),

            mc("mem-mc-08", Topic.MEMORY_REFERENCE_STATIC, 3,
                "What is garbage collection?",
                "The programmer must free memory with free() or delete()",
                "The JVM automatically reclaims memory occupied by unreachable objects",
                "Java deletes objects immediately when they go out of scope",
                "Garbage collection only works for primitive types",
                "b",
                "Java uses automatic memory management. The garbage collector (GC) identifies objects "
                + "with no live references and reclaims their heap space. "
                + "Programmers don't need to free memory manually — preventing most memory leaks."),

            mc("mem-mc-09", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What does 'String s = \"hello\";' store on the stack vs heap?",
                "The entire String object is on the stack",
                "The reference s is on the stack; the String object is on the heap",
                "Both the reference and the object are on the heap",
                "Strings are primitives and stored on the stack",
                "b",
                "All local variables (including references) live on the stack. "
                + "The String object itself lives on the heap. "
                + "s is a reference (pointer) stored on the stack, pointing to the object on the heap."),

            mc("mem-mc-10", Topic.MEMORY_REFERENCE_STATIC, 3,
                "What is a memory leak in Java?",
                "When new fails to allocate memory",
                "When objects that are no longer needed are still referenced, preventing GC from reclaiming them",
                "When too many local variables are on the stack",
                "When static fields are declared with the wrong type",
                "b",
                "Java GC only collects unreachable objects. If you keep references to objects you no longer need "
                + "(e.g., in a static collection), the objects cannot be collected. "
                + "Example: adding to a static List without ever removing entries causes a memory leak."),

            trace("mem-tr-03", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What is printed?",
                "class Box {\n"
                + "    int value;\n"
                + "    Box(int v) { value = v; }\n"
                + "}\n"
                + "Box x = new Box(10);\n"
                + "Box y = x;\n"
                + "y.value = 99;\n"
                + "System.out.println(x.value);",
                "99",
                "x and y point to the same Box object. "
                + "Changing y.value also changes x.value because they reference the same heap object."),

            trace("mem-tr-04", Topic.MEMORY_REFERENCE_STATIC, 2,
                "What is printed?",
                "class Config {\n"
                + "    static String mode = \"debug\";\n"
                + "}\n"
                + "Config a = new Config();\n"
                + "Config b = new Config();\n"
                + "a.mode = \"release\";\n"
                + "System.out.println(b.mode);",
                "release",
                "mode is static — shared across all Config instances. "
                + "Changing mode via 'a' (or directly via Config.mode) changes the single shared field. "
                + "b.mode is also 'release'."),

            debug("mem-db-02", Topic.MEMORY_REFERENCE_STATIC, 2,
                "Why does this code print the same value for both variables?",
                "int[] a = {1, 2, 3};\n"
                + "int[] b = a;\n"
                + "b[0] = 99;\n"
                + "System.out.println(a[0]);  // 99 (not 1!)",
                "Arrays copy automatically when assigned",
                "b = a copies the reference — both a and b point to the same array in memory",
                "int[] uses value semantics like primitives",
                "b[0] = 99 is a compile error",
                "b",
                "Array assignment copies the reference, not the array. "
                + "a and b point to the same int[]. Modifying b[0] modifies the shared array, so a[0] == 99. "
                + "To get an independent copy: int[] b = Arrays.copyOf(a, a.length);"),

            debug("mem-db-03", Topic.MEMORY_REFERENCE_STATIC, 3,
                "This static utility class can be instantiated. What is the fix?",
                "class MathUtils {\n"
                + "    static int square(int n) { return n * n; }\n"
                + "    static int cube(int n) { return n * n * n; }\n"
                + "}",
                "Make all methods private",
                "Add a private no-arg constructor to prevent instantiation",
                "Make the class abstract",
                "Declare the class as final",
                "b",
                "A utility class with only static methods should not be instantiated. "
                + "Without a constructor, Java provides a default public no-arg constructor. "
                + "Fix: add 'private MathUtils() { }' to block instantiation. "
                + "This pattern matches java.lang.Math and similar classes."),

            codegen("mem-cg-03", Topic.MEMORY_REFERENCE_STATIC, 2,
                "Which code creates an independent copy of an array (not a shared reference)?",
                "int[] copy = original;",
                "int[] copy = Arrays.copyOf(original, original.length);",
                "int[] copy = (int[]) original.clone(); // also valid",
                "Both b and c create independent copies",
                "d",
                "Arrays.copyOf() and .clone() both create new arrays with the same element values. "
                + "Modifying copy does not affect original (and vice versa). "
                + "Option A copies only the reference — both variables point to the same array.")
        );
    }
}
