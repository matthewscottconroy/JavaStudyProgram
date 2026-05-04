package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class MetaprogrammingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.METAPROGRAMMING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("meta-mc-01", Topic.METAPROGRAMMING, 3,
                "What is a Java annotation?",
                "A comment that is visible in the compiled bytecode",
                "A metadata marker applied to code elements that can be read at compile time or runtime",
                "A runtime error message attached to a method",
                "A special import for framework classes",
                "b",
                "Annotations (introduced in Java 5) attach metadata to classes, methods, fields, etc. "
                + "Examples: @Override (compile-time check), @Deprecated (warning), @SuppressWarnings, "
                + "@Test (JUnit runtime processing). Frameworks use annotations extensively for configuration."),

            mc("meta-mc-02", Topic.METAPROGRAMMING, 3,
                "What does @Retention(RetentionPolicy.RUNTIME) mean on a custom annotation?",
                "The annotation expires after the method returns",
                "The annotation is available at runtime via reflection, not just at compile time",
                "The annotation is removed after the first run",
                "The annotation is retained only in source files, not in .class files",
                "b",
                "RetentionPolicy.RUNTIME keeps the annotation in the bytecode and makes it readable via reflection. "
                + "RetentionPolicy.CLASS (default) keeps it in .class but not at runtime. "
                + "RetentionPolicy.SOURCE removes it after compilation (e.g., @Override)."),

            mc("meta-mc-03", Topic.METAPROGRAMMING, 3,
                "What does @Target(ElementType.METHOD) restrict?",
                "The annotation can only be applied to methods",
                "The annotation can only be processed by methods",
                "The annotation generates code for all methods in the class",
                "The annotation runs at method invocation time",
                "a",
                "@Target specifies where an annotation may be placed. "
                + "ElementType.METHOD → methods only. "
                + "ElementType.FIELD → fields. ElementType.TYPE → classes/interfaces. "
                + "Multiple targets: @Target({ElementType.METHOD, ElementType.FIELD})."),

            mc("meta-mc-04", Topic.METAPROGRAMMING, 4,
                "What does an annotation processor do?",
                "Runs annotations at runtime to inject behavior",
                "Processes annotations at compile time to generate source code, validate constraints, or produce reports",
                "Converts annotations to XML configuration",
                "Replaces reflection with faster code",
                "b",
                "Annotation processors (javax.annotation.processing) hook into javac. "
                + "Frameworks like Lombok, Dagger, and AutoValue use them to generate boilerplate code at compile time. "
                + "Processors implement Processor and are registered via META-INF/services."),

            trace("meta-tr-01", Topic.METAPROGRAMMING, 3,
                "How many annotations are on the method?",
                "@Override\n"
                + "@Deprecated\n"
                + "@SuppressWarnings(\"unchecked\")\n"
                + "public List getData() {\n"
                + "    return new ArrayList();\n"
                + "}",
                "3",
                "Three annotations appear before the method signature: @Override, @Deprecated, @SuppressWarnings. "
                + "Multiple annotations on a single element are allowed in Java."),

            debug("meta-db-01", Topic.METAPROGRAMMING, 4,
                "Reading this annotation at runtime returns null. Why?",
                "@interface MyTag { String value(); }\n"
                + "\n"
                + "@MyTag(\"hello\")\n"
                + "class Example {}\n"
                + "\n"
                + "// At runtime:\n"
                + "MyTag tag = Example.class.getAnnotation(MyTag.class); // null!",
                "getAnnotation() only works on methods",
                "Custom annotations default to RetentionPolicy.CLASS — not available at runtime via reflection",
                "Example.class cannot be loaded at runtime",
                "value() must be named 'name' for runtime access",
                "b",
                "The default retention policy is RetentionPolicy.CLASS — the annotation exists in the .class file "
                + "but is not accessible via reflection at runtime. "
                + "Fix: add @Retention(RetentionPolicy.RUNTIME) to the @interface MyTag declaration."),

            codegen("meta-cg-01", Topic.METAPROGRAMMING, 4,
                "Which defines a runtime-visible annotation that can only be applied to fields?",
                "@interface FieldTag { }",
                "@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface FieldTag { String description() default \"\"; }",
                "@Runtime @FieldOnly @interface FieldTag { }",
                "@Retention(RUNTIME) @interface FieldTag { @Target(FIELD) String desc; }",
                "b",
                "@Retention(RUNTIME) makes it readable via reflection. "
                + "@Target(FIELD) restricts application to fields. "
                + "Adding a description() element with a default makes the annotation optional to configure. "
                + "Option C: @Runtime and @FieldOnly don't exist. Option D: @Target belongs on the annotation type, not inside it."),

            mc("meta-mc-05", Topic.METAPROGRAMMING, 3,
                "What does @Inherited mean on an annotation declaration?",
                "The annotation is shared between all instances of a class",
                "If a class has this annotation, its subclasses automatically inherit it",
                "The annotation can be used on interfaces",
                "The annotation is visible to sub-packages",
                "b",
                "@Inherited (java.lang.annotation) makes a class-level annotation automatically inherited by subclasses. "
                + "Example: @Retention(RUNTIME) @Inherited @interface Audited {} — if Dog has @Audited, "
                + "Poodle extends Dog also has it accessible via getAnnotation(Audited.class). "
                + "@Inherited only works on class annotations, not methods or fields."),

            mc("meta-mc-06", Topic.METAPROGRAMMING, 3,
                "What is a repeatable annotation in Java 8+?",
                "An annotation that can be applied multiple times to the same element",
                "An annotation that repeats its processing at runtime",
                "An annotation that loops over all class members",
                "An annotation that inherits from another annotation",
                "a",
                "Mark the annotation with @Repeatable(Container.class) and define a container annotation "
                + "that holds an array of the repeatable annotation. "
                + "Then you can write: @Tag(\"a\") @Tag(\"b\") class Foo {} instead of the old workaround."),

            mc("meta-mc-07", Topic.METAPROGRAMMING, 4,
                "What is a bytecode manipulation library (e.g., ASM or Javassist) used for?",
                "Compiling Java source to native machine code",
                "Reading, modifying, or generating .class bytecode programmatically at build time or runtime",
                "Decompiling .class files back to source code",
                "Profiling JVM memory usage",
                "b",
                "Libraries like ASM, Javassist, and ByteBuddy modify or create .class bytecode directly. "
                + "Used by: frameworks that instrument methods (Spring AOP, Hibernate), mocking frameworks (Mockito), "
                + "and code coverage tools (JaCoCo). They work at a lower level than annotation processors."),

            trace("meta-tr-02", Topic.METAPROGRAMMING, 3,
                "What is printed?",
                "@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n"
                + "@interface Color { String value() default \"red\"; }\n"
                + "\n"
                + "@Color(\"blue\")\n"
                + "class Sky {}\n"
                + "\n"
                + "Color c = Sky.class.getAnnotation(Color.class);\n"
                + "System.out.println(c.value());",
                "blue",
                "The annotation is RUNTIME-retained. getAnnotation() retrieves it. value() returns the configured string 'blue'."),

            trace("meta-tr-03", Topic.METAPROGRAMMING, 3,
                "What is printed?",
                "@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n"
                + "@interface Priority { int level() default 1; }\n"
                + "\n"
                + "@Priority\n"
                + "class Task {}\n"
                + "\n"
                + "Priority p = Task.class.getAnnotation(Priority.class);\n"
                + "System.out.println(p.level());",
                "1",
                "@Priority is used without specifying level(), so the default value 1 is used. p.level() returns 1."),

            debug("meta-db-02", Topic.METAPROGRAMMING, 3,
                "The compiler rejects this annotation usage. Why?",
                "@Target(java.lang.annotation.ElementType.METHOD)\n"
                + "@interface Log {}\n"
                + "\n"
                + "@Log  // applied to a field\n"
                + "private int count;",
                "@Log requires parentheses: @Log()",
                "Field names cannot start with 'count'",
                "@Log has @Target(METHOD) — it can only be applied to methods, not fields",
                "@interface declarations must be public",
                "c",
                "@Target(ElementType.METHOD) restricts @Log to methods only. "
                + "Applying it to a field causes a compile error: 'annotation type not applicable to this kind of declaration'. "
                + "To allow both, use @Target({ElementType.METHOD, ElementType.FIELD})."),

            codegen("meta-cg-02", Topic.METAPROGRAMMING, 4,
                "Which reads all runtime-visible annotations from a class called 'Service'?",
                "Service.annotations();",
                "Annotation[] annotations = Service.class.getAnnotations(); for (Annotation a : annotations) System.out.println(a);",
                "Class.getAnnotations(Service);",
                "Service.class.getDeclaredAnnotations(RetentionPolicy.RUNTIME);",
                "b",
                "Class.getAnnotations() returns all RUNTIME-retained annotations on the class (including inherited). "
                + "getDeclaredAnnotations() returns only annotations directly on the class, not inherited ones. "
                + "Use instanceof or annotation.annotationType() to distinguish specific annotation types.")
        );
    }
}
