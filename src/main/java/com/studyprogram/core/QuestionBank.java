package com.studyprogram.core;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;
import com.studyprogram.questions.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central registry of all questions. Loads from every registered {@link QuestionLoader}.
 * To add questions for a new topic, create a QuestionLoader implementation and add it
 * to the {@code ALL_LOADERS} list in {@link #registerAll()}.
 */
public class QuestionBank {

    private final Map<Topic, List<Question>> byTopic = new EnumMap<>(Topic.class);
    private final Map<String, Question> byId = new HashMap<>();

    public QuestionBank() {
        registerAll();
    }

    // ── Registration ─────────────────────────────────────────────────────────

    private void registerAll() {
        List<QuestionLoader> loaders = List.of(
                // ── Fundamentals ──────────────────────────────────────────
                new VariablesQuestionLoader(),
                new PrintingQuestionLoader(),
                new ArgumentsQuestionLoader(),
                new HexBinaryQuestionLoader(),
                new CompilingRunningQuestionLoader(),

                // ── Control Flow ──────────────────────────────────────────
                new IfCaseQuestionLoader(),
                new LoopsQuestionLoader(),
                new FunctionsQuestionLoader(),

                // ── Strings & Collections ─────────────────────────────────
                new StringsQuestionLoader(),
                new ArraysQuestionLoader(),
                new ArraysArrayListsQuestionLoader(),
                new CollectionsQuestionLoader(),

                // ── OOP ───────────────────────────────────────────────────
                new ClassesQuestionLoader(),
                new AccessModifiersQuestionLoader(),
                new MemoryReferenceStaticQuestionLoader(),
                new InterfacesQuestionLoader(),
                new InheritanceQuestionLoader(),
                new CompositionQuestionLoader(),
                new ClassHierarchyQuestionLoader(),
                new InheritancePolymorphismQuestionLoader(),

                // ── Error Handling & I/O ──────────────────────────────────
                new ExceptionHandlingQuestionLoader(),
                new FileProcessingQuestionLoader(),

                // ── Advanced & Tooling ────────────────────────────────────
                new GenericsQuestionLoader(),
                new LambdasQuestionLoader(),
                new StreamApiQuestionLoader(),
                new UnitTestsQuestionLoader(),
                new MavenQuestionLoader(),

                // ── GUI ───────────────────────────────────────────────────
                new GuiSwingQuestionLoader(),
                new GuiEventModelQuestionLoader(),
                new GuiComponentsQuestionLoader(),
                new GuiLayoutQuestionLoader(),
                new MouseInputQuestionLoader(),
                new KeyboardInputQuestionLoader(),
                new PaintingQuestionLoader(),
                new Graphics2DQuestionLoader(),
                new ImagesSoundQuestionLoader(),
                new SwingComponentsQuestionLoader(),

                // ── Advanced Topics ───────────────────────────────────────
                new RandomQuestionLoader(),
                new ThreadsQuestionLoader(),
                new ReflectionQuestionLoader(),
                new DesignPatternsQuestionLoader(),
                new NetworkingQuestionLoader(),
                new DatabasesQuestionLoader(),
                new MetaprogrammingQuestionLoader(),
                new MachineLearningQuestionLoader(),
                new EvolutionaryProgrammingQuestionLoader(),

                // ── Tooling & Best Practices ──────────────────────────────
                new IdeQuestionLoader(),
                new DebuggingToolsQuestionLoader(),
                new JavadocQuestionLoader(),
                new ProjectOrganizationQuestionLoader(),

                // ── Java Platform ─────────────────────────────────────────
                new JavaClassHierarchyQuestionLoader(),
                new SerializationQuestionLoader(),
                new StandardStreamsQuestionLoader(),
                new FunctionalParadigmQuestionLoader()
        );

        for (QuestionLoader loader : loaders) {
            List<Question> questions = loader.load();
            byTopic.computeIfAbsent(loader.getTopic(), k -> new ArrayList<>()).addAll(questions);
            for (Question q : questions) byId.put(q.getId(), q);
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
}
