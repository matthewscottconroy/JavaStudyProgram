package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class LoopsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.LOOPS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("lp-mc-01", Topic.LOOPS, 1,
                "In a for loop 'for (int i = 0; i < 5; i++)', how many times does the body execute?",
                "4", "5", "6", "0",
                "b",
                "i starts at 0 and increments while i < 5: values 0,1,2,3,4 — that's 5 iterations."),

            mc("lp-mc-02", Topic.LOOPS, 2,
                "What keyword immediately exits a loop?",
                "exit", "return", "break", "stop",
                "c",
                "'break' terminates the nearest enclosing loop or switch. "
                + "'return' exits the entire method. 'exit' and 'stop' are not loop-control keywords."),

            mc("lp-mc-03", Topic.LOOPS, 2,
                "What keyword skips the rest of the current iteration and starts the next one?",
                "skip", "next", "continue", "pass",
                "c",
                "'continue' jumps to the loop's update step (for-loop) or condition check (while-loop). "
                + "The other options are not Java keywords used for this purpose."),

            mc("lp-mc-04", Topic.LOOPS, 3,
                "What is the output?\n\n"
                + "    int sum = 0;\n"
                + "    for (int i = 1; i <= 4; i++) {\n"
                + "        sum += i;\n"
                + "    }\n"
                + "    System.out.println(sum);",
                "4", "6", "10", "15",
                "c",
                "sum = 0+1+2+3+4 = 10. The loop adds each i to sum."),

            // ── Tracing ───────────────────────────────────────────────────────

            trace("lp-tr-01", Topic.LOOPS, 1,
                "What numbers are printed (one per line)?",
                "for (int i = 0; i < 3; i++) {\n    System.out.println(i);\n}",
                "0\n1\n2",
                "i starts at 0, prints 0; increments to 1, prints 1; increments to 2, prints 2; "
                + "increments to 3, condition 3 < 3 is false, loop ends.",
                "0 1 2"),

            trace("lp-tr-02", Topic.LOOPS, 2,
                "What is printed?",
                "int x = 1;\nwhile (x < 20) {\n    x *= 2;\n}\nSystem.out.println(x);",
                "32",
                "x doubles each iteration: 1→2→4→8→16→32. When x=32, 32 < 20 is false. "
                + "32 is printed after the loop."),

            trace("lp-tr-03", Topic.LOOPS, 2,
                "What is printed?",
                "for (int i = 0; i < 5; i++) {\n    if (i == 3) break;\n    System.out.print(i + \" \");\n}",
                "0 1 2",
                "i=0,1,2 print normally. When i=3, break exits the loop before printing.",
                "0 1 2 "),

            trace("lp-tr-04", Topic.LOOPS, 2,
                "What is the output?",
                "for (int i = 0; i < 5; i++) {\n    if (i % 2 == 0) continue;\n    System.out.print(i + \" \");\n}",
                "1 3",
                "continue skips even numbers (0,2,4). Odd numbers 1 and 3 are printed.",
                "1 3 "),

            // ── Debugging ────────────────────────────────────────────────────

            debug("lp-db-01", Topic.LOOPS, 2,
                "This loop is supposed to print 1 through 5 but produces an infinite loop. Why?",
                "int i = 1;\nwhile (i <= 5) {\n    System.out.println(i);\n}",
                "The condition should use < not <=",
                "i is never incremented inside the loop body",
                "println doesn't work inside loops",
                "i should be declared outside the while",
                "b",
                "i is never changed, so i <= 5 is always true. "
                + "Add i++; inside the loop body to fix the infinite loop."),

            // ── Code Generation ───────────────────────────────────────────────

            codegen("lp-cg-01", Topic.LOOPS, 1,
                "Which code prints the numbers 1 through 5, each on its own line?",
                "for (int i = 1; i < 5; i++) { System.out.println(i); }",
                "for (int i = 1; i <= 5; i++) { System.out.println(i); }",
                "for (int i = 0; i < 5; i++) { System.out.println(i); }",
                "for (int i = 1; i < 6; i++) { System.out.print(i); }",
                "b",
                "i <= 5 includes 5. Option A stops at 4 (i < 5). "
                + "Option C starts at 0. Option D uses print instead of println.",
                "To include n in the range, use i <= n or i < n+1."),

            mc("lp-mc-05", Topic.LOOPS, 2,
                "What is a for-each (enhanced for) loop used for?",
                "Iterating over a range of integers with a step other than 1",
                "Iterating over all elements of an array or Iterable without using an index",
                "Executing a loop a fixed number of times",
                "Creating a loop that can run backwards",
                "b",
                "for (String s : list) { ... } iterates over each element. "
                + "The loop variable gets the value of each element in order. "
                + "You cannot access the index or modify the array/list position using a for-each loop."),

            mc("lp-mc-06", Topic.LOOPS, 2,
                "What is the difference between a while loop and a do-while loop?",
                "While loops use a counter; do-while loops use a boolean",
                "A while loop checks the condition before each iteration; a do-while always executes at least once",
                "Do-while loops are faster",
                "While loops require break; do-while loops use continue",
                "b",
                "while: check condition → maybe execute body → check again. "
                + "do-while: execute body → check condition → maybe repeat. "
                + "do-while guarantees at least one execution. Useful for menus: 'show menu, then loop while user doesn't quit'."),

            mc("lp-mc-07", Topic.LOOPS, 3,
                "What does a labeled break do?",
                "Breaks out of all loops in the method",
                "Breaks out of the specifically labeled outer loop",
                "Creates a named loop that can be called like a method",
                "Breaks and re-enters the loop from the top",
                "b",
                "outer: for (...) { inner: for (...) { break outer; } } — 'break outer' exits the outer loop, "
                + "not just the inner one. Useful for early exit from nested loops without a boolean flag."),

            mc("lp-mc-08", Topic.LOOPS, 3,
                "What is the output?\n\n"
                + "    for (int i = 0; i < 3; i++) {\n"
                + "        for (int j = 0; j < 3; j++) {\n"
                + "            if (i == j) continue;\n"
                + "            System.out.print(i + \"\" + j + \" \");\n"
                + "        }\n"
                + "    }",
                "00 11 22", "01 02 10 12 20 21", "00 01 02 10 11 12 20 21 22", "01 10",
                "b",
                "continue skips iterations where i == j (the diagonal: 0,0 / 1,1 / 2,2). "
                + "The remaining pairs printed: 01, 02, 10, 12, 20, 21."),

            trace("lp-tr-05", Topic.LOOPS, 2,
                "What is printed?",
                "int count = 0;\n"
                + "for (int i = 1; i <= 10; i++) {\n"
                + "    if (i % 3 == 0) count++;\n"
                + "}\n"
                + "System.out.println(count);",
                "3",
                "Multiples of 3 in 1..10: 3, 6, 9. count = 3."),

            trace("lp-tr-06", Topic.LOOPS, 2,
                "What is printed?",
                "int n = 1;\n"
                + "do {\n"
                + "    System.out.print(n + \" \");\n"
                + "    n++;\n"
                + "} while (n <= 3);",
                "1 2 3",
                "do-while: print 1, n=2 (2<=3 true). Print 2, n=3 (3<=3 true). Print 3, n=4 (4<=3 false). Output: 1 2 3."),

            trace("lp-tr-07", Topic.LOOPS, 3,
                "What is printed?",
                "int[] nums = {5, 3, 8, 1};\n"
                + "int max = nums[0];\n"
                + "for (int n : nums) {\n"
                + "    if (n > max) max = n;\n"
                + "}\n"
                + "System.out.println(max);",
                "8",
                "max starts at 5. Loop: 5 not > 5; 3 not > 5; 8 > 5 → max=8; 1 not > 8. Final max = 8."),

            trace("lp-tr-08", Topic.LOOPS, 2,
                "How many times does the loop body execute?",
                "for (int i = 10; i > 0; i -= 3) {\n"
                + "    System.out.println(i);\n"
                + "}",
                "4",
                "i starts at 10, decrements by 3: 10, 7, 4, 1. When i becomes -2, 10>0 was false at that check, "
                + "but actually: i=10 (>0 ✓), i=7 (>0 ✓), i=4 (>0 ✓), i=1 (>0 ✓), i=-2 (>0 ✗). 4 iterations."),

            debug("lp-db-02", Topic.LOOPS, 2,
                "This loop runs one too few times. What is wrong?",
                "for (int i = 1; i < 10; i++) {\n"
                + "    System.out.println(i);\n"
                + "}",
                "i should start at 0",
                "The condition i < 10 excludes 10 — use i <= 10 to include it",
                "i++ should be i--",
                "println cannot be in a loop",
                "b",
                "i < 10 stops before printing 10. To print 1 through 10, use i <= 10 (or i < 11). "
                + "This is a very common off-by-one error."),

            debug("lp-db-03", Topic.LOOPS, 3,
                "This loop modifies the list while iterating it. What exception will be thrown?",
                "List<String> items = new ArrayList<>(Arrays.asList(\"a\", \"b\", \"c\"));\n"
                + "for (String item : items) {\n"
                + "    if (item.equals(\"b\")) items.remove(item);\n"
                + "}",
                "NullPointerException",
                "IndexOutOfBoundsException",
                "ConcurrentModificationException",
                "UnsupportedOperationException",
                "c",
                "Modifying a collection while iterating it with a for-each loop throws ConcurrentModificationException. "
                + "Fix: use Iterator.remove(), or collect items to delete and remove after the loop, or use removeIf()."),

            codegen("lp-cg-02", Topic.LOOPS, 2,
                "Which loop correctly iterates backwards from 5 to 1?",
                "for (int i = 5; i > 0; i--) { System.out.println(i); }",
                "for (int i = 5; i >= 0; i--) { System.out.println(i); }",
                "for (int i = 1; i <= 5; i--) { System.out.println(i); }",
                "for (int i = 5; i < 0; i--) { System.out.println(i); }",
                "a",
                "i starts at 5, condition i > 0 (runs for 5,4,3,2,1), decrements each iteration. "
                + "Option B includes 0. Option C: i starts at 1 and decrements → infinite loop. "
                + "Option D: condition i < 0 is immediately false, loop never runs.")
        );
    }
}
