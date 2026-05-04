package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class JavadocQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.JAVADOC; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("jdoc-mc-01", Topic.JAVADOC, 1,
                "Which comment syntax starts a Javadoc comment?",
                "// Javadoc", "/* comment */", "/** comment */", "# comment",
                "c",
                "Javadoc comments use /** ... */ (note the double asterisk after the slash). "
                + "The javadoc tool processes these to generate HTML documentation. "
                + "Regular /* */ block comments are ignored by javadoc. "
                + "Javadoc comments must appear immediately before the class, method, or field they document."),

            mc("jdoc-mc-02", Topic.JAVADOC, 1,
                "Which Javadoc tag documents a method parameter?",
                "@arg", "@parameter", "@param", "@input",
                "c",
                "@param name description documents a single method parameter. "
                + "Use one @param tag per parameter. The name must match the actual parameter name. "
                + "Example: @param size the initial capacity of the list"),

            mc("jdoc-mc-03", Topic.JAVADOC, 2,
                "Which Javadoc tag documents the value a method returns?",
                "@result", "@return", "@output", "@yields",
                "b",
                "@return description documents what the method returns. "
                + "Omit it for void methods. "
                + "Example: @return the number of elements in the list, or -1 if empty"),

            mc("jdoc-mc-04", Topic.JAVADOC, 2,
                "Which Javadoc tag documents an exception a method may throw?",
                "@exception (synonym for @throws)", "@throws ExceptionType description", "@raise", "@error",
                "b",
                "@throws (or @exception) ExceptionType description documents checked and notable unchecked exceptions. "
                + "Example: @throws IllegalArgumentException if size is negative. "
                + "You should document all checked exceptions and important unchecked ones."),

            trace("jdoc-tr-01", Topic.JAVADOC, 2,
                "How many Javadoc tags are in this comment?",
                "/**\n"
                + " * Adds two integers.\n"
                + " * @param a the first addend\n"
                + " * @param b the second addend\n"
                + " * @return the sum of a and b\n"
                + " */\n"
                + "public int add(int a, int b) { return a + b; }",
                "3",
                "There are three Javadoc block tags: @param a, @param b, and @return. "
                + "The opening sentence is a summary (not a tag). Tags start with @."),

            debug("jdoc-db-01", Topic.JAVADOC, 2,
                "The Javadoc tool warns 'no @return'. What is wrong?",
                "/**\n"
                + " * Computes the square of n.\n"
                + " * @param n the input value\n"
                + " */\n"
                + "public int square(int n) {\n"
                + "    return n * n;\n"
                + "}",
                "The comment should use /* */ not /** */",
                "@return tag is missing for a non-void method",
                "@param n should be @param int n",
                "The method body must be in the comment",
                "b",
                "Non-void methods should have a @return tag describing what is returned. "
                + "Add: @return the square of n. "
                + "Javadoc will warn or produce incomplete HTML documentation without it."),

            codegen("jdoc-cg-01", Topic.JAVADOC, 2,
                "Which is a correctly formatted Javadoc comment for a method that divides a by b?",
                "// divides a by b, returns quotient",
                "/** Divides a by b. @param a dividend @param b divisor @return a / b @throws ArithmeticException if b is zero */",
                "/**\n * Divides a by b.\n * @param a the dividend\n * @param b the divisor\n * @return the quotient a / b\n * @throws ArithmeticException if b is zero\n */",
                "/* param: a, b; returns: double */",
                "c",
                "Option C uses proper /** */ syntax with each tag on its own line — the standard Javadoc style. "
                + "Option A is a plain line comment. Option B has no newlines (technically valid but unreadable). "
                + "Option D uses /* */ which javadoc ignores."),

            mc("jdoc-mc-05", Topic.JAVADOC, 2,
                "Which Javadoc tag creates a hyperlink to another class or method in the generated HTML?",
                "@link", "{@link ClassName}", "@see ClassName", "@href",
                "b",
                "{@link ClassName#methodName} creates an inline hyperlink in the Javadoc HTML. "
                + "@see creates a 'See Also' section (separate, not inline). "
                + "Example: 'Use {@link Collections#sort(List)} to sort.' "
                + "{@link} must be in curly braces."),

            mc("jdoc-mc-06", Topic.JAVADOC, 2,
                "What is the purpose of the @deprecated Javadoc tag (in addition to the @Deprecated annotation)?",
                "To delete the method from the compiled output",
                "To explain WHY the element is deprecated and what to use instead",
                "To prevent the method from being called at runtime",
                "To generate a compile error when the method is used",
                "b",
                "@Deprecated annotation triggers compiler warnings. "
                + "The @deprecated Javadoc tag (lowercase) provides the explanation: "
                + "@deprecated Use {@link #newMethod()} instead. "
                + "Both should be used together so tools and developers know about and understand the deprecation."),

            mc("jdoc-mc-07", Topic.JAVADOC, 1,
                "Which command generates HTML Javadoc documentation from source files?",
                "javac -doc MyClass.java",
                "javadoc MyClass.java",
                "java --docs MyClass",
                "mvn generate",
                "b",
                "The 'javadoc' tool (part of the JDK) processes /** */ comments and produces HTML. "
                + "For Maven projects: 'mvn javadoc:javadoc' generates docs in target/site/apidocs/. "
                + "Good Javadoc on public APIs lets IDEs show documentation on hover."),

            trace("jdoc-tr-02", Topic.JAVADOC, 2,
                "What will the generated Javadoc show for the summary sentence of this method?",
                "/**\n"
                + " * Returns the maximum of two integers.\n"
                + " * This method does not handle NaN.\n"
                + " * @param a first value\n"
                + " * @param b second value\n"
                + " * @return the larger of a and b\n"
                + " */\n"
                + "public int max(int a, int b) { return Math.max(a, b); }",
                "Returns the maximum of two integers.",
                "Javadoc uses the first sentence (up to the first period) as the method summary shown in tables. "
                + "The full first sentence is: 'Returns the maximum of two integers.'"),

            trace("jdoc-tr-03", Topic.JAVADOC, 2,
                "How many @param tags should this method's Javadoc have?",
                "public double average(int a, int b, int c) { return (a + b + c) / 3.0; }",
                "3",
                "There are 3 parameters (a, b, c), so 3 @param tags are needed — one per parameter. "
                + "Each should describe what the parameter represents."),

            debug("jdoc-db-02", Topic.JAVADOC, 2,
                "The generated Javadoc shows 'unknown tag: @returns'. What is wrong?",
                "/**\n"
                + " * Computes the area.\n"
                + " * @param radius the radius of the circle\n"
                + " * @returns the area in square units\n"
                + " */\n"
                + "public double area(double radius) { return Math.PI * radius * radius; }",
                "The method must be static to have Javadoc",
                "@returns is not a valid tag — the correct tag is @return (no 's')",
                "Javadoc doesn't support double types",
                "@param must come after @return",
                "b",
                "The Javadoc tag is @return (singular), not @returns. "
                + "Typos in tag names cause the javadoc tool to emit 'unknown tag' warnings and the tag is ignored in the output."),

            codegen("jdoc-cg-02", Topic.JAVADOC, 2,
                "Which is the best Javadoc comment for a public class named 'BankAccount'?",
                "// BankAccount class",
                "/** */",
                "/**\n * Represents a bank account with a balance and transaction history.\n * Instances are not thread-safe.\n */",
                "/** This is the BankAccount class. It is a class. */",
                "c",
                "Good class Javadoc summarizes the class's purpose, its key invariants or constraints (thread-safety), "
                + "and any design notes that help users. "
                + "Option A is a line comment (ignored by javadoc). Option B is empty. "
                + "Option D is redundant — it says nothing meaningful.")
        );
    }
}
