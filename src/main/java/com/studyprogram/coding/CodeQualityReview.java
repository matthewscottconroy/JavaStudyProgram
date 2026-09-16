package com.studyprogram.coding;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Advice on code that already works.
 *
 * <p>Tests are binary, and a student can pass every one of them with a sixty-line method called
 * {@code doStuff}, four levels of nesting and a variable named {@code x2} — and hear "all tests
 * passed!" and move on. Nobody ever tells them. That is the gap this fills: once the tests are
 * green, a short structural read of their source points at the things an instructor would circle
 * in the margin.
 *
 * <p>Two rules govern everything here. <b>It never fails an exercise.</b> The tests decide whether
 * the program is correct; this is commentary, and a student who ignores it has still finished.
 * <b>It never nags.</b> At most {@link #MAX_NOTES} notes, the most important first, so the advice
 * stays readable instead of becoming a wall the student learns to scroll past.
 *
 * <p>Findings come from the real syntax tree via the compiler API rather than from pattern
 * matching on text, so a brace inside a string literal or a comment cannot manufacture a finding.
 */
public final class CodeQualityReview {

    /** More advice than this at once stops being advice. */
    public static final int MAX_NOTES = 4;

    /** Statements in one method before it is worth breaking up. */
    static final int LONG_METHOD = 25;
    /** Nesting depth before the logic is hard to follow. */
    static final int DEEP_NESTING = 4;
    /** Parameters before a method is asking for too much. */
    static final int MANY_PARAMETERS = 5;

    /**
     * One observation. {@code severity} orders the list: lower is more important, so a swallowed
     * exception is reported before a naming nit.
     */
    public record Note(int severity, String summary, String why) {}

    private CodeQualityReview() {}

    /**
     * Reviews a student's source. Returns an empty list when the code is fine, when nothing can be
     * parsed, or when no compiler is available — advice is a bonus, never a prerequisite.
     */
    public static List<Note> review(String source) {
        CompilationUnitTree unit = parse(source);
        if (unit == null) return List.of();

        Collector collector = new Collector();
        collector.scan(unit, null);

        List<Note> notes = new ArrayList<>(collector.notes.values());
        notes.sort((a, b) -> a.severity() - b.severity());
        return notes.size() > MAX_NOTES ? new ArrayList<>(notes.subList(0, MAX_NOTES)) : notes;
    }

    /** Renders the review for a terminal, or an empty string when there is nothing to say. */
    public static String render(String source) {
        List<Note> notes = review(source);
        if (notes.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (Note note : notes) {
            out.append("  • ").append(note.summary()).append('\n')
               .append("    ").append(note.why()).append('\n');
        }
        return out.toString();
    }

    private static CompilationUnitTree parse(String source) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) return null;   // running on a JRE
        JavaFileObject file = new SimpleJavaFileObject(
                URI.create("string:///Review.java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try {
            JavacTask task = (JavacTask) compiler.getTask(
                    null, null, diagnostic -> {}, List.of("-proc:none"), null, List.of(file));
            Trees.instance(task);   // forces the parser to attach positions
            for (CompilationUnitTree unit : task.parse()) return unit;
        } catch (Exception | Error e) {
            // A syntax error, or a compiler that will not cooperate. The tests already passed,
            // so there is nothing here worth surfacing to the student as a problem.
            return null;
        }
        return null;
    }

    /** Walks the tree once, recording at most one note per kind of problem. */
    private static final class Collector extends TreeScanner<Void, Void> {

        /** Keyed by note kind so a repeated problem is mentioned once, not once per occurrence. */
        final Map<String, Note> notes = new LinkedHashMap<>();
        private int depth;

        private void note(String kind, int severity, String summary, String why) {
            notes.putIfAbsent(kind, new Note(severity, summary, why));
        }

        @Override
        public Void visitClass(ClassTree node, Void ignored) {
            String name = node.getSimpleName().toString();
            if (!name.isEmpty() && !Character.isUpperCase(name.charAt(0))) {
                note("class-name", 40, "Class " + name + " should start with a capital letter.",
                        "Java code everywhere follows this, so a lower-case class name reads as a "
                        + "variable to anyone else looking at your file.");
            }
            return super.visitClass(node, ignored);
        }

        @Override
        public Void visitMethod(MethodTree node, Void ignored) {
            String name = node.getName().toString();
            if (node.getBody() != null) {
                int statements = countStatements(node.getBody());
                if (statements > LONG_METHOD) {
                    note("long-method", 20,
                            name + " is " + statements + " statements long.",
                            "A method that long is usually doing several jobs. Look for a block "
                            + "with a name you could give it, and move it into its own method.");
                }
            }
            if (node.getParameters().size() >= MANY_PARAMETERS) {
                note("many-params", 35,
                        name + " takes " + node.getParameters().size() + " parameters.",
                        "Long parameter lists are easy to pass in the wrong order. Often several "
                        + "of them belong together in one object.");
            }
            if (!name.isEmpty() && Character.isUpperCase(name.charAt(0))
                    && !name.equals("<init>")) {
                note("method-name", 45, "Method " + name + " should start lower-case.",
                        "Capitalised names are for classes; methods and variables start "
                        + "lower-case and use camelCase after that.");
            }
            if (VAGUE_NAMES.contains(name)) {
                note("vague-method-name", 30, name + " does not say what the method does.",
                        "A name is the shortest documentation you will ever write. Say what it "
                        + "returns or what it changes.");
            }
            return super.visitMethod(node, ignored);
        }

        @Override
        public Void visitVariable(VariableTree node, Void ignored) {
            String name = node.getName().toString();
            if (name.length() == 1 && !LOOP_NAMES.contains(name)
                    && node.getInitializer() != null) {
                note("short-name", 50, "The variable " + name + " could use a real name.",
                        "Single letters are fine for loop counters and little else. A name like "
                        + "total or index saves the reader from working out what it holds.");
            }
            return super.visitVariable(node, ignored);
        }

        @Override
        public Void visitCatch(CatchTree node, Void ignored) {
            if (node.getBlock().getStatements().isEmpty()) {
                note("empty-catch", 10, "An empty catch block hides the error it catches.",
                        "If a failure genuinely can be ignored, say so in a comment. Otherwise "
                        + "print it, handle it, or let it out — silently swallowing it is how "
                        + "bugs go unnoticed for weeks.");
            }
            return super.visitCatch(node, ignored);
        }

        @Override
        public Void visitBinary(BinaryTree node, Void ignored) {
            // The tests may well pass with == on Strings: short literals are interned, so the
            // comparison is accidentally true right up until the string comes from input.
            if ((node.getKind() == Tree.Kind.EQUAL_TO || node.getKind() == Tree.Kind.NOT_EQUAL_TO)
                    && (isStringLiteral(node.getLeftOperand())
                        || isStringLiteral(node.getRightOperand()))) {
                note("string-equality", 5, "Comparing Strings with == compares identity, not text.",
                        "This can pass its tests and still be wrong: short literals are shared, so "
                        + "== looks right until the text comes from input. Use .equals.");
            }
            return super.visitBinary(node, ignored);
        }

        // ── Nesting depth ────────────────────────────────────────────────────

        @Override public Void visitIf(IfTree n, Void i)         { return nested(() -> super.visitIf(n, i)); }
        @Override public Void visitForLoop(ForLoopTree n, Void i){ return nested(() -> super.visitForLoop(n, i)); }
        @Override public Void visitWhileLoop(WhileLoopTree n, Void i) { return nested(() -> super.visitWhileLoop(n, i)); }
        @Override public Void visitDoWhileLoop(DoWhileLoopTree n, Void i) { return nested(() -> super.visitDoWhileLoop(n, i)); }
        @Override public Void visitEnhancedForLoop(EnhancedForLoopTree n, Void i) { return nested(() -> super.visitEnhancedForLoop(n, i)); }

        private Void nested(java.util.function.Supplier<Void> body) {
            depth++;
            if (depth >= DEEP_NESTING) {
                note("deep-nesting", 15, "Logic nested " + depth + " levels deep.",
                        "Each level is another condition the reader has to hold in mind. An early "
                        + "return, or pulling the inner block into its own method, usually "
                        + "flattens it.");
            }
            try {
                return body.get();
            } finally {
                depth--;
            }
        }

        private static int countStatements(BlockTree block) {
            int[] count = {0};
            new TreeScanner<Void, Void>() {
                @Override public Void scan(Tree node, Void ignored) {
                    if (node instanceof StatementTree && !(node instanceof BlockTree)) count[0]++;
                    return super.scan(node, ignored);
                }
            }.scan(block, null);
            return count[0];
        }

        private static boolean isStringLiteral(ExpressionTree tree) {
            return tree instanceof LiteralTree literal && literal.getValue() instanceof String;
        }

        private static final List<String> LOOP_NAMES = List.of("i", "j", "k", "n", "x", "y");
        private static final List<String> VAGUE_NAMES =
                List.of("doStuff", "doIt", "process", "handle", "stuff", "thing", "method1", "foo");
    }
}
