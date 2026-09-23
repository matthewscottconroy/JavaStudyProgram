package com.studyprogram.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Keeps the translation bundle honest: every key the code asks for must exist, and every language
 * that ships must cover the same keys as English.
 */
class MessagesTest {

    private static final Pattern LOOKUP = Pattern.compile("Messages\\.get\\(\"([^\"]+)\"");

    @AfterEach
    void resetLocale() {
        Messages.setLocale(Locale.getDefault());
    }

    @Test
    void everyKeyTheCodeAsksForIsDefined() throws IOException {
        List<String> missing = new ArrayList<>();
        try (Stream<Path> sources = Files.walk(Path.of("src/main/java"))) {
            for (Path file : sources.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher m = LOOKUP.matcher(Files.readString(file));
                while (m.find()) {
                    String key = m.group(1);
                    if (!Messages.has(key)) {
                        missing.add(key + " (" + file.getFileName() + ")");
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "undefined message key(s): " + missing);
    }

    @Test
    void placeholdersAreFilledIn() {
        assertTrue(Messages.get("banner.questions", 2738).contains("2,738")
                || Messages.get("banner.questions", 2738).contains("2738"));
        assertTrue(Messages.get("profile.created", "Ada").contains("Ada"));
    }

    @Test
    void anUnknownKeyDegradesToTheKeyRatherThanCrashing() {
        assertEquals("no.such.key.exists", Messages.get("no.such.key.exists"));
        assertFalse(Messages.has("no.such.key.exists"));
    }

    @Test
    void anUntranslatedLocaleFallsBackToEnglish() {
        Messages.setLocale(Locale.forLanguageTag("qq"));   // a language we certainly do not ship
        assertEquals("Java Study Program", Messages.get("app.title"),
                "an unknown locale must fall back rather than show keys");
    }

    @Test
    void translationsCoverTheSameKeysAsEnglish() throws IOException {
        Path resources = Path.of("src/main/resources");
        var english = new java.util.Properties();
        try (var in = Files.newInputStream(resources.resolve("messages.properties"))) {
            english.load(in);
        }
        assertFalse(english.isEmpty(), "the default bundle must not be empty");

        try (Stream<Path> files = Files.list(resources)) {
            for (Path translation : files
                    .filter(f -> f.getFileName().toString().matches("messages_.+\\.properties"))
                    .toList()) {
                var translated = new java.util.Properties();
                try (var in = Files.newInputStream(translation)) {
                    translated.load(in);
                }
                List<String> gaps = english.stringPropertyNames().stream()
                        .filter(k -> !translated.containsKey(k))
                        .sorted().toList();
                assertTrue(gaps.isEmpty(),
                        translation.getFileName() + " is missing " + gaps.size() + " key(s): "
                                + gaps.subList(0, Math.min(5, gaps.size())));
            }
        }
    }

    @Test
    void translationsKeepEveryPlaceholderTheEnglishHas() throws IOException {
        Path resources = Path.of("src/main/resources");
        var english = load(resources.resolve("messages.properties"));

        try (Stream<Path> files = Files.list(resources)) {
            for (Path translation : files
                    .filter(f -> f.getFileName().toString().matches("messages_.+\\.properties"))
                    .toList()) {
                var translated = load(translation);
                List<String> broken = new ArrayList<>();
                for (String key : english.stringPropertyNames()) {
                    String from = english.getProperty(key);
                    String to = translated.getProperty(key, "");
                    for (int i = 0; i < 3; i++) {
                        String slot = "{" + i + "}";
                        // A translator who drops {0} does not get a compile error; they get a
                        // sentence that silently loses the number it was about.
                        if (from.contains(slot) && !to.contains(slot)) {
                            broken.add(key + " lost " + slot);
                        }
                    }
                }
                assertTrue(broken.isEmpty(), translation.getFileName() + ": " + broken);
            }
        }
    }

    @Test
    void spanishLoadsWithItsAccentsIntact() {
        Messages.setLocale(Locale.forLanguageTag("es"));

        assertEquals("Programa de Estudio de Java", Messages.get("app.title"));
        assertTrue(Messages.get("answer.correct").contains("¡"),
                "a mangled encoding shows up here first: " + Messages.get("answer.correct"));
        assertTrue(Messages.get("menu.examMode").contains("cronometrado"));
        assertTrue(Messages.get("profile.created", "Ada").contains("Ada"),
                "placeholders must still work in translation");
    }

    @Test
    void aTranslationIsUsedOnlyForTheLocaleThatAsksForIt() {
        Messages.setLocale(Locale.ENGLISH);
        assertEquals("Java Study Program", Messages.get("app.title"));
        Messages.setLocale(Locale.forLanguageTag("es"));
        assertNotEquals("Java Study Program", Messages.get("app.title"));
    }

    /** Properties files are UTF-8 here, so they must be read as UTF-8 rather than ISO-8859-1. */
    private static java.util.Properties load(Path file) throws IOException {
        var props = new java.util.Properties();
        try (var reader = Files.newBufferedReader(file, java.nio.charset.StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }
}
