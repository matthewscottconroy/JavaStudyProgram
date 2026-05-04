package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class RandomQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.RANDOM; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("rnd-mc-01", Topic.RANDOM, 1,
                "Which expression produces a random int in the range [0, 10)?",
                "Math.random()",
                "new Random().nextInt()",
                "new Random().nextInt(10)",
                "Random.next(10)",
                "c",
                "nextInt(n) returns a random int from 0 (inclusive) to n (exclusive). "
                + "nextInt(10) gives 0–9. "
                + "Math.random() gives a double in [0.0, 1.0). "
                + "Random.next() does not exist as a public method."),

            mc("rnd-mc-02", Topic.RANDOM, 2,
                "What does seeding a Random with the same value guarantee?",
                "The sequence will be truly random",
                "The same sequence of random numbers will be produced every run",
                "The numbers will be cryptographically secure",
                "The numbers will all be positive",
                "b",
                "Pseudorandom number generators are deterministic given the same seed. "
                + "new Random(42) always produces the same sequence. "
                + "This is useful for reproducible tests or simulations. "
                + "For security, use SecureRandom instead."),

            mc("rnd-mc-03", Topic.RANDOM, 2,
                "Which produces a random int in the range [5, 15]?",
                "new Random().nextInt(10)",
                "new Random().nextInt(10) + 5",
                "new Random().nextInt(11) + 5",
                "new Random().nextInt(15) + 5",
                "c",
                "nextInt(11) gives 0–10. Adding 5 shifts the range to 5–15 (inclusive). "
                + "nextInt(10) + 5 gives 5–14. nextInt(15) + 5 gives 5–19. "
                + "Formula: nextInt(max - min + 1) + min."),

            mc("rnd-mc-04", Topic.RANDOM, 2,
                "What does Math.random() return?",
                "A random int from 0 to Integer.MAX_VALUE",
                "A random double in [0.0, 1.0)",
                "A random double in [0.0, 1.0]",
                "A random boolean",
                "b",
                "Math.random() returns a double d where 0.0 <= d < 1.0. "
                + "The upper bound (1.0) is exclusive. "
                + "It is internally backed by a shared Random instance."),

            trace("rnd-tr-01", Topic.RANDOM, 2,
                "If rng is seeded with 0, what does nextInt(4) return?",
                "Random rng = new Random(0);\n"
                + "System.out.println(rng.nextInt(4));",
                "0",
                "With seed 0, Java's Random.nextInt(4) returns 0 on the first call. "
                + "The exact sequence is deterministic and depends on the algorithm implementation."),

            debug("rnd-db-01", Topic.RANDOM, 2,
                "This code always produces the same number during a run. Why?",
                "for (int i = 0; i < 5; i++) {\n"
                + "    Random r = new Random();\n"
                + "    System.out.println(r.nextInt(100));\n"
                + "}",
                "new Random() is not allowed inside loops",
                "nextInt(100) returns the same value for the same object",
                "A new Random() created quickly may receive the same time-based seed, producing the same first value",
                "Random numbers repeat after 5 calls",
                "c",
                "Creating a new Random() each iteration may seed from the same millisecond clock, "
                + "giving the same first number repeatedly. "
                + "Fix: create ONE Random instance outside the loop and call nextInt() on it repeatedly."),

            codegen("rnd-cg-01", Topic.RANDOM, 2,
                "Which code correctly simulates a dice roll (1–6)?",
                "new Random().nextInt(6);",
                "new Random().nextInt(6) + 1;",
                "new Random().nextInt(7);",
                "Math.random() * 6;",
                "b",
                "nextInt(6) gives 0–5. Adding 1 shifts to 1–6. "
                + "Option A gives 0–5 (wrong range). "
                + "Option C gives 0–6 (7 values, not 6). "
                + "Option D gives a double in [0.0, 6.0), not an integer."),

            mc("rnd-mc-05", Topic.RANDOM, 2,
                "What does Random.nextBoolean() return?",
                "Always true",
                "A random boolean value (true or false with 50% probability each)",
                "A random int that is either 0 or 1",
                "The result of nextInt(2) == 1",
                "b",
                "nextBoolean() returns a random boolean — true or false with equal probability. "
                + "Useful for coin flips, random branching in simulations, etc."),

            trace("rnd-tr-02", Topic.RANDOM, 2,
                "What is the range of values this produces?",
                "Random r = new Random();\n"
                + "int n = r.nextInt(10) + 5;",
                "5 to 14",
                "nextInt(10) produces 0–9. Adding 5 shifts to 5–14 inclusive."),

            mc("rnd-mc-06", Topic.RANDOM, 3,
                "Which Java 17+ method generates a random int in a range [min, max) without manual math?",
                "new Random().nextInt(min, max)",
                "Random.between(min, max)",
                "ThreadLocalRandom.current().ints(min, max).findFirst()",
                "Math.random(min, max)",
                "a",
                "Java 17 added Random.nextInt(int origin, int bound) that generates [origin, bound). "
                + "ThreadLocalRandom (Java 7+) also has nextInt(origin, bound). "
                + "Before this: rng.nextInt(max - min) + min was the standard idiom."),

            mc("rnd-mc-07", Topic.RANDOM, 2,
                "What does Random.nextDouble() return?",
                "A random int from 0 to Integer.MAX_VALUE",
                "A random double in [0.0, 1.0)",
                "A random double in [0.0, 100.0)",
                "A random double that can be negative",
                "b",
                "nextDouble() returns a double d where 0.0 <= d < 1.0. "
                + "Scale it: nextDouble() * max gives [0.0, max). "
                + "For a range: nextDouble() * (max - min) + min."),

            mc("rnd-mc-08", Topic.RANDOM, 3,
                "When should you use SecureRandom instead of Random?",
                "When you need faster random numbers",
                "When you need cryptographically strong random numbers (e.g., password generation, tokens)",
                "When seeding with a specific value",
                "When generating random booleans",
                "b",
                "Random uses a linear congruential generator — predictable if the seed is known. "
                + "SecureRandom uses OS-level entropy (e.g., /dev/urandom) and is unpredictable. "
                + "Use SecureRandom for security-sensitive operations: tokens, OTPs, encryption keys."),

            mc("rnd-mc-09", Topic.RANDOM, 2,
                "What is ThreadLocalRandom?",
                "A random number generator that is thread-safe by locking",
                "A per-thread random number generator that avoids contention in concurrent code",
                "A random generator that produces the same values on every thread",
                "A deprecated version of Random",
                "b",
                "ThreadLocalRandom.current().nextInt(n) is preferred in multi-threaded code. "
                + "A single shared Random requires synchronization under contention. "
                + "ThreadLocalRandom gives each thread its own generator — no contention, better performance."),

            trace("rnd-tr-03", Topic.RANDOM, 2,
                "What range of values can this produce?",
                "int result = (int)(Math.random() * 100) + 1;",
                "1 to 100",
                "Math.random() gives [0.0, 1.0). *100 gives [0.0, 100.0). "
                + "(int) truncates to 0–99. +1 shifts to 1–100 inclusive."),

            trace("rnd-tr-04", Topic.RANDOM, 2,
                "What is printed?",
                "Random r = new Random(42);\n"
                + "int a = r.nextInt(10);\n"
                + "int b = r.nextInt(10);\n"
                + "System.out.println(a == b);",
                "false",
                "With seed 42, the first two calls to nextInt(10) typically produce different values "
                + "(the exact values depend on the PRNG algorithm but sequential calls generally differ). "
                + "The output is deterministic for the same seed but a == b is false for seed 42."),

            debug("rnd-db-02", Topic.RANDOM, 2,
                "The program generates the same dice roll every execution. Why?",
                "Random rng = new Random(1234);\n"
                + "System.out.println(rng.nextInt(6) + 1);",
                "nextInt(6) always returns the same value",
                "The seed 1234 is fixed — the same seed always produces the same sequence",
                "new Random() requires the current time to work",
                "nextInt(6) is not the correct way to simulate a die",
                "b",
                "A fixed seed gives a deterministic sequence. "
                + "new Random(1234) always starts the PRNG in the same state. "
                + "For random results each run, use new Random() (no seed) — it uses a time-based seed by default."),

            debug("rnd-db-03", Topic.RANDOM, 3,
                "The shuffled list is the same every time the program runs. Why?",
                "List<Integer> nums = new ArrayList<>(List.of(1,2,3,4,5));\n"
                + "Collections.shuffle(nums, new Random(99));\n"
                + "System.out.println(nums);",
                "Collections.shuffle() doesn't actually shuffle lists",
                "The fixed seed 99 causes the same shuffle permutation every run",
                "The list must be sorted before shuffling",
                "new Random(99) and new Random() are equivalent",
                "b",
                "Collections.shuffle(list, random) uses the provided Random instance. "
                + "A fixed seed produces the same shuffle each time. "
                + "Fix: Collections.shuffle(nums) — uses an internally seeded Random."),

            codegen("rnd-cg-02", Topic.RANDOM, 2,
                "Which code picks a random element from a List<String> named 'words'?",
                "words[new Random().nextInt(words.size())]",
                "words.get(new Random().nextInt(words.size()))",
                "Collections.random(words)",
                "words.getRandomElement()",
                "b",
                "words.get(index) retrieves by index. "
                + "nextInt(words.size()) gives a valid random index (0 to size-1). "
                + "Option A uses array syntax — List doesn't support []. "
                + "Options C and D don't exist in Java's standard API.")
        );
    }
}
