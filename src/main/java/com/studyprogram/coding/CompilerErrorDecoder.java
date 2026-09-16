package com.studyprogram.coding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a javac diagnostic into something a beginner can act on.
 *
 * <p>Compile errors are where new programmers stall, and the compiler writes for someone who
 * already knows Java: "incompatible types: int cannot be converted to boolean" is precise and
 * useless if you do not yet know that {@code if} needs a condition. Each rule here adds a plain
 * sentence saying what the compiler means and where to look — without saying what to type, so the
 * student still makes the fix.
 *
 * <p>Every decoded error also carries a short {@link Decoded#kind()}. That is what the attempt log
 * records, so a student's report can show the mistakes they keep making and an instructor's class
 * report can show where a whole cohort is stuck.
 */
public final class CompilerErrorDecoder {

    /** One diagnostic, with plain-language help when the pattern is recognised. */
    public record Decoded(String original, String kind, String explanation) {
        public boolean isRecognised() { return explanation != null; }
    }

    /** {@code generic} is the same advice with no specifics to fill in, for summary reports. */
    private record Rule(String kind, Pattern pattern, String explanation, String generic) {}

    /** The category recorded when nothing matches. */
    public static final String OTHER = "other";

    private static final List<Rule> RULES = new ArrayList<>();

    private static void rule(String kind, String regex, String explanation) {
        rule(kind, regex, explanation, explanation);
    }

    private static void rule(String kind, String regex, String explanation, String generic) {
        RULES.add(new Rule(kind, Pattern.compile(regex), explanation, generic));
    }

    static {
        // Ordering matters: the most specific reading of a message wins, so the narrow
        // "condition is not boolean" rule is registered before the general type-mismatch one.
        rule("condition is not boolean",
            "incompatible types:\\s*\\w+ cannot be converted to boolean",
            "A condition has to be true or false. A single = assigns a value, while comparing "
            + "takes == (or .equals for objects).");
        rule("void used as value",
            "'void' type not allowed here|incompatible types:\\s*void cannot be converted to",
            "You used the result of a method that returns nothing. A void method does its work "
            + "instead of handing a value back, so there is nothing to assign or print.");
        rule("number used as text",
            "incompatible types:\\s*(?:int|long|double|float|char) cannot be converted to String",
            "A number is not text. String.valueOf(...), or joining with \"\", converts it.");
        rule("incompatible types",
            "incompatible types:\\s*(\\S+) cannot be converted to (\\S+)",
            "You supplied a {0} where Java needs a {1}. Check the variable's declared type, or "
            + "the type the method promises to return.",
            "You supplied one type where Java needs a different one. Check the variable's "
            + "declared type, or the type the method promises to return.");
        rule("cannot find symbol", "cannot find symbol",
            "Java does not know this name here. Usually that is a typo, a variable declared "
            + "inside a different block, or a method you have not written yet. Check spelling "
            + "and capitalisation against where the name was declared.");
        rule("missing semicolon", "';' expected",
            "A statement is unfinished. The semicolon usually belongs at the end of the line "
            + "ABOVE the one reported.");
        rule("unbalanced parentheses",
            "'\\)'[^\\n]{0,20}expected|'\\('[^\\n]{0,20}expected|'\\]'[^\\n]{0,20}expected",
            "Brackets are unbalanced. Count the opening and closing brackets on the reported "
            + "line; one is missing or one too many.");
        rule("unclosed block",
            "'\\}' expected|'\\{' expected|reached end of file while parsing",
            "A block was opened and never closed. Check that every { has a matching }, and that "
            + "your indentation matches the nesting you meant.");
        rule("missing return", "missing return statement",
            "A method with a return type must return a value on EVERY path out of it — including "
            + "the branch where an if is false, or where a loop runs zero times.");
        rule("unreported exception", "unreported exception (\\S+)",
            "{0} is a checked exception: Java insists you either catch it or declare it with "
            + "throws on the method.",
            "That is a checked exception: Java insists you either catch it or declare it with "
            + "throws on the method.");
        rule("static context",
            "non-static (?:method|variable|field) (\\S+).*cannot be referenced from a static context",
            "{0} belongs to an object, but you are calling it from a static method where no "
            + "object exists. Either create an instance first, or make the member static too.",
            "That member belongs to an object, but you are using it from a static method where "
            + "no object exists. Either create an instance first, or make the member static too.");
        rule("variable already defined", "variable (\\S+) is already defined",
            "{0} is declared twice in the same scope. The second one does not need its type "
            + "again — assigning to the existing variable is enough.",
            "That variable is declared twice in the same scope. The second one does not need its "
            + "type again — assigning to the existing variable is enough.");
        rule("uninitialised variable", "variable (\\S+) might not have been initialized",
            "{0} can be read before anything is ever stored in it. Give it a starting value "
            + "where you declare it, or make sure every branch assigns it first.",
            "That variable can be read before anything is stored in it. Give it a starting value "
            + "where you declare it, or make sure every branch assigns it first.");
        rule("unreachable statement", "unreachable statement",
            "This line can never run — something above it always returns, breaks or throws "
            + "first.");
        rule("wrong arguments", "method (\\S+) in .* cannot be applied to given types",
            "{0} exists, but not with the arguments you passed. Compare the number, order and "
            + "types of your arguments against the method's parameters.",
            "The method exists, but not with the arguments you passed. Compare the number, order "
            + "and types of your arguments against the method's parameters.");
        rule("wrong constructor arguments",
            "constructor (\\S+) in class \\S+ cannot be applied to given types",
            "There is no {0} constructor taking those arguments. Check what the class's "
            + "constructor actually asks for.",
            "There is no constructor taking those arguments. Check what the class's constructor "
            + "actually asks for.");
        rule("abstract instantiation", "(\\S+) is abstract; cannot be instantiated",
            "{0} is abstract (or an interface), so it has no complete implementation to create. "
            + "Instantiate a concrete subclass instead.",
            "That type is abstract (or an interface), so it has no complete implementation to "
            + "create. Instantiate a concrete subclass instead.");
        rule("unimplemented method",
            "is not abstract and does not override abstract method (\\S+)",
            "Your class promises to implement something it has not supplied yet: {0}. Add that "
            + "method with exactly the signature the parent declares.",
            "Your class promises to implement a method it has not supplied yet. Add it with "
            + "exactly the signature the parent declares.");
        rule("bad operand types", "bad operand types for binary operator",
            "This operator does not work on those types. Notably < > <= >= do not work on "
            + "objects or Strings, and + between two Strings joins rather than adds.");
        rule("lossy conversion", "possible lossy conversion from (\\S+) to (\\S+)",
            "A {0} does not fit in a {1} without losing information, so Java will not do it "
            + "silently. Decide whether you want the wider type or an explicit cast.",
            "The value does not fit in the target type without losing information, so Java will "
            + "not do it silently. Decide whether you want the wider type or an explicit cast.");
        rule("array required", "array required, but (\\S+) found",
            "You used [ ] on a {0}, which is not an array. A List uses get(index) instead.",
            "You used [ ] on something that is not an array. A List uses get(index) instead.");
        rule("file name mismatch",
            "class (\\S+) is public, should be declared in a file named",
            "A public class must live in a file with exactly its own name, capitals included.");
        rule("illegal start", "illegal start of (?:expression|type)",
            "The compiler hit something unexpected here — very often a brace, bracket or "
            + "semicolon missing EARLIER that left it mid-statement.");
        rule("not a statement", "not a statement",
            "This line is not an action Java recognises. A bare expression is not a statement — "
            + "assign it, return it, or pass it somewhere.");
        rule("incomparable types", "incomparable types: (\\S+) and (\\S+)",
            "A {0} and a {1} can never be equal, so Java rejects the comparison as a mistake.",
            "Those two types can never be equal, so Java rejects the comparison as a mistake.");
        rule("private access", "(\\S+) has private access in (\\S+)",
            "{0} is private, so only code inside {1} may touch it. Use a public method the class "
            + "provides instead.",
            "That member is private, so only code inside its own class may touch it. Use a "
            + "public method the class provides instead.");
        rule("unclosed literal", "unclosed string literal|unclosed character literal",
            "A quote was opened and never closed on this line.");
        rule("duplicate method", "method (\\S+) is already defined",
            "{0} is declared twice with the same parameter types. Overloads must differ in their "
            + "parameters, not merely in their return type.",
            "That method is declared twice with the same parameter types. Overloads must differ "
            + "in their parameters, not merely in their return type.");
        rule("assignment to final", "cannot assign a value to final variable (\\S+)",
            "{0} is final, so it can be set only once. Either drop final, or work the value out "
            + "before assigning it.",
            "That variable is final, so it can be set only once. Either drop final, or work the "
            + "value out before assigning it.");
        rule("generic mismatch",
            "no suitable method found for|inference variable .* has incompatible bounds",
            "The generic types do not line up. Check the type arguments on the collection or "
            + "method against what you are actually passing in.");
        rule("class expected", "class, interface, enum, or record expected",
            "Something is outside any class — usually one } too many earlier in the file, which "
            + "closed the class before this code.");
    }

    private CompilerErrorDecoder() {}

    /** Decodes one raw javac message. */
    public static Decoded decode(String message) {
        String flat = message == null ? "" : message.replace('\n', ' ');
        for (Rule rule : RULES) {
            Matcher m = rule.pattern().matcher(flat);
            if (m.find()) {
                return new Decoded(message, rule.kind(), fill(rule.explanation(), m));
            }
        }
        return new Decoded(message, OTHER, null);
    }

    /** Decodes several messages, preserving order. */
    public static List<Decoded> decodeAll(List<String> messages) {
        List<Decoded> all = new ArrayList<>();
        for (String message : messages) all.add(decode(message));
        return all;
    }

    /** The distinct error categories in a set of messages — what the attempt log stores. */
    public static List<String> kindsOf(List<String> messages) {
        List<String> kinds = new ArrayList<>();
        for (String message : messages) {
            String kind = decode(message).kind();
            if (!kinds.contains(kind)) kinds.add(kind);
        }
        return kinds;
    }

    /** Generic advice for a recorded category, with no specifics to fill in. Empty if unknown. */
    public static String explanationFor(String kind) {
        for (Rule rule : RULES) {
            if (rule.kind().equals(kind)) return rule.generic();
        }
        return "";
    }

    /** Every category this decoder can report, for tests and documentation. */
    public static List<String> knownKinds() {
        List<String> kinds = new ArrayList<>();
        for (Rule rule : RULES) kinds.add(rule.kind());
        return kinds;
    }

    private static String fill(String text, Matcher m) {
        String out = text;
        for (int g = 1; g <= m.groupCount(); g++) {
            String value = m.group(g);
            out = out.replace("{" + (g - 1) + "}", value == null ? "it" : value);
        }
        return out;
    }
}
