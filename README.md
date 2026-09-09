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
the compiler — get a JDK at [adoptium.net](https://adoptium.net)). On Linux, installing
`bubblewrap` additionally sandboxes every exercise run — see [docs/SECURITY.md](docs/SECURITY.md).

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
prerequisites are mastered. Click a node to add or remove it from your session topics,
**double-click to start studying it immediately**, or click a lit boss to fight it — the map is a
way to navigate the program, not just a picture of it. (It is itself a custom-painted
`Graphics2D` component with Swing Timers driving its animations — once you reach the GUI world,
you can read its source as course material.)

**Boss Challenges** gate each world: once a world's average mastery reaches 50%, its boss appears
— a no-hints test across the whole world that ends with a **coding finale**, so a world cannot be
passed by recognition alone. Score 80%+ to clear it (retries draw a different fight), and the
cleared star shows on the concept map.

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

**Spaced repetition:** each question is individually scheduled from your attempt history. A correct
answer multiplies its interval by an ease factor (starting ~1.5 days and growing, capped at 60);
a miss resets the streak *and* lowers that question's ease, so material you keep forgetting comes
back more often than material you got right first time. Due dates carry a deterministic ±15%
jitter so a big study day doesn't become a big review day weeks later.

**Progress card:** the Progress Report menu option also prints and saves a compact text card
(totals, streak, bosses, per-world mastery) ready to paste into a lab submission. The code at the
bottom is a checksum of the card's own numbers, or an HMAC when `STUDY_SIGNING_KEY` is set;
`--verify-card` checks it. Be clear-eyed about what that proves: it catches an edited card, but on
a machine the student controls it is a tamper-check, not an attestation of proctored work.

**Instructor class report:** `java -jar java-study-program.jar --class-report`
aggregates every profile on the machine into one HTML page: per-student
summary, class-wide weakest topics, and calibration-flagged questions.

**Question calibration:** authored difficulty labels are self-correcting. Each student carries an
ability rating and each question a difficulty rating, both updated on every attempt (an Elo-style
latent-trait model), so a hard question that only strong students attempted no longer measures as
easy. The engine matches students against that *measured* difficulty, and reports list questions
whose measurement has drifted far from their label — instructor-ready content review powered by
ordinary use. Percentages come with Wilson confidence intervals, so 3-for-3 never reads the same
as 40-for-40.

## Command line

```
java -jar java-study-program.jar                      # interactive
java -jar java-study-program.jar --help               # all options

--class-report [dir]           HTML report over every profile (optionally a collected folder)
--export-profile <name> [out]  bundle one profile + its attempt log into a file
--import-profile <file|dir>    import one bundle, or a whole folder of them
--verify-card <card.txt>       check a progress card against its local profile
--no-color / --ascii          accessibility fallbacks (NO_COLOR is honoured too)
```

Students on their own laptops can `--export-profile` and hand in the bundle; the instructor
imports the folder and runs `--class-report` over it.

## Accessibility

Colour is never the only signal: every state also carries a symbol, in the terminal and on the
map (`▶` available, `◐` in progress, `★` mastered, `✖` locked, `?` secret). `NO_COLOR` or
`--no-color` disables ANSI; `JAVASTUDY_ASCII=1` or `--ascii` swaps box-drawing and emoji for plain
ASCII on limited terminals. The concept map is fully keyboard-driven — arrows move, Enter selects,
`S` studies the focused topic, `B` fights its world boss, Escape closes — and carries accessible
names for screen readers.

## Optional AI, configured by file

AI help is optional and off by default. Point it wherever you like with `data/llm.json`:

```json
{ "provider": "openai",
  "baseUrl": "http://localhost:11434/v1/chat/completions",
  "model": "llama3.1",
  "apiKeyEnv": "OLLAMA_API_KEY",
  "maxCallsPerSession": 100 }
```

`provider` is `anthropic` (Claude Messages API, the default) or `openai` (any OpenAI-compatible
endpoint — **including a local Ollama or LM Studio server, so students without an API budget still
get AI help**). Every configuration carries a per-session call cap. AI-generated questions are
never used as shipped content: they go to `data/generated/` for a human to review, because they
have not passed the content gate.

## Instructor customization

- **Course overlays** (`data/courses/*.json`): map your syllabus units onto
  topics; students then pick `[u] course unit review` at session start and
  enter a unit range ("1-4") for exactly-scoped quiz prep. A sample Java II
  overlay ships in `data/courses/sample-java2.json`.
- **Prerequisite overrides** (`data/topic-graph.json`): reshape the concept
  map's prerequisite edges without rebuilding —
  `{ "overrides": { "generics": { "prerequisites": ["collections"] } } }`.
  Unknown slugs are warned about and cyclic overrides are rejected. The same file's
  `"hidden": ["metaprogramming"]` takes topics out of scope entirely — they vanish from the auto
  feed, the map and topic selection. Copy
  [data/topic-graph.example.json](data/topic-graph.example.json) to get started.
- **Collecting work**: `--import-profile <folder>` then `--class-report <dir>`.
- **Student-reported problems**: pressing `f` on any question records it to
  `data/flags.jsonl`, which the class report lists alongside the questions whose
  measured difficulty has drifted from their label.

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

CI (GitHub Actions) runs the same suite on every push, including the content gate that compiles
and tests all coding exercises. Tagging `v*` builds installers for Linux, macOS and Windows and
attaches them to a GitHub release.

Security model — what running third-party question files does and does not risk, and how exercise
execution is contained: [docs/SECURITY.md](docs/SECURITY.md).

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
