package com.studyprogram.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Named things students get wrong, so a wrong answer can say what the student was probably
 * thinking instead of only what the right answer was.
 *
 * <p>"Incorrect. The correct answer is B" teaches almost nothing: it corrects the symptom and
 * leaves the belief that produced it untouched, which is why the same student picks the same kind
 * of wrong answer next week. A well-built multiple-choice question already encodes this — each
 * distractor is there because it is what someone with a particular wrong model would choose — but
 * until now that design was invisible to the program. Tagging a distractor with the misconception
 * it represents turns a wrong answer into a diagnosis.
 *
 * <p>The catalogue is deliberately small and general. These are reusable across hundreds of
 * questions precisely because they are the handful of ideas that actually trip people up, and a
 * catalogue that grew a new entry per question would tell a student nothing they could act on.
 */
public enum Misconception {

    STRING_IDENTITY("== compares references, not text",
            "For objects, == asks whether two names point at the same object, not whether they "
            + "hold the same value. Short string literals are shared, so == on them often looks "
            + "right until the text comes from input. Use .equals for the contents."),

    INTEGER_DIVISION("Dividing two ints throws away the remainder",
            "5 / 2 is 2, not 2.5. Java picks the operation from the operand types, not from what "
            + "you are assigning to, so the decimal is gone before the assignment happens. Make "
            + "one side a double if you want a fractional answer."),

    ARRAY_LENGTH_VS_SIZE("length, length() and size() are three different things",
            "An array has a field called length, a String has a method called length(), and a "
            + "collection has size(). Which one applies is decided by the type, and mixing them "
            + "up is a compile error rather than a wrong answer."),

    OFF_BY_ONE("A loop bound that runs one time too many or too few",
            "Indices run from 0 to length - 1, so <= length walks off the end and < length - 1 "
            + "stops one short. When a loop is wrong by exactly one element, the bound is "
            + "almost always why."),

    REFERENCE_SEMANTICS("Assigning an object copies the reference, not the object",
            "Two variables can name the same object, so a change made through one is visible "
            + "through the other. Arrays and collections passed to a method are not copies - the "
            + "method can change what the caller sees."),

    STRING_IMMUTABILITY("Strings are never modified in place",
            "Every String method returns a NEW string and leaves the original alone, so calling "
            + "one and ignoring the result does nothing at all. Assign the result to keep it."),

    STATIC_VS_INSTANCE("Static belongs to the class, instance to the object",
            "A static member exists once no matter how many objects there are, and cannot see "
            + "instance fields because there is no object to read them from. An instance member "
            + "needs an object before it means anything."),

    CHAR_VS_STRING("A char is a single character, a String is text",
            "'a' and \"a\" are different types. A char is a number underneath, so adding chars "
            + "adds their codes, while adding Strings joins them."),

    OPERATOR_PRECEDENCE("Operators do not all run left to right",
            "Multiplication and division happen before addition and subtraction, and comparison "
            + "happens before && and ||. When an expression surprises you, parenthesise what you "
            + "meant and see whether the answer changes."),

    ASSIGNMENT_VS_COMPARISON("= assigns, == compares",
            "A single = stores a value and evaluates to that value. In a condition Java rejects "
            + "it unless the type is boolean, in which case it silently assigns and tests."),

    POST_VS_PRE_INCREMENT("i++ uses the old value, ++i uses the new one",
            "Both leave i one larger. The difference is only what the expression itself gives "
            + "back, which matters when you use the result in the same statement."),

    SHORT_CIRCUIT("&& and || stop as soon as the answer is known",
            "If the left side of && is false, the right side never runs - which is what makes "
            + "a null check before a call on the same line safe. & and | do not do this."),

    FLOATING_POINT_EQUALITY("Decimal values are stored approximately",
            "0.1 + 0.2 is not exactly 0.3 in binary floating point, so == on doubles fails for "
            + "values that are mathematically equal. Compare the difference against a small "
            + "tolerance."),

    OVERRIDE_VS_OVERLOAD("Overriding replaces a method, overloading adds another",
            "An override has exactly the parent's signature and is chosen at run time by the "
            + "object's real type. Change the parameters and you have written a second, unrelated "
            + "method that the parent will never call."),

    CHECKED_EXCEPTIONS("A checked exception must be caught or declared",
            "Java forces you to say what happens to a checked exception. Catching Exception to "
            + "silence the compiler also catches the bugs you wanted to hear about."),

    NULL_VS_EMPTY("null is not the same as empty",
            "An empty string or list is an object you can safely ask questions of. null is the "
            + "absence of one, and any method call on it throws."),

    SCOPE("A variable exists only inside the block that declares it",
            "Declaring inside a loop or an if means it is gone at the closing brace. If it has "
            + "to outlive the block, declare it before the block."),

    RETURN_ENDS_METHOD("return leaves the method immediately",
            "Nothing after a return in the same branch ever runs. A return inside a loop ends "
            + "the whole method, not just that iteration - that is what break is for."),

    PASS_BY_VALUE("Reassigning a parameter does not affect the caller",
            "Java copies the value of the argument, so assigning to the parameter changes only "
            + "the method's own copy. Changing the object a reference parameter points AT is a "
            + "different thing, and that the caller does see."),

    SWITCH_FALLTHROUGH("A case without break falls into the next one",
            "In the classic switch form, execution continues into the following case until it "
            + "meets a break or the end. That is occasionally useful and usually a bug.");

    public final String summary;
    public final String explanation;

    Misconception(String summary, String explanation) {
        this.summary = summary;
        this.explanation = explanation;
    }

    /** Looks one up by enum name, case-insensitively; empty when the name is not in the catalogue. */
    public static Optional<Misconception> byId(String id) {
        if (id == null) return Optional.empty();
        String wanted = id.trim().toUpperCase().replace('-', '_');
        return Arrays.stream(values()).filter(m -> m.name().equals(wanted)).findFirst();
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }

    @Override
    public String toString() { return summary; }
}
