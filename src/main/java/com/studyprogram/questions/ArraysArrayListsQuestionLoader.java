package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class ArraysArrayListsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.ARRAYS_ARRAYLISTS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("aal-mc-01", Topic.ARRAYS_ARRAYLISTS, 1,
                "Which method adds an element to the end of an ArrayList?",
                "push()", "append()", "add()", "insert()",
                "c",
                "ArrayList.add(element) appends to the end. "
                + "push() is for Stack/Deque. append() is for StringBuilder. "
                + "insert() does not exist on ArrayList."),

            mc("aal-mc-02", Topic.ARRAYS_ARRAYLISTS, 1,
                "How do you get the number of elements in an ArrayList called 'list'?",
                "list.length", "list.length()", "list.size()", "list.count()",
                "c",
                "ArrayList uses .size() (a method). Arrays use .length (a field without parentheses). "
                + "length() and count() do not exist on ArrayList."),

            mc("aal-mc-03", Topic.ARRAYS_ARRAYLISTS, 2,
                "What is the main advantage of ArrayList over a plain array?",
                "ArrayList is faster for index access",
                "ArrayList can grow and shrink dynamically",
                "ArrayList can hold primitive types directly",
                "ArrayList uses less memory",
                "b",
                "ArrayList resizes automatically as elements are added or removed. "
                + "Plain arrays have a fixed size. ArrayList stores boxed objects, not primitives directly, "
                + "and plain array index access is slightly faster."),

            mc("aal-mc-04", Topic.ARRAYS_ARRAYLISTS, 2,
                "Which call removes the element at index 1 from an ArrayList?",
                "list.delete(1)", "list.remove(1)", "list.pop(1)", "list.erase(1)",
                "b",
                "ArrayList.remove(int index) removes the element at that position and shifts later elements left. "
                + "delete(), pop(), and erase() are not ArrayList methods."),

            trace("aal-tr-01", Topic.ARRAYS_ARRAYLISTS, 2,
                "What is printed?",
                "import java.util.*;\n"
                + "ArrayList<String> words = new ArrayList<>(List.of(\"cat\", \"dog\", \"bird\"));\n"
                + "words.remove(1);\n"
                + "System.out.println(words.get(1));",
                "bird",
                "remove(1) deletes \"dog\" (index 1) and shifts \"bird\" from index 2 to index 1. "
                + "get(1) returns \"bird\"."),

            debug("aal-db-01", Topic.ARRAYS_ARRAYLISTS, 2,
                "This code throws ConcurrentModificationException. Why?",
                "ArrayList<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4));\n"
                + "for (Integer n : nums) {\n"
                + "    if (n % 2 == 0) nums.remove(n);\n"
                + "}",
                "ArrayList does not support the enhanced for loop",
                "You cannot store Integer in ArrayList",
                "Modifying a list while iterating it with for-each corrupts the iterator",
                "remove() requires the index, not the value",
                "c",
                "Structural modification during a for-each loop invalidates the iterator, causing "
                + "ConcurrentModificationException. Fix: use removeIf(n -> n % 2 == 0) "
                + "or iterate with an explicit Iterator and call iterator.remove()."),

            codegen("aal-cg-01", Topic.ARRAYS_ARRAYLISTS, 2,
                "Which code correctly creates a typed ArrayList and adds one element?",
                "ArrayList list = new ArrayList(); list.add(\"hello\");",
                "ArrayList<String> list = new ArrayList<>(); list.add(\"hello\");",
                "String[] list = new ArrayList<>(); list.add(\"hello\");",
                "List list = ArrayList.create(); list.add(\"hello\");",
                "b",
                "ArrayList<String> is the properly typed form — the compiler checks element types. "
                + "Option A uses a raw type (no generic — works but generates warnings). "
                + "Option C declares list as String[] (an array, not a list). "
                + "Option D: ArrayList.create() does not exist."),

            mc("aal-mc-05", Topic.ARRAYS_ARRAYLISTS, 2,
                "Which method converts an array to an ArrayList?",
                "ArrayList.of(array)", "Arrays.asList(array)", "array.toList()", "Collections.list(array)",
                "b",
                "Arrays.asList(T... a) returns a fixed-size List backed by the array. "
                + "To get a resizable ArrayList: new ArrayList<>(Arrays.asList(array)). "
                + "Java 9+: List.of(elements) creates an immutable list. "
                + "Note: Arrays.asList returns a fixed-size list — add/remove throw UnsupportedOperationException."),

            trace("aal-tr-02", Topic.ARRAYS_ARRAYLISTS, 2,
                "What is printed?",
                "ArrayList<Integer> list = new ArrayList<>(List.of(10, 20, 30));\n"
                + "list.remove(Integer.valueOf(20));\n"
                + "System.out.println(list.size() + \" \" + list.get(1));",
                "2 30",
                "remove(Integer.valueOf(20)) removes by value (not index). "
                + "List becomes [10, 30]. size()=2. get(1)=30."),

            mc("aal-mc-06", Topic.ARRAYS_ARRAYLISTS, 2,
                "What is the key advantage of ArrayList over a plain array?",
                "ArrayList stores primitives more efficiently",
                "ArrayList can dynamically resize as elements are added or removed",
                "ArrayList is faster than arrays for random access",
                "ArrayList is thread-safe by default",
                "b",
                "Arrays have a fixed size set at creation. "
                + "ArrayList grows automatically when capacity is exceeded (copies to a larger internal array). "
                + "Random access (get(i)) is O(1) for both. "
                + "Arrays are slightly faster for primitives; ArrayList requires boxing."),

            codegen("aal-cg-02", Topic.ARRAYS_ARRAYLISTS, 2,
                "Which removes all elements from an ArrayList that are less than 5?",
                "list.removeIf(n -> n < 5);",
                "for (int i = 0; i < list.size(); i++) if (list.get(i) < 5) list.remove(i);",
                "list.stream().filter(n -> n >= 5);",
                "Collections.remove(list, n -> n < 5);",
                "a",
                "removeIf(Predicate) is the cleanest way — removes all matching elements atomically. "
                + "Option B has an index bug: removing at i shifts elements left, so the next element is skipped. "
                + "Option C returns a Stream but doesn't modify the original list. "
                + "Option D: Collections.remove() doesn't accept a predicate."),

            mc("aal-mc-07", Topic.ARRAYS_ARRAYLISTS, 2,
                "What does Collections.sort(list) do to an ArrayList<Integer>?",
                "Returns a new sorted list, leaving the original unchanged",
                "Sorts the list in-place in ascending natural order",
                "Sorts the list in descending order",
                "Throws UnsupportedOperationException",
                "b",
                "Collections.sort() sorts the list in-place in natural (ascending) order. "
                + "For descending: Collections.sort(list, Collections.reverseOrder()). "
                + "Java 8+: list.sort(Comparator.naturalOrder()); is equivalent."),

            mc("aal-mc-08", Topic.ARRAYS_ARRAYLISTS, 3,
                "When is LinkedList faster than ArrayList?",
                "Random access by index (e.g., get(500))",
                "Frequent insertions/deletions at the beginning or middle of the list",
                "Sorting",
                "Memory usage",
                "b",
                "ArrayList: O(1) random access, O(n) insertion at the middle (shifts elements). "
                + "LinkedList: O(n) random access (traversal), O(1) insertions at known positions (pointer update). "
                + "In practice, ArrayList outperforms LinkedList for most use cases due to cache locality."),

            mc("aal-mc-09", Topic.ARRAYS_ARRAYLISTS, 2,
                "What does ArrayList.contains(\"hello\") return if \"hello\" is in the list?",
                "The index of \"hello\"",
                "true",
                "A new list with only \"hello\"",
                "1",
                "b",
                "contains(Object) returns true if the list contains the element (uses .equals()). "
                + "indexOf(Object) returns the index. "
                + "For null: contains(null) works if the list contains a null element."),

            mc("aal-mc-10", Topic.ARRAYS_ARRAYLISTS, 3,
                "What is the correct way to remove elements while iterating an ArrayList?",
                "Use a regular for-each loop and call list.remove() inside",
                "Use an Iterator and call iterator.remove()",
                "Use a regular for loop with index and call list.remove(i)",
                "Both b and c are safe approaches",
                "d",
                "Iterator.remove() safely removes during traversal. "
                + "A backward index loop (i-- after remove) also works. "
                + "A forward index loop without adjustment skips elements. "
                + "For-each with list.remove() throws ConcurrentModificationException. "
                + "removeIf() is the cleanest modern approach."),

            trace("aal-tr-03", Topic.ARRAYS_ARRAYLISTS, 2,
                "What is printed?",
                "import java.util.*;\n"
                + "ArrayList<String> list = new ArrayList<>(List.of(\"banana\", \"apple\", \"cherry\"));\n"
                + "Collections.sort(list);\n"
                + "System.out.println(list.get(0) + \" \" + list.get(2));",
                "apple cherry",
                "Collections.sort() sorts alphabetically: [apple, banana, cherry]. "
                + "get(0)=apple, get(2)=cherry."),

            trace("aal-tr-04", Topic.ARRAYS_ARRAYLISTS, 2,
                "What is printed?",
                "ArrayList<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));\n"
                + "list.removeIf(n -> n % 2 == 0);\n"
                + "System.out.println(list);",
                "[1, 3, 5]",
                "removeIf removes all even numbers: 2 and 4. "
                + "Remaining: [1, 3, 5]. ArrayList's toString prints in [a, b, c] format."),

            debug("aal-db-02", Topic.ARRAYS_ARRAYLISTS, 3,
                "The loop skips some elements. Why?",
                "ArrayList<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));\n"
                + "for (int i = 0; i < list.size(); i++) {\n"
                + "    if (list.get(i) % 2 == 0) list.remove(i);\n"
                + "}",
                "get() and remove() cannot be used in the same loop",
                "After removing at index i, the element that was at i+1 shifts to i and is never checked",
                "list.size() must be stored before the loop",
                "Integers cannot be removed by index from ArrayList",
                "b",
                "When list.remove(1) removes the element at index 1 (value 2), "
                + "value 3 shifts from index 2 to index 1. i increments to 2 — skipping 3. "
                + "Fix: use removeIf(n -> n % 2 == 0) or decrement i after each removal."),

            debug("aal-db-03", Topic.ARRAYS_ARRAYLISTS, 2,
                "This throws IndexOutOfBoundsException. Why?",
                "ArrayList<String> list = new ArrayList<>();\n"
                + "list.add(0, \"hello\");  // insert at index 0\n"
                + "list.add(5, \"world\");  // insert at index 5 — bug",
                "add(index, element) requires the index to be 0",
                "list is empty so no index is valid",
                "The index 5 is out of range — the list has only 1 element (valid indices: 0 and 1)",
                "String elements cannot be inserted at specific indices",
                "c",
                "After adding \"hello\" the list has size 1. Valid indices for add() are 0..size (0 or 1). "
                + "Index 5 is out of bounds. "
                + "list.add(element) (without index) always adds at the end safely."),

            codegen("aal-cg-03", Topic.ARRAYS_ARRAYLISTS, 2,
                "Which converts an ArrayList<String> to a String[]?",
                "String[] arr = list.toArray();",
                "String[] arr = list.toArray(new String[0]);",
                "String[] arr = (String[]) list.toArray();",
                "String[] arr = Arrays.asList(list);",
                "b",
                "toArray(new String[0]) is the idiomatic way — the runtime uses the type of the argument "
                + "to infer the component type of the returned array. "
                + "Option A returns Object[]. "
                + "Option C casts Object[] to String[] — throws ClassCastException at runtime. "
                + "Option D: Arrays.asList returns a List, not an array.")
        );
    }
}
