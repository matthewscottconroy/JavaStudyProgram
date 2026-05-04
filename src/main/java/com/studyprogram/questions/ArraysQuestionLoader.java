package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ArraysQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.ARRAYS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("arr-mc-01", Topic.ARRAYS, 1,
                "What is the index of the first element in a Java array?",
                "1", "0", "-1", "It depends on how you declare it",
                "b",
                "Java (like most languages) uses zero-based indexing. The first element is at index 0."),

            mc("arr-mc-02", Topic.ARRAYS, 2,
                "What is the value of arr[2] after: int[] arr = {10, 20, 30, 40};",
                "10", "20", "30", "40",
                "c",
                "Index 0→10, 1→20, 2→30, 3→40. arr[2] is 30."),

            mc("arr-mc-03", Topic.ARRAYS, 2,
                "How do you get the number of elements in an array named 'nums'?",
                "nums.size()", "nums.length()", "nums.length", "length(nums)",
                "c",
                "Arrays use the 'length' field (no parentheses). "
                + "ArrayList uses .size(). Python uses len(). "
                + "nums.length() with parentheses would be a method call — arrays don't have that."),

            trace("arr-tr-01", Topic.ARRAYS, 2,
                "What is printed?",
                "int[] a = {5, 10, 15};\nSystem.out.println(a[0] + a[2]);",
                "20",
                "a[0] = 5, a[2] = 15. 5 + 15 = 20."),

            trace("arr-tr-02", Topic.ARRAYS, 2,
                "What is printed?",
                "int[] nums = new int[3];\nnums[1] = 7;\nSystem.out.println(nums[0] + \" \" + nums[1]);",
                "0 7",
                "new int[3] creates an array of 3 ints all defaulting to 0. "
                + "nums[1] is set to 7. nums[0] is still 0."),

            debug("arr-db-01", Topic.ARRAYS, 2,
                "What exception will this throw at runtime?",
                "int[] arr = {1, 2, 3};\nSystem.out.println(arr[3]);",
                "NullPointerException",
                "ArrayIndexOutOfBoundsException",
                "ClassCastException",
                "IllegalArgumentException",
                "b",
                "The array has indices 0, 1, 2. Accessing index 3 is out of bounds. "
                + "Java throws ArrayIndexOutOfBoundsException at runtime."),

            codegen("arr-cg-01", Topic.ARRAYS, 2,
                "Which code creates an int array containing the values 1, 2, 3?",
                "int[] arr = new int[1, 2, 3];",
                "int arr[] = {1, 2, 3};",
                "int[] arr = (1, 2, 3);",
                "array<int> arr = {1, 2, 3};",
                "b",
                "int arr[] = {1, 2, 3}; is equivalent to int[] arr = {1, 2, 3}; "
                + "(both are valid Java syntax). The initializer list {} creates and fills the array. "
                + "Option A, C, and D are not valid Java syntax."),

            mc("arr-mc-04", Topic.ARRAYS, 2,
                "What is the default value of elements in a newly created int[] array?",
                "1", "null", "undefined", "0",
                "d",
                "Numeric array elements default to 0. boolean[] defaults to false. "
                + "Object array elements (e.g., String[]) default to null. "
                + "This applies to instance/class fields and local array elements alike."),

            trace("arr-tr-03", Topic.ARRAYS, 2,
                "What is printed?",
                "int[] arr = {3, 1, 4, 1, 5};\n"
                + "int sum = 0;\n"
                + "for (int x : arr) sum += x;\n"
                + "System.out.println(sum);",
                "14",
                "Enhanced for loop adds each element: 3+1+4+1+5 = 14."),

            mc("arr-mc-05", Topic.ARRAYS, 3,
                "What does Arrays.sort(arr) do?",
                "Returns a new sorted array without modifying arr",
                "Sorts arr in-place in ascending order",
                "Returns the index of the smallest element",
                "Sorts arr in descending order",
                "b",
                "Arrays.sort(arr) sorts the array in-place (modifies arr directly) in ascending order. "
                + "It uses a dual-pivot quicksort for primitives and mergesort for objects. "
                + "To sort descending: for objects use Arrays.sort(arr, Collections.reverseOrder())."),

            codegen("arr-cg-02", Topic.ARRAYS, 3,
                "Which finds the maximum value in an int array 'nums'?",
                "int max = Arrays.max(nums);",
                "int max = nums[0]; for (int x : nums) if (x > max) max = x;",
                "int max = Collections.max(nums);",
                "int max = nums.sort()[nums.length - 1];",
                "b",
                "Initialize max to the first element, then loop updating max when a larger value is found. "
                + "Arrays.max() doesn't exist. Collections.max() works on List, not int[]. "
                + "Option D: sort() doesn't return an array and modifying the array has side effects."),

            mc("arr-mc-06", Topic.ARRAYS, 2,
                "How do you copy an array so that changes to the copy don't affect the original?",
                "int[] copy = original;",
                "int[] copy = Arrays.copyOf(original, original.length);",
                "int[] copy = original.clone(); // not recommended",
                "Both b and c create an independent copy",
                "d",
                "Assigning (option A) copies only the reference — both variables point to the same array. "
                + "Arrays.copyOf() and .clone() create a new array with the same values. "
                + "For primitive arrays, both produce independent copies."),

            mc("arr-mc-07", Topic.ARRAYS, 2,
                "What does Arrays.fill(arr, 0) do?",
                "Returns a new array filled with zeros",
                "Sets all elements of arr to 0 in-place",
                "Counts zeros in arr",
                "Removes all zero elements from arr",
                "b",
                "Arrays.fill(array, value) sets every element in the array to the given value. "
                + "Useful to reset or initialize arrays: Arrays.fill(grid, -1); "
                + "You can also fill a range: Arrays.fill(arr, start, end, value)."),

            mc("arr-mc-08", Topic.ARRAYS, 3,
                "What is a 2D array in Java?",
                "A special array type with a fixed 2-column width",
                "An array of arrays — each element is itself an array",
                "A LinkedList with two dimensions",
                "A hash map using two keys",
                "b",
                "int[][] grid = new int[3][4] creates a 3×4 grid (3 rows, 4 columns). "
                + "Access with grid[row][col]. Rows can have different lengths (jagged arrays): "
                + "int[][] jagged = new int[3][]; jagged[0] = new int[2]; jagged[1] = new int[5];"),

            mc("arr-mc-09", Topic.ARRAYS, 2,
                "What does Arrays.toString(arr) return for int[] arr = {1, 2, 3}?",
                "\"123\"", "\"1 2 3\"", "\"[1, 2, 3]\"", "1 + 2 + 3",
                "c",
                "Arrays.toString() returns a formatted string like \"[1, 2, 3]\". "
                + "This is useful for debugging since array's default toString() gives an unhelpful reference. "
                + "For 2D arrays: Arrays.deepToString(arr2D)."),

            mc("arr-mc-10", Topic.ARRAYS, 3,
                "What is the output?\n\n"
                + "    int[] a = {1, 2, 3};\n"
                + "    int[] b = a;\n"
                + "    b[0] = 99;\n"
                + "    System.out.println(a[0]);",
                "1", "99", "3", "ArrayIndexOutOfBoundsException",
                "b",
                "int[] b = a; copies the reference — a and b point to the same array in memory. "
                + "Modifying b[0] also modifies a[0]. a[0] is now 99."),

            trace("arr-tr-04", Topic.ARRAYS, 2,
                "What is printed?",
                "int[][] grid = {{1,2},{3,4},{5,6}};\n"
                + "System.out.println(grid[1][0]);",
                "3",
                "grid[1] is the second row: {3,4}. grid[1][0] is the first element: 3."),

            trace("arr-tr-05", Topic.ARRAYS, 2,
                "What is printed?",
                "int[] arr = {10, 20, 30};\n"
                + "System.out.println(Arrays.toString(arr));",
                "[10, 20, 30]",
                "Arrays.toString() formats the array as a bracketed, comma-separated string."),

            trace("arr-tr-06", Topic.ARRAYS, 3,
                "What is printed?",
                "String[] words = {\"banana\", \"apple\", \"cherry\"};\n"
                + "Arrays.sort(words);\n"
                + "System.out.println(words[0]);",
                "apple",
                "Arrays.sort(String[]) sorts in lexicographic (alphabetical) order. "
                + "apple < banana < cherry. words[0] = \"apple\"."),

            debug("arr-db-02", Topic.ARRAYS, 2,
                "The code prints the array address instead of its contents. Why?",
                "int[] arr = {1, 2, 3};\n"
                + "System.out.println(arr);",
                "int[] cannot be printed",
                "Arrays don't override toString() — the default prints a type prefix and hash code",
                "println requires a String argument",
                "arr must be cast to Object first",
                "b",
                "int[] inherits Object's toString(): '[I@1a2b3c' (type code + hash). "
                + "Fix: System.out.println(Arrays.toString(arr)); → [1, 2, 3]."),

            debug("arr-db-03", Topic.ARRAYS, 3,
                "The Arrays.equals() check returns false for two arrays with the same values. Why?",
                "int[] a = {1, 2, 3};\n"
                + "int[] b = {1, 2, 3};\n"
                + "System.out.println(a == b);  // false (expected)",
                "Arrays.equals() should be used instead of ==",
                "== compares array references, not content — this is actually correct behavior",
                "int arrays cannot be compared",
                "Both a and b must be sorted first",
                "b",
                "a == b checks if both variables point to the same array object — they don't, so false is correct. "
                + "To compare content: Arrays.equals(a, b) → true. "
                + "The code is using == correctly for reference identity; the comment says 'false (expected)'."),

            codegen("arr-cg-03", Topic.ARRAYS, 2,
                "Which creates a 3×3 int grid and sets grid[1][1] to 5?",
                "int[] grid = new int[3][3]; grid[1][1] = 5;",
                "int[][] grid = new int[3][3]; grid[1][1] = 5;",
                "int[][] grid = {{0,0,0},{0,5,0},{0,0,0}};",
                "Both b and c result in grid[1][1] == 5",
                "d",
                "Option B: dynamically creates the grid then assigns the value. "
                + "Option C: initializes with an array literal that already has 5 at [1][1]. "
                + "Both are valid approaches. Option A incorrectly declares a 1D array.")
        );
    }
}
