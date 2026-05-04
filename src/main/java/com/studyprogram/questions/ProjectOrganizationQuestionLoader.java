package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ProjectOrganizationQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.PROJECT_ORGANIZATION; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("pkg-mc-01", Topic.PROJECT_ORGANIZATION, 1,
                "What is a Java package?",
                "A compressed JAR file",
                "A namespace that organizes related classes and controls access",
                "A collection of methods inside a class",
                "An import statement",
                "b",
                "A package is a namespace declared at the top of a .java file with 'package com.example;'. "
                + "It organizes related classes, prevents naming collisions, and controls access "
                + "(package-private visibility is the default when no modifier is specified)."),

            mc("pkg-mc-02", Topic.PROJECT_ORGANIZATION, 1,
                "Where must the 'package' statement appear in a Java source file?",
                "After the import statements",
                "Before the class declaration, and before any import statements",
                "Inside the class body",
                "At the end of the file",
                "b",
                "The package statement must be the very first non-comment statement in the file. "
                + "Order: package → imports → class declaration. "
                + "Having it in the wrong position causes a compile error."),

            mc("pkg-mc-03", Topic.PROJECT_ORGANIZATION, 2,
                "What does 'import java.util.*;' do?",
                "Imports every class from every java.* package",
                "Imports all public classes from the java.util package",
                "Imports static methods from java.util",
                "Creates an alias for java.util.ArrayList",
                "b",
                "The wildcard import imports all public classes from java.util (ArrayList, HashMap, List, etc.) "
                + "without importing sub-packages. It does not affect performance — the compiler resolves which "
                + "classes are used. Explicit imports (import java.util.ArrayList;) are preferred for clarity."),

            mc("pkg-mc-04", Topic.PROJECT_ORGANIZATION, 2,
                "What directory structure should the file 'com/example/app/Main.java' be in for 'package com.example.app'?",
                "Any directory — the package statement overrides the filesystem",
                "The file must be in a directory named 'com/example/app/' relative to the source root",
                "The file must be in a directory named 'app/'",
                "The file must be named after the package: 'com.example.app.java'",
                "b",
                "Java requires the directory structure to mirror the package hierarchy. "
                + "The javac compiler and class loader use the directory path to locate files. "
                + "If the package is com.example.app, the file must be at src/main/java/com/example/app/Main.java "
                + "in a Maven project."),

            trace("pkg-tr-01", Topic.PROJECT_ORGANIZATION, 2,
                "What is the fully qualified class name of this class?",
                "// File: src/main/java/com/university/cs210/Student.java\n"
                + "package com.university.cs210;\n"
                + "\n"
                + "public class Student { }",
                "com.university.cs210.Student",
                "The fully qualified name combines the package name and the simple class name with a dot. "
                + "It uniquely identifies the class across all packages in the JVM."),

            debug("pkg-db-01", Topic.PROJECT_ORGANIZATION, 2,
                "This code fails to compile. Why?",
                "// File: src/com/example/Helper.java\n"
                + "package com.example;\n"
                + "\n"
                + "// File: src/Main.java\n"
                + "public class Main {\n"
                + "    public static void main(String[] args) {\n"
                + "        Helper h = new Helper();\n"
                + "    }\n"
                + "}",
                "Main.java must also be in package com.example",
                "Helper is in package com.example but Main has no import — it can't see Helper",
                "Helper must be public to be used from Main",
                "Both files must be in the same directory",
                "b",
                "Main.java (in the default package) uses Helper from com.example. "
                + "Without 'import com.example.Helper;', the compiler can't resolve 'Helper'. "
                + "Add the import statement, or reference it by fully qualified name: com.example.Helper h = new com.example.Helper();"),

            codegen("pkg-cg-01", Topic.PROJECT_ORGANIZATION, 2,
                "Which correctly declares a class in package 'edu.university.lab1' and imports ArrayList?",
                "import java.util.ArrayList;\npackage edu.university.lab1;\npublic class Lab1 {}",
                "package edu.university.lab1;\nimport java.util.ArrayList;\npublic class Lab1 {}",
                "namespace edu.university.lab1;\nimport java.util.ArrayList;\npublic class Lab1 {}",
                "package edu.university.lab1; public class Lab1 { import ArrayList; }",
                "b",
                "The correct order is: package statement first, then imports, then the class declaration. "
                + "Option A puts import before package (compile error). "
                + "Option C uses 'namespace' which is C#, not Java. "
                + "Option D puts import inside the class body (invalid)."),

            mc("pkg-mc-05", Topic.PROJECT_ORGANIZATION, 2,
                "What does 'import static' allow you to do?",
                "Import a class from a static library",
                "Use static methods and fields without qualifying them with the class name",
                "Import a package of static classes",
                "Declare a class as static at the package level",
                "b",
                "import static java.lang.Math.sqrt; allows writing sqrt(x) instead of Math.sqrt(x). "
                + "import static java.lang.Math.*; imports all static members. "
                + "Overuse can make code harder to read — the source of methods becomes unclear."),

            mc("pkg-mc-06", Topic.PROJECT_ORGANIZATION, 2,
                "What is the default (package-private) access modifier?",
                "The class is accessible to all classes everywhere",
                "The class is accessible only within its own package",
                "The class is accessible only to subclasses",
                "The class is accessible only within its own file",
                "b",
                "If no access modifier is given (class Foo {} or void method() {}), "
                + "the element has package-private access: visible to all classes in the same package, "
                + "but not to classes in other packages. "
                + "This is less restrictive than private but more restrictive than protected or public."),

            mc("pkg-mc-07", Topic.PROJECT_ORGANIZATION, 3,
                "In a Maven project, where should test classes be placed?",
                "src/main/java",
                "src/test/java",
                "src/main/test",
                "test/java/src",
                "b",
                "Maven's standard layout separates production code (src/main/java) from test code (src/test/java). "
                + "Test classes in src/test/java are compiled only when running 'mvn test' or 'mvn package' "
                + "and are NOT included in the production JAR. "
                + "Test resources go in src/test/resources."),

            trace("pkg-tr-02", Topic.PROJECT_ORGANIZATION, 2,
                "Which import is needed for 'List' in this code?",
                "package com.example;\n"
                + "// import ???\n"
                + "public class Demo {\n"
                + "    List<String> names = new ArrayList<>();\n"
                + "}",
                "import java.util.List; and import java.util.ArrayList;",
                "Both List and ArrayList are in java.util. "
                + "They must be imported explicitly (or via wildcard import java.util.*). "
                + "java.lang classes (String, Integer, etc.) are auto-imported."),

            trace("pkg-tr-03", Topic.PROJECT_ORGANIZATION, 2,
                "What does the compiler infer about class 'Helper' if it has no access modifier?",
                "package com.example;\n"
                + "class Helper {\n"
                + "    void assist() {}\n"
                + "}",
                "Helper is package-private — visible only within com.example",
                "No modifier means package-private access. Helper can be used by other classes in com.example "
                + "but not by classes in other packages, even if they try to import it."),

            debug("pkg-db-02", Topic.PROJECT_ORGANIZATION, 2,
                "This code causes a compile error. What is wrong?",
                "// File: com/example/Printer.java\n"
                + "package com.example;\n"
                + "class Printer {\n"
                + "    public void print(String s) { System.out.println(s); }\n"
                + "}\n"
                + "\n"
                + "// File: com/other/App.java\n"
                + "package com.other;\n"
                + "import com.example.Printer;\n"
                + "public class App {\n"
                + "    public static void main(String[] args) {\n"
                + "        new Printer().print(\"hi\");\n"
                + "    }\n"
                + "}",
                "import com.example.Printer is invalid syntax",
                "Printer is package-private (no modifier) — it cannot be imported from another package",
                "print() must be static to be called from main()",
                "App must be in the same package as Printer",
                "b",
                "Printer has no access modifier — it's package-private. "
                + "Only classes in com.example can use it. "
                + "Fix: declare 'public class Printer' so it's accessible from com.other."),

            codegen("pkg-cg-02", Topic.PROJECT_ORGANIZATION, 2,
                "Which import statement lets you use both HashMap and ArrayList without qualifying them?",
                "import java.util.HashMap; import java.util.ArrayList;",
                "import java.util.*;",
                "import java.util.HashMap, java.util.ArrayList;",
                "import java.*;",
                "b",
                "The wildcard import java.util.* imports all public types from java.util in a single statement. "
                + "Option A requires two lines (also correct but more verbose). "
                + "Option C is invalid syntax — Java doesn't support comma-separated imports. "
                + "Option D is invalid — wildcards work at the package level, not across sub-packages.")
        );
    }
}
