package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;
import com.studyprogram.coding.CodeSafetyScanner;
import com.studyprogram.questions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Central registry of all questions.
 *
 * Every question is JSON, from one of two places:
 *   1. Files bundled inside the jar under the {@code questions/} classpath root.
 *   2. Files in an external {@code data/questions/<topic-slug>/} directory next to the working
 *      directory. External files override bundled questions with the same ID, so anyone can fix
 *      or replace shipped questions without rebuilding — and every shipped question is editable
 *      the same way the ones you write are.
 *
 * Problems found while loading (duplicate IDs, directories that match no topic) are
 * collected as warnings via {@link #getWarnings()} rather than failing the whole load.
 */
public class QuestionBank {

    public static final Path DEFAULT_EXTERNAL_DIR = Paths.get("data", "questions");
    private static final String CLASSPATH_ROOT = "/questions";

    private final Map<String, Question> byId = new LinkedHashMap<>();
    /** Code of questions bundled in the jar, by id — an external copy matching this is first-party. */
    private final Map<String, List<String>> bundledCode = new HashMap<>();
    private final Map<Topic, List<Question>> byTopic = new EnumMap<>(Topic.class);
    private final List<String> warnings = new ArrayList<>();

    public QuestionBank() {
        this(DEFAULT_EXTERNAL_DIR);
    }

    /** Private no-load constructor backing {@link #of(Collection)}. */
    private QuestionBank(Collection<Question> questions, boolean derive) {
        for (Question q : questions) byId.put(q.getId(), q);
        if (derive) { deriveParsons(); deriveFadedExamples(); }
        rebuildTopicIndex();
    }

    /**
     * A bank containing exactly the given questions, with nothing loaded from disk or the
     * classpath. Lets callers — tests especially — exercise the engine and the interactive flow
     * against a small, predictable set instead of the whole shipped bank.
     */
    public static QuestionBank of(Collection<Question> questions) {
        return new QuestionBank(questions, false);
    }

    public QuestionBank(Path externalQuestionsDir) {
        loadFromClasspath();
        loadExternal(externalQuestionsDir);
        screenUntrusted();
        deriveParsons();
        deriveFadedExamples();
        rebuildTopicIndex();
    }

    /**
     * Screens questions loaded from an external overlay for capabilities a practice exercise
     * should not need. Anything flagged is refused rather than compiled and run, because loading
     * a question file means executing the code inside it. Set {@code JAVASTUDY_TRUST_EXTERNAL=1}
     * to load them anyway (only for question packs you wrote or reviewed yourself).
     */
    private void screenUntrusted() {
        boolean optedIn = "1".equals(System.getenv("JAVASTUDY_TRUST_EXTERNAL"))
                || Boolean.getBoolean("javastudy.trustExternal");
        // With no OS containment there is only one layer of defence left, so the lexical screen
        // has to be the whole answer rather than a first pass. On Windows in particular there is
        // no backend at all, and running a stranger's code there with neither containment nor a
        // clean scan is the one combination worth refusing outright.
        boolean contained = com.studyprogram.coding.Sandbox.backend()
                != com.studyprogram.coding.Sandbox.Backend.NONE;

        List<String> refused = new ArrayList<>();
        for (Question q : byId.values()) {
            if (q.isTrusted() || !q.isCoding()) continue;   // only coding exercises execute
            if (q.allCode().equals(bundledCode.get(q.getId()))) continue;   // unmodified first-party copy
            var findings = CodeSafetyScanner.scan(q.allCode());

            if (findings.isEmpty()) {
                if (!contained && !optedIn) {
                    refused.add(q.getId());
                    warnings.add("REFUSED external question '" + q.getId()
                            + "': this machine has no exercise sandbox, so third-party code is "
                            + "not run. Set JAVASTUDY_TRUST_EXTERNAL=1 only for packs you wrote "
                            + "or reviewed yourself.");
                }
                continue;
            }
            String detail = findings.stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
            if (optedIn) {
                warnings.add("External question '" + q.getId() + "' uses " + detail
                        + " — loaded anyway because external questions are trusted.");
            } else {
                refused.add(q.getId());
                warnings.add("REFUSED external question '" + q.getId() + "': uses " + detail
                        + ". Set JAVASTUDY_TRUST_EXTERNAL=1 only if you wrote or reviewed it.");
            }
        }
        refused.forEach(byId::remove);
    }

    /**
     * Auto-derive a Parsons (reorder-the-lines) variant from every coding exercise
     * with a suitably sized solution. Derived deterministically at load time, so the
     * Parsons bank grows with the coding bank at no authoring cost.
     */
    private void deriveParsons() {
        List<Question> derived = new ArrayList<>();
        for (Question q : byId.values()) {
            ParsonsDeriver.derive(q).ifPresent(derived::add);
        }
        for (Question p : derived) {
            if (!byId.containsKey(p.getId())) byId.put(p.getId(), p);
        }
    }

    /**
     * Auto-derive the faded worked-example ladder from every coding exercise: study a correct
     * solution with one line missing, then with three. Derived after Parsons so the ladder from
     * reading code to writing it is complete, and at the same zero authoring cost.
     */
    private void deriveFadedExamples() {
        List<Question> derived = new ArrayList<>();
        for (Question q : byId.values()) {
            derived.addAll(FadedExampleDeriver.derive(q));
        }
        for (Question f : derived) {
            if (!byId.containsKey(f.getId())) byId.put(f.getId(), f);
        }
    }

    // ── Loading ──────────────────────────────────────────────────────────────

    /**
     * Loads JSON questions bundled inside the jar (or target/classes) under
     * {@code questions/<topic-slug>/}. Works both when running from an exploded
     * classpath directory and from inside a fat jar.
     */
    private void loadFromClasspath() {
        var url = QuestionBank.class.getResource(CLASSPATH_ROOT);
        if (url == null) return;   // nothing bundled (e.g. running from an IDE before packaging)
        try {
            URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) {
                    loadQuestionTree(fs.getPath(CLASSPATH_ROOT), "bundled");
                }
                byId.values().forEach(q -> bundledCode.put(q.getId(), q.allCode()));
            } else {
                loadQuestionTree(Path.of(uri), "bundled");
            }
            byId.values().forEach(q -> bundledCode.put(q.getId(), q.allCode()));
        } catch (IOException | URISyntaxException e) {
            warnings.add("Could not read bundled questions: " + e.getMessage());
        }
    }

    /** Loads the external overlay directory. Same-ID questions override earlier sources. */
    private void loadExternal(Path root) {
        if (root == null || !Files.isDirectory(root)) return;
        try {
            loadQuestionTree(root, "external");
        } catch (IOException e) {
            warnings.add("Could not read external questions from " + root + ": " + e.getMessage());
        }
    }

    /**
     * Walks {@code root}/&lt;topic-slug&gt;/*.json. Directories that match no topic slug
     * are reported as warnings so misnamed content is never silently dropped.
     */
    private void loadQuestionTree(Path root, String sourceLabel) throws IOException {
        Set<String> seen = new HashSet<>();
        boolean override = "external".equals(sourceLabel);
        try (Stream<Path> dirs = Files.list(root)) {
            for (Path dir : dirs.filter(Files::isDirectory).sorted().toList()) {
                String slug = dir.getFileName().toString();
                Topic topic = Topic.fromDirSlug(slug);
                if (topic == null) {
                    warnings.add("Question directory '" + slug + "' (" + sourceLabel
                            + ") matches no topic — its questions were NOT loaded.");
                    continue;
                }
                try (Stream<Path> files = Files.list(dir)) {
                    for (Path p : files.filter(f -> f.getFileName().toString().endsWith(".json"))
                                       .sorted().toList()) {
                        try (InputStream in = Files.newInputStream(p)) {
                            Question q = JsonQuestionParser.parse(in, topic, !override);
                            if (override && byId.containsKey(q.getId()) && !seen.contains(q.getId())) {
                                byId.put(q.getId(), q);   // external file replaces bundled question
                                seen.add(q.getId());
                            } else {
                                putQuestion(q, seen, sourceLabel + " file " + p.getFileName());
                            }
                        } catch (IOException | RuntimeException e) {
                            warnings.add("Skipping " + p + " — " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void putQuestion(Question q, Set<String> seenThisPhase, String source) {
        if (seenThisPhase.contains(q.getId())) {
            warnings.add("Duplicate question ID '" + q.getId() + "' (" + source + ") — skipped.");
            return;
        }
        if (byId.containsKey(q.getId())) {
            // same ID from an earlier phase: hardcoded questions win over bundled JSON copies
            seenThisPhase.add(q.getId());
            return;
        }
        byId.put(q.getId(), q);
        seenThisPhase.add(q.getId());
    }

    private void rebuildTopicIndex() {
        byTopic.clear();
        for (Question q : byId.values()) {
            byTopic.computeIfAbsent(q.getTopic(), k -> new ArrayList<>()).add(q);
        }
    }

    // ── Query API ────────────────────────────────────────────────────────────

    public List<Question> getQuestionsForTopic(Topic topic) {
        return Collections.unmodifiableList(byTopic.getOrDefault(topic, List.of()));
    }

    public List<Question> getQuestionsForTopics(Collection<Topic> topics) {
        return topics.stream()
                     .flatMap(t -> getQuestionsForTopic(t).stream())
                     .collect(Collectors.toList());
    }

    public Optional<Question> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public int totalQuestions() {
        return byId.size();
    }

    public Map<Topic, Integer> questionCountsByTopic() {
        return byTopic.entrySet().stream()
                      .collect(Collectors.toMap(Map.Entry::getKey,
                                                e -> e.getValue().size(),
                                                (a, b) -> a,
                                                () -> new EnumMap<>(Topic.class)));
    }

    /** Problems found while loading (misnamed directories, duplicate IDs, bad JSON). */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
