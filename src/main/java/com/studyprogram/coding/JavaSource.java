package com.studyprogram.coding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small helpers for working with Java source code held as strings. */
public final class JavaSource {

    // First public top-level type wins; falls back to the first top-level type of any kind.
    private static final Pattern PUBLIC_TYPE = Pattern.compile(
            "(?m)^\\s*public\\s+(?:final\\s+|abstract\\s+)*(?:class|interface|enum|record)\\s+([A-Za-z_$][\\w$]*)");
    private static final Pattern ANY_TYPE = Pattern.compile(
            "(?m)^\\s*(?:final\\s+|abstract\\s+)*(?:class|interface|enum|record)\\s+([A-Za-z_$][\\w$]*)");

    private JavaSource() {}

    /**
     * The name of the type that determines the source file's name:
     * the public top-level type if present, otherwise the first top-level type.
     *
     * @throws IllegalArgumentException if no type declaration can be found
     */
    public static String primaryTypeName(String source) {
        Matcher m = PUBLIC_TYPE.matcher(source);
        if (m.find()) return m.group(1);
        m = ANY_TYPE.matcher(source);
        if (m.find()) return m.group(1);
        throw new IllegalArgumentException("No class/interface/enum/record declaration found");
    }
}
