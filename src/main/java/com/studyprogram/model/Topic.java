package com.studyprogram.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * All study topics, ordered from first principles upward.
 * Each topic declares its prerequisites so the adaptive engine can gate access.
 */
public enum Topic {

    // ── Absolute Foundations ────────────────────────────────────────────────
    VARIABLES("Variables",
            "Primitive types, declaration, assignment, type inference",
            1),
    PRINTING("Printing",
            "System.out.println, print, printf, format strings",
            1, VARIABLES),
    ARGUMENTS("Arguments",
            "main(String[] args), command-line arguments, parsing",
            1, VARIABLES, PRINTING),
    HEX_BINARY_ENCODING("Hex / Binary / Character Encoding",
            "Binary, hexadecimal, ASCII, Unicode, char casting",
            2, VARIABLES),
    COMPILING_RUNNING("Compiling and Running",
            "javac, java, classpath, .class files, JVM overview",
            1),
    IDE("IDE",
            "IntelliJ / VS Code / Eclipse basics, project setup, run configs",
            1),

    // ── Control Flow ────────────────────────────────────────────────────────
    IF_CASE("If / Switch",
            "if, else-if, else, switch expressions (Java 14+), ternary",
            2, VARIABLES),
    LOOPS("Loops",
            "for, while, do-while, enhanced for, break, continue",
            2, VARIABLES, IF_CASE),
    FUNCTIONS("Functions / Methods",
            "Method signatures, return types, void, overloading, recursion",
            2, VARIABLES, IF_CASE),

    // ── Strings & Collections ───────────────────────────────────────────────
    STRINGS("Strings",
            "String API, immutability, StringBuilder, String.format",
            2, VARIABLES, PRINTING),
    ARRAYS("Arrays",
            "1D and 2D arrays, length, initialization, iteration",
            2, VARIABLES, LOOPS),
    ARRAYS_ARRAYLISTS("Arrays and ArrayLists",
            "ArrayList, add/remove/get, generics basics, Collections utility",
            3, ARRAYS),
    COLLECTIONS("Collections",
            "List, Set, Map, Queue, Deque, HashMap, TreeMap, iterators",
            3, ARRAYS_ARRAYLISTS),

    // ── OOP Foundations ─────────────────────────────────────────────────────
    CLASSES("Classes",
            "Fields, constructors, instance methods, this keyword",
            3, FUNCTIONS, STRINGS),
    PUBLIC_PRIVATE_PROTECTED("Access Modifiers",
            "public, private, protected, package-private, getters/setters",
            3, CLASSES),
    MEMORY_REFERENCE_STATIC("Memory / References / Static",
            "Stack vs heap, object references, null, static fields/methods",
            3, CLASSES),
    INTERFACES("Interfaces",
            "interface keyword, default methods, functional interfaces",
            3, CLASSES),
    INHERITANCE("Inheritance",
            "extends, method overriding, super, final",
            3, CLASSES),
    COMPOSITION("Composition",
            "Has-a vs is-a, dependency injection basics, delegation",
            3, CLASSES),
    CLASS_HIERARCHY("Class Hierarchy",
            "Object class, instanceof, casting, abstract classes",
            3, CLASSES, INHERITANCE),
    JAVA_CLASS_HIERARCHY("Java Class Hierarchy",
            "java.lang.Object methods, Comparable, Cloneable, Iterable",
            4, CLASS_HIERARCHY, COLLECTIONS),
    INHERITANCE_POLYMORPHISM_ENCAPSULATION("Inheritance, Polymorphism & Encapsulation",
            "Liskov substitution, dynamic dispatch, encapsulation patterns",
            4, INHERITANCE, INTERFACES),

    // ── Error Handling & I/O ─────────────────────────────────────────────────
    DEBUGGING_TOOLS("Debugging",
            "Breakpoints, step-through, watch expressions, stack traces",
            2, IDE),
    TRY_CATCH("Try / Catch / Finally / Exceptions",
            "Checked vs unchecked, custom exceptions, multi-catch, try-with-resources",
            3, CLASSES),
    FILE_PROCESSING("File Processing",
            "File, Path, Files API, BufferedReader/Writer, Scanner",
            3, STRINGS, TRY_CATCH),
    SERIALIZATION("Serialization",
            "Serializable, ObjectOutputStream/InputStream, JSON alternatives",
            4, FILE_PROCESSING),

    // ── Advanced OOP ─────────────────────────────────────────────────────────
    GENERICS("Generics",
            "Type parameters, bounded wildcards, generic methods",
            4, COLLECTIONS, INHERITANCE_POLYMORPHISM_ENCAPSULATION),
    OO_DESIGN_PATTERNS("OO Design Patterns",
            "Singleton, Factory, Strategy, Observer, Builder, Decorator",
            5, INHERITANCE_POLYMORPHISM_ENCAPSULATION, COMPOSITION),

    // ── Functional Style ─────────────────────────────────────────────────────
    LAMBDAS("Lambdas",
            "Lambda syntax, method references, functional interfaces",
            4, FUNCTIONS, INTERFACES, GENERICS),
    STREAM_API("Stream API",
            "stream(), filter, map, reduce, collect, Optional",
            4, LAMBDAS, COLLECTIONS),
    STANDARD_STREAMS("Standard Streams (IO Streams)",
            "InputStream, OutputStream, Reader, Writer, NIO channels",
            4, FILE_PROCESSING, GENERICS),
    FUNCTIONAL_PARADIGM("Functional Paradigm",
            "Immutability, pure functions, higher-order functions, composition",
            5, STREAM_API, LAMBDAS),

    // ── Tooling ──────────────────────────────────────────────────────────────
    JAVADOC("Javadoc",
            "@param, @return, @throws, generating HTML docs",
            2, FUNCTIONS),
    UNIT_TESTS("Unit Tests",
            "JUnit 5, assertions, test lifecycle, mocking basics",
            3, FUNCTIONS),
    PROJECT_ORGANIZATION("Project Organization & Packages",
            "packages, import, directory layout, module-info",
            2, CLASSES),
    MAVEN("Maven",
            "pom.xml, lifecycle, dependencies, plugins, mvn commands",
            3, PROJECT_ORGANIZATION),

    // ── GUI & Events ─────────────────────────────────────────────────────────
    GUI_SWING("GUI and Swing",
            "JFrame, JPanel, Swing threading model, EDT",
            4, CLASSES, TRY_CATCH),
    GUI_COMPONENTS("GUI Components",
            "JButton, JLabel, JTextField, JComboBox, JList, JTable",
            4, GUI_SWING),
    GUI_LAYOUT("GUI Layout",
            "BorderLayout, GridLayout, FlowLayout, GridBagLayout, BoxLayout",
            4, GUI_SWING),
    GUI_EVENT_MODEL("GUI Event Model",
            "ActionListener, EventObject, EventQueue, event dispatch",
            4, GUI_SWING, INTERFACES),
    MOUSE_INPUT("Mouse Input",
            "MouseListener, MouseMotionListener, MouseEvent",
            4, GUI_EVENT_MODEL),
    KEYBOARD_INPUT("Keyboard Input",
            "KeyListener, KeyEvent, key codes, focus management",
            4, GUI_EVENT_MODEL),
    PAINTING("Painting / Repainting",
            "paintComponent, Graphics2D, repaint, double buffering",
            4, GUI_SWING),
    GRAPHICS_2D("2D Graphics",
            "Shapes, strokes, gradients, transforms, RenderingHints",
            4, PAINTING),
    IMAGES_SOUND("Images and Sound",
            "ImageIO, BufferedImage, Clip, AudioInputStream",
            4, GUI_SWING),
    SWING_COMPONENTS("Swing Components (Advanced)",
            "JScrollPane, JSplitPane, JTabbedPane, JMenuBar, dialogs",
            5, GUI_COMPONENTS, GUI_LAYOUT),

    // ── Concurrency ──────────────────────────────────────────────────────────
    ASYNC_THREADS("Async / Runnable / Future / Threads",
            "Thread, Runnable, ExecutorService, Future, CompletableFuture",
            5, CLASSES, INTERFACES),

    // ── Advanced Topics ──────────────────────────────────────────────────────
    RANDOM("Random",
            "java.util.Random, Math.random, SecureRandom, seeding",
            2, VARIABLES),
    NETWORKING("Networking",
            "Socket, ServerSocket, URL, HttpURLConnection, HTTP basics",
            4, TRY_CATCH, FILE_PROCESSING),
    DATABASES("Databases",
            "JDBC, PreparedStatement, ResultSet, connection pooling",
            4, TRY_CATCH, FILE_PROCESSING),
    REFLECTION("Reflection",
            "Class<?>, getDeclaredFields, invoke, annotations",
            5, CLASS_HIERARCHY),
    METAPROGRAMMING("Metaprogramming",
            "Annotation processors, dynamic proxies, bytecode manipulation",
            5, REFLECTION),
    MACHINE_LEARNING("Machine Learning",
            "Linear algebra basics, gradient descent, neural nets in Java",
            5, ARRAYS, FUNCTIONS),
    EVOLUTIONARY_PROGRAMMING("Evolutionary Programming",
            "Genetic algorithms, fitness functions, selection, crossover",
            5, MACHINE_LEARNING);

    // ── Metadata ─────────────────────────────────────────────────────────────
    public final String displayName;
    public final String description;
    public final int baseLevel;         // 1 = absolute beginner, 5 = advanced
    public final List<Topic> prerequisites;

    Topic(String displayName, String description, int baseLevel, Topic... prerequisites) {
        this.displayName    = displayName;
        this.description    = description;
        this.baseLevel      = baseLevel;
        this.prerequisites  = Arrays.asList(prerequisites);
    }

    /** Returns the list of topics that must be mastered before this one is unlocked. */
    public List<Topic> getPrerequisites() { return prerequisites; }

    /** True when the student has ≥40% mastery in every prerequisite. */
    public boolean isUnlocked(Map<Topic, TopicPerformance> performance) {
        return prerequisites.stream().allMatch(prereq -> {
            TopicPerformance p = performance.get(prereq);
            return p != null && p.getMasteryScore() >= 0.4;
        });
    }

    @Override
    public String toString() {
        return displayName;
    }
}
