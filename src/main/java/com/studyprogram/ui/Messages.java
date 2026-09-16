package com.studyprogram.ui;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * User-facing text, looked up by key so the program can be translated.
 *
 * <p>Strings live in {@code src/main/resources/messages.properties}. To add a language, copy that
 * file to {@code messages_<language>.properties} (for example {@code messages_es.properties}),
 * translate the values, and run with that locale — nothing in the code changes. English ships as
 * the default and is the fallback for any key a translation has not covered yet.
 *
 * <p>A missing key is a bug, not a crash: the key itself is returned so the screen stays usable,
 * and {@code MessagesTest} fails the build if the code asks for a key the bundle does not define.
 */
public final class Messages {

    private static final String BUNDLE = "messages";
    private static ResourceBundle bundle = load(Locale.getDefault());

    private Messages() {}

    private static ResourceBundle load(Locale locale) {
        try {
            return ResourceBundle.getBundle(BUNDLE, locale);
        } catch (MissingResourceException e) {
            return ResourceBundle.getBundle(BUNDLE, Locale.ENGLISH);
        }
    }

    /** Switches language at runtime (used by tests and by a future --locale flag). */
    public static void setLocale(Locale locale) {
        bundle = load(locale);
    }

    /** The text for a key, with {0}-style placeholders filled in. */
    public static String get(String key, Object... args) {
        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;   // visible, harmless, and caught by the test suite
        }
        return args.length == 0 ? pattern : MessageFormat.format(pattern, args);
    }

    /** True when the bundle defines this key — used by the completeness test. */
    public static boolean has(String key) {
        return bundle.containsKey(key);
    }
}
