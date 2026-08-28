# Java Study Program

A terminal-based practice system for learning Java — from first variables through
GUIs, generics, and concurrency. It is built around **writing real programs**: the
app hands you a starter file, you edit it in your own editor or IDE, and the app
compiles your code and runs tests against it, right there in your session.

An adaptive engine tracks your per-topic mastery, matches question difficulty to
your level, avoids repeating what you just answered, and prefers hands-on coding
exercises whenever a topic has them. A prerequisite graph of 60+ topics gates
advanced material until its foundations are in place.

## Quick start

Requires a **JDK 17 or newer** (a plain JRE runs the app, but coding exercises need
the compiler — get a JDK at [adoptium.net](https://adoptium.net)).

```bash
# From a release jar
java -jar java-study-program.jar

# From a checkout
mvn package
java -jar target/java-study-program-1.0-SNAPSHOT.jar
```

Create a profile and start a session. By default the **auto feed** picks your
topics for you: it follows the concept map's prerequisite graph, keeps you on
your learning frontier, introduces newly unlocked topics gently, and mixes in
spaced review of mastered material (mastery also decays slowly with inactivity,
so stale topics resurface). Prefer to drive? Choose manual mode and select any
topics yourself. Progress is saved automatically — to `data/profiles/` when
running from a checkout, or `~/.javastudy/profiles/` when running a downloaded
jar (`PROFILE_DIR` overrides both).

Every attempt is also appended to a per-profile log (`<name>.attempts.jsonl`),
which powers the **Progress Report** menu option: a self-contained HTML page
with an activity calendar, accuracy-over-time chart, per-topic mastery bars,
accuracy by difficulty and question type, and a prioritized "what to work on
next" list derived from the prerequisite graph.

The **Concept Map** menu option opens a Swing overworld: topics as nodes in
five level-band "worlds", prerequisite paths between them, colors showing your
progress, and advanced optional topics hidden as `? ? ?` until their
prerequisites are mastered. Click nodes to add or remove them from your session
topics. (The map is itself a custom-painted `Graphics2D` component — once you
reach the GUI world, you can read its source as course material.)

**Boss Challenges** gate each world: once a world's average mastery reaches
50%, its boss appears — a 10-question, no-hints quiz across the whole world.
Score 80%+ to clear it (retries draw different questions), and the cleared
star shows on the concept map.

Questions can also carry `relatedTopics` — prerequisite topics they genuinely
exercise. Missing an arrays question whose solution hinges on reference
semantics nudges *that* prerequisite's mastery down too, so the auto feed
drills into the real gap instead of just repeating the surface topic.

GUI topics have real coding exercises too: you build actual Swing panels and
the tests interact with them programmatically (`doClick()`, component-tree
inspection, pixel-sampling painted `BufferedImage`s) — fully headless, so they
work everywhere including CI.

Some coding exercises are **multi-file projects**: the workspace gets several
`.java` files (some provided complete, some yours to finish), and every file in
the folder — including extra helper classes you add yourself — is compiled and
tested together.

**Question calibration:** authored difficulty labels are self-correcting. The
engine blends each question's label with its measured pass rate across every
profile's attempt log on the machine, matches students against that *effective*
difficulty, and the progress report lists questions whose measurement has
drifted far from their label — instructor-ready content review, powered by
ordinary use.

## Instructor customization

- **Course overlays** (`data/courses/*.json`): map your syllabus units onto
  topics; students then pick `[u] course unit review` at session start and
  enter a unit range ("1-4") for exactly-scoped quiz prep. A sample Java II
  overlay ships in `data/courses/sample-java2.json`.
- **Prerequisite overrides** (`data/topic-graph.json`): reshape the concept
  map's prerequisite edges without rebuilding —
  `{ "overrides": { "generics": { "prerequisites": ["collections"] } } }`.
  Unknown slugs are warned about and cyclic overrides are rejected.

## Question types

| Type | What you do |
|---|---|
| **Coding** | Edit a real `.java` file in `workspace/<exercise-id>/`, press Enter to compile and run the tests. Repeat until green. |
| **Code ordering (Parsons)** | Reorder the scrambled lines of a working program — auto-derived from every coding exercise, so this bank grows for free. |
| **Fill in the blank** | Type the one missing expression in a working program. |
| Tracing | Read code, predict its output. |
| Debugging | Spot the bug in a snippet. |
| Multiple choice / code generation | Classic A/B/C/D questions. |

Together these form a skill ladder inside each topic: trace → debug → reorder →
fill in → write from scratch. The adaptive engine prefers the hands-on end of
the ladder whenever a topic has it.

During a coding exercise: `Enter` compiles and tests, `h` gives progressive hints,
`r` resets the file to the starter, `g` gives up and shows the reference solution,
`s` skips, `q` quits. Student programs run in a subprocess with a memory cap and a
10-second timeout, so an accidental infinite loop is caught, not fatal.

## Optional AI support

Set `ANTHROPIC_API_KEY` to enable extra hints, richer explanations of wrong
answers, and on-demand concept explanations. **The app is fully functional without
it** — AI support is a layer on top, never a requirement.

## Adding questions

Drop a JSON file into `data/questions/<topic>/` — no code changes needed. The
directory name must match a topic slug (the lower-cased `Topic` enum name, e.g.
`arrays_arraylists`); the app warns at startup about directories that match no
topic, and files in `data/questions/` override bundled questions with the same id.

Multiple-choice / tracing questions:

```json
{
  "id": "arr-mc-99",
  "type": "MULTIPLE_CHOICE",
  "difficulty": 2,
  "prompt": "…",
  "code": "optional code snippet",
  "choices": ["…", "…", "…", "…"],
  "answer": "b",
  "explanation": "…",
  "hints": ["…"]
}
```

Coding exercises add two fields — `starterCode` (the file written to the student's
workspace; put the task description in a comment at the top, and make it compile
as-is with placeholder returns) and `testCode` (a self-contained `<Class>Test`
class whose `main` prints `PASS`/`FAIL` lines and exits non-zero on failure).
`answer` holds the reference solution. See
`data/questions/loops/lp-code-01.json` for a complete example. The test suite
(`CodingExerciseRunnerTest`) verifies every shipped coding exercise: the starter
must compile but fail its tests, and the reference solution must pass.

## Project layout

```
src/main/java/com/studyprogram/
  core/      question bank, adaptive engine, curriculum (auto feed), study session
  coding/    JDK-compiler harness for coding exercises
  report/    self-contained HTML progress report generator
  grading/   graders for each question type
  llm/       optional Anthropic-backed hints/explanations (null-object without a key)
  model/     Topic graph (with prerequisites), Question, StudentProfile
  questions/ hardcoded question loaders + JSON parsing
  storage/   JSON profile persistence
  ui/        terminal CLI and rendering
data/questions/   JSON question bank (bundled into the jar at build time)
workspace/        created at runtime; your coding-exercise files live here
```

## Development

```bash
mvn test      # full suite, including compile-and-run verification of every coding exercise
mvn package   # executable fat jar in target/
```

CI (GitHub Actions) runs the same suite on every push, including the content
gate that compiles and tests all coding exercises.

### Native installer (no JDK needed by students)

```bash
mvn package
./packaging/build-app-image.sh        # app image in target/dist/
TYPE=deb ./packaging/build-app-image.sh   # or rpm / msi / dmg on the matching OS
```

The bundled runtime includes `jdk.compiler`, so coding exercises compile and
run even on machines with no Java installed at all.

## License

See [LICENSE](LICENSE).
