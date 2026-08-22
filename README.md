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

Create a profile, pick topics (or `all`), and start a session. Your progress is
saved automatically — to `data/profiles/` when running from a checkout, or
`~/.javastudy/profiles/` when running a downloaded jar (`PROFILE_DIR` overrides
both).

## Question types

| Type | What you do |
|---|---|
| **Coding** | Edit a real `.java` file in `workspace/<exercise-id>/`, press Enter to compile and run the tests. Repeat until green. |
| Tracing | Read code, predict its output. |
| Debugging | Spot the bug in a snippet. |
| Multiple choice / code generation | Classic A/B/C/D questions. |

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
  core/      question bank, adaptive engine, study session
  coding/    JDK-compiler harness for coding exercises
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

## License

See [LICENSE](LICENSE).
