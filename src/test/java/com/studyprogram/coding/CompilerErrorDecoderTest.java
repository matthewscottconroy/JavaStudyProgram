package com.studyprogram.coding;

import org.junit.jupiter.api.Test;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The decoder is only worth anything if its patterns match what javac actually prints, so this
 * test does not hand it hand-written strings: it compiles genuinely broken programs and decodes
 * the real diagnostics.
 */
class CompilerErrorDecoderTest {

    /** Compiles a source string and returns javac's raw error messages. */
    private static List<String> errorsFrom(String source) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "these tests need a JDK");
        DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
        JavaFileObject file = new SimpleJavaFileObject(
                URI.create("string:///Broken.java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        compiler.getTask(null, null, diags, List.of("-proc:none"), null, List.of(file)).call();
        List<String> messages = new ArrayList<>();
        for (var d : diags.getDiagnostics()) {
            if (d.getKind() == javax.tools.Diagnostic.Kind.ERROR) messages.add(d.getMessage(null));
        }
        assertFalse(messages.isEmpty(), "expected this program not to compile:\n" + source);
        return messages;
    }

    private static void assertDecodes(String expectedKind, String source) {
        List<String> messages = errorsFrom(source);
        List<String> kinds = CompilerErrorDecoder.kindsOf(messages);
        assertTrue(kinds.contains(expectedKind),
                "expected kind '" + expectedKind + "' from javac saying " + messages
                + " but decoded " + kinds);
    }

    @Test
    void decodesTheMistakesBeginnersActuallyMake() {
        assertDecodes("cannot find symbol",
                "class B { void f() { System.out.println(tota1); } }");
        assertDecodes("missing semicolon",
                "class B { void f() { int x = 1 } }");
        assertDecodes("missing return",
                "class B { int f(boolean b) { if (b) return 1; } }");
        assertDecodes("unreported exception",
                "class B { void f() throws Exception { g(); } "
                + "void h() { g(); } void g() throws java.io.IOException {} }");
        assertDecodes("static context",
                "class B { int n; int get() { return n; } "
                + "public static void main(String[] a) { System.out.println(get()); } }");
        assertDecodes("condition is not boolean",
                "class B { void f() { int x = 1; if (x) { } } }");
        assertDecodes("lossy conversion",
                "class B { void f() { double d = 1.5; int i = d; } }");
        assertDecodes("variable already defined",
                "class B { void f() { int x = 1; int x = 2; } }");
        assertDecodes("uninitialised variable",
                "class B { void f() { int x; System.out.println(x); } }");
        assertDecodes("unreachable statement",
                "class B { int f() { return 1; int x = 2; } }");
        assertDecodes("wrong arguments",
                "class B { void g(int a, int b) {} void f() { g(1); } }");
        assertDecodes("abstract instantiation",
                "abstract class A {} class B { void f() { A a = new A(); } }");
        assertDecodes("unimplemented method",
                "interface I { void go(); } class B implements I { }");
        assertDecodes("bad operand types",
                "class B { void f() { String s = \"a\"; if (s < \"b\") {} } }");
        assertDecodes("array required",
                "import java.util.*; class B { void f() { List<String> l = new ArrayList<>(); "
                + "System.out.println(l[0]); } }");
        assertDecodes("not a statement",
                "class B { void f() { int x = 1; x + 1; } }");
        assertDecodes("incomparable types",
                "class B { void f() { String s = \"a\"; Integer i = 1; if (s == i) {} } }");
        assertDecodes("private access",
                "class A { private int n; } class B { void f() { System.out.println(new A().n); } }");
        assertDecodes("unclosed literal",
                "class B { void f() { String s = \"oops; } }");
        assertDecodes("duplicate method",
                "class B { void f() {} void f() {} }");
        assertDecodes("assignment to final",
                "class B { void f() { final int x = 1; x = 2; } }");
        assertDecodes("void used as value",
                "class B { void g() {} void f() { int x = g(); } }");
        assertDecodes("unclosed block",
                "class B { void f() { int x = 1; ");
        assertDecodes("class expected",
                "class B { void f() {} } }");
        assertDecodes("unbalanced parentheses",
                "class B { void f() { System.out.println(\"hi\"; } }");
        assertDecodes("illegal start",
                "class B { void f() { int x = ; } }");
    }

    @Test
    void everyKnownKindHasAdviceThatNamesNoPlaceholders() {
        for (String kind : CompilerErrorDecoder.knownKinds()) {
            String advice = CompilerErrorDecoder.explanationFor(kind);
            assertFalse(advice.isBlank(), kind + " has no generic advice");
            assertFalse(advice.contains("{0}") || advice.contains("{1}"),
                    kind + " leaks an unfilled placeholder into report text: " + advice);
        }
    }

    @Test
    void unrecognisedMessagesAreReportedHonestlyRatherThanGuessedAt() {
        CompilerErrorDecoder.Decoded d =
                CompilerErrorDecoder.decode("some future javac message nobody has seen");
        assertFalse(d.isRecognised());
        assertEquals(CompilerErrorDecoder.OTHER, d.kind());
        assertNull(d.explanation());
    }

    @Test
    void specificsFromTheDiagnosticAreQuotedBackToTheStudent() {
        CompilerErrorDecoder.Decoded d = CompilerErrorDecoder.decode(
                "unreported exception java.io.IOException; must be caught or declared to be thrown");
        assertEquals("unreported exception", d.kind());
        assertTrue(d.explanation().contains("java.io.IOException"),
                "the explanation should name the actual exception: " + d.explanation());
    }

    @Test
    void theRunnerAttachesDecodedAdviceAndCategoriesToCompileFailures() {
        CodingResult result = new CodingExerciseRunner().compileAndTest(
                java.util.Map.of("Broken.java", "public class Broken { int f() { } }"),
                "public class BrokenTest { public static void main(String[] a) {} }");

        assertEquals(CodingResult.Status.COMPILE_ERROR, result.status());
        assertTrue(result.output().contains("missing return statement"),
                "javac's own wording must survive: " + result.output());
        assertTrue(result.output().contains("What that means"),
                "the decoded reading must be attached: " + result.output());
        assertEquals(List.of("missing return"), result.errorKinds());
    }
}
